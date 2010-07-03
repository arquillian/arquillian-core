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
package org.jboss.arquillian.impl.handler;

import java.io.File;
import java.io.FileOutputStream;

import org.jboss.arquillian.impl.ToolingDeploymentFormatter;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.impl.base.Validate;

/**
 * Handler that will export a XML version of the Deployed Archive. 
 * 
 * Used by tooling to show a view of the ShrinkWrap archive.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArchiveDeploymentToolingExporter implements EventHandler<ClassEvent>
{
   
   static final String ARQUILLIAN_TOOLING_DEPLOYMENT_FOLDER = "arquillian.tooling.deployment.folder";
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, ClassEvent event) throws Exception 
   {
      String deploymentOutputFolder = System.getProperty(ARQUILLIAN_TOOLING_DEPLOYMENT_FOLDER);
      if(deploymentOutputFolder == null) // tooling not activated, nothing to do 
      {
         return;
      }
      Archive<?> deployment = context.get(Archive.class); // deployment not in context?, nothing to do
      if(deployment == null)
      {
         return;
      }
      
      TestClass testClass = event.getTestClass();
      String deploymentContent = deployment.toString(new ToolingDeploymentFormatter(testClass.getJavaClass()));
      writeOutToFile(
            new File(deploymentOutputFolder + "/" + testClass.getName() + ".xml"), 
            deploymentContent);
   }
   
   protected void writeOutToFile(File target, String content) 
   {
      Validate.notNull(target, "Target must be specified");
      Validate.notNull(content, "Content must be specified");
      
      FileOutputStream output = null;
      try
      {
         output = new FileOutputStream(target);
         output.write(content.getBytes());
         output.close();
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not write content to file", e);
      }
      finally
      {
         if(output != null)
         {
            try {output.close(); } 
            catch (Exception e) 
            { 
               throw new RuntimeException("Could not close output stream", e);  
            }
         }
      }
   }
}
