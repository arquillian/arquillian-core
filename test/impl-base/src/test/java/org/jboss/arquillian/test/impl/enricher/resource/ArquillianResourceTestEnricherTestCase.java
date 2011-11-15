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
package org.jboss.arquillian.test.impl.enricher.resource;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
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
 * @author Vineet Reynolds
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ArquillianResourceTestEnricherTestCase extends AbstractTestTestBase
{
   @Inject
   private Instance<Injector> injector;

   @Mock
   private ServiceLoader serviceLoader;

   @Mock
   private ResourceProvider resourceProvider;

   @Mock
   private Object resource;

   @Before
   public void addServiceLoader() throws Exception
   {
      List<ResourceProvider> resourceProviders = Arrays.asList(new ResourceProvider[]{resourceProvider});
      Mockito.when(serviceLoader.all(ResourceProvider.class)).thenReturn(resourceProviders);
      Mockito.when(resourceProvider.canProvide(Object.class)).thenReturn(true);
      Mockito.when(
            resourceProvider.lookup(ObjectClass.class.getField("resource").getAnnotation(ArquillianResource.class)))
            .thenReturn(resource);

      bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
   }

   @Test
   public void shouldBeAbleToInjectBaseContext() throws Exception
   {
      TestEnricher enricher = new ArquillianResourceTestEnricher();
      injector.get().inject(enricher);

      ObjectClass test = new ObjectClass();
      enricher.enrich(test);

      Assert.assertEquals(resource, test.resource);
   }

   public class ObjectClass
   {
      @ArquillianResource
      public Object resource;
   }
}
