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
package org.jboss.arquillian.config.impl.extension;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * External utility capable of accepting a {@link ArquillianDescriptor},
 * replacing any sysprop EL expressions with a proper value or default,
 * and returning a new instance of the {@link ArquillianDescriptor}.
 * Fulfills ARQ-148.
 *
 * TODO To eventually become part of a chain-based event mechanism
 * as defined by ARQ-284.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
class ConfigurationSysPropResolver
{
   /**
    * Internal ctor, not to be called
    */
   private ConfigurationSysPropResolver()
   {
      throw new UnsupportedOperationException("No instances permitted");
   }

   /**
    * Returns a new instance of {@link ArquillianDescriptor} by resolving any
    * sysprop EL expressions in the provided {@link ArquillianDescriptor} to real
    * values or defaults
    * @param descriptor The input to resolve, required
    * @return
    * @throws IllegalArgumentException
    */
   static ArquillianDescriptor resolveSystemProperties(final ArquillianDescriptor descriptor)
         throws IllegalArgumentException
   {
      final String descrStr = descriptor.exportAsString();
      final ArquillianDescriptor newArqDescriptor = Descriptors.importAs(ArquillianDescriptor.class).fromString(
            StringPropertyReplacer.replaceProperties(descrStr));

      return newArqDescriptor;
   }

}
