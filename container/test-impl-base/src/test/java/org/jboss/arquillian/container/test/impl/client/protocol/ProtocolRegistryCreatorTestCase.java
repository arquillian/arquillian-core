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
package org.jboss.arquillian.container.test.impl.client.protocol;

import java.util.Collections;
import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.container.test.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * ProtocolRegistryCreatorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolRegistryCreatorTestCase extends AbstractContainerTestTestBase {
    @Mock
    private ServiceLoader serviceLoader;

    @Mock
    private Protocol<?> protocol;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ProtocolRegistryCreator.class);
    }

    @Before
    public void setup() throws Exception {
        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
    }

    @Test
    public void shouldBindProtocolRegistryToContext() throws Exception {
        fire(createDescriptor());

        assertEventFiredInContext(ProtocolRegistry.class, ApplicationContext.class);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void shouldSetDefaultDefinedProtocolAsDefault() {
        String protocolName = "default-protocol";

        when(protocol.getDescription()).thenReturn(new ProtocolDescription(protocolName));
        when(serviceLoader.all(Protocol.class)).thenReturn(Collections.singletonList((Protocol) protocol));

        fire(createDescriptor(protocolName));

        ProtocolDefinition protocol = verifyRegistryProtocol(protocolName);
        Assert.assertTrue(protocol.isDefaultProtocol());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void shouldBindFoundProtocolsToRegistry() throws Exception {
        String protocolName = "protocol";
        when(protocol.getDescription()).thenReturn(new ProtocolDescription(protocolName));
        when(serviceLoader.all(Protocol.class)).thenReturn(Collections.singletonList((Protocol) protocol));

        fire(createDescriptor());
        ProtocolDefinition protocol = verifyRegistryProtocol(protocolName);
        Assert.assertFalse(protocol.isDefaultProtocol());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfDefaultDefinedProtocolNotFound() throws Exception {
        try {
            fire(createDescriptor("DOES_NOT_EXIST"));
        } catch (Exception e) {
            Assert.assertTrue("Verify thrown exception", e.getMessage().contains("Defined default protocol"));
            throw e;
        }
    }

    private ProtocolDefinition verifyRegistryProtocol(String protocolName) {
        ProtocolRegistry registry = getManager().resolve(ProtocolRegistry.class);
        ProtocolDefinition registeredProtocol = registry.getProtocol(new ProtocolDescription(protocolName));
        Assert.assertNotNull(
            "Verify " + Protocol.class.getSimpleName() + " was registered",
            registeredProtocol);

        Assert.assertEquals(
            "Verify same protocol instance was registered",
            protocol, registeredProtocol.getProtocol());

        return registeredProtocol;
    }

    private ArquillianDescriptor createDescriptor() {
        return createDescriptor(null);
    }

    private ArquillianDescriptor createDescriptor(String defaultProtocol) {
        ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class);
        if (defaultProtocol != null) {
            desc.defaultProtocol(defaultProtocol);
        }
        return desc;
    }
}
