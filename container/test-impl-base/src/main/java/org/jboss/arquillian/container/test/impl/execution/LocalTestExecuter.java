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
package org.jboss.arquillian.container.test.impl.execution;

import java.lang.reflect.Method;
import java.util.Collection;
import org.jboss.arquillian.container.test.impl.execution.event.LocalExecutionEvent;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.annotation.TestScoped;

/**
 * A Handler for executing the Test Method.<br/>
 * <br/>
 * <b>Imports:</b><br/>
 * {@link Injector}<br/>
 * {@link ServiceLoader}<br/>
 * <br/>
 * <b>Exports:</b><br/>
 * {@link TestResult}<br/>
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LocalTestExecuter {
    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    @TestScoped
    private InstanceProducer<TestResult> testResult;

    public void execute(@Observes LocalExecutionEvent event) throws Exception {
        TestResult result = new TestResult();
        try {
            event.getExecutor().invoke(
                enrichArguments(
                    event.getExecutor().getMethod(),
                    serviceLoader.get().all(TestEnricher.class)));
            result.setStatus(Status.PASSED);
        } catch (Throwable e) {
            result.setStatus(Status.FAILED);
            result.setThrowable(e);
        } finally {
            result.setEnd(System.currentTimeMillis());
        }
        testResult.set(result);
    }

    /**
     * Enrich the method arguments of a method call.<br/>
     * The Object[] index will match the method parameterType[] index.
     *
     * @return the argument values
     */
    private Object[] enrichArguments(Method method, Collection<TestEnricher> enrichers) {
        Object[] values = new Object[method.getParameterTypes().length];
        if (method.getParameterTypes().length == 0) {
            return values;
        }
        for (TestEnricher enricher : enrichers) {
            mergeValues(values, enricher.resolve(method));
        }
        return values;
    }

    private void mergeValues(Object[] values, Object[] resolvedValues) {
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
