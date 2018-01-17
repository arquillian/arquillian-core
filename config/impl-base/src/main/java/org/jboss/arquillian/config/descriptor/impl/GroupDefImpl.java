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

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.GroupDef;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * GroupDefImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class GroupDefImpl extends ArquillianDescriptorImpl implements GroupDef {
    private Node group;

    public GroupDefImpl(String descriptorName, Node model, Node group) {
        super(descriptorName, model);
        this.group = group;
    }

    //-------------------------------------------------------------------------------------||
    // Required Implementations - ProtocolDescriptor --------------------------------------||
    //-------------------------------------------------------------------------------------||

    /* (non-Javadoc)
     * @see org.jboss.arquillian.impl.configuration.api.GroupDef#setName(java.lang.String)
     */
    @Override
    public GroupDef setGroupName(String name) {
        group.attribute("qualifier", name);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.impl.configuration.api.GroupDef#getGroupName()
     */
    @Override
    public String getGroupName() {
        return group.getAttribute("qualifier");
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.config.descriptor.api.GroupDef#setGroupDefault()
     */
    @Override
    public GroupDef setGroupDefault() {
        group.attribute("default", true);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.config.descriptor.api.GroupDef#isGroupDefault()
     */
    @Override
    public boolean isGroupDefault() {
        return Boolean.parseBoolean(group.getAttribute("default"));
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.impl.configuration.ArquillianDescriptorImpl#container(java.lang.String)
     */
    @Override
    public ContainerDef container(String name) {
        return new GroupContainerDefImpl(getDescriptorName(), getRootNode(), group,
            group.getOrCreate("container@qualifier=" + name));
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.impl.configuration.api.GroupDef#getGroupContainers()
     */
    @Override
    public List<ContainerDef> getGroupContainers() {
        List<ContainerDef> containers = new ArrayList<ContainerDef>();
        for (Node container : group.get("container")) {
            containers.add(new GroupContainerDefImpl(getDescriptorName(), getRootNode(), group, container));
        }
        return containers;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return group.toString(true);
    }
}
