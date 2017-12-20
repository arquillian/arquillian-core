package org.jboss.arquillian.junit;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * ARQ-404 Better reporting when Arquillian fails to initialise
 * <p>
 * Only run first test, ignore the rest
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class InitializationExceptionWithRuleTestCase extends JUnitTestBaseClass {

    @Test
    public void shouldKeepInitializationExceptionBetweenTestCases() throws Exception {
        String exceptionMessage = "TEST_EXCEPTION_BEFORE_SUITE_FAILING";
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        doThrow(new Exception(exceptionMessage)).when(adaptor).beforeSuite();

        Result result =
            run(adaptor, ClassWithArquillianClassAndMethodRule.class, ClassWithArquillianClassAndMethodRule.class);

        Assert.assertFalse(result.wasSuccessful());

        result.getFailures().get(0).getException().printStackTrace();
        Assert.assertEquals(2, result.getFailureCount());
        Assert.assertEquals(exceptionMessage, result.getFailures().get(0).getMessage());

        Assert.assertEquals(exceptionMessage, result.getFailures().get(1).getMessage());

        verify(adaptor, times(0)).afterSuite();
    }
}
