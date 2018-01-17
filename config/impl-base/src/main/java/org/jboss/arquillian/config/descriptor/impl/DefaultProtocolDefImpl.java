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
package org.jboss.arquillian.config.descriptor.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.DefaultProtocolDef;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * DefaultProtocolDefImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DefaultProtocolDefImpl extends ArquillianDescriptorImpl implements DefaultProtocolDef {
    private Node protocol;

    public DefaultProtocolDefImpl(String descriptorName, Node model, Node protocol) {
        super(descriptorName, model);
        this.protocol = protocol;
    }

    //-------------------------------------------------------------------------------------||
    // Required Implementations - ProtocolDescription -------------------------------------||
    //-------------------------------------------------------------------------------------||

    /* (non-Javadoc)
     * @see org.jboss.arquillian.impl.configuration.api.DefaultProtocolDef#setType(java.lang.String)
     */
    @Override
    public DefaultProtocolDef setType(String type) {
        protocol.attribute("type", type);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.impl.configuration.api.DefaultProtocolDef#getType()
     */
    @Override
    public String getType() {
        return protocol.getAttribute("type");
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.impl.configuration.api.ProtocolDescription#property(java.lang.String, java.lang.String)
     */
    @Override
    public DefaultProtocolDef property(String name, String value) {
        protocol.getOrCreate("property@name=" + name).text(value);
        return this;
    }

    public Map<String, String> getProperties() {
        List<Node> props = protocol.get("property");
        Map<String, String> properties = new HashMap<String, String>();

        for (Node prop : props) {
            properties.put(prop.getAttribute("name"), prop.getText());
        }
        return properties;
    }

    @Override
    public String getProperty(String name) {
        final Node value = protocol.getSingle("property@name=" + name);
        return value != null ? value.getText() : null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return protocol.toString(true);
    }
}
