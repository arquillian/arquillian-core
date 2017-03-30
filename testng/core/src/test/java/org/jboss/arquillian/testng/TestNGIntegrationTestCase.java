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
package org.jboss.arquillian.testng;

import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.TestListenerAdapter;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verify the that JUnit integration adaptor fires the expected events even when Handlers are failing.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class TestNGIntegrationTestCase extends TestNGTestBaseClass {
    @Test
    @Ignore("ARQ-582")
    public void shouldNotCallAnyMethodsWithoutLifecycleHandlers() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        when(adaptor.test(isA(TestMethodExecutor.class))).thenReturn(new TestResult(Status.PASSED));

        TestListenerAdapter result = run(adaptor, ArquillianClass1.class);

        Assert.assertTrue(wasSuccessful(result));
        assertCycle(0, Cycle.values());

        assertCycle(1, Cycle.BEFORE_SUITE, Cycle.AFTER_SUITE);
    }

    @Test
    public void shouldCallAllMethods() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        TestListenerAdapter result = run(adaptor, ArquillianClass1.class);

        Assert.assertTrue(wasSuccessful(result));
        assertCycle(1, Cycle.values());

        assertCycle(1, Cycle.BEFORE_SUITE, Cycle.AFTER_SUITE);
    }

    @Test
    public void shouldCallAfterClassWhenBeforeThrowsException() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        throwException(Cycle.BEFORE_CLASS, new Throwable());

        TestListenerAdapter result = run(adaptor, ArquillianClass1.class);
        Assert.assertFalse(wasSuccessful(result));

        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS);
        assertCycle(0, Cycle.BEFORE, Cycle.AFTER, Cycle.TEST);

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldCallAfterWhenBeforeThrowsException() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        throwException(Cycle.BEFORE, new Throwable());

        TestListenerAdapter result = run(adaptor, ArquillianClass1.class);
        Assert.assertFalse(wasSuccessful(result));

        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS, Cycle.BEFORE, Cycle.AFTER);
        assertCycle(0, Cycle.TEST);

        assertCycle(1, Cycle.BEFORE_SUITE, Cycle.AFTER_SUITE);
    }

    @Test
    public void shouldOnlyCallSecondTestWhenFirstBeforeClassThrowsException() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // throwException removes exception marker when thrown
        throwException(Cycle.BEFORE_CLASS, new Throwable());

        TestListenerAdapter result = run(adaptor, ArquillianClass1.class, ArquillianClass2.class);
        Assert.assertFalse(wasSuccessful(result));

        assertCycle(2, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS);
        assertCycle(1, Cycle.BEFORE, Cycle.TEST, Cycle.AFTER);

        assertCycle(1, Cycle.BEFORE_SUITE, Cycle.AFTER_SUITE);
    }

    @Test
    public void shouldOnlyCallBeforeAfterSuiteOnce() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        TestListenerAdapter result = run(adaptor, ArquillianClass1.class, ArquillianClass2.class);
        Assert.assertTrue(wasSuccessful(result));

        assertCycle(1, Cycle.BEFORE_SUITE, Cycle.AFTER_SUITE);
    }

    @Test
    public void shouldCallAllWhenTestThrowsException() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        throwException(Cycle.TEST, new Throwable());

        TestListenerAdapter result = run(adaptor, ArquillianClass1.class);
        Assert.assertFalse(wasSuccessful(result));

        assertCycle(1, Cycle.values());
    }

    @Test
    public void shouldNotCallArquillianWhenNonArquillianClassIsRan() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        TestListenerAdapter result = run(adaptor, NonArquillianClass1.class);
        Assert.assertTrue(wasSuccessful(result));

        assertCycle(0, Cycle.values());
    }

    @Test
    public void shouldNotCallArquillianWhenNonArquillianGroupIsRan() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        TestListenerAdapter result =
            run(new String[] {"non-arq"}, adaptor, NonArquillianClass1.class, ArquillianClass1.class);
        Assert.assertTrue(wasSuccessful(result));

        assertCycle(0, Cycle.values());
    }

    @Test
    @Ignore("ARQ-646")
    public void shouldCallArquillianWhenGroupIsRan() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        TestListenerAdapter result =
            run(new String[] {"arq"}, adaptor, NonArquillianClass1.class, ArquillianClass2.class);
        Assert.assertTrue(wasSuccessful(result));

        assertCycle(1, Cycle.values());
    }

    @Test // workaround for ARQ-646, enable the arquillian group
    public void shouldCallArquillianWhenArquillianGroupIsActive() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        TestListenerAdapter result =
            run(new String[] {"arq", "arquillian"}, adaptor, NonArquillianClass1.class, ArquillianClass2.class);
        Assert.assertTrue(wasSuccessful(result));

        assertCycle(1, Cycle.values());
    }
}