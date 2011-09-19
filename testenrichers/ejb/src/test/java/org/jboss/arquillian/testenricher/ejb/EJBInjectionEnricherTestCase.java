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
package org.jboss.arquillian.testenricher.ejb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link EJBInjectionEnricher}.
 *
 * These tests doesn't use embedded container, as they're just simple unit tests.
 *
 * @author PedroKowalski
 *
 */
public class EJBInjectionEnricherTestCase
{
   private EJBInjectionEnricher cut;

   private EJBEnrichedClass enrichedClass;

   @Before
   public void before()
   {
      cut = new EJBInjectionEnricher();
      enrichedClass = new EJBEnrichedClass();
   }

   @Test
   public void testResolveJNDIName()
   {
      List<Field> injectionPoints = SecurityActions.getFieldsWithAnnotation(enrichedClass.getClass(), EJB.class);

      Field simpleEJBField = getField(injectionPoints, "simpleInjection");

      // Should invoke default JNDI names resolution.
      String[] r = cut.resolveJNDINames(simpleEJBField.getType(), null, null);

      // TODO: change to something more appropriate (test default JNDI names).
      assertThat(r.length > 5, is(true));
   }

   @Test(expected = IllegalStateException.class)
   public void testResolveJNDINameMappedNameAndBeanNameSpecified()
   {

      // What field doesn't matter for this test case.
      Field anyField = enrichedClass.getClass().getDeclaredFields()[0];

      // Specifying both: mappedName and beanName is not allowed.
      cut.resolveJNDINames(anyField.getType(), "anyString()", "anyString()");
   }

   @Test
   public void testResolveJNDINameMappedNameSpecified()
   {
      List<Field> injectionPoints = SecurityActions.getFieldsWithAnnotation(enrichedClass.getClass(), EJB.class);

      Field mappedNameEJBField = getField(injectionPoints, "mappedNameInjection");

      EJB fieldAnnotation = (EJB) mappedNameEJBField.getAnnotation(EJB.class);

      String[] r = cut.resolveJNDINames(mappedNameEJBField.getType(), fieldAnnotation.mappedName(), null);

      /*
       * When 'mappedName' is set, the only JNDI name to check is the exact value specified in the annotation.
       */
      assertThat(r, is(notNullValue()));
      assertThat(r.length, is(1));
      assertThat(r[0], is(fieldAnnotation.mappedName()));
   }

   @Test
   public void testResolveJNDINameBeanNameSpecified()
   {
      List<Field> injectionPoints = SecurityActions.getFieldsWithAnnotation(enrichedClass.getClass(), EJB.class);

      Field beanNameEJBField = getField(injectionPoints, "beanNameInjection");

      EJB fieldAnnotation = (EJB) beanNameEJBField.getAnnotation(EJB.class);

      String[] r = cut.resolveJNDINames(beanNameEJBField.getType(), null, fieldAnnotation.beanName());

      // Expected: java:module/<bean-name>[!<fully-qualified-interface-name>]
      String expected = "java:module/" + ExemplaryEJBMockImpl.class.getSimpleName() + "!"
            + beanNameEJBField.getType().getName();

      assertThat(r, is(notNullValue()));
      assertThat(r.length, is(1));
      assertThat(r[0], is(expected));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testResolveJNDINameFieldNotSet()
   {

      // Annotated field must be set.
      cut.resolveJNDINames(null, "anyString()", null);
   }

   /**
    * Helper method which returns the {@link Field} object with the given <tt>name</tt>.
    *
    * Object's fields fetched using Java Reflection doesn't have any particular order, so to fetch the appropriate field this
    * method can be used.
    *
    * @param fields fields that will be iterated.
    * @param name name of the searched field.
    *
    * @return {@link Field} with the given <tt>name</tt> or null if the name couldn't be found.
    */
   private Field getField(List<Field> fields, String name)
   {
      for (Field field : fields)
      {
         if (field.getName().equals(name))
         {
            return field;
         }
      }

      return null;
   }

   /**
    * Exemplary class with EJB annotations which will be tested for JNDI resolution.
    *
    * Note: As a field type this class uses the interface which has two implementations. Appropriate implementation injection
    * should also be tested.
    *
    * @author PedroKowalski
    *
    */
   public static final class EJBEnrichedClass
   {

      @EJB
      ExemplaryEJB simpleInjection;

      @EJB(mappedName = "java:module/org/arquillian/Test")
      ExemplaryEJB mappedNameInjection;

      @EJB(beanName = "ExemplaryEJBMockImpl")
      ExemplaryEJB beanNameInjection;
   }

   /**
    * Exemplary EJB's local interface.
    *
    * @author PedroKowalski
    *
    */
   @Local
   public static interface ExemplaryEJB
   {

   }

   /**
    * Exemplary implementation of the EJB's local interface.
    *
    * @author PedroKowalski
    *
    */
   @Stateless
   public static class ExemplaryEJBMockImpl implements ExemplaryEJB
   {

   }

   /**
    * Exemplary implementation of the EJB's local interface.
    *
    * This class is here only to be sure that despite more than one implementation of the interface, only the one pointed by
    * <tt>beanName</tt> attribute of the {@link EJB} annotation will be used.
    *
    * @author PedroKowalski
    *
    */
   @Stateless
   public static class ExemplaryEJBProductionImpl implements ExemplaryEJB
   {

   }
}
