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
package org.jboss.arquillian.core.impl;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.SuiteScoped;
import org.jboss.arquillian.core.impl.InjectionPointImpl;
import org.jboss.arquillian.core.spi.InjectionPoint;
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
}
