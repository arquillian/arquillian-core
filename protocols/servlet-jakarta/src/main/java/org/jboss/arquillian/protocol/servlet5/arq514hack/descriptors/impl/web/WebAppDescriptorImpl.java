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
package org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.impl.web;

import org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.web.ServletDef;
import org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.jboss.shrinkwrap.descriptor.spi.node.NodeDescriptorImplBase;

/**
 * @author Dan Allen
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WebAppDescriptorImpl extends NodeDescriptorImplBase implements WebAppDescriptor {
    // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    // -------------------------------------------------------------------------------------||
    // Instance Members -------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    private final Node model;

    // -------------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    public WebAppDescriptorImpl(String descriptorName) {
        this(descriptorName, new Node("web-app")
            .attribute("xmlns", "http://java.sun.com/xml/ns/javaee")
            .attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            .attribute("xsi:schemaLocation",
                "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"));
        version("3.0");
    }

    public WebAppDescriptorImpl(String descriptorName, Node model) {
        super(descriptorName);
        this.model = model;
    }

    // -------------------------------------------------------------------------------------||
    // API --------------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @Override
    public WebAppDescriptor version(final String version) {
        if (version == null || version.length() == 0) {
            throw new IllegalArgumentException("Version must be specified");
        }
        model.attribute("xsi:schemaLocation",
            "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_" + version.replace(".", "_")
                + ".xsd");
        model.attribute("version", version);
        return this;
    }

    @Override
    public WebAppDescriptor displayName(String displayName) {
        model.getOrCreate("display-name").text(displayName);
        return this;
    }

    @Override
    public ServletDef servlet(String clazz, String... urlPatterns) {
        return servlet(getSimpleName(clazz), clazz, urlPatterns);
    }

    @Override
    public ServletDef servlet(String name, String clazz, String[] urlPatterns) {
        Node servletNode = model.createChild("servlet");
        servletNode.createChild("servlet-name").text(name);
        servletNode.createChild("servlet-class").text(clazz);
        ServletDef servlet = new ServletDefImpl(getDescriptorName(), model, servletNode);

        servlet.mapping().urlPatterns(urlPatterns);
        return servlet;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.shrinkwrap.descriptor.spi.NodeProvider#getRootNode()
     */
    @Override
    public Node getRootNode() {
        return model;
    }

    // -------------------------------------------------------------------------------------||
    // Internal Helper Methods ------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /*
     * org.test.MyClass -> MyClass
     */
    private String getSimpleName(String fqcn) {
        if (fqcn.indexOf('.') >= 0) {
            return fqcn.substring(fqcn.lastIndexOf('.') + 1);
        }
        return fqcn;
    }
}
