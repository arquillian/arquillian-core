/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class DeploymentContent {

    TargetsContainer targetsContainer;
    OverProtocol overProtocol;
    ShouldThrowException shouldThrowException;
    Archive archive;
    Descriptor descriptor;
    Deployment deployment = new DeploymentClass();

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

        DeploymentContent deploymentContent = new DeploymentContent();

        public DeploymentContentBuilder(Archive<?> archive) {
            this.deploymentContent.archive = archive;
        }

        public DeploymentContentBuilder(Descriptor descriptor) {
            this.deploymentContent.descriptor = descriptor;
        }

        public DeploymentContentBuilder withTargetsContainer(String targetsContainer) {
            this.deploymentContent.targetsContainer = new TargetsContainerClass(targetsContainer);
            return this;
        }

        public DeploymentContentBuilder withOverProtocol(String overProtocol) {
            this.deploymentContent.overProtocol = new OverProtocolClass(overProtocol);
            return this;
        }

        public DeploymentContentBuilder withShouldThrowException(Class<? extends Exception> exception, boolean testeable) {
            this.deploymentContent.shouldThrowException = new ShouldThrowExceptionClass(exception, testeable);
            return this;
        }

        public DeploymentContentBuilder withShouldThrowException(Class<? extends Exception> exception) {
            this.deploymentContent.shouldThrowException = new ShouldThrowExceptionClass(exception, false);
            return this;
        }

        public DeploymentBuilder withDeployment() {
            return new DeploymentBuilder(this);
        }

        public DeploymentContent get() {
            return this.deploymentContent;
        }

    }

    public static class DeploymentBuilder {
        private DeploymentContentBuilder deploymentContentBuilder;
        private DeploymentClass deploymentClass = new DeploymentClass();

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
            deploymentContentBuilder.deploymentContent.deployment = deploymentClass;
            return deploymentContentBuilder;
        }
    }

    static class ShouldThrowExceptionClass implements ShouldThrowException {

        private Class<? extends Exception> exception;
        private boolean testable;

        public ShouldThrowExceptionClass(Class<? extends Exception> exception, boolean testable) {
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

        private String overProtocol;

        public OverProtocolClass(String overProtocol) {
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

        private String value;

        public TargetsContainerClass(String value) {
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

}
