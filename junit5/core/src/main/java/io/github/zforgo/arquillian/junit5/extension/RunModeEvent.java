package io.github.zforgo.arquillian.junit5.extension;

import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;

import java.lang.reflect.Method;

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
