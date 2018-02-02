package org.jboss.arquillian.container.test.impl.client.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.DeploymentConfiguration;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;

public abstract class AbstractDeploymentScenarioGenerator implements DeploymentScenarioGenerator {

    private static final Logger log = Logger.getLogger(AbstractDeploymentScenarioGenerator.class.getName());

    protected abstract List<DeploymentConfiguration> generateDeploymentContent(TestClass testClass);

    public List<DeploymentDescription> generate(TestClass testClass) {
        List<DeploymentDescription> deployments = new ArrayList<DeploymentDescription>();

        final List<DeploymentConfiguration> deploymentConfigurations = generateDeploymentContent(testClass);

        for (DeploymentConfiguration deploymentConfiguration : deploymentConfigurations) {
            deployments.add(configureDeploymentDescription(deploymentConfiguration, testClass));
        }

        sortByDeploymentOrder(deployments);

        return deployments;
    }

    protected Archive manipulateArchive(TestClass testClass, String deploymentName, Archive archive) {
        return archive;
    }

    private DeploymentDescription configureDeploymentDescription(DeploymentConfiguration deploymentConfiguration, TestClass testClass) {

        DeploymentDescription deploymentDescription = null;
        final Deployment deployment = deploymentConfiguration.getDeployment();
        if (deploymentConfiguration.getArchive() != null) {
            deploymentDescription = new DeploymentDescription(deployment.name(), manipulateArchive(testClass, deployment.name(),
                deploymentConfiguration.getArchive()));
            deploymentDescription.shouldBeTestable(deployment.testable());
        } else if (deploymentConfiguration.getDescriptor() != null) {
            deploymentDescription = new DeploymentDescription(deployment.name(), deploymentConfiguration.getDescriptor());
        }

        if (deploymentDescription == null) {
            throw new IllegalArgumentException("Deployment does not contain an archive nor a descriptor to deploy");
        }

        logWarningIfArchiveHasUnexpectedFileExtension(deploymentDescription);

        deploymentDescription.shouldBeManaged(deployment.managed());
        deploymentDescription.setOrder(deployment.order());

        final TargetDescription target = generateTarget(deploymentConfiguration.getTargets());

        if (target != null) {
            deploymentDescription.setTarget(target);
        }

        final ProtocolDescription protocol = generateProtocol(deploymentConfiguration.getOverProtocol());

        if (protocol != null) {
            deploymentDescription.setProtocol(protocol);
        }

        final ShouldThrowException shouldThrowException = deploymentConfiguration.getShouldThrowException();
        if (shouldThrowException != null) {
            deploymentDescription.setExpectedException(shouldThrowException.value());
            deploymentDescription.shouldBeTestable(shouldThrowException.testable());
        }

        return deploymentDescription;
    }



    /**
     * @param targetsContainer
     * @return
     */
    private TargetDescription generateTarget(TargetsContainer targetsContainer) {
        if (targetsContainer != null) {
            return new TargetDescription(targetsContainer.value());
        }
        return TargetDescription.DEFAULT;
    }

    /**
     * @param overProtocol
     * @return
     */
    private ProtocolDescription generateProtocol(OverProtocol overProtocol) {
        if (overProtocol != null) {
            return new ProtocolDescription(overProtocol.value());
        }
        return ProtocolDescription.DEFAULT;
    }

    private void sortByDeploymentOrder(List<DeploymentDescription> deploymentDescriptions) {
        // sort them by order
        Collections.sort(deploymentDescriptions, new Comparator<DeploymentDescription>() {
            public int compare(DeploymentDescription d1, DeploymentDescription d2) {
                return new Integer(d1.getOrder()).compareTo(d2.getOrder());
            }
        });
    }

    private void logWarningIfArchiveHasUnexpectedFileExtension(final DeploymentDescription deployment) {
        if (!Validate.archiveHasExpectedFileExtension(deployment.getArchive())) {
            log.warning("Deployment archive of type " + deployment.getArchive().getClass().getSimpleName()
                + " has been given an unexpected file extension. Archive name: " + deployment.getArchive().getName()
                + ", deployment name: " + deployment.getName() + ". It might not be wrong, but the container will"
                + " rely on the given file extension, the archive type is only a description of a certain structure.");
        }
    }

}
