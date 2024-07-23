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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.DeploymentConfiguration;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.execution.ExecUtils;
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
    @Inject
    private Instance<ServiceLoader> serviceLoader;

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
            // This will throw IllegalArgumentException if check fails
            hasZeroOrOnlyArquillianResourceArgs(deploymentMethod);
    }

    private void hasZeroOrOnlyArquillianResourceArgs(Method deploymentMethod) throws IllegalArgumentException{
        boolean isOk = deploymentMethod.getParameterTypes().length == 0;
        if (!isOk) {
            ArrayList<String> badArgs = new ArrayList<>();
            for (Parameter param : deploymentMethod.getParameters()) {
                if (param.getAnnotation(ArquillianResource.class) == null) {
                    badArgs.add(param.getName());
                }
            }
            if (!badArgs.isEmpty()) {
                throw new IllegalArgumentException("Method annotated with "
                    + Deployment.class.getName()
                    + " can not accept parameters that are not annotated with "
                    + ArquillianResource.class.getName()
                    + ". "
                    + deploymentMethod
                    + " has invalid parameters: "
                    + badArgs);
            }
        }
    }

    /**
     * Call the deployment method and generate the deployment content and return a {@link DeploymentConfiguration}
     * populated with the content and any relevant deployment method annotation information
     * @param deploymentMethod - {@link Deployment} annotated method
     * @return configured {@link DeploymentConfiguration}
     */
    private DeploymentConfiguration generateDeploymentContent(Method deploymentMethod) {

        Deployment deploymentAnnotation = deploymentMethod.getAnnotation(Deployment.class);
        DeploymentConfiguration.DeploymentContentBuilder deploymentContentBuilder = null;
        if (Archive.class.isAssignableFrom(deploymentMethod.getReturnType())) {
            Archive<?> archive = invoke(Archive.class, deploymentMethod);
            deploymentContentBuilder = new DeploymentConfiguration.DeploymentContentBuilder(archive);
        } else if (Descriptor.class.isAssignableFrom(deploymentMethod.getReturnType())) {
            Descriptor descriptor = invoke(Descriptor.class, deploymentMethod);
            deploymentContentBuilder = new DeploymentConfiguration.DeploymentContentBuilder(descriptor);
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
     * Invoke the deployment method to generate the test archive or descriptor
     * @param type - the expected return type
     * @param deploymentMethod - class deployment method
     * @return the generated archive or descriptor
     */
    private <T> T invoke(Class<T> type, Method deploymentMethod) {
        try {
            Object[] args = null;
            if(deploymentMethod.getParameterCount() > 0) {
                Collection<TestEnricher> enrichers = serviceLoader.get().all(TestEnricher.class);
                args = ExecUtils.enrichArguments(deploymentMethod, enrichers);
            }
            return type.cast(deploymentMethod.invoke(null, args));
        } catch (Exception e) {
            throw new RuntimeException("Could not invoke deployment method: " + deploymentMethod, e);
        }
    }

}
