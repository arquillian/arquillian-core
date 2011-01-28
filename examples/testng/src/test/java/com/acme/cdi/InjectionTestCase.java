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
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.acme.ejb.GreetingManager;
import com.acme.ejb.GreetingManagerBean;

/**
 * InjectionTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Test(groups = "integration")
public class InjectionTestCase extends Arquillian
{
   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class)
               .addClasses(
                     GreetingManager.class,
                     GreetingManagerBean.class)
               .addManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }
   
   @Inject GreetingManager greetingManager;
   
   @Inject BeanManager beanManager;
   
   @Test
   public void shouldBeAbleToInjectCDI() throws Exception {
      
      String userName = "Devoxx";
      
      Assert.assertNotNull(
            beanManager,
            "Should have injected manager");

      Assert.assertEquals(
            greetingManager.greet(userName),
            "Hello " + userName);
   }
}
