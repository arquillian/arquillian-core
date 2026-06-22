# bindAndFire Replacement: ArquillianDescriptor Configuration Flow

This document shows how the implicit `bindAndFire` event chain for `ArquillianDescriptor`
works today, and how it would be replaced with explicit `ConfigurationListener` dispatch.

---

## Current Flow: Implicit bindAndFire Chain

The `ArquillianDescriptor` reaches `ContainerRegistryCreator` and `ProtocolRegistryCreator`
through a hidden coupling between the DI system and the event bus. `InstanceProducer.set()`
internally calls `manager.bindAndFire()`, which both stores the value in the scoped context
AND fires it as an event on the bus.

```mermaid
sequenceDiagram
    participant Manager as ManagerImpl
    participant CfgReg as ConfigurationRegistrar
    participant InstImpl as InstanceImpl (InstanceProducer)
    participant AppCtx as ApplicationContext ObjectStore
    participant EventBus as ManagerImpl fire()
    participant EventCtx as EventContextImpl
    participant CRC as ContainerRegistryCreator
    participant PRC as ProtocolRegistryCreator

    Note over Manager: Manager.start() fires ManagerStarted

    Manager->>CfgReg: @Observes ManagerStarted
    activate CfgReg
    CfgReg->>CfgReg: loadConfiguration()
    Note right of CfgReg: parse arquillian.xml, resolve placeholders
    CfgReg->>InstImpl: descriptorInst.set(resolvedDesc)
    activate InstImpl

    Note over InstImpl: InstanceProducer.set() calls manager.bindAndFire()

    InstImpl->>Manager: bindAndFire(ApplicationScoped, ArquillianDescriptor, resolvedDesc)
    activate Manager

    Note over Manager,AppCtx: Step 1: bind into scope

    Manager->>AppCtx: bind(ArquillianDescriptor, resolvedDesc)
    AppCtx-->>Manager: stored

    Note over Manager,EventBus: Step 2: fire the value AS an event — hidden coupling

    Manager->>EventBus: fire(resolvedDesc)
    activate EventBus
    EventBus->>EventBus: resolveObservers(ArquillianDescriptor.class)

    Note over EventBus: Finds @Observes ArquillianDescriptor in CRC and PRC

    EventBus->>EventCtx: new EventContextImpl(observers=[CRC, PRC], event=resolvedDesc)
    activate EventCtx
    EventCtx->>CRC: @Observes ArquillianDescriptor — createRegistry(resolvedDesc)
    activate CRC
    CRC->>CRC: validate config, create LocalContainerRegistry
    CRC->>CRC: registry.set(reg) — binds ContainerRegistry into AppScope
    CRC-->>EventCtx: return
    deactivate CRC

    EventCtx->>PRC: @Observes ArquillianDescriptor — createRegistry(resolvedDesc)
    activate PRC
    PRC->>PRC: create ProtocolRegistry, discover protocols
    PRC->>PRC: registry.set(reg) — binds ProtocolRegistry into AppScope
    PRC-->>EventCtx: return
    deactivate PRC

    EventCtx-->>EventBus: return
    deactivate EventCtx
    EventBus-->>Manager: return
    deactivate EventBus
    Manager-->>InstImpl: return
    deactivate Manager
    InstImpl-->>CfgReg: return
    deactivate InstImpl
    deactivate CfgReg
```

### Problems with Current Flow

1. **Hidden coupling**: `InstanceProducer.set()` implicitly fires the value as an event — callers don't know this happens
2. **Untraceable**: Debugger shows `set()` → `bindAndFire()` → `fire()` → observer dispatch — indirect and surprising
3. **Fragile**: If `bindAndFire` is removed or the event bus is deprecated, `ContainerRegistryCreator` silently stops being invoked
4. **Testing**: No unit test verifies the `set()` → `fire()` → `@Observes` chain; tests mock around it

---

## Proposed Flow: Explicit ConfigurationListener Dispatch

Replace the implicit `fire(resolvedDesc)` with an explicit loop over registered
`ConfigurationListener` instances. The `InstanceProducer.set()` only binds into scope —
it no longer fires. The `ConfigurationRegistrar` itself is responsible for notifying listeners.

```mermaid
sequenceDiagram
    participant Manager as ManagerImpl
    participant CfgReg as ConfigurationRegistrar
    participant AppCtx as ApplicationContext ObjectStore
    participant CRC as ContainerRegistryCreator
    participant PRC as ProtocolRegistryCreator
    participant Binder as ContextBinder

    Note over Manager: Manager.start() invokes ManagerLifecycleListener.managerStarted()

    Manager->>Manager: getListeners(ManagerLifecycleListener.class)
    Manager->>CfgReg: managerStarted()
    activate CfgReg
    CfgReg->>CfgReg: loadConfiguration()
    Note right of CfgReg: parse arquillian.xml, resolve placeholders

    Note over CfgReg,AppCtx: Step 1: bind into scope — NO implicit fire()

    CfgReg->>Manager: bind(ApplicationScoped, ArquillianDescriptor, resolvedDesc)
    Manager->>AppCtx: store(ArquillianDescriptor, resolvedDesc)
    AppCtx-->>Manager: stored
    Manager-->>CfgReg: done

    Note over CfgReg,CRC: Step 2: explicitly notify ConfigurationListeners

    CfgReg->>Manager: getListeners(ConfigurationListener.class)
    Manager-->>CfgReg: [CRC, PRC]

    CfgReg->>CRC: onDescriptorLoaded(resolvedDesc, binder)
    activate CRC
    CRC->>CRC: validate config, create LocalContainerRegistry
    CRC->>Binder: bind(ApplicationScoped, ContainerRegistry, reg)
    Binder->>AppCtx: store(ContainerRegistry, reg)
    CRC-->>CfgReg: return
    deactivate CRC

    CfgReg->>PRC: onDescriptorLoaded(resolvedDesc, binder)
    activate PRC
    PRC->>PRC: create ProtocolRegistry, discover protocols
    PRC->>Binder: bind(ApplicationScoped, ProtocolRegistry, reg)
    Binder->>AppCtx: store(ProtocolRegistry, reg)
    PRC-->>CfgReg: return
    deactivate PRC

    deactivate CfgReg

    Note over Manager: Configuration complete. ContainerRegistry and ProtocolRegistry in AppScope.
```

### Key Changes

| Aspect | Current (bindAndFire) | Proposed (ConfigurationListener) |
|---|---|---|
| **Trigger** | `InstanceProducer.set()` implicitly fires event | `ConfigurationRegistrar` explicitly calls listeners |
| **Dispatch** | Event bus resolves `@Observes ArquillianDescriptor` | `manager.getListeners(ConfigurationListener.class)` |
| **Scope binding** | `bindAndFire()` does both bind + fire | `manager.bind()` for storage; listener call for notification |
| **Discoverability** | Must know that `set()` fires an event | Listener interface is explicit in the call chain |
| **Debuggability** | Stack trace goes through `fire()` → `EventContextImpl` → reflection | Direct method call: `listener.onDescriptorLoaded()` |
| **Dependency resolution** | Listeners use `@Inject Instance<T>` | Listeners receive `ContextBinder` parameter |
| **Ordering** | `@Observes` precedence (implicit) | Registration order or explicit priority parameter |

### ContextBinder Interface

```java
public interface ContextBinder {
    <T> void bind(Class<? extends Annotation> scope, Class<T> type, T instance);
    <T> T resolve(Class<T> type);
}
```

The `ContextBinder` replaces `@Inject @ApplicationScoped InstanceProducer<T>` — listeners
receive it as a parameter and use it to bind values into scoped contexts without needing
DI-managed fields.

### ConfigurationListener Interface

```java
public interface ConfigurationListener {
    default void onDescriptorLoaded(ArquillianDescriptor descriptor,
                                    ContextBinder binder) throws Exception {}
}
```

### ManagerImpl.bindAndFire() Deprecation Path

```java
// Current
public <T> void bindAndFire(Class<? extends Annotation> scope, Class<T> type, T instance) {
    bind(scope, type, instance);
    fire(instance);  // <-- this is the hidden coupling
}

// Transitional: add bind-only method, keep bindAndFire for backward compat
public <T> void bind(Class<? extends Annotation> scope, Class<T> type, T instance) {
    // store into scoped context only, no event firing
}

// InstanceProducer.set() changes from bindAndFire() to bind()
// Only after all @Observes-on-produced-value patterns are migrated to listeners
```

### What Must Change in InstanceImpl

```java
// Current (InstanceImpl.java:62-67)
@Override
public void set(T value) {
    if (scope == null) {
        throw new IllegalStateException("...");
    }
    manager.bindAndFire(scope, type, value);  // fires event
}

// Proposed
@Override
public void set(T value) {
    if (scope == null) {
        throw new IllegalStateException("...");
    }
    manager.bind(scope, type, value);  // bind only, no fire
}
```

**Warning**: This change breaks any code that depends on `set()` firing an event.
Every `@Observes SomeType` where `SomeType` is a value produced via `InstanceProducer.set()`
must be migrated to an explicit listener BEFORE `InstanceImpl` is changed.

### Affected bindAndFire Patterns

The following `InstanceProducer.set()` calls currently trigger implicit event dispatch
that downstream `@Observes` handlers depend on:

| Producer | Value Type | Downstream Observer(s) |
|---|---|---|
| `ConfigurationRegistrar.descriptorInst.set()` | `ArquillianDescriptor` | `ContainerRegistryCreator`, `ProtocolRegistryCreator` |

All other `InstanceProducer.set()` calls (e.g., `ContainerRegistryCreator.registry.set()`)
bind values that are consumed via `@Inject Instance<T>.get()` — not via `@Observes`.
The `ArquillianDescriptor` is the **only** value type where `set()` triggers observer dispatch
that downstream code depends on.
