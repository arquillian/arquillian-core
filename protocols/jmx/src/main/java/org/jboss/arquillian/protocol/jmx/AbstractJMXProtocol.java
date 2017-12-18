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
package org.jboss.arquillian.protocol.jmx;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.JMXContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;

/**
 * JMXProtocol
 *
 * @author thomas.diesler@jboss.com
 * @since 21-Apr-2011
 */
public abstract class AbstractJMXProtocol<T extends JMXProtocolConfiguration> implements Protocol<T> {

    public abstract String getProtocolName();

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getProtocolConfigurationClass() {
        return (Class<T>) JMXProtocolConfiguration.class;
    }

    @Override
    public ProtocolDescription getDescription() {
        return new ProtocolDescription(getProtocolName());
    }

    @Override
    public ContainerMethodExecutor getExecutor(T config, ProtocolMetaData metaData, CommandCallback callback) {
        if (metaData.hasContext(JMXContext.class)) {
            MBeanServerConnection mbeanServer = metaData.getContext(JMXContext.class).getConnection();

            Map<String, String> protocolProps = new HashMap<String, String>();
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(config.getClass());
                for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                    String key = propertyDescriptor.getName();
                    Object value = propertyDescriptor.getReadMethod().invoke(config);
                    if (value != null) {
                        protocolProps.put(key, "" + value);
                    }
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot obtain protocol config");
            }
            return new JMXMethodExecutor(mbeanServer, callback, JMXTestRunnerMBean.OBJECT_NAME, protocolProps);
        } else {
            throw new IllegalStateException(
                "No " + JMXContext.class.getName() + " was found in " + ProtocolMetaData.class.getName() +
                    ". The JMX Protocol can not be used without a connection, " +
                    "please verify your protocol configuration or contact the DeployableContainer developer");
        }
    }
}
