# Test Framework Integration — Class Diagrams

This document shows how JUnit 4, JUnit 5 (Jupiter), and TestNG each integrate with Arquillian
using Mermaid class diagrams. All three frameworks share a common adaptor layer
(`TestRunnerAdaptor` / `EventTestRunnerAdaptor`) and differ only in the entry-point glue code.

---

## 1. Shared Infrastructure

All three frameworks delegate to the same `EventTestRunnerAdaptor`, which translates lifecycle
calls into `Manager#fire()` events. The `Manager` event bus then dispatches to all registered
`@Observes` observers.

```mermaid
classDiagram
    class TestRunnerAdaptor {
        <<interface>>
        +beforeSuite() void
        +afterSuite() void
        +beforeClass(Class, LifecycleMethodExecutor) void
        +afterClass(Class, LifecycleMethodExecutor) void
        +before(Object, Method, LifecycleMethodExecutor) void
        +after(Object, Method, LifecycleMethodExecutor) void
        +test(TestMethodExecutor) TestResult
        +fireCustomLifecycle(TestLifecycleEvent) void
        +shutdown() void
    }

    class EventTestRunnerAdaptor {
        -manager Manager
        +beforeSuite() void
        +afterSuite() void
        +beforeClass(Class, LifecycleMethodExecutor) void
        +afterClass(Class, LifecycleMethodExecutor) void
        +before(Object, Method, LifecycleMethodExecutor) void
        +after(Object, Method, LifecycleMethodExecutor) void
        +test(TestMethodExecutor) TestResult
        +fireCustomLifecycle(TestLifecycleEvent) void
        +shutdown() void
    }

    class TestRunnerAdaptorBuilder {
        +build(TestMethodExecutor) TestRunnerAdaptor$
    }

    class TestMethodExecutor {
        <<interface>>
        +invoke() void
        +getInstance() Object
        +getMethod() Method
        +getTestClass() Class
    }

    class Manager {
        <<interface>>
        +fire(Object) void
        +addListener(Class~T~, T) void
        +getListeners(Class~T~) List~T~
        +getHookSystem() LifecycleHookSystem
    }

    class ClientTestExecuter {
        +execute(Test) void
    }

    TestRunnerAdaptor <|.. EventTestRunnerAdaptor : implements
    TestRunnerAdaptorBuilder ..> TestRunnerAdaptor : creates
    EventTestRunnerAdaptor --> Manager : fires events via fire()
    EventTestRunnerAdaptor ..> TestMethodExecutor : wraps in Test event
    Manager ..> ClientTestExecuter : notifies via @Observes Test
```

**Key point:** `TestRunnerAdaptorBuilder` uses `LoadableExtensionLoader` to discover and load
all `LoadableExtension` SPI implementations before returning the configured adaptor.

---

## 2. JUnit 4 Integration

### 2a. Runner and Lifecycle Delegation

```mermaid
classDiagram
    class BlockJUnit4ClassRunner {
        <<JUnit 4 Framework>>
        +run(RunNotifier) void
        +runChild(FrameworkMethod, RunNotifier) void
    }

    class Arquillian {
        -adaptorManager AdaptorManager
        +run(RunNotifier) void
        +createTest() Object
        +withBefores(FrameworkMethod, Object, Statement) Statement
        +withAfters(FrameworkMethod, Object, Statement) Statement
    }

    class AdaptorManager {
        <<abstract>>
        #adaptor TestRunnerAdaptor
        +run(RunNotifier)$ void
        +fireCustomLifecycleEvent(TestLifecycleEvent)$ void
        +getAdaptor() TestRunnerAdaptor
    }

    class AdaptorManagerWithNotifier {
        -notifier RunNotifier
        +run(RunNotifier) void
        +fireCustomLifecycleEvent(TestLifecycleEvent) void
    }

    class MethodInvoker {
        <<abstract>>
        +evaluate() void
        +evaluateLifecycle()$ void
    }

    class ArquillianTestClass {
        <<implements TestRule>>
        +apply(Statement, Description) Statement
    }

    class ArquillianTest {
        <<implements MethodRule>>
        +apply(Statement, FrameworkMethod, Object) Statement
    }

    class RulesEnricher {
        +enrich(RulesEnrichment, Object) void
    }

    class JUnitCoreExtension {
        <<implements LoadableExtension>>
        +register(ExtensionBuilder) void
    }

    class JUnitTestRunner {
        <<implements TestRunner>>
        +execute(LocalExecutionEvent) void
    }

    BlockJUnit4ClassRunner <|-- Arquillian : extends
    AdaptorManager <|-- AdaptorManagerWithNotifier : extends
    Arquillian --> AdaptorManager : delegates lifecycle to
    AdaptorManagerWithNotifier --> ArquillianTestClass : creates as class-level rule
    AdaptorManagerWithNotifier --> ArquillianTest : creates as method-level rule
    ArquillianTestClass --> MethodInvoker : wraps as JUnit Statement
    ArquillianTest --> MethodInvoker : wraps as JUnit Statement
    MethodInvoker ..> RulesEnricher : fires RulesEnrichment event
    JUnitCoreExtension ..> JUnitTestRunner : registers as observer
    JUnitCoreExtension ..> RulesEnricher : registers as observer
```

### 2b. JUnit 4 Custom Lifecycle Events

JUnit 4 fires several custom event types that extend the standard lifecycle hierarchy. These
events are dispatched via `Manager#fire()` and reach `@Observes` observers, but are **not**
visible to the `TestLifecycleListener` SPI (which observes only exact concrete types like
`Before`/`After`).

```mermaid
classDiagram
    class TestLifecycleEvent {
        <<abstract>>
        +getTestInstance() Object
        +getTestMethod() Method
        +getExecutor() LifecycleMethodExecutor
    }

    class BeforeTestLifecycleEvent {
        <<abstract>>
    }

    class AfterTestLifecycleEvent {
        <<abstract>>
    }

    class Before {
    }

    class After {
    }

    class BeforeRules {
        -statement Statement
        -testClass TestClass
    }

    class AfterRules {
        -statement Statement
        -testClass TestClass
    }

    class RulesEnrichment {
        -statement Statement
        -testClass TestClass
    }

    TestLifecycleEvent <|-- BeforeTestLifecycleEvent : extends
    TestLifecycleEvent <|-- AfterTestLifecycleEvent : extends
    BeforeTestLifecycleEvent <|-- Before : extends
    BeforeTestLifecycleEvent <|-- BeforeRules : extends
    BeforeTestLifecycleEvent <|-- RulesEnrichment : extends
    AfterTestLifecycleEvent <|-- After : extends
    AfterTestLifecycleEvent <|-- AfterRules : extends
```

---

## 3. JUnit 5 (Jupiter) Integration

### 3a. Extension and Lifecycle Manager

JUnit 5 uses the `Extension` SPI rather than a custom runner. `ArquillianExtension` is
registered via `@ExtendWith(ArquillianExtension.class)` and implements seven JUnit 5 extension
interfaces.

```mermaid
classDiagram
    class BeforeAllCallback {
        <<JUnit 5 Interface>>
        +beforeAll(ExtensionContext) void
    }

    class AfterAllCallback {
        <<JUnit 5 Interface>>
        +afterAll(ExtensionContext) void
    }

    class BeforeEachCallback {
        <<JUnit 5 Interface>>
        +beforeEach(ExtensionContext) void
    }

    class AfterEachCallback {
        <<JUnit 5 Interface>>
        +afterEach(ExtensionContext) void
    }

    class BeforeTestExecutionCallback {
        <<JUnit 5 Interface>>
        +beforeTestExecution(ExtensionContext) void
    }

    class InvocationInterceptor {
        <<JUnit 5 Interface>>
        +interceptTestMethod(Invocation, ReflectiveInvocationContext, ExtensionContext) void
    }

    class ParameterResolver {
        <<JUnit 5 Interface>>
        +supportsParameter(ParameterContext, ExtensionContext) boolean
        +resolveParameter(ParameterContext, ExtensionContext) Object
    }

    class ArquillianExtension {
        +beforeAll(ExtensionContext) void
        +afterAll(ExtensionContext) void
        +beforeEach(ExtensionContext) void
        +afterEach(ExtensionContext) void
        +beforeTestExecution(ExtensionContext) void
        +interceptTestMethod(Invocation, ReflectiveInvocationContext, ExtensionContext) void
        +supportsParameter(ParameterContext, ExtensionContext) boolean
        +resolveParameter(ParameterContext, ExtensionContext) Object
    }

    class JUnitJupiterTestClassLifecycleManager {
        <<implements AutoCloseable>>
        <<implements CloseableResource>>
        -adaptor TestRunnerAdaptor
        +beforeAll(Class) void
        +afterAll() void
        +beforeEach(Object, Method) void
        +afterEach(Object, Method) void
        +close() void
    }

    class MethodParameterObserver {
        +on(MethodParameterProducerEvent) void
    }

    class RunModeEventHandler {
        +on(RunModeEvent) void
    }

    class JUnitJupiterCoreExtension {
        <<implements LoadableExtension>>
        +register(ExtensionBuilder) void
    }

    class JUnitJupiterTestRunner {
        <<implements TestRunner>>
        +execute(LocalExecutionEvent) void
    }

    BeforeAllCallback <|.. ArquillianExtension : implements
    AfterAllCallback <|.. ArquillianExtension : implements
    BeforeEachCallback <|.. ArquillianExtension : implements
    AfterEachCallback <|.. ArquillianExtension : implements
    BeforeTestExecutionCallback <|.. ArquillianExtension : implements
    InvocationInterceptor <|.. ArquillianExtension : implements
    ParameterResolver <|.. ArquillianExtension : implements

    ArquillianExtension --> JUnitJupiterTestClassLifecycleManager : stores in ExtensionContext.Store
    JUnitJupiterTestClassLifecycleManager --> TestRunnerAdaptor : uses
    JUnitJupiterCoreExtension ..> MethodParameterObserver : registers
    JUnitJupiterCoreExtension ..> RunModeEventHandler : registers
    JUnitJupiterCoreExtension ..> JUnitJupiterTestRunner : registers
```

### 3b. JUnit 5 Custom Lifecycle Events

JUnit 5 introduces additional events beyond the standard `Before`/`After`. Like the JUnit 4
custom events, these reach `@Observes` observers but are invisible to the `TestLifecycleListener`
SPI.

```mermaid
classDiagram
    class TestLifecycleEvent {
        <<abstract>>
    }

    class BeforeTestLifecycleEvent {
        <<abstract>>
    }

    class RunModeEvent {
        -runAsClient boolean
        +isRunAsClient() boolean
    }

    class BeforeTestExecutionEvent {
    }

    class MethodParameterProducerEvent {
        -parameterTypes List
        +getParameterTypes() List
        +addParameter(Object) void
        +getParameters() List
    }

    TestLifecycleEvent <|-- BeforeTestLifecycleEvent : extends
    BeforeTestLifecycleEvent <|-- RunModeEvent : extends
    TestLifecycleEvent <|-- BeforeTestExecutionEvent : extends
    TestLifecycleEvent <|-- MethodParameterProducerEvent : extends
```

---

## 4. TestNG Integration

TestNG uses a base class (`Arquillian`) rather than a runner or extension. Tests extend this
class, which implements `IHookable` so TestNG delegates test method invocation through it. An
`IInvokedMethodListener` (`UpdateResultListener`) is registered via `@Listeners` to capture test
outcomes.

```mermaid
classDiagram
    class IHookable {
        <<TestNG Interface>>
        +run(IHookCallBack, ITestResult) void
    }

    class IInvokedMethodListener {
        <<TestNG Interface>>
        +beforeInvocation(IInvokedMethod, ITestResult) void
        +afterInvocation(IInvokedMethod, ITestResult) void
    }

    class Arquillian {
        <<abstract>>
        <<@Listeners(UpdateResultListener)>>
        #adaptor TestRunnerAdaptor
        +run(IHookCallBack, ITestResult) void
    }

    class UpdateResultListener {
        +afterInvocation(IInvokedMethod, ITestResult) void
    }

    class TestNGCoreExtension {
        <<implements LoadableExtension>>
        +register(ExtensionBuilder) void
    }

    class TestNGTestRunner {
        <<implements TestRunner>>
        +execute(LocalExecutionEvent) void
    }

    IHookable <|.. Arquillian : implements
    IInvokedMethodListener <|.. UpdateResultListener : implements
    Arquillian ..> UpdateResultListener : registers via @Listeners
    Arquillian --> TestRunnerAdaptor : uses adaptor
    TestNGCoreExtension ..> TestNGTestRunner : registers
```

---

## 5. Client vs. In-Container Execution

All three framework runners eventually fire a `Test` event into the `Manager`. `ClientTestExecuter`
observes `Test` and decides whether to run locally (client-side) or remotely (in-container).

```mermaid
classDiagram
    class Test {
        -executor TestMethodExecutor
        +getExecutor() TestMethodExecutor
    }

    class ClientTestExecuter {
        +execute(Test) void
    }

    class LocalExecutionEvent {
        -executor TestMethodExecutor
        +getExecutor() TestMethodExecutor
    }

    class RemoteExecutionEvent {
        -executor TestMethodExecutor
        +getExecutor() TestMethodExecutor
    }

    class JUnitTestRunner {
        <<JUnit 4>>
        +execute(LocalExecutionEvent) void
    }

    class JUnitJupiterTestRunner {
        <<JUnit 5>>
        +execute(LocalExecutionEvent) void
    }

    class TestNGTestRunner {
        <<TestNG>>
        +execute(LocalExecutionEvent) void
    }

    class RemoteTestExecuter {
        +execute(RemoteExecutionEvent) void
    }

    ClientTestExecuter ..> Test : @Observes
    ClientTestExecuter ..> LocalExecutionEvent : fires if @RunAsClient or not testable
    ClientTestExecuter ..> RemoteExecutionEvent : fires if in-container

    LocalExecutionEvent <.. JUnitTestRunner : @Observes
    LocalExecutionEvent <.. JUnitJupiterTestRunner : @Observes
    LocalExecutionEvent <.. TestNGTestRunner : @Observes
    RemoteExecutionEvent <.. RemoteTestExecuter : @Observes
```

**Decision logic in `ClientTestExecuter`:**
- If the deployment is marked `testable=false`, or the test method is annotated `@RunAsClient`,
  fire `LocalExecutionEvent` (runs on the client JVM without going to the container).
- Otherwise fire `RemoteExecutionEvent`, which the protocol layer (Servlet or JMX) forwards to
  the in-container side for execution.

---

## 6. Complete Integration Overview

```mermaid
classDiagram
    class Arquillian_JUnit4 {
        <<extends BlockJUnit4ClassRunner>>
        JUnit 4 entry point
    }

    class ArquillianExtension_JUnit5 {
        <<implements 7 JUnit 5 interfaces>>
        JUnit 5 entry point
    }

    class Arquillian_TestNG {
        <<abstract, implements IHookable>>
        TestNG entry point
    }

    class TestRunnerAdaptorBuilder {
        +build(TestMethodExecutor) TestRunnerAdaptor$
    }

    class EventTestRunnerAdaptor {
        <<implements TestRunnerAdaptor>>
        Shared adaptor layer
    }

    class Manager {
        <<interface>>
        Core event bus
    }

    class LoadableExtension {
        <<interface>>
        JUnitCoreExtension
        JUnitJupiterCoreExtension
        TestNGCoreExtension
        ContainerExtension
        TestExtension
        ...
    }

    Arquillian_JUnit4 --> TestRunnerAdaptorBuilder : uses to create adaptor
    ArquillianExtension_JUnit5 --> TestRunnerAdaptorBuilder : uses to create adaptor
    Arquillian_TestNG --> TestRunnerAdaptorBuilder : uses to create adaptor

    TestRunnerAdaptorBuilder ..> EventTestRunnerAdaptor : creates
    TestRunnerAdaptorBuilder ..> LoadableExtension : discovers via SPI

    EventTestRunnerAdaptor --> Manager : fires lifecycle events
    LoadableExtension ..> Manager : registers observers with
```
