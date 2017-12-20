package org.jboss.arquillian.junit;


import org.jboss.arquillian.junit.event.AfterRules;
import org.jboss.arquillian.junit.event.BeforeRules;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
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
public class JUnitIntegrationWithRuleTestCase extends JUnitTestBaseClass {

    @Test
    public void shouldCallBeforeClassAndAfterClassWithoutLifecycleHandlers() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        when(adaptor.test(isA(TestMethodExecutor.class))).thenReturn(TestResult.passed());

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRule.class);

        Assert.assertTrue(result.wasSuccessful());

        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS);
        assertCycle(0, Cycle.BEFORE, Cycle.AFTER, Cycle.AFTER_RULE, Cycle.BEFORE_RULE);

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldCallAllMethods() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRule.class);

        Assert.assertTrue(result.wasSuccessful());
        assertCycle(1, Cycle.basics());

        verify(adaptor, times(1)).fireCustomLifecycle(isA(BeforeRules.class));
        verify(adaptor, times(1)).fireCustomLifecycle(isA(AfterRules.class));

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldCallAfterClassWhenBeforeThrowsException() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        throwException(Cycle.BEFORE_CLASS, new Throwable());

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRule.class);
        Assert.assertFalse(result.wasSuccessful());

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

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRule.class);
        Assert.assertFalse(result.wasSuccessful());

        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS, Cycle.BEFORE, Cycle.AFTER);
        assertCycle(0, Cycle.TEST);

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldOnlyCallBeforeAfterSuiteOnce() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        Result result =
            run(adaptor, ClassWithArquillianClassAndMethodRule.class, ClassWithArquillianClassAndMethodRule.class,
                ClassWithArquillianClassAndMethodRule.class, ClassWithArquillianClassAndMethodRule.class);
        Assert.assertTrue(result.wasSuccessful());

        verify(adaptor, times(4)).beforeSuite();
        verify(adaptor, times(4)).afterSuite();
    }

    /*
     * ARQ-391, After not called when Error's are thrown, e.g. AssertionError
     */
    @Test
    public void shouldCallAllWhenTestThrowsException() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        throwException(Cycle.TEST, new Throwable());

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRule.class);
        Assert.assertFalse(result.wasSuccessful());

        assertCycle(1, Cycle.basics());

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldWorkWithTimeout() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRuleWithTimeout.class);

        Assert.assertFalse(result.wasSuccessful());
        Assert.assertTrue(result.getFailures().get(0).getMessage().contains("timed out"));
        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.BEFORE, Cycle.AFTER, Cycle.AFTER_CLASS);

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldWorkWithExpectedExceptionRule() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRuleWithExpectedExceptionRule.class);

        Assert.assertTrue(result.wasSuccessful());
        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.BEFORE, Cycle.TEST, Cycle.AFTER, Cycle.AFTER_CLASS);

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldWorkWithExpectedException() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRuleWithExpectedException.class);

        Assert.assertTrue(result.wasSuccessful());
        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.BEFORE, Cycle.TEST, Cycle.AFTER, Cycle.AFTER_CLASS);

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldWorkWithAssume() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        executeAllLifeCycles(adaptor);
        final List<Failure> assumptionFailure = new ArrayList<Failure>();
        Result result = run(adaptor, new RunListener() {
            @Override
            public void testAssumptionFailure(Failure failure) {
                assumptionFailure.add(failure);
            }
        }, ClassWithArquillianClassAndMethodRuleWithAssume.class);

        Assert.assertEquals(1, assumptionFailure.size());
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertEquals(0, result.getFailureCount());
        Assert.assertEquals(0, result.getIgnoreCount());
        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.BEFORE, Cycle.AFTER, Cycle.AFTER_CLASS);

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldThrowMultipleExceptionsWhenBeforeAndAfterThrowException() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRuleWithExceptionInBeforeAndAfter.class);

        Assert.assertFalse(result.wasSuccessful());
        Assert.assertEquals(2, result.getFailureCount());
        Assert.assertTrue(result.getFailures().get(0).getMessage().equals("BeforeException"));
        Assert.assertTrue(result.getFailures().get(1).getMessage().equals("AfterException"));
        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.BEFORE, Cycle.AFTER, Cycle.AFTER_CLASS);
        assertCycle(0, Cycle.TEST);

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldCallAfterRuleIfFailureInBeforeRule() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRuleWithExceptionInBeforeRule.class);

        Assert.assertFalse(result.wasSuccessful());
        Assert.assertEquals(1, result.getFailureCount());
        Assert.assertTrue(result.getFailures().get(0).getMessage().equals("BeforeRuleException"));
        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS);
        assertCycle(0, Cycle.BEFORE, Cycle.TEST, Cycle.AFTER);

        verify(adaptor, times(1)).fireCustomLifecycle(isA(BeforeRules.class));
        verify(adaptor, times(1)).fireCustomLifecycle(isA(AfterRules.class));
        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldCallAfterRuleIfFailureInAfterRule() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRuleWithExceptionInAfterRule.class);

        Assert.assertFalse(result.wasSuccessful());
        Assert.assertEquals(1, result.getFailureCount());
        Assert.assertTrue(result.getFailures().get(0).getMessage().equals("AfterRuleException"));
        assertCycle(1, Cycle.basics());

        verify(adaptor, times(1)).fireCustomLifecycle(isA(BeforeRules.class));
        verify(adaptor, times(1)).fireCustomLifecycle(isA(AfterRules.class));
        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldThrowMultipleExceptionIfFailureInBeforeAndAfterRule() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);

        doAnswer(new ThrowsException(new RuntimeException("AfterRuleException"))).when(adaptor)
            .fireCustomLifecycle(isA(AfterRules.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).fireCustomLifecycle(isA(BeforeRules.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).beforeClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor).afterClass(any(Class.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor)
            .before(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new ExecuteLifecycle()).when(adaptor)
            .after(any(Object.class), any(Method.class), any(LifecycleMethodExecutor.class));
        doAnswer(new TestExecuteLifecycle(TestResult.passed())).when(adaptor).test(any(TestMethodExecutor.class));

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRuleWithExceptionInAfterAndAfterRule.class);

        Assert.assertFalse(result.wasSuccessful());

        Assert.assertEquals(2, result.getFailureCount());
        Assert.assertTrue(result.getFailures().get(0).getMessage().equals("AfterException"));

        Assert.assertTrue(result.getFailures().get(1).getMessage().equals("AfterRuleException"));
        assertCycle(1, Cycle.basics());

        verify(adaptor, times(1)).fireCustomLifecycle(isA(BeforeRules.class));
        verify(adaptor, times(1)).fireCustomLifecycle(isA(AfterRules.class));
        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }

    @Test
    public void shouldThrowExceptionIfTestClassContainsArquillianRunnerAndRule() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianClassAndMethodRuleAndWithArquillianRunner.class);

        Assert.assertFalse(result.wasSuccessful());
        assertCycle(0, Cycle.basics());

        Assert.assertTrue(result.getFailures().get(0).getMessage().equals(
            "TestClass: org.jboss.arquillian.junit.ClassWithArquillianClassAndMethodRuleAndWithArquillianRunner contains "
                + "Arquillian runner and Arquillian Rule. Arquillian doesn't support @RunWith(Arquillian.class) and "
                + "ArquillianTestClass or ArquillianTest to use at the same time. You have to decide whether you want use "
                + "runner: http://arquillian.org/arquillian-core/#how-it-works or "
                + "rules : http://arquillian.org/arquillian-core/#_how_to_use_it"));
    }

    @Test
    public void shouldThrowExceptionIfTestClassContainsOnlyArquillianTestRule() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        Result result = run(adaptor, ClassWithArquillianMethodRule.class);

        Assert.assertFalse(result.wasSuccessful());
        assertCycle(1, Cycle.BEFORE_CLASS, Cycle.AFTER_CLASS);
        assertCycle(0, Cycle.BEFORE, Cycle.BEFORE_RULE, Cycle.AFTER_RULE, Cycle.AFTER);

        Assert.assertTrue(result.getFailures().get(0).getMessage().equals(
            "arquillian not initialized. Please make sure to define `ArquillianTestClass` Rule in your testclass. "
                + "This could be one of the reason for arquillian not to be initialized."));
    }
}
