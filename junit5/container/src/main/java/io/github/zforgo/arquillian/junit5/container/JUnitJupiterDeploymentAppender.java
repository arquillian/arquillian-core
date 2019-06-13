package io.github.zforgo.arquillian.junit5.container;


import io.github.zforgo.arquillian.junit5.ArquillianExtension;
import io.github.zforgo.arquillian.junit5.ArquillianTestClassLifecycleManager;
import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestEngine;

public class JUnitJupiterDeploymentAppender extends CachedAuxilliaryArchiveAppender {
	@Override
	protected Archive<?> buildArchive() {
		JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-junit5.jar")
				.addPackages(
						true,
						"org.junit",
						"org/opentest4j",
						ArquillianExtension.class.getPackage().getName(),
						ArquillianTestClassLifecycleManager.class.getPackage().getName())
				.addAsServiceProvider(
						TestRunner.class,
						JUnitJupiterTestRunner.class)
				.addAsServiceProvider(TestEngine.class, JupiterTestEngine.class);
		return archive;
	}
}
