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
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link EJBInjectionEnricher}.
 * <p>
 * These tests doesn't use embedded container, as they're just simple unit tests.
 *
 * @author PedroKowalski
 */
public class EJBInjectionEnricherBase {
    protected EJBInjectionEnricher cut;

    protected String[] resolvedJndiName;
    protected RuntimeException caughtResolveException;

    @Before
    public void before() throws Exception {
        cut = new EJBInjectionEnricher() {
            @Override
            protected Context createContext() throws Exception {
                // just need to return non null. lookupEJB is overwritten so usage of context is under our control
                return new InitialContext();
            }

            @Override
            protected Object lookupEJB(String[] jndiNames) throws Exception {
                resolvedJndiName = jndiNames;
                return new ExemplaryEJBMockImpl();
            }

            @Override
            protected String[] resolveJNDINames(Class<?> fieldType, String mappedName, String beanName, String lookup) {
                try {
                    return super.resolveJNDINames(fieldType, mappedName, beanName, lookup);
                } catch (RuntimeException e) {
                    caughtResolveException = e;
                    throw e;
                }
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveJNDINameFieldNotSet() {
        // Annotated field must be set.
        cut.resolveJNDINames(null, "anyString()", null, null);
    }

    /**
     * Exemplary EJB's local interface.
     *
     * @author PedroKowalski
     */
    @Local
    public static interface ExemplaryEJB {
    }

    /**
     * Exemplary implementation of the EJB's local interface.
     *
     * @author PedroKowalski
     */
    @Stateless
    public static class ExemplaryEJBMockImpl implements ExemplaryEJB {
    }

    /**
     * Exemplary implementation of the EJB's local interface.
     * <p>
     * This class is here only to be sure that despite more than one implementation of the interface, only the one pointed
     * by
     * <tt>beanName</tt> attribute of the {@link EJB} annotation will be used.
     *
     * @author PedroKowalski
     */
    @Stateless
    public static class ExemplaryEJBProductionImpl implements ExemplaryEJB {
    }
}
