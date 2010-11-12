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
package org.jboss.arquillian.impl;

import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.context.ClassContextImpl;
import org.jboss.arquillian.impl.core.context.ContainerContextImpl;
import org.jboss.arquillian.impl.core.context.DeploymentContextImpl;
import org.jboss.arquillian.impl.core.context.SuiteContextImpl;
import org.jboss.arquillian.impl.core.context.TestContextImpl;
import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.Profile;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestRunnerAdaptor;

/**
 * DeployableTestBuilder
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeployableTestBuilder
{
   private DeployableTestBuilder() {}

   private static ContainerProfile profile = null;

   public static void setProfile(ContainerProfile profile)
   {
      Validate.notNull(profile, "Profile must be specified");
      
      DeployableTestBuilder.profile = profile;
   }
   
   public static ContainerProfile getProfile()
   {
      return DeployableTestBuilder.profile;
   }
   
   public static void clearProfile() 
   {
      DeployableTestBuilder.profile = null;
   }

   /**
    * @return
    */
   public static TestRunnerAdaptor build() 
   {
      return build(DeployableTestBuilder.profile == null ? ContainerProfile.CLIENT:DeployableTestBuilder.profile);
   }
   
   public static TestRunnerAdaptor build(ContainerProfile profileType) 
   {
      ServiceLoader serviceLoader = new DynamicServiceLoader();
      ManagerBuilder builder = ManagerBuilder.from()
         .context(SuiteContextImpl.class)
         .context(ClassContextImpl.class)
         .context(TestContextImpl.class)
         .context(ContainerContextImpl.class)
         .context(DeploymentContextImpl.class);
         
      Profile profile = serviceLoader.onlyOne(Profile.class, ArquillianProfile.class);
      switch (profileType)
      {
         case CLIENT :
            builder.extensions(profile.getClientProfile().toArray(new Class<?>[0]));
            break;
         case CONTAINER :
            builder.extensions(profile.getContainerProfile().toArray(new Class<?>[0]));
            break;
      }
      return new EventTestRunnerAdaptor(builder.create());     
   }
}
