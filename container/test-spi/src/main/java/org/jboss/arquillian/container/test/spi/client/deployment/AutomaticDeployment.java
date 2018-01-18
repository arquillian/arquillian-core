package org.jboss.arquillian.container.test.spi.client.deployment;

import org.jboss.arquillian.container.test.api.DeploymentContent;
import org.jboss.arquillian.test.spi.TestClass;

public interface AutomaticDeployment {

    DeploymentContent generateDeploymentScenario(TestClass testClass);

}
