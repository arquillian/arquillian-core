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
package org.jboss.arquillian.container.spi.client.protocol.metadata;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * JMXContext
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JMXContext extends NamedContext {
    private static final Logger log = Logger.getLogger(JMXContext.class.getName());

    private static Set<Reference<MBeanServerConnection>> jmxConnectorRefs =
        Collections.synchronizedSet(new HashSet<Reference<MBeanServerConnection>>());

    /*
     * The Host and Port of a Remote MBean Server
     */
    private String host;
    private int port;

    /*
     * A existing MBeanServerConnection provided by the Container
     */
    private MBeanServerConnection connection;

    public JMXContext(String host, int port) {
        this("no-named", host, port);
    }

    public JMXContext(String name, String host, int port) {
        super(name);
        this.host = host;
        this.port = port;
    }

    public JMXContext(MBeanServerConnection connection) {
        this("no-named", connection);
    }

    public JMXContext(String name, MBeanServerConnection connection) {
        super(name);
        this.connection = connection;
    }

    /**
     * @return the openConnection
     */
    public MBeanServerConnection getConnection() {
        if (connection == null) {
            try {
                JMXConnector connector = JMXConnectorFactory.connect(getRemoteJMXURL(), null);
                connection = connector.getMBeanServerConnection();
                WeakReference<MBeanServerConnection> ref = new JMXConnectorReference(connection, connector);
                jmxConnectorRefs.add(ref);
            } catch (IOException e) {
                throw new RuntimeException("Could not create remote JMX connection: " + this, e);
            }
        }
        return connection;
    }

    private JMXServiceURL getRemoteJMXURL() {
        try {
            return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        } catch (Exception e) {
            throw new RuntimeException("Could not create JMXServiceURL:" + this, e);
        }
    }

    @Override
    public String toString() {
        return "JMXContext [host=" + host + ", port=" + port + ", connection=" + connection + "]";
    }

    private static class JMXConnectorReference extends WeakReference<MBeanServerConnection> {
        private JMXConnector connector;

        public JMXConnectorReference(final MBeanServerConnection connection, JMXConnector connector) {
            super(connection);
            this.connector = connector;
        }

        @Override
        public boolean enqueue() {
            try {
                connector.close();
            } catch (Exception ignored) {
                log.warning("Could not close JMXConnector: " + connector);
            } finally {
                connector = null;
                jmxConnectorRefs.remove(this);
            }
            return super.enqueue();
        }
    }
}
