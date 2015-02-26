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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.annotation.ClassInjection;
import org.jboss.arquillian.test.spi.annotation.MethodInjection;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.VarargMatcher;
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

      bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
   }

   @Test
   public void shouldBeAbleToInjectBaseContext() throws Exception
   {
      Mockito.when(resourceProvider.lookup(
                  (ArquillianResource) Mockito.any(),
                  Mockito.argThat(new ClassInjectionAnnotationMatcher())))
              .thenReturn(resource);

      TestEnricher enricher = new ArquillianResourceTestEnricher();
      injector.get().inject(enricher);

      ObjectClass test = new ObjectClass();
      enricher.enrich(test);

      Assert.assertEquals(resource, test.resource);
   }

   @Test
   public void shouldBeAbleToInjectBaseContextOnMethod() throws Exception
   {
      Method resourceMethod = ObjectClass.class.getMethod("test", Object.class);

      Mockito.when(
            resourceProvider.lookup(
                    (ArquillianResource) Mockito.any(),
                    Mockito.argThat(new MethodInjectionAnnotationMatcher())))
                .thenReturn(resource);

      TestEnricher enricher = new ArquillianResourceTestEnricher();
      injector.get().inject(enricher);

      Object[] result = enricher.resolve(resourceMethod);

      Assert.assertEquals(resource, result[0]);
   }

   @Test
   public void shouldBeAbleToInjectBaseContextWithQualifier() throws Exception
   {
      Field resource2Field = ObjectClass2.class.getField("resource2");

      Mockito.when(
          resourceProvider.lookup(
                  (ArquillianResource) Mockito.any(),
                  Mockito.argThat(new CustomAnnotationMatcher(resource2Field.getAnnotation(ArquillianTestQualifier.class), ClassInjection.class))))
              .thenReturn(resource);

      TestEnricher enricher = new ArquillianResourceTestEnricher();
      injector.get().inject(enricher);

      ObjectClass2 test = new ObjectClass2();
      enricher.enrich(test);

      Assert.assertEquals(resource, test.resource2);
   }

   @Test
   public void shouldBeAbleToInjectBaseContextOnMethodWithQualifier() throws Exception
   {
      Method resourceMethod = ObjectClass.class.getMethod("testWithQualifier", Object.class);

      Mockito.when(
          resourceProvider.lookup(
                  (ArquillianResource) Mockito.any(),
                  Mockito.argThat(new CustomAnnotationMatcher(resourceMethod.getParameterAnnotations()[0][1], MethodInjection.class))))
              .thenReturn(resource);

      TestEnricher enricher = new ArquillianResourceTestEnricher();
      injector.get().inject(enricher);

      Object[] result = enricher.resolve(resourceMethod);

      Assert.assertEquals(resource, result[0]);
   }

   public class ObjectClass
   {
      @ArquillianResource
      public Object resource;

      public void test(@ArquillianResource Object resource) {}

      public void testWithQualifier(@ArquillianResource @ArquillianTestQualifier Object resource) {}
   }

   public class ObjectClass2
   {
      @ArquillianResource @ArquillianTestQualifier
      public Object resource2;
   }

   @Retention(RUNTIME)
   @Target({ElementType.FIELD, ElementType.PARAMETER})
   public @interface ArquillianTestQualifier { }
   
   private class ClassInjectionAnnotationMatcher extends ArgumentMatcher<Annotation[]> implements VarargMatcher
   {
       private static final long serialVersionUID = 7468313136186740343L;

       @Override
       public boolean matches(Object varargArgument)
       {
           Class<?> qualifier = ((Annotation) varargArgument).annotationType();

           return qualifier.equals(ClassInjection.class);
       }
   }
   
   private class MethodInjectionAnnotationMatcher extends ArgumentMatcher<Annotation[]> implements VarargMatcher
   {
       private static final long serialVersionUID = 7468313136186740343L;

       @Override
       public boolean matches(Object varargArgument)
       {
           Class<?> qualifier = ((Annotation) varargArgument).annotationType();
           
           return qualifier.equals(MethodInjection.class);
       }
   }
   
   private class CustomAnnotationMatcher extends ArgumentMatcher<Annotation[]> implements VarargMatcher
   {
       private static final long serialVersionUID = 7468313136186740343L;

       private Annotation additionalQualifier;
       
       private Class<? extends Annotation> injectionScope;
       
       public CustomAnnotationMatcher(Annotation additionalQualifier, Class<? extends Annotation> injectionScope)
       {
           this.additionalQualifier = additionalQualifier;
           this.injectionScope = injectionScope;
       }

       @Override
       public boolean matches(Object varargArgument)
       {
           Annotation[] annotations = (Annotation[]) varargArgument;
           
           return annotations.length == 2
               && annotations[0].annotationType().equals(additionalQualifier.annotationType())
               && annotations[1].annotationType().equals(injectionScope); 
       }
   }
}
