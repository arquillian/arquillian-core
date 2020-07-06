/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.web.ServletDef;
import org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.web.ServletMappingDef;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ServletDefImpl extends WebAppDescriptorImpl implements ServletDef {
    private final Node servlet;

    public ServletDefImpl(String descriptorName, Node webApp, Node servlet) {
        super(descriptorName, webApp);
        this.servlet = servlet;
    }

    @Override
    public ServletDef name(String name) {
        servlet.getOrCreate("servlet-name").text(name);
        return this;
    }

    @Override
    public ServletDef asyncSupported(boolean value) {
        servlet.getOrCreate("async-supported").text(value);
        return this;
    }

    @Override
    public ServletDef initParam(String name, Object value) {
        InitParamDefImpl param = new InitParamDefImpl(getDescriptorName(), getRootNode(), servlet);
        param.initParam(name, value == null ? null : value.toString());
        return this;
    }

    @Override
    public ServletDef loadOnStartup(int order) {
        servlet.getOrCreate("load-on-startup").text(order);
        return this;
    }

    @Override
    public ServletMappingDef mapping() {
        Node mappingNode = getRootNode().createChild("servlet-mapping");
        ServletMappingDef mapping = new ServletMappingDefImpl(getDescriptorName(), getRootNode(), servlet, mappingNode);
        mapping.servletName(getName());
        return mapping;
    }

    @Override
    public ServletDef servletClass(Class<?> clazz) {
        return servletClass(clazz.getName());
    }

    @Override
    public ServletDef servletClass(String clazz) {
        servlet.getOrCreate("servlet-class").text(clazz);
        return this;
    }

    @Override
    public String getServletClass() {
        if (servlet.getSingle("servlet-class") != null) {
            return servlet.getSingle("servlet-class").getText();
        }
        return null;
    }

    @Override
    public String getName() {
        return servlet.getTextValueForPatternName("servlet-name");
    }

    @Override
    public String getInitParam(String name) {
        Map<String, String> params = getInitParams();
        for (Entry<String, String> e : params.entrySet()) {
            if (e.getKey() != null && e.getKey().equals(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getInitParams() {
        Map<String, String> result = new HashMap<String, String>();
        List<Node> params = servlet.get("init-param");
        for (Node node : params) {
            result.put(node.getTextValueForPatternName("param-name"), node.getTextValueForPatternName("param-value"));
        }
        return result;
    }

    @Override
    public boolean isAsyncSupported() {
        return Strings.isTrue(servlet.getTextValueForPatternName("async-supported"));
    }

    @Override
    public int getLoadOnStartup() throws NumberFormatException {
        String tex = servlet.getTextValueForPatternName("load-on-startup");
        return tex == null ? null : Integer.valueOf(tex);
    }

    public Node getNode() {
        return servlet;
    }
}
