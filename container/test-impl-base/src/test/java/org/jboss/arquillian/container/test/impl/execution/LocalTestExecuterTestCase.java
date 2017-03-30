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
import java.util.List;
import org.jboss.arquillian.container.test.impl.execution.event.LocalExecutionEvent;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * InContainerExecuterTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalTestExecuterTestCase extends AbstractContainerTestTestBase {
    @Mock
    private TestMethodExecutor testExecutor;

    @Mock
    private ServiceLoader serviceLoader;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(LocalTestExecuter.class);
    }

    @Test
    public void shouldReturnPassed() throws Throwable {
        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);

        Mockito.when(testExecutor.getInstance()).thenReturn(this);
        Mockito.when(testExecutor.getMethod()).thenReturn(
            getTestMethod("shouldReturnPassed"));

        fire(new LocalExecutionEvent(testExecutor));

        TestResult result = getManager().resolve(TestResult.class);
        Assert.assertNotNull(
            "Should have set result",
            result);

        Assert.assertEquals(
            "Should have passed test",
            TestResult.Status.PASSED,
            result.getStatus());

        Assert.assertNull(
            "Should not have set cause",
            result.getThrowable());
    }

    @Test
    public void shouldReturnFailedOnException() throws Throwable {
        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);

        Exception exception = new Exception();

        Mockito.when(testExecutor.getInstance()).thenReturn(this);
        Mockito.when(testExecutor.getMethod()).thenReturn(
            getTestMethod("shouldReturnFailedOnException"));
        Mockito.doThrow(exception).when(testExecutor).invoke();

        fire(new LocalExecutionEvent(testExecutor));

        TestResult result = getManager().resolve(TestResult.class);
        Assert.assertNotNull(
            "Should have set result",
            result);

        Assert.assertEquals(
            "Should have failed test",
            TestResult.Status.FAILED,
            result.getStatus());

        Assert.assertEquals(
            "Should have set failed cause",
            exception,
            result.getThrowable());
    }

    private Method getTestMethod(String name) throws Exception {
        return this.getClass().getMethod(name);
    }
}
