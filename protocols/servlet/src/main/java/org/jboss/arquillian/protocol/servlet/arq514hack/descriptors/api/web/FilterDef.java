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
import java.util.Map;

/**
 * Filter
 * 
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @version $Revision: $
 */
public interface FilterDef extends WebAppDescriptor
{
   String getName();

   FilterDef name(String name);

   FilterDef asyncSupported(boolean value);

   boolean isAsyncSupported();

   String getFilterClass();

   FilterDef filterClass(String clazz);

   FilterDef initParam(String name, Object value);

   String getInitParam(String name);

   Map<String, String> getInitParams();

   FilterMappingDef mapping();

   List<FilterMappingDef> getMappings();

}