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
package org.jboss.arquillian.impl.configuration;

import org.jboss.arquillian.impl.configuration.api.EngineDef;
import org.jboss.shrinkwrap.descriptor.api.Node;
import org.jboss.shrinkwrap.descriptor.api.query.Queries;
import org.jboss.shrinkwrap.descriptor.api.query.Query;

/**
 * EngineDefImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EngineDefImpl extends ArquillianDescriptorImpl implements EngineDef
{
   private static final Query exportPath = Queries.from("property@name=deploymentExportPath");
   private static final Query maxTestClasses = Queries.from("property@name=maxTestClassesBeforeRestart");
   
   private Node engine;
   
   public EngineDefImpl(String descirptorName, Node model, Node engine)
   {
      super(descirptorName, model);
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
   
   private Integer getTextIfExistsAsInteger(Query query)
   {
      String text = getTextIfExists(query);
      if(text != null)
      {
         return Integer.parseInt(text);
      }
      return null;
   }

   private String getTextIfExists(Query query)
   {
      Node propery = engine.getSingle(query);
      if(propery != null)
      {
         return propery.text();
      }
      return null;
   }
}
