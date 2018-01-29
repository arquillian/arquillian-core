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
public class AutomaticDeploymentScenarioGenerator extends AbstractDeploymentScenarioGenerator implements DeploymentScenarioGenerator {

    AutomaticDeploymentLocator automaticDeploymentLocator;

    public AutomaticDeploymentScenarioGenerator() {
        automaticDeploymentLocator = new AutomaticDeploymentLocator() {
            public ServiceLoader<AutomaticDeployment> find() {
                return ServiceLoader.load(AutomaticDeployment.class);
            }
        };
    }

    protected List<DeploymentContent> generateDeploymentContent(TestClass testClass) {

        final List<DeploymentContent> deploymentContents = new ArrayList<DeploymentContent>();
        final ServiceLoader<AutomaticDeployment> deploymentSpis = automaticDeploymentLocator.find();
        final Iterator<AutomaticDeployment> deploymentSpiIterator = deploymentSpis.iterator();

        while (deploymentSpiIterator.hasNext()) {
            final DeploymentContent deploymentContent =
                deploymentSpiIterator.next().generateDeploymentScenario(testClass);

            if (deploymentContent != null) {
                deploymentContents.add(deploymentContent);
            }
        }

        return deploymentContents;
    }

    protected Archive manipulateArchive(TestClass testClass, String deploymentName, Archive archive) {

        final Method[] beforeDeploymentMethods = testClass.getMethods(BeforeDeployment.class);

        for (Method beforeDeploymentMethod : beforeDeploymentMethods) {
            final BeforeDeployment beforeDeploymentMethodAnnotation = beforeDeploymentMethod.getAnnotation(BeforeDeployment.class);

            if (beforeDeploymentMethodAnnotation.name().equals(deploymentName)) {
                validate(beforeDeploymentMethod);
                archive = invoke(Archive.class, beforeDeploymentMethod, archive);
                break;
            }
        }

        return archive;
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

    interface AutomaticDeploymentLocator {
        ServiceLoader<AutomaticDeployment> find();
    }

}
