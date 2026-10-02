# Arquillian 2.0 Migration Plan — Critical Review

This document identifies gaps, untested assumptions, and structural risks in the
Event-to-SPI migration plan. Each issue is rated by severity and includes a
suggested resolution.

---

## 1. The bindAndFire Hidden Event Chain (Severe)

`InstanceProducer.set(value)` calls `manager.bindAndFire(scope, type, value)` in
`InstanceImpl.java:62-67`. This both stores the value in the scoped context AND
fires it as an event on the bus. The plan proposes `ConfigurationListener.onDescriptorLoaded()`
with an adaptor that `@Observes ArquillianDescriptor`, but if the event bus is
deprecated/removed in Phase 8, `InstanceProducer.set()` would stop firing the
event, and the adaptor would never trigger.

**What the plan misses**: There is no strategy for replacing `bindAndFire`. Phase 6's
`ContextBinder` addresses the setter side but not the implicit event-firing side.
`InstanceImpl.set()` must be changed from `bindAndFire()` to `bind()` — but only
after every `@Observes SomeType` where `SomeType` is produced via `set()` has been
migrated to a listener.

**Resolution**: See `docs/arch-notes/bindAndFire-replacement.md` for the full
replacement design. The `ArquillianDescriptor` appears to be the only value type
where `set()` triggers observer dispatch that downstream code depends on, which
limits the blast radius. Add an explicit step between Phase 2 and Phase 6 to
audit all `InstanceProducer.set()` calls and verify which ones have downstream
`@Observes` handlers.

---

## 2. Injectable Anonymous Inner Classes (Severe)

`ContainerLifecycleController` and `ContainerDeployController` create anonymous
`Operation` inner classes with `@Inject` fields that get DI-managed via
`injector.get().inject(operation)`. For example in
`ContainerLifecycleController.java:50-59`:

```java
forEachContainer(new Operation<Container>() {
    @Inject
    private Event<SetupContainer> event;
    @Override
    public void perform(Container container) {
        event.fire(new SetupContainer(container));
    }
});
```

This is a deeply unusual DI pattern — ad-hoc anonymous objects get field injection
at runtime. A listener SPI implementation cannot replicate this because listeners
are not DI-managed. The plan says these controllers should "implement
`ContainerMultiControlListener`" but does not address how the injected
`Event<SetupContainer>` inside the anonymous operation would be replaced.

**Resolution**: When migrating these controllers to listener implementations,
the controller must receive a `Manager` reference (via constructor injection or
method parameter) and call `manager.fire(new SetupContainer(container))` directly
instead of relying on an injected `Event<T>` field. The anonymous `Operation`
classes should be replaced with lambdas that capture the Manager reference.
This is a Java 21 modernization opportunity.

---

## 3. NonManagedObserver and TestResult Collection (Missed)

`EventTestRunnerAdaptor.test()` at line 138 uses a pattern not mentioned in the plan:

```java
manager.fire(new Test(testMethodExecutor), new NonManagedObserver<Test>() {
    @Inject private Instance<TestResult> testResult;
    @Override
    public void fired(Test event) {
        results.add(testResult.get());
    }
});
```

This fires a `Test` event with an anonymous callback that gets injected and
collects the `TestResult` from the scoped context after all observers have run.
The `TestExecutionListener.executeTest()` proposed in Phase 4 has no equivalent
mechanism for returning a `TestResult`. This is the core of how test results
propagate back to the test framework runner.

**Resolution**: The `TestExecutionListener` interface needs a return type or a
result-collection callback. Options:

- `TestResult executeTest(TestMethodExecutor executor)` — listener returns the result
- Add a `TestResultCollector` parameter that listeners call with the result
- Keep `NonManagedObserver` as a supported pattern alongside the new listener system

The third option is simplest for incremental migration. `EventTestRunnerAdaptor.test()`
can continue using `manager.fire(event, nonManagedObserver)` even after other
methods switch to direct listener dispatch.

---

## 4. fireCustomLifecycle() is Unaddressed (Missed)

`EventTestRunnerAdaptor.fireCustomLifecycle(TestLifecycleEvent)` fires arbitrary
custom event subtypes from JUnit 4 and JUnit 5:

- `BeforeRules`, `AfterRules`, `RulesEnrichment` (JUnit 4)
- `RunModeEvent`, `BeforeTestExecutionEvent`, `MethodParameterProducerEvent` (JUnit 5)

The `CustomEventListenerBehaviorTestCase` already on the branch documents that
custom events are invisible to typed listener SPI callbacks. The plan's Phase 8
evolution of `EventTestRunnerAdaptor` does not address how `fireCustomLifecycle()`
will work if the event bus is deprecated.

**Resolution**: `fireCustomLifecycle()` must continue to use `manager.fire(event)`
because the custom events are framework-specific subtypes that no listener SPI
can anticipate. This means the event bus cannot be fully removed — it must remain
operational for custom lifecycle events. The Phase 8 deprecation should be
"deprecated for core lifecycle events" rather than a blanket deprecation.

---

## 5. No Listener Ordering Mechanism (Design Gap)

The current `@Observes(precedence=N)` system provides explicit execution ordering.
The plan says listener ordering is "handled by registration order," but
registration order is determined by `ServiceLoader` discovery order — which
depends on classpath order and is non-deterministic across environments.

This matters concretely: `ContainerLifecycleController` observing `SetupContainers`
must run before `ClientContainerControllerCreator` observing `SetupContainers`.
If both become `ContainerMultiControlListener` implementations, nothing guarantees
the order.

**Resolution**: Add a priority mechanism to listener registration:

```java
manager.addListener(ContainerMultiControlListener.class, controller, 100);
manager.addListener(ContainerMultiControlListener.class, creator, 50);
```

Or use an annotation/interface like `Prioritized` that listeners can implement.
This must be designed before Phase 3, where controllers and notification
listeners first coexist in the same listener type.

---

## 6. Cascading Event Chains Cannot Be Expressed as Listeners (Architectural)

The event sequence diagrams show cascading chains:

```
SetupContainers → [for each container] SetupContainer → BeforeSetup → container.setup() → AfterSetup
```

Three different listener SPIs fire in this chain:
- `ContainerMultiControlListener.setupContainers()`
- `ContainerControlListener.setupContainer(Container)`
- `ContainerLifecycleListener.beforeSetup(DeployableContainer)` / `afterSetup()`

If `ContainerLifecycleController` implements both `ContainerMultiControlListener`
and `ContainerControlListener`, its `setupContainers()` method needs to fire
`SetupContainer` events that trigger `ContainerControlListener.setupContainer()`
callbacks. But without the event bus, who dispatches between the listener layers?

The controller would need to manually iterate `manager.getListeners(ContainerControlListener.class)`
and call each one — duplicating the dispatch logic the Manager currently handles.

**Resolution**: Accept that during the transition, the controllers will still use
`manager.fire()` internally to trigger the cascading chain. The listener SPI is
for external extensions to observe; the internal orchestration can continue using
the event bus until the final phase. Alternatively, introduce a
`ListenerDispatcher` utility that wraps the iteration pattern.

---

## 7. Interceptor Type Matching with Inheritance (Underspecified)

`ContainerDeploymentContextHandler` observes `EventContext<ContainerControlEvent>`.
`ContainerControlEvent` is a base class; the actual fired events are subclasses
like `SetupContainer`, `StartContainer`, etc. The current event bus matches
observers against the event type hierarchy.

The plan proposes `manager.addInterceptor(ContainerControlEvent.class, ...)` but
does not specify whether this matches subclasses. If it doesn't, the interceptor
silently stops working. If it does, the matching logic needs explicit design.

**Resolution**: The `addInterceptor` implementation in `ManagerImpl` must walk the
event's class hierarchy and match against registered interceptor types, just as
the current observer resolution does. This must be specified in the Phase 5
design and tested with a subclass-matching test case.

---

## 8. ContainerTestExtension Duplicates TestExtension (Risk)

`ContainerTestExtension` re-registers `SuiteContextImpl`, `ClassContextImpl`,
`TestContextImpl`, and `TestContextHandler` (lines 68-73) that `TestExtension`
also registers. The comment says "Copied from TestExtension."

If both extensions register the same classes as listeners, you get double
invocations. Currently, the Manager handles duplicate observer registrations
(same class registered twice results in two instances), but the listener
registry uses `addListener()` which always appends.

**Resolution**: Audit the extension loading to understand whether both
`TestExtension` and `ContainerTestExtension` are loaded simultaneously (they
are in different META-INF/services files, but both appear on the classpath in
integration tests). If they are, listener deduplication logic is needed — either
in `addListener()` or by ensuring only one extension registers listeners.

---

## 9. Testing the Transition State (Verification Gap)

During Phases 2-7, both the adaptor bridge and direct call paths exist. The plan
says "remove adaptor to prevent double dispatch" in Phase 8, but there is no
verification strategy for ensuring double dispatch does not happen during
intermediate phases.

Each time a new listener SPI is introduced and an observer is migrated to
implement it, there is a window where both the old `@Observes` path and the new
listener path could fire. The existing test suite tests behavior (correct outcome),
not invocation counts.

**Resolution**: Add invocation-counting test infrastructure. For each listener SPI,
write a test that:
1. Registers a counting listener via `manager.addListener()`
2. Fires the corresponding event
3. Asserts the listener was called exactly once, not twice

This test catches double dispatch immediately. It should be written as part of
each phase, not deferred to Phase 8.

---

## 10. Integration Tests May Not Run After Phase 0 (Practical)

The integration tests (`-P integration-tests`) use container adaptors (WildFly,
Payara). If these adaptors do not support Java 21, the integration test suite
cannot run after Phase 0, leaving Phases 2-7 without end-to-end verification.

**Resolution**: Before starting Phase 0, identify which container adaptors already
support Java 21. The Payara dependency is already at 7.2026.x which likely
supports Java 21. Verify WildFly adaptor compatibility. If needed, create a
minimal embedded container adaptor for integration testing that runs on Java 21.

---

## 11. DeploymentScoped and ContainerScoped Not Addressed (Gap)

Phase 6's `ContextBinder` interface shows `bind(scope, type, instance)` with
examples using `ApplicationScoped` and `ClassScoped`. But
`ContainerDeployController.deploy()` uses:

- `@DeploymentScoped InstanceProducer<DeploymentDescription>`
- `@DeploymentScoped InstanceProducer<Deployment>`
- `@DeploymentScoped InstanceProducer<ProtocolMetaData>`

These scopes are activated/deactivated by interceptors
(`ContainerDeploymentContextHandler`). If interceptors are migrated in Phase 5
and DI evolution happens in Phase 6, there is an ordering problem: Phase 6 needs
working interceptors from Phase 5, but the interceptors manage the context
lifecycle that Phase 6 is reworking.

**Resolution**: The `ContextBinder` interface works with any scope annotation — it
just needs to be documented that `DeploymentScoped` and container-specific scopes
are supported. The ordering concern is valid: Phase 5 interceptors must be fully
working and tested before Phase 6 touches the DI system. Add this as an explicit
prerequisite in the phase dependency graph.

---

## Summary: What Is Testable vs. What Is Not

### Easily Testable
- Phase 0: full build passes with Java 21
- Phase 1: already tested on branch
- Phase 2: simple adaptor bridge, standard listener dispatch
- Simple listener migrations in Phases 3 and 7

### Hard to Test
- Correct ordering of listener vs. observer invocations during transition (Phases 3-4)
- Absence of double dispatch during any intermediate state
- Interceptor type matching with inheritance (Phase 5)
- Cascading event chains working correctly in hybrid mode

### Requires New Test Infrastructure
- `NonManagedObserver` result collection in the listener model
- `fireCustomLifecycle()` behavior with direct dispatch
- `bindAndFire` replacement — no existing tests verify the implicit event-from-set chain
- Invocation-counting tests for double dispatch prevention

---

## Structural Assessment

The plan's biggest structural weakness is that it treats the event bus, DI system,
and interceptor chain as separable concerns. They are deeply entangled:

- `InstanceProducer.set()` fires events (DI → event bus coupling)
- Anonymous `Operation` classes get `@Inject` fields (event bus → DI coupling)
- Interceptors activate scopes that DI depends on (interceptor → DI coupling)
- `NonManagedObserver` gets injected after event dispatch (event bus → DI coupling)

This means the phases cannot be truly independent. Changes in one layer ripple
through the others. The plan should acknowledge this and add cross-phase
integration checkpoints — points where the entire system is verified end-to-end,
not just the module that was changed.