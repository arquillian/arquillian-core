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
package org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.impl.application;

import org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.application.ApplicationDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.jboss.shrinkwrap.descriptor.spi.node.NodeDescriptorImplBase;

/**
 * ApplicationDescriptorImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ApplicationDescriptorImpl extends NodeDescriptorImplBase implements ApplicationDescriptor {
    // -------------------------------------------------------------------------------------||
    // Instance Members -------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    private final Node model;

    // -------------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    public ApplicationDescriptorImpl(String descriptorName) {
        this(descriptorName, new Node("application")
            .attribute("xmlns", "http://java.sun.com/xml/ns/javaee")
            .attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));

        version("6");
    }

    public ApplicationDescriptorImpl(String descriptorName, Node model) {
        super(descriptorName);
        this.model = model;
    }

    // -------------------------------------------------------------------------------------||
    // API --------------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#description()
     */
    @Override
    public ApplicationDescriptor description(String description) {
        model.getOrCreate("description").text(description);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#displayName(java.lang.String)
     */
    @Override
    public ApplicationDescriptor displayName(String displayName) {
        model.getOrCreate("display-name").text(displayName);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#libraryDirectory(java.lang.String)
     */
    @Override
    public ApplicationDescriptor libraryDirectory(String libraryDirectory) {
        model.getOrCreate("library-directory").text(libraryDirectory);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#ejbModule(java.lang.String)
     */
    @Override
    public ApplicationDescriptor ejbModule(String uri) {
        model.createChild("module").createChild("ejb").text(uri);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#javaModule(java.lang.String)
     */
    @Override
    public ApplicationDescriptor javaModule(String uri) {
        model.createChild("module").createChild("java").text(uri);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#connectorModule(java.lang.String)
     */
    @Override
    public ApplicationDescriptor connectorModule(String uri) {
        model.createChild("module").createChild("connector").text(uri);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#webModule(java.lang.String,
     * java.lang.String)
     */
    @Override
    public ApplicationDescriptor webModule(String uri, String contextRoot) {
        Node web = model.createChild("module").createChild("web");
        web.createChild("web-uri").text(uri);
        web.createChild("context-root").text(contextRoot);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#version(java.lang.String)
     */
    @Override
    public ApplicationDescriptor version(String version) {
        model.attribute("version", version);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#securityRole(java.lang.String)
     */
    @Override
    public ApplicationDescriptor securityRole(String roleName) {
        return securityRole(roleName, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.api.spec.ee.application.ApplicationDescriptor#securityRole(java.lang.String,
     * java.lang.String)
     */
    @Override
    public ApplicationDescriptor securityRole(String roleName, String description) {
        Node security = model.createChild("security-role");
        if (roleName != null) {
            security.createChild("role-name").text(roleName);
        }
        if (description != null) {
            security.createChild("description").text(description);
        }
        return this;
    }

    // -------------------------------------------------------------------------------------||
    // Required Implementations - NodeProviderImplBase ------------------------------------||
    // -------------------------------------------------------------------------------------||

    @Override
    public Node getRootNode() {
        return model;
    }
}
