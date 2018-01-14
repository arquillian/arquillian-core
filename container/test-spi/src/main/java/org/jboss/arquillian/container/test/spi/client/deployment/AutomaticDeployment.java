package org.jboss.arquillian.container.test.spi.client.deployment;

import java.lang.annotation.Annotation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public interface AutomaticDeployment {

    class DeploymentContent {

        TargetsContainer targetsContainer;
        OverProtocol overProtocol;
        ShouldThrowException shouldThrowException;
        Archive archive;
        Descriptor descriptor;
        Deployment deployment;

        public TargetsContainer getTargets() {return targetsContainer;}

        public OverProtocol getOverProtocol() {return overProtocol;}

        public ShouldThrowException getShouldThrowException() {return shouldThrowException;}

        public Archive getArchive() {return archive;}

        public Descriptor getDescriptor() {return descriptor;}

        public Deployment getDeployment() {return deployment;}
    }

    class DeploymentClass implements Deployment {

        private String name = "_DEFAULT_";
        private boolean managed = true;
        private int order = -1;
        private boolean testable = true;

        public void setName(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public void setManaged(boolean managed) {
            this.managed = managed;
        }

        public boolean managed() {
            return managed;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int order() {
            return order;
        }

        public void setTestable(boolean testable) {
            this.testable = testable;
        }

        public boolean testable() {
            return testable;
        }

        public Class<? extends Annotation> annotationType() {
            return Deployment.class;
        }
    }

    class DeploymentContentBuilder {

        DeploymentContent deploymentContent = new DeploymentContent();

        public DeploymentContentBuilder(Archive<?> archive) {
            this.deploymentContent.archive = archive;
        }

        public DeploymentContentBuilder(Descriptor descriptor) {
            this.deploymentContent.descriptor = descriptor;
        }

        public DeploymentContentBuilder withTargetsContainer(String targetsContainer) {
            return this;
        }

        public DeploymentContentBuilder withOverProtocol(String overProtocol) {
            return this;
        }

        public DeploymentContentBuilder withShouldThrowException(boolean testeable) {
            return this;
        }

        public DeploymentBuilder withDeployment() {
            return new DeploymentBuilder(this);
        }

        public DeploymentContent get() {
            return this.deploymentContent;
        }

    }

    class DeploymentBuilder {
        private DeploymentContentBuilder deploymentContentBuilder;
        private DeploymentClass deploymentClass = new DeploymentClass();

        DeploymentBuilder(DeploymentContentBuilder deploymentContentBuilder) {
            this.deploymentContentBuilder = deploymentContentBuilder;
        }

        public DeploymentBuilder withTestable(boolean testable) {
            this.deploymentClass.setTestable(testable);
            return this;
        }

        public DeploymentContentBuilder build() {
            deploymentContentBuilder.deploymentContent.deployment = deploymentClass;
            return deploymentContentBuilder;
        }
    }

    DeploymentContent generateDeploymentScenario(TestClass testClass);

}
