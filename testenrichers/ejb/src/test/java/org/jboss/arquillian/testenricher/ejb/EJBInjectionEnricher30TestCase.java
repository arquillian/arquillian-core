/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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

import javax.ejb.EJB;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link EJBInjectionEnricher}.
 * <p>
 * These tests doesn't use embedded container, as they're just simple unit tests.
 *
 * @author PedroKowalski
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 */
public class EJBInjectionEnricher30TestCase extends EJBInjectionEnricherBase {
    @Test
    public void testResolveJNDIName() {
        cut.enrich(new EJBEnrichedSimpleInjectionClass());
        assertThat(resolvedJndiName.length > 5, is(true));
    }

    @Test
    public void testResolveJNDINameMappedNameSpecified() {
        cut.enrich(new EJBEnrichedMappedNameClass());

        String expected = EJBEnrichedMappedNameClass.class.getDeclaredFields()[0].getAnnotation(EJB.class).mappedName();
      /*
       * When 'mappedName' is set, the only JNDI name to check is the exact value specified in the annotation.
       */
        assertThat(resolvedJndiName, is(notNullValue()));
        assertThat(resolvedJndiName.length, is(1));
        assertThat(resolvedJndiName[0], is(expected));
    }

    @Test
    public void testResolveJNDINameBeanNameSpecified() {
        cut.enrich(new EJBEnrichedBeanNameClass());

        Class<?> fieldType = EJBEnrichedMappedNameClass.class.getDeclaredFields()[0].getType();
        String[] r = resolvedJndiName;

        // Expected: java:module/<bean-name>[!<fully-qualified-interface-name>]
        String expected = "java:module/" + ExemplaryEJBMockImpl.class.getSimpleName() + "!"
            + fieldType.getName();

        assertThat(r, is(notNullValue()));
        assertThat(r.length, is(1));
        assertThat(r[0], is(expected));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnBeanAndMappedName() {
        cut.enrich(new EJBInvalidBeanAndMappedNameClass());

        assertThat(caughtResolveException, notNullValue());
        throw caughtResolveException;
    }

    /**
     * Exemplary class with EJB annotations which will be tested for JNDI resolution.
     * <p>
     * Note: As a field type this class uses the interface which has two implementations. Appropriate implementation
     * injection
     * should also be tested.
     *
     * @author PedroKowalski
     */
    public static final class EJBEnrichedSimpleInjectionClass {
        @EJB
        ExemplaryEJB simpleInjection;
    }

    public static final class EJBEnrichedMappedNameClass {
        @EJB(mappedName = "java:module/org/arquillian/Test")
        ExemplaryEJB mappedNameInjection;
    }

    public static final class EJBEnrichedBeanNameClass {
        @EJB(beanName = "ExemplaryEJBMockImpl")
        ExemplaryEJB beanNameInjection;
    }

    public static final class EJBInvalidBeanAndMappedNameClass {
        @EJB(beanName = "any", mappedName = "any")
        ExemplaryEJB lookupInjection;
    }
}