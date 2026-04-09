# Arquillian Event Sequence Diagrams

This document contains sequence diagrams for the various event flows in the Arquillian framework.

## Core Event Flow

The core event system in Arquillian is based on an observer pattern where events are fired by the `Manager` and processed by observers. Here's the sequence diagram for the core event flow:

```mermaid
sequenceDiagram
    participant Client as Client Code
    participant Manager as ManagerImpl
    participant EventContext as EventContextImpl
    participant Observer as ObserverMethod
    participant NonManagedObserver as NonManagedObserver

    Client->>Manager: fire(event)
    Manager->>Manager: resolveObservers(event.class)
    Manager->>Manager: resolveInterceptorObservers(event.class)
    Manager->>EventContext: new EventContextImpl(manager, interceptors, observers, nonManagedObserver, event)
    EventContext->>EventContext: proceed()
    
    alt Has Interceptors
        EventContext->>Observer: invoke(manager, eventContext)
        Observer-->>EventContext: proceed()
    end
    
    loop For each observer
        EventContext->>Observer: invoke(manager, event)
        Observer->>Observer: resolveArguments(manager, event)
        Observer->>Observer: method.invoke(target, arguments)
    end
    
    alt Has NonManagedObserver
        EventContext->>Manager: inject(nonManagedObserver)
        EventContext->>NonManagedObserver: fired(event)
    end
    
    EventContext-->>Manager: return
    Manager-->>Client: return
```

This diagram shows how events are processed in Arquillian:

1. A client fires an event through the Manager
2. The Manager resolves all observers and interceptors for the event type
3. An EventContext is created to handle the event processing
4. If there are interceptors, they are invoked first
5. Then all observers are invoked with the event
6. Finally, if there's a non-managed observer, it's injected and notified
7. Control returns to the client

## Container Lifecycle Events

Container lifecycle events represent the lifecycle of a container in Arquillian. Here's the sequence diagram:

```mermaid
sequenceDiagram
    participant Client as Client Code
    participant Manager as Manager
    participant Container as Container
    participant DeployableContainer as DeployableContainer
    
    Client->>Manager: fire(SetupContainer)
    Manager->>Manager: fire(BeforeSetup)
    Manager->>Container: setup()
    Manager->>Manager: fire(AfterSetup)
    
    Client->>Manager: fire(StartContainer)
    Manager->>Manager: fire(BeforeStart)
    Manager->>DeployableContainer: start()
    Manager->>Manager: fire(AfterStart)
    
    Client->>Manager: fire(DeployDeployment)
    Manager->>Manager: fire(BeforeDeploy)
    Manager->>DeployableContainer: deploy()
    Manager->>Manager: fire(AfterDeploy)
    
    Client->>Manager: fire(UnDeployDeployment)
    Manager->>Manager: fire(BeforeUnDeploy)
    Manager->>DeployableContainer: undeploy()
    Manager->>Manager: fire(AfterUnDeploy)
    
    Client->>Manager: fire(StopContainer)
    Manager->>Manager: fire(BeforeStop)
    Manager->>DeployableContainer: stop()
    Manager->>Manager: fire(AfterStop)
    
    Client->>Manager: fire(KillContainer)
    Manager->>Manager: fire(BeforeKill)
    Manager->>DeployableContainer: kill()
    Manager->>Manager: fire(AfterKill)
```

This diagram shows the container lifecycle events:

1. Container setup: SetupContainer → BeforeSetup → Container.setup() → AfterSetup
2. Container start: StartContainer → BeforeStart → DeployableContainer.start() → AfterStart
3. Deployment: DeployDeployment → BeforeDeploy → DeployableContainer.deploy() → AfterDeploy
4. Undeployment: UnDeployDeployment → BeforeUnDeploy → DeployableContainer.undeploy() → AfterUnDeploy
5. Container stop: StopContainer → BeforeStop → DeployableContainer.stop() → AfterStop
6. Container kill: KillContainer → BeforeKill → DeployableContainer.kill() → AfterKill

## Test Lifecycle Events

Test lifecycle events represent the execution of tests in Arquillian. Here's the sequence diagram:

```mermaid
sequenceDiagram
    participant Client as Test Runner
    participant Manager as Manager
    participant TestInstance as Test Instance
    
    Client->>Manager: fire(BeforeSuite)
    
    loop For each test class
        Client->>Manager: fire(BeforeClass)
        
        loop For each test method
            Client->>Manager: fire(Before)
            Client->>Manager: fire(BeforeEnrichment)
            Manager->>TestInstance: enrich test instance
            Client->>Manager: fire(AfterEnrichment)
            
            Client->>Manager: fire(Test)
            alt Local Execution
                Manager->>Manager: fire(LocalExecutionEvent)
            else Remote Execution
                Manager->>Manager: fire(RemoteExecutionEvent)
            end
            
            Client->>Manager: fire(After)
        end
        
        Client->>Manager: fire(AfterClass)
    end
    
    Client->>Manager: fire(AfterSuite)
```

This diagram shows the test lifecycle events:

1. Suite lifecycle: BeforeSuite → (test execution) → AfterSuite
2. Class lifecycle: BeforeClass → (test methods execution) → AfterClass
3. Test method lifecycle: Before → BeforeEnrichment → AfterEnrichment → Test → (LocalExecutionEvent or RemoteExecutionEvent) → After

## Deployment Events

Deployment events represent the deployment of artifacts to containers. Here's the sequence diagram:

```mermaid
sequenceDiagram
    participant Client as Client Code
    participant Manager as Manager
    participant Container as Container
    participant DeployableContainer as DeployableContainer
    
    Client->>Manager: fire(DeployManagedDeployments)
    
    loop For each deployment
        Manager->>Manager: fire(DeployDeployment)
        Manager->>Manager: fire(BeforeDeploy)
        Manager->>DeployableContainer: deploy()
        Manager->>Manager: fire(AfterDeploy)
    end
    
    Client->>Manager: fire(UnDeployManagedDeployments)
    
    loop For each deployment
        Manager->>Manager: fire(UnDeployDeployment)
        Manager->>Manager: fire(BeforeUnDeploy)
        Manager->>DeployableContainer: undeploy()
        Manager->>Manager: fire(AfterUnDeploy)
    end
```

This diagram shows the deployment events:

1. Managed deployments: DeployManagedDeployments → (for each deployment) → DeployDeployment → BeforeDeploy → deploy() → AfterDeploy
2. Managed undeployments: UnDeployManagedDeployments → (for each deployment) → UnDeployDeployment → BeforeUnDeploy → undeploy() → AfterUnDeploy

## Execution Events

Execution events represent the execution of test methods. Here's the sequence diagram:

```mermaid
sequenceDiagram
    participant Client as Test Runner
    participant Manager as Manager
    participant Executor as TestMethodExecutor
    
    Client->>Manager: fire(Test)
    
    alt Local Execution
        Manager->>Manager: fire(LocalExecutionEvent)
        Manager->>Executor: execute()
    else Remote Execution
        Manager->>Manager: fire(RemoteExecutionEvent)
        Manager->>Executor: execute() (via protocol)
    end
```

This diagram shows the execution events:

1. Local execution: Test → LocalExecutionEvent → execute()
2. Remote execution: Test → RemoteExecutionEvent → execute() (via protocol)