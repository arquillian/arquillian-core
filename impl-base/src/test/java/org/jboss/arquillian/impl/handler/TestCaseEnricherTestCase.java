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

import java.util.Arrays;

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.jboss.arquillian.spi.event.suite.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Verifies that the TestEnricher SPI is called.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseEnricherTestCase extends AbstractManagerTestBase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock
   private TestEnricher enricher;
   
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(TestCaseEnricher.class);
   }
   
   @Test
   public void shouldCallAllEnrichers() throws Exception
   {
      Mockito.when(serviceLoader.all(TestEnricher.class)).thenReturn(Arrays.asList(enricher, enricher));
      bind(SuiteScoped.class, ServiceLoader.class, serviceLoader);
      
      fire(new Before(this, getClass().getMethod("shouldCallAllEnrichers")));
      
      Mockito.verify(enricher, Mockito.times(2)).enrich(this);
   }
}
