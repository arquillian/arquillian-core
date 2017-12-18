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
package org.jboss.arquillian.container.test.impl.domain;

import java.util.HashMap;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.client.protocol.ProtocolConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * DomainModelTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolRegistryTestCase {
    private String name = "some-name";

    @Mock
    private Protocol<DummyProtocolConfiguration> protocol;

    @Before
    public void setup() throws Exception {
        Mockito.when(protocol.getDescription()).thenReturn(new ProtocolDescription(name));
        Mockito.when(protocol.getProtocolConfigurationClass()).thenReturn(DummyProtocolConfiguration.class);
    }

    @Test
    public void shouldBeAbleToDefaultProtocolIfOnlyOneFound() throws Exception {
        ProtocolRegistry registry = createRegistry()
            .addProtocol(new ProtocolDefinition(protocol, new HashMap<String, String>()));

        Assert.assertEquals(protocol, registry.getProtocol(ProtocolDescription.DEFAULT).getProtocol());
    }

    @Test
    public void shouldBeAbleToDefaultToProtocolMakedAsDefault() throws Exception {
        Protocol<?> localProtocol = Mockito.mock(Protocol.class);
        Mockito.when(localProtocol.getDescription()).thenReturn(new ProtocolDescription("local"));
        Protocol<?> otherProtocol = Mockito.mock(Protocol.class);
        Mockito.when(otherProtocol.getDescription()).thenReturn(new ProtocolDescription("other"));

        ProtocolRegistry registry = createRegistry()
            .addProtocol(new ProtocolDefinition(protocol, new HashMap<String, String>(), true))
            .addProtocol(new ProtocolDefinition(localProtocol, new HashMap<String, String>()))
            .addProtocol(new ProtocolDefinition(otherProtocol, new HashMap<String, String>()));

        Assert.assertEquals(protocol, registry.getProtocol(ProtocolDescription.DEFAULT).getProtocol());
    }

    @Test
    public void shouldReturnNullTryingToDefaultWithMultipleNonDefinedDefaultProtocols() throws Exception {
        Protocol<?> localProtocol = Mockito.mock(Protocol.class);
        Mockito.when(localProtocol.getDescription()).thenReturn(new ProtocolDescription("local"));
        Protocol<?> otherProtocol = Mockito.mock(Protocol.class);
        Mockito.when(otherProtocol.getDescription()).thenReturn(new ProtocolDescription("other"));

        ProtocolRegistry registry = createRegistry()
            .addProtocol(new ProtocolDefinition(protocol, new HashMap<String, String>()))
            .addProtocol(new ProtocolDefinition(localProtocol, new HashMap<String, String>()))
            .addProtocol(new ProtocolDefinition(otherProtocol, new HashMap<String, String>()));

        Assert.assertNull(registry.getProtocol(ProtocolDescription.DEFAULT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMultipleProtocolsWithTheSameDescription() {
        createRegistry()
            .addProtocol(new ProtocolDefinition(protocol, new HashMap<String, String>()))
            .addProtocol(new ProtocolDefinition(protocol, new HashMap<String, String>()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullProtocolRegistration() throws Exception {
        createRegistry().addProtocol(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullProtocolLookup() throws Exception {
        createRegistry().getProtocol(null);
    }

    protected ProtocolRegistry createRegistry() {
        return new ProtocolRegistry();
    }

    @Test
    public void shouldBeAbleToMatchAndConfigureProtocol() throws Exception {
    }

    public static class DummyProtocolConfiguration implements ProtocolConfiguration {
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
    }
}
