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

import java.util.List;
import org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.web.ServletMappingDef;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ServletMappingDefImpl extends ServletDefImpl implements ServletMappingDef {
    private final Node mapping;

    public ServletMappingDefImpl(String descriptorName, Node webApp, Node servletNode, Node mappingNode) {
        super(descriptorName, webApp, servletNode);
        this.mapping = mappingNode;
    }

    @Override
    public String getServletName() {
        return mapping.getTextValueForPatternName("servlet-name");
    }

    @Override
    public ServletMappingDef servletName(String servletName) {
        mapping.getOrCreate("servlet-name").text(servletName);
        return this;
    }

    @Override
    public List<String> getUrlPatterns() {
        return mapping.getTextValuesForPatternName("url-pattern");
    }

    @Override
    public ServletMappingDef urlPattern(String urlPattern) {
        mapping.createChild("url-pattern").text(urlPattern);
        return this;
    }

    @Override
    public ServletMappingDef urlPatterns(String... urlPatterns) {
        for (String string : urlPatterns) {
            urlPattern(string);
        }
        return this;
    }
}
