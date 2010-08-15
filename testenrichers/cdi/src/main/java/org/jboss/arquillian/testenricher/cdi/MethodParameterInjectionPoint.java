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
package org.jboss.arquillian.testenricher.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

/**
 * MethodParameterInjectionPoint
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @author Pete Muir
 * @version $Revision: $
 */
public class MethodParameterInjectionPoint<T> implements InjectionPoint
{
   private Method method;
   private int position;
   private BeanManager beanManager;
   
   public MethodParameterInjectionPoint(Method method, int position, BeanManager beanManager)
   {
      this.method = method;
      this.position = position;
      this.beanManager = beanManager;
   }
   
   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.InjectionPoint#getBean()
    */
   public Bean<?> getBean()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.InjectionPoint#getMember()
    */
   public Member getMember()
   {
      return method;
   }

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.InjectionPoint#getQualifiers()
    */
   public Set<Annotation> getQualifiers()
   {
      Set<Annotation> qualifiers = new HashSet<Annotation>();
      for(Annotation annotation : method.getParameterAnnotations()[position])
      {
         if(beanManager.isQualifier(annotation.annotationType()))
         {
            qualifiers.add(annotation);
         }
      }
      /*
       * TODO: ARQ-240 We should not add @Default by default, this should be handled by CDI. 
       * Due to bug in Weld 1.0.0.SP4(fixed in trunk). Remove this when 1.1.0 is out. 
       */
      if(qualifiers.size() == 0)
      {
         qualifiers.add(new DefaultLiteral());
      }
      return qualifiers;
   }

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.InjectionPoint#getType()
    */
   public Type getType()
   {
      return findTypeOrGenericType();
   }

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.InjectionPoint#isDelegate()
    */
   public boolean isDelegate()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.InjectionPoint#isTransient()
    */
   public boolean isTransient()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see javax.enterprise.inject.spi.InjectionPoint#getAnnotated()
    */
   public Annotated getAnnotated()
   {
      return new ArgumentAnnotated<T>();
   }

   private class ArgumentAnnotated<X> implements AnnotatedParameter<X> {

      /* (non-Javadoc)
       * @see javax.enterprise.inject.spi.AnnotatedParameter#getDeclaringCallable()
       */
      public AnnotatedCallable<X> getDeclaringCallable()
      {
         return null;
      }

      /* (non-Javadoc)
       * @see javax.enterprise.inject.spi.AnnotatedParameter#getPosition()
       */
      public int getPosition()
      {
         return position;
      }

      /* (non-Javadoc)
       * @see javax.enterprise.inject.spi.Annotated#getAnnotation(java.lang.Class)
       */
      public <Y extends Annotation> Y getAnnotation(Class<Y> annotationType)
      {
         for(Annotation annotation : method.getParameterAnnotations()[position])
         {
            if(annotation.annotationType() == annotationType)
            {
               return annotationType.cast(annotation);
            }
         }
         return null;
      }

      /* (non-Javadoc)
       * @see javax.enterprise.inject.spi.Annotated#getAnnotations()
       */
      public Set<Annotation> getAnnotations()
      {
         return new HashSet<Annotation>(Arrays.asList(method.getParameterAnnotations()[position]));
      }

      /* (non-Javadoc)
       * @see javax.enterprise.inject.spi.Annotated#getBaseType()
       */
      public Type getBaseType()
      {
         return getType();
      }

      /* (non-Javadoc)
       * @see javax.enterprise.inject.spi.Annotated#getTypeClosure()
       */
      public Set<Type> getTypeClosure()
      {
         Set<Type> types = new HashSet<Type>();
         types.add(findTypeOrGenericType());
         types.add(Object.class);
         return types; 
      }

      /* (non-Javadoc)
       * @see javax.enterprise.inject.spi.Annotated#isAnnotationPresent(java.lang.Class)
       */
      public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
      {
         return getAnnotation(annotationType) != null;
      }
   }
   
   private Type findTypeOrGenericType()
   {
      if(method.getGenericParameterTypes().length > 0)
      {
         return method.getGenericParameterTypes()[position];
      }
      return method.getParameterTypes()[position];
   }
   
   private static class DefaultLiteral extends AnnotationLiteral<Default> implements Default 
   {
      private static final long serialVersionUID = 1L;
   }
}
