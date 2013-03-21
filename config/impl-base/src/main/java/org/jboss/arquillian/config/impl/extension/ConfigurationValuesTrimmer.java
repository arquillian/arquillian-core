/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.config.impl.extension;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 *
 */
public class ConfigurationValuesTrimmer
{

   private static final String NEW_LINES = "(?m)\r?\n|(?m)\r*";
   private static final String LEADING_AND_TRAILING_WHITESPACES = "(?m)^[ \t]+|(?m)[\t ]+$";

   public static ArquillianDescriptor trim(ArquillianDescriptor descriptor)
   {
      final String exportedDescriptor = descriptor.exportAsString().replaceAll(LEADING_AND_TRAILING_WHITESPACES, " ").replaceAll(NEW_LINES, "");
      return Descriptors.importAs(ArquillianDescriptor.class).fromString(exportedDescriptor);
   }

}
