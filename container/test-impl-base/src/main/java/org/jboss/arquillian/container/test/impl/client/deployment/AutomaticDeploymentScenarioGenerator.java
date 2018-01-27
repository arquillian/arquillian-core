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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.DeploymentContent;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.spi.client.deployment.AutomaticDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.container.test.spi.util.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;

/**
 * {@link DeploymentScenarioGenerator} that builds a {@link DeploymentScenario} based on
 * the {@link AutomaticDeployment} registered services.
 *
 * @version $Revision: $
 */
public class AutomaticDeploymentScenarioGenerator implements DeploymentScenarioGenerator {

    private static Logger log = Logger.getLogger(AutomaticDeploymentScenarioGenerator.class.getName());

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.deployment.DeploymentScenarioGenerator#generate(org.jboss.arquillian.spi.TestClass)
     */
    public List<DeploymentDescription> generate(TestClass testClass) {
        final List<DeploymentDescription> deployments = new ArrayList<DeploymentDescription>();

        Method[] deploymentMethods = testClass.getMethods(BeforeDeployment.class);

        final ServiceLoader<AutomaticDeployment> deploymentSpis = ServiceLoader.load(AutomaticDeployment.class);

        final Iterator<AutomaticDeployment> deploymentSpiIterator = deploymentSpis.iterator();
        while (deploymentSpiIterator.hasNext()) {
            final DeploymentContent deploymentContent =
                deploymentSpiIterator.next().generateDeploymentScenario(testClass);

            if (deploymentContent != null) {
                final DeploymentDescription deploymentDescription = generateDeployment(deploymentContent, deploymentMethods);
                deployments.add(deploymentDescription);
            }
        }

        sortByDeploymentOrder(deployments);

        return deployments;
    }

    /**
     * @param deploymentContent
     * @param deploymentMethods
     * @return
     */
    private DeploymentDescription generateDeployment(DeploymentContent deploymentContent,
        Method[] deploymentMethods) {
        TargetDescription target = generateTarget(deploymentContent);
        ProtocolDescription protocol = generateProtocol(deploymentContent);

        Deployment deploymentAnnotation = deploymentContent.getDeployment();
        DeploymentDescription deployment = null;
        Archive archive = deploymentContent.getArchive();
        if (archive != null) {

            for (Method beforeDeploymentMethod : deploymentMethods) {
                final BeforeDeployment beforeDeploymentMethodAnnotation = beforeDeploymentMethod.getAnnotation(BeforeDeployment.class);

                if (beforeDeploymentMethodAnnotation.name().equals(deploymentAnnotation.name())) {
                    validate(beforeDeploymentMethod);
                    archive = invoke(Archive.class, beforeDeploymentMethod, archive);
                    break;
                }
            }

            deployment = new DeploymentDescription(deploymentAnnotation.name(), archive);
            logWarningIfArchiveHasUnexpectedFileExtension(deployment);
            deployment.shouldBeTestable(deploymentAnnotation.testable());
        } else if (deploymentContent.getDescriptor() != null) {
            deployment =
                new DeploymentDescription(deploymentAnnotation.name(), deploymentContent.getDescriptor());
        }
        deployment.shouldBeManaged(deploymentAnnotation.managed());
        deployment.setOrder(deploymentAnnotation.order());
        if (target != null) {
            deployment.setTarget(target);
        }
        if (protocol != null) {
            deployment.setProtocol(protocol);
        }

        if (deploymentContent.getShouldThrowException() != null) {
            ShouldThrowException shouldThrowException = deploymentContent.getShouldThrowException();
            deployment.setExpectedException(shouldThrowException.value());
            deployment.shouldBeTestable(shouldThrowException.testable());
        }

        return deployment;
    }

    /**
     * @param deploymentMethod
     * @return
     */
    private <T> T invoke(Class<T> type, Method deploymentMethod, Archive currentArchive) {
        try {
            return type.cast(deploymentMethod.invoke(null, currentArchive));
        } catch (Exception e) {
            throw new RuntimeException("Could not invoke deployment method: " + deploymentMethod, e);
        }
    }

    private void validate(Method deploymentMethod) {
        if (!Modifier.isStatic(deploymentMethod.getModifiers())) {
            throw new IllegalArgumentException(
                "Method annotated with " + BeforeDeployment.class.getName() + " is not static. " + deploymentMethod);
        }
        if (!Archive.class.isAssignableFrom(deploymentMethod.getReturnType())) {
            throw new IllegalArgumentException(
                "Method annotated with "
                    + BeforeDeployment.class.getName()
                    +
                    " must have return type "
                    + Archive.class.getName()
                    + ". "
                    + deploymentMethod);
        }
        if (deploymentMethod.getParameterTypes().length != 1
            && Archive.class == deploymentMethod.getParameterTypes()[0]) {
            throw new IllegalArgumentException("Method annotated with "
                + BeforeDeployment.class.getName()
                + " only accept one parameter of type ." + Archive.class + " "
                + deploymentMethod);
        }
    }

    private void logWarningIfArchiveHasUnexpectedFileExtension(final DeploymentDescription deployment) {
        if (!Validate.archiveHasExpectedFileExtension(deployment.getArchive())) {
            log.warning("Deployment archive of type " + deployment.getArchive().getClass().getSimpleName()
                + " has been given an unexpected file extension. Archive name: " + deployment.getArchive().getName()
                + ", deployment name: " + deployment.getName() + ". It might not be wrong, but the container will"
                + " rely on the given file extension, the archive type is only a description of a certain structure.");
        }
    }

    /**
     * @param deploymentContent
     * @return
     */
    private TargetDescription generateTarget(DeploymentContent deploymentContent) {
        if (deploymentContent.getTargets() != null) {
            return new TargetDescription(deploymentContent.getTargets().value());
        }
        return TargetDescription.DEFAULT;
    }

    /**
     * @param deploymentContent
     * @return
     */
    private ProtocolDescription generateProtocol(DeploymentContent deploymentContent) {
        if (deploymentContent.getOverProtocol() != null) {
            return new ProtocolDescription(deploymentContent.getOverProtocol().value());
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

}
