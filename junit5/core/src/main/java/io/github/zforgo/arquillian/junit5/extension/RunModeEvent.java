package io.github.zforgo.arquillian.junit5.extension;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;

public class RunModeEvent extends BeforeTestLifecycleEvent {

    private boolean runAsClient = true;

    public RunModeEvent(Object testInstance, Method testMethod) {
        super(testInstance, testMethod);
    }

    public boolean isRunAsClient() {
        return runAsClient;
    }

    public void setRunAsClient(boolean runAsClient) {
        this.runAsClient = runAsClient;
    }
}
