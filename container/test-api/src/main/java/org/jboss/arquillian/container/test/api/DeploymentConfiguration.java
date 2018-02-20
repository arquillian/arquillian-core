package org.jboss.arquillian.container.test.api;

import java.lang.annotation.Annotation;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 *
 * Model class that represents a deployment configuration.
 *
 * It wraps all possible annotations that can be used in a {@link Deployment } method.
 *
 * @version $Revision: $
 */
public class DeploymentConfiguration {

    private TargetsContainer targetsContainer;
    private OverProtocol overProtocol;
    private ShouldThrowException shouldThrowException;
    private Archive archive;
    private Descriptor descriptor;
    private Deployment deployment = new DeploymentClass();

    public TargetsContainer getTargets() {
        return targetsContainer;
    }

    public OverProtocol getOverProtocol() {
        return overProtocol;
    }

    public ShouldThrowException getShouldThrowException() {
        return shouldThrowException;
    }

    public Archive getArchive() {
        return archive;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public static class DeploymentContentBuilder {

        final DeploymentConfiguration deploymentConfiguration = new DeploymentConfiguration();

        public DeploymentContentBuilder(Archive<?> archive) {
            this.deploymentConfiguration.archive = archive;
        }

        public DeploymentContentBuilder(Descriptor descriptor) {
            this.deploymentConfiguration.descriptor = descriptor;
        }

        public DeploymentContentBuilder withTargetsContainer(String targetsContainer) {
            this.deploymentConfiguration.targetsContainer = new TargetsContainerClass(targetsContainer);
            return this;
        }

        public DeploymentContentBuilder withOverProtocol(String overProtocol) {
            this.deploymentConfiguration.overProtocol = new OverProtocolClass(overProtocol);
            return this;
        }

        public DeploymentContentBuilder withShouldThrowException(Class<? extends Exception> exception, boolean testeable) {
            this.deploymentConfiguration.shouldThrowException = new ShouldThrowExceptionClass(exception, testeable);
            return this;
        }

        public DeploymentContentBuilder withShouldThrowException(Class<? extends Exception> exception) {
            this.deploymentConfiguration.shouldThrowException = new ShouldThrowExceptionClass(exception, false);
            return this;
        }

        public DeploymentBuilder withDeployment() {
            return new DeploymentBuilder(this);
        }

        public DeploymentConfiguration get() {
            return this.deploymentConfiguration;
        }

    }

    public static class DeploymentBuilder {
        private final DeploymentContentBuilder deploymentContentBuilder;
        private final DeploymentClass deploymentClass = new DeploymentClass();

        DeploymentBuilder(DeploymentContentBuilder deploymentContentBuilder) {
            this.deploymentContentBuilder = deploymentContentBuilder;
        }

        public DeploymentBuilder withTestable(boolean testable) {
            this.deploymentClass.setTestable(testable);
            return this;
        }

        public DeploymentBuilder withOrder(int order) {
            this.deploymentClass.setOrder(order);
            return this;
        }

        public DeploymentBuilder withManaged(boolean managed) {
            this.deploymentClass.setManaged(managed);
            return this;
        }

        public DeploymentBuilder withName(String name) {
            this.deploymentClass.setName(name);
            return this;
        }

        public DeploymentContentBuilder build() {
            deploymentContentBuilder.deploymentConfiguration.deployment = deploymentClass;
            return deploymentContentBuilder;
        }
    }

    static class ShouldThrowExceptionClass implements ShouldThrowException {

        private Class<? extends Exception> exception;
        private boolean testable;

        ShouldThrowExceptionClass(Class<? extends Exception> exception, boolean testable) {
            this.exception = exception;
            this.testable = testable;
        }

        public Class<? extends Exception> value() {
            return exception;
        }

        public void setException(Class<? extends Exception> exception) {
            this.exception = exception;
        }

        public boolean testable() {
            return testable;
        }

        public void setTestable(boolean testable) {
            this.testable = testable;
        }

        public Class<? extends Annotation> annotationType() {
            return ShouldThrowException.class;
        }
    }

    static class OverProtocolClass implements OverProtocol {

        private final String overProtocol;

        OverProtocolClass(String overProtocol) {
            this.overProtocol = overProtocol;
        }

        public String value() {
            return overProtocol;
        }

        public Class<? extends Annotation> annotationType() {
            return OverProtocol.class;
        }
    }

    static class TargetsContainerClass implements TargetsContainer {

        private final String value;

        TargetsContainerClass(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

        public Class<? extends Annotation> annotationType() {
            return TargetsContainerClass.class;
        }
    }

    static class DeploymentClass implements Deployment {

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

        void setManaged(boolean managed) {
            this.managed = managed;
        }

        public boolean managed() {
            return managed;
        }

        void setOrder(int order) {
            this.order = order;
        }

        public int order() {
            return order;
        }

        void setTestable(boolean testable) {
            this.testable = testable;
        }

        public boolean testable() {
            return testable;
        }

        public Class<? extends Annotation> annotationType() {
            return Deployment.class;
        }
    }

}
