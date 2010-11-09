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

import java.util.Collection;

import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.Before;

/**
 * A Handler for enriching the Test instance.<br/>
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestCaseEnricher 
{
   @Inject
   private Instance<ServiceLoader> serviceLoader;
   
   @Inject
   private Instance<Injector> injector;

   public void enrich(@Observes Before event) throws Exception
   {
      Collection<TestEnricher> testEnrichers = serviceLoader.get().all(TestEnricher.class);
      for(TestEnricher enricher : testEnrichers) 
      {
         injector.get().inject(enricher);
         enricher.enrich(event.getTestInstance());
      }
   }
}
