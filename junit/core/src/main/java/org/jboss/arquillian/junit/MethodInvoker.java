package org.jboss.arquillian.junit;

import java.lang.reflect.Method;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.execution.SkippedTestExecutionException;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;

abstract class MethodInvoker {

    void invoke(final TestRunnerAdaptor adaptor, final FrameworkMethod method,
        final Object test) throws Throwable {
        TestResult result = adaptor.test(new TestMethodExecutor() {
            @Override
            public void invoke(Object... parameters) throws Throwable {
                invokeMethod(parameters);
            }

            @Override
            public String getMethodName() {
                return method.getName();
            }

            public Method getMethod() {
                return method.getMethod();
            }

            public Object getInstance() {
                return test;
            }
        });
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            if (result.getStatus() == TestResult.Status.SKIPPED) {
                if (throwable instanceof SkippedTestExecutionException) {
                    result.setThrowable(new AssumptionViolatedException(throwable.getMessage()));
                }
            }
            throw result.getThrowable();
        }
    }

    abstract void invokeMethod(Object... parameters) throws Throwable;
}
