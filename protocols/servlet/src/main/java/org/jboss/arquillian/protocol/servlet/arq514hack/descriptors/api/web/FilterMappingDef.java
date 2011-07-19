/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web;

import java.util.List;
import java.util.Set;

import javax.servlet.DispatcherType;

/**
 * FilterMapping
 * 
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface FilterMappingDef extends FilterDef
{
   String getFilterName();

   FilterMappingDef filterName(String filterName);

   List<String> getUrlPatterns();

   FilterMappingDef urlPattern(String urlPattern);

   FilterMappingDef urlPatterns(String... urlPatterns);

   FilterMappingDef dispatchType(DispatcherType type);

   FilterMappingDef dispatchTypes(DispatcherType... types);

   Set<DispatcherType> getDispatchTypes();

   List<String> getServletNames();

   FilterMappingDef servletName(String name);

   FilterMappingDef servletNames(String... names);
}