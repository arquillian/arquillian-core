/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020 Red Hat Inc. and/or its affiliates and other contributors
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

import jakarta.ejb.EJB;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link EJBInjectionEnricher}.
 * <p>
 * These tests doesn't use embedded container, as they're just simple unit tests.
 *
 * @author PedroKowalski
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 */
public class EJBInjectionEnricher40TestCase extends EJBInjectionEnricherBase {

    @Test
    public void testResolveJNDINameLookupSpecified() {
        cut.enrich(new EJBEnrichedLookupClass());

        String expected = EJBEnrichedLookupClass.class.getDeclaredFields()[0].getAnnotation(EJB.class).lookup();

      /*
       * When 'lookup' is set, the only JNDI name to check is the exact value specified in the annotation.
       */
        assertThat(resolvedJndiName, is(notNullValue()));
        assertThat(resolvedJndiName.length, is(1));
        assertThat(resolvedJndiName[0], is(expected));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMappedNameAndLookup() {
        cut.enrich(new EJBInvalidMappedNameAndLookupClass());

        assertThat(caughtResolveException, notNullValue());
        throw caughtResolveException;
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnBeanNameAndLookup() {
        cut.enrich(new EJBInvalidBeanNameAndLookupClass());

        assertThat(caughtResolveException, notNullValue());
        throw caughtResolveException;
    }

    public static final class EJBEnrichedLookupClass {
        @EJB(lookup = "java:global/org/arquillian/Test")
        ExemplaryEJB lookupInjection;
    }

    public static final class EJBInvalidMappedNameAndLookupClass {
        @EJB(mappedName = "any", lookup = "any")
        ExemplaryEJB lookupInjection;
    }

    public static final class EJBInvalidBeanNameAndLookupClass {
        @EJB(beanName = "any", lookup = "any")
        ExemplaryEJB lookupInjection;
    }
}
