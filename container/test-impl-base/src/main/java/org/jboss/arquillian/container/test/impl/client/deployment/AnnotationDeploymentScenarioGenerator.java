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
package org.jboss.arquillian.container.test.impl.client.deployment;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.DeploymentConfiguration;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * {@link DeploymentScenarioGenerator} that builds a {@link DeploymentScenario} based on
 * the standard Arquillian API annotations.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class AnnotationDeploymentScenarioGenerator extends AbstractDeploymentScenarioGenerator implements DeploymentScenarioGenerator {

    protected List<DeploymentConfiguration> generateDeploymentContent(TestClass testClass) {

        List<DeploymentConfiguration> deployments = new ArrayList<DeploymentConfiguration>();
        Method[] deploymentMethods = testClass.getMethods(Deployment.class);

        for (Method deploymentMethod : deploymentMethods) {
            validate(deploymentMethod);
            deployments.add(generateDeploymentContent(deploymentMethod));
        }

        return deployments;
    }

    private void validate(Method deploymentMethod) {
        if (!Modifier.isStatic(deploymentMethod.getModifiers())) {
            throw new IllegalArgumentException(
                "Method annotated with " + Deployment.class.getName() + " is not static. " + deploymentMethod);
        }
        if (!Archive.class.isAssignableFrom(deploymentMethod.getReturnType()) && !Descriptor.class.isAssignableFrom(
            deploymentMethod.getReturnType())) {
            throw new IllegalArgumentException(
                "Method annotated with "
                    + Deployment.class.getName()
                    +
                    " must have return type "
                    + Archive.class.getName()
                    + " or "
                    + Descriptor.class.getName()
                    + ". "
                    + deploymentMethod);
        }
        if (deploymentMethod.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Method annotated with "
                + Deployment.class.getName()
                + " can not accept parameters. "
                + deploymentMethod);
        }
    }

    /**
     * @param deploymentMethod
     * @return
     */
    private DeploymentConfiguration generateDeploymentContent(Method deploymentMethod) {

        Deployment deploymentAnnotation = deploymentMethod.getAnnotation(Deployment.class);
        DeploymentConfiguration.DeploymentContentBuilder deploymentContentBuilder = null;
        if (Archive.class.isAssignableFrom(deploymentMethod.getReturnType())) {
            deploymentContentBuilder = new DeploymentConfiguration.DeploymentContentBuilder(invoke(Archive.class, deploymentMethod));
        } else if (Descriptor.class.isAssignableFrom(deploymentMethod.getReturnType())) {
            deploymentContentBuilder = new DeploymentConfiguration.DeploymentContentBuilder(invoke(Descriptor.class, deploymentMethod));
        }

        if (deploymentMethod.isAnnotationPresent(OverProtocol.class)) {
            deploymentContentBuilder.withOverProtocol(deploymentMethod.getAnnotation(OverProtocol.class).value());
        }

        if (deploymentMethod.isAnnotationPresent(TargetsContainer.class)) {
            deploymentContentBuilder.withTargetsContainer(deploymentMethod.getAnnotation(TargetsContainer.class).value());
        }

        if (deploymentMethod.isAnnotationPresent(ShouldThrowException.class)) {
            final ShouldThrowException shouldThrowException = deploymentMethod.getAnnotation(ShouldThrowException.class);
            deploymentContentBuilder.withShouldThrowException(shouldThrowException.value(), shouldThrowException.testable());
        }

        deploymentContentBuilder = deploymentContentBuilder.withDeployment()
            .withManaged(deploymentAnnotation.managed())
            .withName(deploymentAnnotation.name())
            .withOrder(deploymentAnnotation.order())
            .withTestable(deploymentAnnotation.testable())
            .build();

        return deploymentContentBuilder.get();
    }


    /**
     * @param deploymentMethod
     * @return
     */
    private <T> T invoke(Class<T> type, Method deploymentMethod) {
        try {
            return type.cast(deploymentMethod.invoke(null));
        } catch (Exception e) {
            throw new RuntimeException("Could not invoke deployment method: " + deploymentMethod, e);
        }
    }

}
