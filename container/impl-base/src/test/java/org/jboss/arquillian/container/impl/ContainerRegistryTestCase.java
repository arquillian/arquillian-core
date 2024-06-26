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
package org.jboss.arquillian.container.impl;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.impl.ContainerDefImpl;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.ConfigurationMapper;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * DomainModelTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerRegistryTestCase extends AbstractContainerTestBase {
    private static final String ARQUILLIAN_XML = "arquillian.xml";

    @Inject
    private Instance<Injector> injector;

    @Mock
    private ServiceLoader serviceLoader;

    @Mock
    private DeployableContainer<DummyContainerConfiguration> deployableContainer;

    @Before
    public void setup() throws Exception {
        Mockito.when(serviceLoader.onlyOne(Mockito.same(DeployableContainer.class))).thenReturn(deployableContainer);
        Mockito.when(deployableContainer.getConfigurationClass()).thenReturn(DummyContainerConfiguration.class);
    }

    @Test
    public void shouldBeAbleToDefaultTargetToOnlyRegisteredContainer() throws Exception {
        String name = "some-name";

        ContainerRegistry registry = new LocalContainerRegistry(injector.get());
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name), serviceLoader);

        Container container = registry.getContainer(TargetDescription.DEFAULT);

        Assert.assertEquals(
            "Verify that the only registered container is returned as default",
            name, container.getName());
    }

    @Test
    public void shouldBeAbleToDefaultTargetToDefaultRegisteredContainer() throws Exception {
        String name = "some-name";

        ContainerRegistry registry = new LocalContainerRegistry(injector.get());
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName("some-other-name"), serviceLoader);
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name).setDefault(), serviceLoader);

        Container container = registry.getContainer(TargetDescription.DEFAULT);

        Assert.assertEquals(
            "Verify that the default registered container is returned as default",
            name, container.getName());
    }

    @Test
    public void shouldBeAbleToCreateContainerConfiguration() throws Exception {
        String name = "some-name";
        String prop = "prop-value";

        ContainerRegistry registry = new LocalContainerRegistry(injector.get());
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name)
            .property("property", prop), serviceLoader);

        Container container = registry.getContainer(new TargetDescription(name));

        Assert.assertEquals(
            "Verify that the only registered container is returned as default",
            name, container.getName());

        Assert.assertEquals(
            "Verify that the configuration was populated",
            prop,
            ((DummyContainerConfiguration) container.createDeployableConfiguration()).getProperty());
    }

    @Test
    public void shouldBeAbleToCreatePrivateContainerConfiguration() throws Exception {
        // Override default configured class
        ServiceLoader serviceLoader = Mockito.mock(ServiceLoader.class);
        DeployableContainer<PrivateDummyContainerConfiguration> deployableContainer =
            Mockito.mock(DeployableContainer.class);

        Mockito.when(serviceLoader.onlyOne(Mockito.same(DeployableContainer.class))).thenReturn(deployableContainer);
        Mockito.when(deployableContainer.getConfigurationClass()).thenReturn(PrivateDummyContainerConfiguration.class);

        String name = "some-name";
        String prop = "prop-value";

        ContainerRegistry registry = new LocalContainerRegistry(injector.get());
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name)
            .property("property", prop), serviceLoader);

        Container container = registry.getContainer(new TargetDescription(name));

        Assert.assertEquals(
            "Verify that the only registered container is returned as default",
            name, container.getName());

        Assert.assertEquals(
            "Verify that the configuration was populated",
            prop,
            ((PrivateDummyContainerConfiguration) container.createDeployableConfiguration()).getProperty());
    }

    @Test
    public void shouldBeAbleToCreateContainerConfigurationCustomMapper() throws Exception {
        ServiceLoader serviceLoader = Mockito.mock(ServiceLoader.class);
        DeployableContainer<CustomContainerConfiguration> deployableContainer =
            Mockito.mock(DeployableContainer.class);

        Mockito.when(serviceLoader.onlyOne(Mockito.same(DeployableContainer.class))).thenReturn(deployableContainer);
        Mockito.when(deployableContainer.getConfigurationClass()).thenReturn(CustomContainerConfiguration.class);
        Mockito.when(deployableContainer.getConfigurationMapper()).thenReturn(new CustomMapper());

        String name = "custom-container";
        String prop = "prop-value";
        String[] hosts = {"host1", "host2", "host3"};

        ContainerRegistry registry = new LocalContainerRegistry(injector.get());
        ContainerDefImpl containerDef = new ContainerDefImpl(ARQUILLIAN_XML);
        containerDef.setContainerName(name);
        containerDef.property("property", prop);
        containerDef.property("hosts", "host1,host2,host3");

        registry.create(containerDef, serviceLoader);

        Container<CustomContainerConfiguration> container = registry.getContainer(new TargetDescription(name));

        Assert.assertEquals(
            "Verify that the only registered container is returned as default",
            name, container.getName());

        CustomContainerConfiguration config = container.createDeployableConfiguration();
        Assert.assertEquals(
            "Verify that the custom configuration 'property' was populated",
            prop,
            config.getProperty());

        Assert.assertArrayEquals(
            "Verify that the custom configuration 'hosts' was populated",
            hosts,
            config.getHosts());
    }

    @Test
    public void shouldBeAbleToSpecifyTarget() throws Exception {
        String name = "some-name";

        ContainerRegistry registry = new LocalContainerRegistry(injector.get());
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName("other-name"), serviceLoader);
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name), serviceLoader);

        Container container = registry.getContainer(new TargetDescription(name));

        Assert.assertEquals(
            "Verify that the specific registered container is returned",
            name, container.getName());
    }

    @Test
    public void shouldBeAbleToGetContainerByName() throws Exception {
        String name = "some-name";

        ContainerRegistry registry = new LocalContainerRegistry(injector.get());
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName("other-name"), serviceLoader);
        registry.create(new ContainerDefImpl(ARQUILLIAN_XML).setContainerName(name), serviceLoader);

        Container container = registry.getContainer(name);

        Assert.assertEquals(
            "Verify that the specific registered container is returned",
            name, container.getName());
    }

    public static class DummyContainerConfiguration implements ContainerConfiguration {
        private String property;

        /**
         * @return the property
         */
        public String getProperty() {
            return property;
        }

        /**
         * @param property
         *     the property to set
         */
        public void setProperty(String property) {
            this.property = property;
        }

        @Override
        public void validate() throws ConfigurationException {
        }
    }

    private static class PrivateDummyContainerConfiguration extends DummyContainerConfiguration {
        private PrivateDummyContainerConfiguration() {
        }
    }
    private static class CustomContainerConfiguration extends DummyContainerConfiguration {
        private String[] hosts;
        public String[] getHosts() {
            return hosts;
        }
        public void setHosts(String[] hosts) {
            this.hosts = hosts;
        }
    }
    private static class CustomMapper implements ConfigurationMapper<CustomContainerConfiguration> {
        @Override
        public void populateConfiguration(CustomContainerConfiguration containerConfiguration, ContainerDef definition) {
            String property = definition.getContainerProperty("property");
            containerConfiguration.setProperty(property);
            String hostsString = definition.getContainerProperty("hosts");
            String[] hosts = hostsString.split(",");
            containerConfiguration.setHosts(hosts);
        }
    }
}
