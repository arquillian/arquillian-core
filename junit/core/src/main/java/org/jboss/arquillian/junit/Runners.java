package org.jboss.arquillian.junit;

import org.jboss.arquillian.junit.suite.Suite;
import org.jboss.arquillian.test.spi.TestClass;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;


public final class Runners {

    public static Runner runners(Class<?> testClass) throws InitializationError {
        if(testClass.isAnnotationPresent(Suite.class)) {
            return new ArquillianSuiteRunner(testClass);
        }
        return new ArquillianTestRunner(testClass);
    }

    public static Runner runners(TestClass suiteTestClass, Class<?> testClass) throws InitializationError {
        if(testClass.isAnnotationPresent(Suite.class)) {
            return new ArquillianSuiteRunner(new TestClass(suiteTestClass, testClass));
        }
        return new ArquillianTestRunner(new TestClass(suiteTestClass, testClass));
    }
}
