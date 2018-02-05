package org.jboss.arquillian.container.test.spi.client.deployment;

import org.jboss.arquillian.container.test.api.DeploymentConfiguration;
import org.jboss.arquillian.test.spi.TestClass;

/**
 * SPI that all automatic deployment implementators must implements in order to generate the archive to be used by Arquillian deployer.
 *
 * @version $Revision: $
 */
public interface AutomaticDeployment {

    // tag::docs[]
    /**
     * Method called for generating the deployment configuration.
     * @param testClass of current running test.
     * @return Model object that contains all the information related to deployment configuration.
     */
    DeploymentConfiguration generateDeploymentScenario(TestClass testClass);
    // end::docs[]
}
