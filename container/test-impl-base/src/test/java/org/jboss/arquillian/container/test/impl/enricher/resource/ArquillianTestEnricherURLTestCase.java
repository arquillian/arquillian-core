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

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Assert;

import org.jboss.arquillian.api.ArquillianResource;
import org.jboss.arquillian.container.test.impl.enricher.resource.ArquillianResourceTestEnricher;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.spi.TestEnricher;
import org.junit.Test;


/**
 * ArquillianTestEnricherTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianTestEnricherURLTestCase extends AbstractContainerTestTestBase
{
   @Inject
   private Instance<Injector> injector;

   @Test
   public void shouldBeAbleToInjectBaseContext() throws Exception
   {
      InitialContext context = new InitialContext();
      bind(ApplicationScoped.class, InitialContext.class, context);
      TestEnricher enricher = new ArquillianResourceTestEnricher();
      injector.get().inject(enricher);

      ContextClass test = new ContextClass();
      enricher.enrich(test);
      
      Assert.assertEquals(context, test.context);
   }
   
   @Test
   public void shouldBeAbleToInjectServlet() throws Exception
   {
      InitialContext context = new InitialContext();
      bind(ApplicationScoped.class, InitialContext.class, context);
      
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
