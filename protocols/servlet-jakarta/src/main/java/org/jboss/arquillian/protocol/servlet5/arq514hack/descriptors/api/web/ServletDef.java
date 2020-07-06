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
package org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.web;

import java.util.Map;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface ServletDef extends WebAppDescriptor {
    ServletDef name(String name);

    ServletDef servletClass(Class<?> clazz);

    ServletDef servletClass(String clazz);

    ServletDef asyncSupported(boolean value);

    ServletDef initParam(String name, Object value);

    ServletDef loadOnStartup(int order);

    ServletMappingDef mapping();

    String getName();

    String getServletClass();

    String getInitParam(String name);

    Map<String, String> getInitParams();

    boolean isAsyncSupported();

    int getLoadOnStartup();
}
