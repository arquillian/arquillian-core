package io.github.zforgo.arquillian.junit5.container;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class JUnitJupiterContainerExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder
				.service(AuxiliaryArchiveAppender.class, JUnitJupiterDeploymentAppender.class)
				.observer(RunModeEventHandler.class)
		;
	}
}
