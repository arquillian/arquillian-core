package io.github.zforgo.arquillian.junit5.container;

import io.github.zforgo.arquillian.junit5.extension.RunModeEvent;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.test.impl.RunModeUtils;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

public class RunModeEventHandler {

	@Inject
	private Instance<Deployment> deployment;

	public void handleEvent(@Observes RunModeEvent event) {
		boolean runAsClient = RunModeUtils.isRunAsClient(deployment.get(), event.getTestClass(), event.getTestMethod());
		event.setRunAsClient(runAsClient);
	}
}
