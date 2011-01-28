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
package com.acme.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.acme.ejb.GreetingManager;
import com.acme.ejb.GreetingManagerBean;

/**
 * InjectionTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class InjectionTestCase
{
   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class)
               .addClasses(
                     GreetingManager.class,
                     GreetingManagerBean.class)
               .addManifestResource(
                     EmptyAsset.INSTANCE, 
                     ArchivePaths.create("beans.xml"));
   }
   
   @Inject GreetingManager greetingManager;
   
   @Inject BeanManager beanManager;
   
   @Test
   public void shouldBeAbleToInjectCDI() throws Exception {
      
      String userName = "Devoxx";
      
      Assert.assertNotNull(
            "Should have injected manager",
            beanManager);

      Assert.assertEquals(
            "Hello " + userName,
            greetingManager.greet(userName));
      
   }
}
