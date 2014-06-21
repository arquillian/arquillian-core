/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.config.descriptor.impl;

import org.jboss.arquillian.config.descriptor.api.EngineDef;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * EngineDefImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EngineDefImpl extends ArquillianDescriptorImpl implements EngineDef
{
   private static final String exportPath = "property@name=deploymentExportPath";
   private static final String exportExploded = "property@name=deploymentExportExploded";
   private static final String maxTestClasses = "property@name=maxTestClassesBeforeRestart";
   
   private Node engine;
   
   public EngineDefImpl(String descriptorName, Node model, Node engine)
   {
      super(descriptorName, model);
      this.engine = engine;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.EngineDef#deploymentExportPath(java.lang.String)
    */
   @Override
   public EngineDef deploymentExportPath(String path)
   {
      engine.getOrCreate(exportPath).text(path);
      return this;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.EngineDef#getDeploymentExportPath()
    */
   @Override
   public String getDeploymentExportPath()
   {
      return getTextIfExists(exportPath);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.EngineDef#deploymentExportExploded(java.lang.Boolean)
    */
   @Override
   public EngineDef deploymentExportExploded(Boolean exploded)
   {
      engine.getOrCreate(exportExploded).text(exploded);
      return this;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.EngineDef#getDeploymentExportExploded()
    */
   @Override
   public Boolean getDeploymentExportExploded()
   {
      return getTextIfExistsAsBoolean(exportExploded, false);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.EngineDef#maxTestClassesBeforeRestart(java.lang.Integer)
    */
   @Override
   public EngineDef maxTestClassesBeforeRestart(Integer max)
   {
      engine.getOrCreate(maxTestClasses).text(max);
      return this;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.EngineDef#getMaxTestClassesBeforeRestart()
    */
   @Override
   public Integer getMaxTestClassesBeforeRestart()
   {
      return getTextIfExistsAsInteger(maxTestClasses);
   }

   private Integer getTextIfExistsAsInteger(String pattern)
   {
      String text = getTextIfExists(pattern);
      if(text != null)
      {
         return Integer.parseInt(text);
      }
      return null;
   }

   private Boolean getTextIfExistsAsBoolean(String pattern, Boolean defaultValue)
   {
      String text = getTextIfExists(pattern);
      if(text != null)
      {
         return Boolean.parseBoolean(text);
      }
      return defaultValue;
   }

   private String getTextIfExists(String pattern)
   {
      Node propery = engine.getSingle(pattern);
      if(propery != null)
      {
         return propery.getText();
      }
      return null;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return engine.toString(true);
   }
}
