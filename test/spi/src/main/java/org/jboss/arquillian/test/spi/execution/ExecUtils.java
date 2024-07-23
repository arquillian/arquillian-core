/*
 * JBoss, Home of Professional Open Source
 * Copyright 2024, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.arquillian.test.spi.execution;

import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * A utility class that provides methods for enriching method arguments.
 */
public class ExecUtils {

    /**
     * Generate the enriched the method arguments of a method call.<br/>
     * The Object[] index will match the method parameterType[] index.
     *
     * @return the enriched argument values to use
     */
    public static Object[] enrichArguments(Method method, Collection<TestEnricher> enrichers) {
        Object[] values = new Object[method.getParameterTypes().length];
        if (method.getParameterTypes().length == 0) {
            return values;
        }
        for (TestEnricher enricher : enrichers) {
            mergeValues(values, enricher.resolve(method));
        }
        return values;
    }

    /**
     * Called for each {@link TestEnricher} to merge the resolved values into the values array.
     * Since differeent TestEnrichers can resolve different arguments, we need to merge the values.
     * @param values - the current values
     * @param resolvedValues - the values from the last TestEnricher
     */
    private static void mergeValues(Object[] values, Object[] resolvedValues) {
        if (resolvedValues == null || resolvedValues.length == 0) {
            return;
        }
        if (values.length != resolvedValues.length) {
            throw new IllegalStateException("TestEnricher resolved wrong argument count, expected " +
                values.length + " returned " + resolvedValues.length);
        }
        for (int i = 0; i < resolvedValues.length; i++) {
            Object resvoledValue = resolvedValues[i];
            if (resvoledValue != null && values[i] == null) {
                values[i] = resvoledValue;
            }
        }
    }
}
