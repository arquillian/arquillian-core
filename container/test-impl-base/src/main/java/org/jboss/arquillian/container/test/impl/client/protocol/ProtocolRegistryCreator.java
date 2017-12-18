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

import java.util.Collection;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.DefaultProtocolDef;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.container.test.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * Responsible for creating and filling the ProtocolRegistry.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ProtocolRegistryCreator {
    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    @ApplicationScoped
    private InstanceProducer<ProtocolRegistry> registryInstance;

    public void createRegistry(@Observes ArquillianDescriptor event) throws Exception {
        @SuppressWarnings("rawtypes")
        Collection<Protocol> protocols = serviceLoader.get().all(Protocol.class);

        Protocol<?> defaultProtocol = null;
        DefaultProtocolDef defaultProtcolDef = event.getDefaultProtocol();
        if (defaultProtcolDef != null) {
            defaultProtocol = findMatch(new ProtocolDescription(defaultProtcolDef.getType()), protocols);
            if (defaultProtocol == null) {
                // TODO: add printout of found protocols
                throw new IllegalStateException(
                    "Defined default protocol " + defaultProtcolDef.getType() + " can not be found on classpath");
            }
        }
        ProtocolRegistry registry = new ProtocolRegistry();
        for (Protocol<?> protocol : protocols) {
            if (defaultProtocol != null && protocol.equals(defaultProtocol)) {
                registry.addProtocol(new ProtocolDefinition(protocol, defaultProtcolDef.getProperties(), true));
            } else {
                registry.addProtocol(new ProtocolDefinition(protocol));
            }
        }
        registryInstance.set(registry);
    }

    private Protocol<?> findMatch(ProtocolDescription description,
        @SuppressWarnings("rawtypes") Collection<Protocol> protocols) {
        for (Protocol<?> protocol : protocols) {
            if (description.equals(protocol.getDescription())) {
                return protocol;
            }
        }
        return null;
    }
}
