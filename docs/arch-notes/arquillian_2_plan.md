# Arquillian Core 2.0: Event-to-SPI Migration Plan

## Context

Arquillian Core uses an implicit event-driven architecture (`@Observes` + `Manager.fire()`) that makes execution flow hard to trace, debug, and discover. The goal is to replace this with explicit listener SPI interfaces and direct method calls, while upgrading the Java baseline from 8 to 21.

Work has already begun on the `2.0.x` branch establishing the "listener SPI + adaptor bridge" pattern for 6 interfaces. This plan covers completing that migration across all remaining observer classes (~30+ across 4 modules), evolving the interceptor chain, and modernizing the DI interaction model.

**Constraint**: The `junit/`, `junit5/`, and `testng/` modules are NOT modified. They continue to use `@Observes` against the existing event bus, which must remain operational for backward compatibility.

---

## Phase 0: Java 21 Baseline Upgrade

**Goal**: Establish Java 21 as compilation target before SPI work, enabling sealed interfaces, records, pattern matching, and enhanced switch in all new code.

**Files to modify**:
- `pom.xml` (root) -- set `maven.compiler.release` from `8` to `21`
- `.github/workflows/` -- update CI matrix, JDK 21 minimum
- Remove `animal-sniffer-maven-plugin` if present (no longer needed)
- Upgrade `com.puppycrawl.tools:checkstyle` to 10.12+ for Java 21 syntax support
- Upgrade Mockito from 4.x to 5.x (required for Java 21 sealed classes)

**Verification**: `./mvnw clean install` -- full build with existing test suite must pass with no behavioral changes.

**Risk**: Downstream container adaptors may not support Java 21 yet. Acceptable for a 2.0.x major version.

---

## Phase 1: Already Completed (Current State on 2.0.x)

The following listener SPIs and their adaptor bridges are implemented and registered:

| Listener SPI | Location | Adaptor | Registered In |
|---|---|---|---|
| `ManagerLifecycleListener` | core/spi | `ManagerLifecycleListenerAdaptor` | `CoreExtension` |
| `TestLifecycleListener` | test/spi | `TestLifecycleListenerAdaptor` | `TestExtension` |
| `TestEnrichmentListener` | test/spi | `TestEnrichmentListenerAdaptor` | `TestExtension` |
| `ContainerLifecycleListener` | container/spi | `ContainerLifecycleListenerAdaptor` | `ContainerExtension` |
| `ContainerControlListener` | container/spi | `ContainerControlListenerAdaptor` | `ContainerExtension` |
| `ContainerMultiControlListener` | container/spi | `ContainerMultiControlListenerAdaptor` | `ContainerExtension` |

`Manager` already has `addListener(Class<T>, T)` and `getListeners(Class<T>)`. `ManagerImpl` stores listeners in `ConcurrentHashMap<Class<?>, List<Object>>`.

---

## Phase 2: Configuration Listener SPI

**Goal**: Create a listener SPI for configuration lifecycle, covering `ConfigurationRegistrar` and the `ArquillianDescriptor`-driven observers.

**Modules changed**: `core/spi`, `config/impl-base`, `container/impl-base`, `container/test-impl-base`

### New SPI

**`core/spi/.../ConfigurationListener.java`**:
```java
public interface ConfigurationListener {
    default void onDescriptorLoaded(ArquillianDescriptor descriptor) throws Exception {}
}
```

### New Adaptor

**`config/impl-base/.../ConfigurationListenerAdaptor.java`**:
- `@Observes ArquillianDescriptor event` -- iterates `manager.getListeners(ConfigurationListener.class)` and calls `onDescriptorLoaded(descriptor)`
- Registered in `ConfigExtension`

### Observer Migrations

| Observer | Current Event | Migration |
|---|---|---|
| `ContainerRegistryCreator` | `@Observes ArquillianDescriptor` | Implement `ConfigurationListener.onDescriptorLoaded()` |
| `ProtocolRegistryCreator` | `@Observes ArquillianDescriptor` | Implement `ConfigurationListener.onDescriptorLoaded()` |

**Challenge**: Both observers use `@Inject InstanceProducer<T>` to bind results into scoped contexts. During this phase, keep the `@Observes` method as-is and additionally implement the listener interface as a parallel path. The adaptor calls listeners; the old observer path remains for the binding logic until Phase 6 addresses DI evolution.

**Testing**: Existing `ContainerRegistryCreatorTestCase` must pass. Add tests verifying listener dispatch.

---

## Phase 3: Container Orchestration -- Reuse Existing SPIs

**Goal**: Migrate internal container controller observers to use the listener SPIs already created in Phase 1.

**Modules changed**: `container/impl-base`

### Observer Migrations

| Observer | Current Events | Migration Target |
|---|---|---|
| `ContainerLifecycleController` (multi-container fan-out) | `@Observes SetupContainers/StartSuiteContainers/...` | Implement `ContainerMultiControlListener` |
| `ContainerLifecycleController` (per-container execution) | `@Observes SetupContainer/StartContainer/...` | Implement `ContainerControlListener` |
| `ArchiveDeploymentExporter` | `@Observes BeforeDeploy` | Implement `ContainerLifecycleListener.beforeDeploy()` |

**Important distinction**: `ContainerControlListener` and `ContainerMultiControlListener` were designed as notification SPIs for external extensions. The controller classes being migrated here are the *implementors* of the actual logic. Both use the same SPI -- external extensions observe, controllers execute. Ordering is handled by registration order.

### Deferred (Interceptor Pattern)

These observers use `EventContext<T>` + `proceed()` and are deferred to Phase 5:
- `ContainerDeploymentContextHandler` -- wraps `ContainerControlEvent`/`DeploymentEvent` with context activation
- `DeploymentExceptionHandler` -- wraps `DeployDeployment` for expected exception handling

**Testing**: `ContainerLifecycleControllerTestCase` must pass. Verify that controller logic runs in correct order relative to notification listeners.

---

## Phase 4: Container Test Orchestration (container/test-impl-base)

**Goal**: Migrate the 15+ observers in `ContainerTestExtension` -- the largest observer module. Organized by responsibility group.

**Modules changed**: `container/test-spi`, `container/test-impl-base`

### Group A: Test-to-Container Bridge (ContainerEventController)

`ContainerEventController` observes `BeforeSuite/AfterSuite/BeforeClass/AfterClass` and fires container multi-control events. It also uses the interceptor pattern for test-level context activation.

**Migration**: Implement `TestLifecycleListener` for the non-interceptor methods:
- `beforeSuite()` -- fires `SetupContainers`, `StartSuiteContainers`
- `afterSuite()` -- fires `StopSuiteContainers`
- `beforeClass(TestClass, executor)` -- fires `StartClassContainers`, `GenerateDeployment`, `DeployManagedDeployments`
- `afterClass(TestClass, executor)` -- fires `UnDeployManagedDeployments`, `StopManualContainers`, `StopClassContainers`

The interceptor methods (test-level context activation) are deferred to Phase 5.

**Challenge**: The listener methods still need to fire events (`container.fire(new SetupContainers())`). The listener receives no Manager reference by default. Options:
1. Pass Manager as constructor parameter when registering
2. Have the adaptor pass Manager as an additional parameter
3. Have ContainerEventController hold a Manager reference set during initialization

**Decision**: Use option 1 -- the `ContainerTestExtension` registration code creates the listener with the Manager reference. This is consistent with Java 21 idioms (explicit dependencies).

### Group B: Creator/Producer Observers

| Observer | Current Event | Migration |
|---|---|---|
| `ClientContainerControllerCreator` | `@Observes SetupContainers` | Implement `ContainerMultiControlListener.setupContainers()` |
| `ClientDeployerCreator` | `@Observes SetupContainers` | Implement `ContainerMultiControlListener.setupContainers()` |
| `ContainerContainerControllerCreator` | `@Observes BeforeSuite` | Implement `TestLifecycleListener.beforeSuite()` |
| `ContainerRestarter` | `@Observes BeforeClass` | Implement `TestLifecycleListener.beforeClass()` |

**Challenge**: These use `@Inject @ApplicationScoped InstanceProducer<T>` to bind values. During transition, they keep the binding via Manager.bind() called directly. Full DI extraction in Phase 6.

### Group C: Test Execution Chain

**New SPI: `container/test-spi/.../TestExecutionListener.java`**:
```java
public interface TestExecutionListener {
    default void executeTest(TestMethodExecutor executor) throws Exception {}
    default void executeLocal(TestMethodExecutor executor) throws Exception {}
    default void executeRemote(TestMethodExecutor executor) throws Exception {}
}
```

**New Adaptor: `container/test-impl-base/.../TestExecutionListenerAdaptor.java`**:
- `@Observes Test event` -- calls `executeTest()`
- `@Observes LocalExecutionEvent event` -- calls `executeLocal()`
- `@Observes RemoteExecutionEvent event` -- calls `executeRemote()`

| Observer | Migration |
|---|---|
| `ClientTestExecuter` | Implement `TestExecutionListener.executeTest()` |
| `LocalTestExecuter` | Implement `TestExecutionListener.executeLocal()` |
| `RemoteTestExecuter` | Implement `TestExecutionListener.executeRemote()` |

### Group D: Lifecycle Executors -- DEFERRED

`BeforeLifecycleEventExecuter`, `AfterLifecycleEventExecuter`, `ClientBeforeAfterLifecycleEventExecuter` use precedence to define execution boundaries. They are tightly coupled to the event ordering system and the junit/testng module contract. **Leave as `@Observes` observers permanently** -- they are the boundary between Arquillian lifecycle and test framework lifecycle.

### Group E: Command Observers -- DEFERRED to Phase 7

`ContainerCommandObserver`, `DeploymentCommandObserver`, `RemoteResourceCommandObserver` form an orthogonal command bus for in-container communication. Lower priority. They can be migrated to a `CommandHandler<T>` pattern in Phase 7 or left as-is.

### Group F: Deployment Generation -- DEFERRED to Phase 6

`DeploymentGenerator` is deeply coupled to the DI system (`@Inject @ClassScoped InstanceProducer<DeploymentScenario>`). Migrate alongside DI evolution.

`ArchiveDeploymentToolingExporter` can implement `ContainerLifecycleListener.beforeDeploy()` -- simple, do in this phase.

**Testing**: `ContainerEventControllerTestCase`, `ClientTestExecuterTestCase`, `DeploymentGeneratorTestCase`. Run integration tests with `-P integration-tests`.

---

## Phase 5: Interceptor Chain Evolution

**Goal**: Replace the implicit `EventContext<T>` + `proceed()` pattern with explicit `Interceptor<T>` registration.

**Modules changed**: `core/spi`, `core/impl-base`, `container/impl-base`, `container/test-impl-base`

### New SPIs

**`core/spi/.../Interceptor.java`**:
```java
@FunctionalInterface
public interface Interceptor<T> {
    void intercept(T event, InterceptorChain<T> chain) throws Exception;
}
```

**`core/spi/.../InterceptorChain.java`**:
```java
public interface InterceptorChain<T> {
    void proceed() throws Exception;
    T getEvent();
}
```

### Manager API Addition

```java
<T> void addInterceptor(Class<T> eventType, Interceptor<T> interceptor, int priority);
```

### ManagerImpl Changes

When `fire(event)` is called:
1. Resolve both `@Observes EventContext<T>` observers (old) AND registered `Interceptor<T>` instances (new)
2. Sort all together by priority
3. Build unified interceptor chain
4. Execute

This ensures old-style (`EventContext<T>`) and new-style (`Interceptor<T>`) coexist and interleave correctly by priority. **Critical for backward compatibility** since `junit/core` `UpdateTestResultBeforeAfter` uses `@Observes(precedence=99) EventContext<AfterTestLifecycleEvent>`.

### Observer Migrations

| Observer | Current Pattern | Migration |
|---|---|---|
| `TestContextHandler` | `@Observes(precedence=100) EventContext<SuiteEvent/ClassEvent/TestEvent>` | `manager.addInterceptor(SuiteEvent.class, ..., 100)` etc. |
| `ContainerDeploymentContextHandler` | `@Observes EventContext<ContainerControlEvent/DeploymentEvent>` | `manager.addInterceptor(...)` |
| `DeploymentExceptionHandler` | `@Observes EventContext<DeployDeployment>` | `manager.addInterceptor(DeployDeployment.class, ...)` |
| `ContainerEventController` (test-level) | `@Observes EventContext<BeforeTestLifecycleEvent/Test/AfterTestLifecycleEvent>` | `manager.addInterceptor(...)` |

**Testing**: Critical -- interceptor ordering must be preserved exactly.
- Verify interceptors called in priority order (higher number = runs first/outermost)
- Verify `proceed()` invokes next interceptor, then observers
- Verify old `@Observes EventContext<T>` and new `Interceptor<T>` interleave correctly
- Verify exceptions propagate correctly through chain

---

## Phase 6: DI System Evolution

**Goal**: Reduce reliance on `@Inject Instance<T>` / `@Inject InstanceProducer<T>` / `@Inject Event<T>` for listener implementations by providing explicit dependency passing.

**Modules changed**: `core/spi`, all impl modules

### New SPIs

**`core/spi/.../ContextBinder.java`**:
```java
public interface ContextBinder {
    <T> void bind(Class<? extends Annotation> scope, Class<T> type, T instance);
    <T> T resolve(Class<T> type);
}
```

### Convention for Listener Methods

Listener methods that need to produce values or resolve dependencies receive a `ContextBinder` parameter. The adaptor creates it from the Manager. Example:

```java
public interface ConfigurationListener {
    default void onDescriptorLoaded(ArquillianDescriptor descriptor, ContextBinder binder) 
        throws Exception {}
}
```

### Observer Migrations (Producer Observers)

| Observer | Current DI Pattern | Migration |
|---|---|---|
| `ConfigurationRegistrar` | `@Inject @ApplicationScoped InstanceProducer<ArquillianDescriptor>` | `binder.bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor)` |
| `ContainerRegistryCreator` | `@Inject @ApplicationScoped InstanceProducer<ContainerRegistry>` | Same pattern |
| `ProtocolRegistryCreator` | `@Inject @ApplicationScoped InstanceProducer<ProtocolRegistry>` | Same pattern |
| `DeploymentGenerator` | `@Inject @ClassScoped InstanceProducer<DeploymentScenario>` | Same pattern |
| `ClientContainerControllerCreator` | `@Inject @ApplicationScoped InstanceProducer<ContainerController>` | Same pattern |

**Testing**: Each migrated producer must verify values correctly bound into expected scope. Use `AbstractManagerTestBase` infrastructure.

**Risk**: High blast radius. Each sub-step must be independently compilable and testable.

---

## Phase 7: Peripheral Module Migration

**Goal**: Migrate remaining observers in `testenrichers/`, `protocols/`, and command observers.

### Simple Listener Migrations

| Observer | Module | Current Event | Migration |
|---|---|---|---|
| `BeanManagerProducer` | testenrichers/cdi | `@Observes ManagerStarted` | `ManagerLifecycleListener.managerStarted()` |
| `InitialContextProducer` | testenrichers/initialcontext | `@Observes AfterClass` | `TestLifecycleListener.afterClass()` |
| `ServletContextRegistrar` | protocols/servlet | `@Observes ManagerStarted` | `ManagerLifecycleListener.managerStarted()` |

### Interceptor Migrations

| Observer | Module | Migration |
|---|---|---|
| `CreationalContextDestroyer` | testenrichers/cdi | `Interceptor<After>` (from Phase 5) |

### Command Handler Pattern (Optional)

**`container/test-spi/.../CommandHandler.java`**:
```java
public interface CommandHandler<T extends Command<?>> {
    boolean canHandle(Class<? extends Command<?>> commandType);
    void handle(T command) throws Exception;
}
```

Migrate `ContainerCommandObserver`, `DeploymentCommandObserver`, `RemoteResourceCommandObserver`. This is optional -- these can remain as `@Observes` observers if the command pattern doesn't warrant full migration.

---

## Phase 8: Cleanup and Final Integration

**Goal**: Remove adaptor bridges, evolve `EventTestRunnerAdaptor`, deprecate old APIs.

### EventTestRunnerAdaptor Evolution

Currently calls `manager.fire(new BeforeSuite())` etc. Evolve to:

```java
public void beforeSuite() throws Exception {
    // Direct listener dispatch (new path)
    for (TestLifecycleListener l : manager.getListeners(TestLifecycleListener.class)) {
        l.beforeSuite();
    }
    // Event dispatch preserved for junit/testng modules and third-party extensions
    manager.fire(new BeforeSuite());
}
```

**Important**: Remove `TestLifecycleListenerAdaptor` at this point to prevent double dispatch (it would observe the event AND the listener was already called directly).

### Adaptor Bridge Removal

For each listener SPI whose adaptor bridge is no longer needed (because the caller now dispatches directly to listeners):
- Remove the adaptor class
- Remove its registration from the LoadableExtension
- Keep the listener SPI interface

### API Deprecation

- `@Observes` -- deprecated for new code, retained for backward compatibility
- `@Inject Instance<T>` -- deprecated for listener implementations
- `@Inject InstanceProducer<T>` -- deprecated, replaced by `ContextBinder`
- `@Inject Event<T>` -- deprecated, replaced by direct `Manager.fire()` or `EventDispatcher`

### ExtensionBuilder Enhancement

```java
interface ExtensionBuilder {
    // Existing
    ExtensionBuilder observer(Class<?> handler);
    ExtensionBuilder context(Class<? extends Context> context);
    <T> ExtensionBuilder service(Class<T> service, Class<? extends T> impl);
    // New
    <T> ExtensionBuilder listener(Class<T> listenerType, Class<? extends T> listenerImpl);
    <T> ExtensionBuilder interceptor(Class<T> eventType, Class<? extends Interceptor<T>> interceptor, int priority);
}
```

---

## Dependency Graph

```
Phase 0 (Java 21) ──────────┐
    │                        │
    v                        │
Phase 2 (Config)             │
    │                        │
Phase 3 (Container Orch) ────┤  (Phases 2-3 can run in parallel)
    │                        │
    v                        │
Phase 4 (Container Test) ────┤
    │                        │
    v                        │
Phase 5 (Interceptors) ──────┤
    │                        │
    v                        │
Phase 6 (DI Evolution) ──────┤
    │                        │
    v                        │
Phase 7 (Peripherals) ───────┤
    │                        │
    v                        │
Phase 8 (Cleanup) ───────────┘
```

---

## Permanently Out of Scope

These observer classes stay as `@Observes` forever:
- **junit/** -- `UpdateTestResultBeforeAfter`, `RulesEnricher`, `AllLifecycleEventExecutor`, `LocalTestMethodExecutor`
- **junit5/** -- `MethodParameterObserver`, `RunModeEventHandler`
- **testng/** -- `UpdateTestResultBeforeAfter`, `LocalTestMethodExecutor`
- **Lifecycle executors** -- `BeforeLifecycleEventExecuter`, `AfterLifecycleEventExecuter`, `ClientBeforeAfterLifecycleEventExecuter` (define the test framework boundary)

---

## Verification Strategy

Each phase must pass before proceeding:

1. **Unit tests**: `./mvnw test -pl <module>` for each changed module
2. **Module build**: `./mvnw install -pl <module> -am` with dependencies
3. **Full build**: `./mvnw clean install`
4. **Integration tests**: `./mvnw clean install -P integration-tests`
5. **Checkstyle**: `./mvnw checkstyle:check` (with updated checkstyle for Java 21)

---

## Risk Assessment

| Risk | Impact | Mitigation |
|---|---|---|
| Event ordering changes break behavior | High | Preserve precedence values exactly; exhaustive ordering tests |
| Double dispatch during transition (listener + observer) | Medium | Adaptor bridge pattern prevents this; remove bridges only in Phase 8 |
| junit/testng modules break | High | Never modify; they interact only via TestRunnerAdaptor + @Observes |
| DI refactoring breaks scoped context lifecycle | High | Small incremental changes per Phase 6 sub-step |
| Interceptor priority mismatch old/new | High | Unified sorting in ManagerImpl; test old+new interleaving |
| Third-party extensions break | Medium | @Observes remains operational; document migration path |

---

## Summary of Affected Classes by Phase

Legend: **NEW** = new file, **MOD** = modify existing file, **DEL** = remove file

### Phase 0: Java 21 Baseline Upgrade

| Action | File | Change |
|---|---|---|
| MOD | `pom.xml` | Set `maven.compiler.release` from `8` to `21`; upgrade checkstyle to 10.12+; upgrade Mockito to 5.x |
| MOD | `.github/workflows/ci.yml` | Update JDK matrix to 21 minimum |
| MOD | `.github/workflows/integration-tests.yml` | Update JDK matrix to 21 minimum |
| MOD | `.github/workflows/maven-jboss-snapshot-publish.yml` | Update JDK version |

### Phase 2: Configuration Listener SPI

| Action | File | Change |
|---|---|---|
| NEW | `core/spi/src/main/java/.../core/spi/ConfigurationListener.java` | New SPI interface with `onDescriptorLoaded(ArquillianDescriptor)` |
| NEW | `config/impl-base/src/main/java/.../config/impl/extension/ConfigurationListenerAdaptor.java` | Adaptor: `@Observes ArquillianDescriptor` → delegates to registered `ConfigurationListener` instances |
| MOD | `config/impl-base/src/main/java/.../config/impl/extension/ConfigExtension.java` | Register `ConfigurationListenerAdaptor` as observer |
| MOD | `container/impl-base/src/main/java/.../container/impl/client/container/ContainerRegistryCreator.java` | Implement `ConfigurationListener.onDescriptorLoaded()`; keep `@Observes` during transition |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/protocol/ProtocolRegistryCreator.java` | Implement `ConfigurationListener.onDescriptorLoaded()`; keep `@Observes` during transition |

### Phase 3: Container Orchestration -- Reuse Existing SPIs

| Action | File | Change |
|---|---|---|
| MOD | `container/impl-base/src/main/java/.../container/impl/client/container/ContainerLifecycleController.java` | Implement `ContainerMultiControlListener` (fan-out methods) and `ContainerControlListener` (per-container execution) |
| MOD | `container/impl-base/src/main/java/.../container/impl/client/deployment/ArchiveDeploymentExporter.java` | Implement `ContainerLifecycleListener.beforeDeploy()` |
| MOD | `container/impl-base/src/main/java/.../container/impl/ContainerExtension.java` | Register controller as listener instances |

### Phase 4: Container Test Orchestration

**Group A -- Test-to-Container Bridge:**

| Action | File | Change |
|---|---|---|
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/ContainerEventController.java` | Implement `TestLifecycleListener` for non-interceptor methods (`beforeSuite`, `afterSuite`, `beforeClass`, `afterClass`); interceptor methods deferred to Phase 5 |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/ContainerTestExtension.java` | Register `ContainerEventController` as `TestLifecycleListener`; register new adaptors |

**Group B -- Creator/Producer Observers:**

| Action | File | Change |
|---|---|---|
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/container/ClientContainerControllerCreator.java` | Implement `ContainerMultiControlListener.setupContainers()`; call `manager.bind()` directly |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/deployment/ClientDeployerCreator.java` | Implement `ContainerMultiControlListener.setupContainers()`; call `manager.bind()` directly |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/container/ContainerContainerControllerCreator.java` | Implement `TestLifecycleListener.beforeSuite()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/container/ContainerRestarter.java` | Implement `TestLifecycleListener.beforeClass()` |

**Group C -- Test Execution Chain:**

| Action | File | Change |
|---|---|---|
| NEW | `container/test-spi/src/main/java/.../container/test/spi/TestExecutionListener.java` | New SPI interface with `executeTest()`, `executeLocal()`, `executeRemote()` |
| NEW | `container/test-impl-base/src/main/java/.../container/test/impl/execution/TestExecutionListenerAdaptor.java` | Adaptor: `@Observes Test/LocalExecutionEvent/RemoteExecutionEvent` → delegates to `TestExecutionListener` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/execution/ClientTestExecuter.java` | Implement `TestExecutionListener.executeTest()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/execution/LocalTestExecuter.java` | Implement `TestExecutionListener.executeLocal()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/execution/RemoteTestExecuter.java` | Implement `TestExecutionListener.executeRemote()` |

**Group F -- Deployment Tooling (simple migration):**

| Action | File | Change |
|---|---|---|
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/deployment/tool/ArchiveDeploymentToolingExporter.java` | Implement `ContainerLifecycleListener.beforeDeploy()` |

**Group D -- Lifecycle Executors (NO CHANGES -- permanently `@Observes`):**

| Action | File | Change |
|---|---|---|
| -- | `container/test-impl-base/src/main/java/.../container/test/impl/execution/BeforeLifecycleEventExecuter.java` | No change -- stays as `@Observes` |
| -- | `container/test-impl-base/src/main/java/.../container/test/impl/execution/AfterLifecycleEventExecuter.java` | No change -- stays as `@Observes` |
| -- | `container/test-impl-base/src/main/java/.../container/test/impl/execution/ClientBeforeAfterLifecycleEventExecuter.java` | No change -- stays as `@Observes` |

### Phase 5: Interceptor Chain Evolution

| Action | File | Change |
|---|---|---|
| NEW | `core/spi/src/main/java/.../core/spi/Interceptor.java` | New `@FunctionalInterface` with `intercept(T event, InterceptorChain<T> chain)` |
| NEW | `core/spi/src/main/java/.../core/spi/InterceptorChain.java` | New interface with `proceed()` and `getEvent()` |
| MOD | `core/spi/src/main/java/.../core/spi/Manager.java` | Add `addInterceptor(Class<T>, Interceptor<T>, int priority)` |
| MOD | `core/impl-base/src/main/java/.../core/impl/ManagerImpl.java` | Implement `addInterceptor()`; unify old `EventContext<T>` observers and new `Interceptor<T>` into single priority-sorted chain in `fire()` |
| MOD | `core/impl-base/src/main/java/.../core/impl/EventContextImpl.java` | Evolve to support unified interceptor chain (old + new interleaved by priority) |
| MOD | `test/impl-base/src/main/java/.../test/impl/TestContextHandler.java` | Replace `@Observes(precedence=100) EventContext<SuiteEvent/ClassEvent/TestEvent>` with `manager.addInterceptor(..., 100)` |
| MOD | `container/impl-base/src/main/java/.../container/impl/client/ContainerDeploymentContextHandler.java` | Replace `@Observes EventContext<ContainerControlEvent/DeploymentEvent>` with `manager.addInterceptor(...)` |
| MOD | `container/impl-base/src/main/java/.../container/impl/client/container/DeploymentExceptionHandler.java` | Replace `@Observes EventContext<DeployDeployment>` with `manager.addInterceptor(...)` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/ContainerEventController.java` | Replace interceptor methods (`EventContext<BeforeTestLifecycleEvent/Test/AfterTestLifecycleEvent>`) with `manager.addInterceptor(...)` |

### Phase 6: DI System Evolution

| Action | File | Change |
|---|---|---|
| NEW | `core/spi/src/main/java/.../core/spi/ContextBinder.java` | New interface with `bind(scope, type, instance)` and `resolve(type)` |
| MOD | `config/impl-base/src/main/java/.../config/impl/extension/ConfigurationRegistrar.java` | Replace `@Inject @ApplicationScoped InstanceProducer<ArquillianDescriptor>` with `ContextBinder.bind()` |
| MOD | `container/impl-base/src/main/java/.../container/impl/client/container/ContainerRegistryCreator.java` | Replace `@Inject @ApplicationScoped InstanceProducer<ContainerRegistry>` with `ContextBinder.bind()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/protocol/ProtocolRegistryCreator.java` | Replace `@Inject @ApplicationScoped InstanceProducer<ProtocolRegistry>` with `ContextBinder.bind()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/deployment/DeploymentGenerator.java` | Replace `@Inject @ClassScoped InstanceProducer<DeploymentScenario>` with `ContextBinder.bind()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/container/ClientContainerControllerCreator.java` | Replace `@Inject @ApplicationScoped InstanceProducer<ContainerController>` with `ContextBinder.bind()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/deployment/ClientDeployerCreator.java` | Replace `@Inject @ApplicationScoped InstanceProducer<Deployer>` with `ContextBinder.bind()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/container/ContainerContainerControllerCreator.java` | Replace `@Inject @ApplicationScoped InstanceProducer<ContainerController>` with `ContextBinder.bind()` |

### Phase 7: Peripheral Module Migration

| Action | File | Change |
|---|---|---|
| MOD | `testenrichers/cdi/src/main/java/.../testenricher/cdi/container/BeanManagerProducer.java` | Implement `ManagerLifecycleListener.managerStarted()` |
| MOD | `testenrichers/initialcontext/src/main/java/.../testenricher/initialcontext/InitialContextProducer.java` | Implement `TestLifecycleListener.afterClass()` |
| MOD | `testenrichers/cdi/src/main/java/.../testenricher/cdi/CreationalContextDestroyer.java` | Replace `@Observes EventContext<After>` with `Interceptor<After>` |
| MOD | `protocols/servlet/src/main/java/.../protocol/servlet/runner/ServletContextRegistrar.java` | Implement `ManagerLifecycleListener.managerStarted()` |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/container/command/ContainerCommandObserver.java` | (Optional) Implement `CommandHandler` pattern |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/client/deployment/command/DeploymentCommandObserver.java` | (Optional) Implement `CommandHandler` pattern |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/enricher/resource/RemoteResourceCommandObserver.java` | (Optional) Implement `CommandHandler` pattern |

### Phase 8: Cleanup and Final Integration

| Action | File | Change |
|---|---|---|
| MOD | `test/impl-base/src/main/java/.../test/impl/EventTestRunnerAdaptor.java` | Add direct listener dispatch before event firing; dual-path for backward compat |
| DEL | `test/impl-base/src/main/java/.../test/impl/TestLifecycleListenerAdaptor.java` | Remove -- listeners now called directly by `EventTestRunnerAdaptor` |
| DEL | `test/impl-base/src/main/java/.../test/impl/TestEnrichmentListenerAdaptor.java` | Remove -- listeners called directly |
| DEL | `container/impl-base/src/main/java/.../container/impl/ContainerLifecycleListenerAdaptor.java` | Remove -- listeners called directly |
| DEL | `container/impl-base/src/main/java/.../container/impl/ContainerControlListenerAdaptor.java` | Remove -- listeners called directly |
| DEL | `container/impl-base/src/main/java/.../container/impl/ContainerMultiControlListenerAdaptor.java` | Remove -- listeners called directly |
| DEL | `core/impl-base/src/main/java/.../core/impl/ManagerLifecycleListenerAdaptor.java` | Remove -- listeners called directly |
| MOD | `core/spi/src/main/java/.../core/spi/LoadableExtension.java` | Add `listener()` and `interceptor()` methods to `ExtensionBuilder` |
| MOD | `test/impl-base/src/main/java/.../test/impl/TestExtension.java` | Remove adaptor observer registrations; add listener registrations |
| MOD | `container/impl-base/src/main/java/.../container/impl/ContainerExtension.java` | Remove adaptor observer registrations; add listener registrations |
| MOD | `core/impl-base/src/main/java/.../core/impl/CoreExtension.java` | Remove adaptor observer registration |
| MOD | `container/test-impl-base/src/main/java/.../container/test/impl/ContainerTestExtension.java` | Update all registrations to use `listener()`/`interceptor()` builder methods |

---

## Summary of New Artifacts by Phase

| Phase | New SPI Interface | New Adaptor | Module |
|---|---|---|---|
| 2 | `ConfigurationListener` | `ConfigurationListenerAdaptor` | core/spi, config/impl |
| 4 | `TestExecutionListener` | `TestExecutionListenerAdaptor` | container/test-spi, container/test-impl |
| 5 | `Interceptor<T>`, `InterceptorChain<T>` | (integrated into ManagerImpl) | core/spi, core/impl |
| 6 | `ContextBinder` | (passed by adaptors) | core/spi |
| 7 | `CommandHandler<T>` (optional) | `CommandHandlerAdaptor` | container/test-spi |
| 8 | Extended `ExtensionBuilder` | -- | core/spi |
