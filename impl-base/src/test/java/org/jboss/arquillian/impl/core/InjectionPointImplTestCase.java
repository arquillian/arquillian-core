/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.impl.core;

import org.jboss.arquillian.impl.core.InjectionPointImpl;
import org.jboss.arquillian.impl.core.spi.InjectionPoint;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.junit.Assert;
import org.junit.Test;


/**
 * InjectionPointImplTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class InjectionPointImplTestCase
{
   @SuppressWarnings("unused")
   private Instance<InjectionPointImplTestCase> instance;

   @SuiteScoped
   @SuppressWarnings("unused")
   private Instance<InjectionPointImplTestCase> suiteInstance;

   @SuppressWarnings("unused")
   private Instance<GenericWildCard<?>> genericWildCardInstance;
   
   @Test
   public void shouldBeAbleToDetermineType() throws Exception
   {
      InjectionPoint point = InjectionPointImpl.of(this, this.getClass().getDeclaredField("instance"));
      
      Assert.assertEquals(InjectionPointImplTestCase.class, point.getType());
   }

   @Test
   public void shouldBeAbleToDetermineScope() throws Exception
   {
      InjectionPoint point = InjectionPointImpl.of(this, this.getClass().getDeclaredField("suiteInstance"));
      
      Assert.assertEquals(SuiteScoped.class, point.getScope());
   }
   
   @Test
   public void shouldBeAbleToFindRawTypeForGenericWildCard() throws Exception
   {
      InjectionPoint point = InjectionPointImpl.of(this, this.getClass().getDeclaredField("genericWildCardInstance"));
      
      try
      {
         Assert.assertEquals(GenericWildCard.class, point.getType());
      }
      catch (Exception e) {
         //e.printStackTrace();
         throw e;
      }
   }
   
   public static class GenericWildCard<T> 
   {
      
   }
}
