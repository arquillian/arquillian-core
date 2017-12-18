/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.config.descriptor.impl.ContainerDefImpl;
import org.jboss.arquillian.container.impl.ContainerImpl;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentTargetDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.impl.enricher.resource.ArquillianResourceTestEnricher;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * OperatesOnDeploymentAwareProviderTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class OperatesOnDeploymentAwareProviderBase extends AbstractContainerTestTestBase {
    @Inject
    private Instance<Injector> injector;

    @Mock
    private ServiceLoader serviceLoader;

    @SuppressWarnings("rawtypes")
    @Mock
    private DeployableContainer deployableContainer;

    @Mock
    private DeploymentScenario scenario;

    @Mock
    private ContainerRegistry registry;

    private ResourceProvider resourceProvider;

    protected abstract ResourceProvider getResourceProvider();

    @Before
    public void addServiceLoader() throws Exception {
        resourceProvider = getResourceProvider();
        injector.get().inject(resourceProvider);

        List<ResourceProvider> resourceProviders = Arrays.asList(resourceProvider);
        Mockito.when(serviceLoader.all(ResourceProvider.class)).thenReturn(resourceProviders);

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
    }

    protected <X, T> X execute(Class<X> enrichType, Class<T> contextualType, T type) throws Exception {
        bind(ApplicationScoped.class, contextualType, type);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        X test = enrichType.cast(enrichType.newInstance());
        enricher.enrich(test);
        return test;
    }

    protected <X, T> X execute(Class<X> enrichType, Class<T> contextualType, T outerType, T innerType) throws Exception {
        return execute(true, true, enrichType, contextualType, outerType, innerType);
    }

    protected <X, T> X execute(boolean reigsterRegistry, boolean registerScenario, Class<X> enrichType,
        Class<T> contextualType, T outerType, T innerType) throws Exception {
        if (reigsterRegistry) {
            bind(ApplicationScoped.class, ContainerRegistry.class, registry);
        }
        if (registerScenario) {
            bind(ApplicationScoped.class, DeploymentScenario.class, scenario);
        }

        Mockito.when(registry.getContainer(
            new TargetDescription("X")))
            .thenReturn(new ContainerImpl("X", deployableContainer, new ContainerDefImpl("X")));
        Mockito.when(registry.getContainer(
            new TargetDescription("Z")))
            .thenReturn(new ContainerImpl("Z", deployableContainer, new ContainerDefImpl("Z")));

        Deployment deploymentZ = new Deployment(new DeploymentDescription("Z", ShrinkWrap.create(JavaArchive.class))
            .setTarget(new TargetDescription("Z")));
        Deployment deploymentX = new Deployment(new DeploymentDescription("X", ShrinkWrap.create(JavaArchive.class))
            .setTarget(new TargetDescription("X")));

        Mockito.when(scenario.deployment(new DeploymentTargetDescription("Z"))).thenReturn(deploymentZ);
        Mockito.when(scenario.deployment(new DeploymentTargetDescription("X"))).thenReturn(deploymentX);

        ContainerContext containerContext = getManager().getContext(ContainerContext.class);
        DeploymentContext deploymentContext = getManager().getContext(DeploymentContext.class);
        try {
            deploymentContext.activate(deploymentX);
            deploymentContext.getObjectStore().add(contextualType, innerType);
            deploymentContext.deactivate();

         /*
          *  deploymentZ is left active and should be handled as 'current'.
          *  The test is to see if the Enricher with Qualifier can activate/deactive and read from X
          */
            deploymentContext.activate(deploymentZ);
            deploymentContext.getObjectStore().add(contextualType, outerType);

            containerContext.activate("TEST");

            TestEnricher enricher = new ArquillianResourceTestEnricher();
            injector.get().inject(enricher);

            X test = enrichType.cast(enrichType.newInstance());
            enricher.enrich(test);

            return test;
        } catch (RuntimeException e) {
            throw (Exception) e.getCause();
        } finally {
            if (deploymentContext.isActive()) {
                Deployment activeContext = deploymentContext.getActiveId();
                if (!"Z".equals(activeContext.getDescription().getName())) {
                    Assert.fail(
                        "Wrong deployment context active, potential leak in Enricher. Active context was " + activeContext
                            .getDescription()
                            .getName());
                }
            }
            if (containerContext.isActive()) {
                String activeContext = containerContext.getActiveId();
                if (!"TEST".equalsIgnoreCase(activeContext)) {
                    Assert.fail("Wrong container context active, potential leak in Enricher. Active context was "
                        + activeContext);
                }
            }
        }
    }
}