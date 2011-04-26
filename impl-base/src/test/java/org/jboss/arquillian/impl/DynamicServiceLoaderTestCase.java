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

import java.util.Collection;

import org.jboss.arquillian.core.impl.loadable.DynamicServiceLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Verify the behavior of the Dynamic Service Loader 
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DynamicServiceLoaderTestCase
{

   @Test(expected = IllegalStateException.class)
   public void shouldFailIfMultipleProvidersFound() throws Exception 
   {
      new DynamicServiceLoader().onlyOne(Service.class);
   }
   
   @Test
   public void shouldNotFailIfMultipleProvidersFoundPointingToSameImpl() throws Exception 
   {
      Service2 service = new DynamicServiceLoader().onlyOne(Service2.class);
      
      Assert.assertTrue(
            "verify that a instance of Service2Impl was loaded",
            service.getClass() == Service2Impl.class);
   }

   @Test
   public void shouldLoadAllInstances() throws Exception { 
      Collection<Service> services = new DynamicServiceLoader().all(Service.class);
      
      Assert.assertEquals(
            "verify that all services where found and loaded",
            2, services.size());
   }
   
   @Test
   public void shouldReturnDefinedOrderInServicesFile() throws Exception
   {
      for(int i = 0; i < 10; i++)
      {
         Collection<Service> services = new DynamicServiceLoader().all(Service.class);
         verifyOrder(services);
      }
   }
   
   @Test
   public void shouldReturnDefaultServiceImplIfNoOtherIsFound() throws Exception
   {
      NonRegisteredService service = new DynamicServiceLoader().onlyOne(
            NonRegisteredService.class, 
            DefaultNonRegisteredServiceImpl.class);
      
      Assert.assertNotNull(service);
      Assert.assertEquals(
            "Verify that the default provided service class was returned",
            DefaultNonRegisteredServiceImpl.class, 
            service.getClass());
   }

   @Test
   public void shouldNotReturnDefaultServiceImplIfOtherIsFound() throws Exception
   {
      Service2 service = new DynamicServiceLoader().onlyOne(
            Service2.class, 
            DefaultService2Impl.class);
      
      Assert.assertNotNull(service);
      Assert.assertEquals(
            "Verify that the default provided service class was returned",
            Service2Impl.class, 
            service.getClass());
   }

   private void verifyOrder(Collection<Service> services)
   {
      int i = 0;
      for(Service service : services)
      {
         switch(i)
         {
            case 0:
               Assert.assertEquals(service.getClass(), ServiceImpl1.class);
               break;
            case 1:
               Assert.assertEquals(service.getClass(), ServiceImpl2.class);
               break;
         }
         i++;
      }
   }
   
   public interface Service {}
   public static class ServiceImpl1 implements Service {}
   public static class ServiceImpl2 implements Service {}
   
   public interface Service2 {}
   public static class Service2Impl implements Service2 {}
   public static class DefaultService2Impl implements Service2 {}

   public interface NonRegisteredService {}
   public static class DefaultNonRegisteredServiceImpl implements NonRegisteredService {}
   
}
