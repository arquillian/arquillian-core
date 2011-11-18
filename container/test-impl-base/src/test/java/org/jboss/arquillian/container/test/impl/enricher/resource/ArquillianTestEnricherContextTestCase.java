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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.util.Arrays;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * ArquillianTestEnricherTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ArquillianTestEnricherContextTestCase extends AbstractContainerTestTestBase
{
   @Inject
   private Instance<Injector> injector;
   
   @Mock
   private ServiceLoader serviceLoader;
   
   private ResourceProvider resourceProvider;
   
   private InitialContext context;

   @Before
   public void addServiceLoader() throws Exception
   {
      context = new InitialContext();
      
      resourceProvider = new InitialContextProvider();
      injector.get().inject(resourceProvider);
      
      List<ResourceProvider> resourceProviders = Arrays.asList(new ResourceProvider[]{resourceProvider});
      Mockito.when(serviceLoader.all(ResourceProvider.class)).thenReturn(resourceProviders);

      bind(ApplicationScoped.class, Context.class, context);
      bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
   }
   
   @Test
   public void shouldBeAbleToInjectBaseContext() throws Exception
   {
      TestEnricher enricher = new ArquillianResourceTestEnricher();
      injector.get().inject(enricher);

      ContextClass test = new ContextClass();
      enricher.enrich(test);
      
      Assert.assertEquals(context, test.context);
   }
   
   @Test
   public void shouldBeAbleToInjectServlet() throws Exception
   {
      TestEnricher enricher = new ArquillianResourceTestEnricher();
      injector.get().inject(enricher);

      InitialContextClass test = new InitialContextClass();
      enricher.enrich(test);
      
      Assert.assertEquals(context, test.context);
   }
   
   public class ContextClass 
   {
      @ArquillianResource
      public Context context;
   }

   public class InitialContextClass 
   {
      @ArquillianResource
      public InitialContext context;
   }
}
