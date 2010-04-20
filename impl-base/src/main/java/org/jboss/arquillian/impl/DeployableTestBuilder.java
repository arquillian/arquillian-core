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

import org.jboss.arquillian.impl.context.ClientProfileBuilder;
import org.jboss.arquillian.impl.context.ContainerProfileBuilder;
import org.jboss.arquillian.impl.context.ContextLifecycleManager;
import org.jboss.arquillian.impl.context.ProfileBuilder;
import org.jboss.arquillian.impl.context.StandaloneProfileBuilder;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ContainerProfile;
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

   /**
    * @return
    */
   public static TestRunnerAdaptor build() 
   {
      return build(DeployableTestBuilder.profile);
   }
   
   // TODO: fix the ContainerProfile loading/selecting
   public static TestRunnerAdaptor build(Configuration configuration)
   {
      ContainerProfile profile = DeployableTestBuilder.profile;
      ContainerConfiguration activeConfiguration = configuration.getActiveContainerConfiguration();
      if(activeConfiguration != null && profile == null) 
      {
         profile = activeConfiguration.getContainerProfile();
      }
      return build(profile, configuration);
   }
   
   public static TestRunnerAdaptor build(ContainerProfile profile) 
   {
      return build(profile, new XmlConfigurationBuilder().build());
   }
   
   public static TestRunnerAdaptor build(ContainerProfile profile, Configuration configuration) 
   {
      ProfileBuilder profileBuilder = null;
      switch(profile) 
      {
         case STANDALONE:
            profileBuilder = new StandaloneProfileBuilder();
            break;
         case CONTAINER:
            profileBuilder = new ContainerProfileBuilder();
            break;
         case CLIENT:
            profileBuilder = new ClientProfileBuilder();
            break;
         default: // TODO: create profile builders dynamic
            throw new IllegalArgumentException("Unknon profile " + profile);
      }
      return build(profileBuilder, configuration);
   }
   
   /**
    * @param profileBuilder
    * @return
    */
   public static TestRunnerAdaptor build(ProfileBuilder profileBuilder, Configuration configuration)
   {
      ServiceLoader serviceLoader = new DynamicServiceLoader();

      ContextLifecycleManager eventManager = new ContextLifecycleManager(
            configuration, 
            profileBuilder,
            serviceLoader
      );

      return new EventTestRunnerAdaptor(eventManager);
   }
}
