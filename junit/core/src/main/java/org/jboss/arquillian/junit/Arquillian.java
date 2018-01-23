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
package org.jboss.arquillian.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.arquillian.junit.event.AfterRules;
import org.jboss.arquillian.junit.event.BeforeRules;
import org.jboss.arquillian.junit.event.RulesEnrichment;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.execution.SkippedTestExecutionException;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Main Arquillian JUnit runner
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class Arquillian extends BlockJUnit4ClassRunner {
    private TestRunnerAdaptor adaptor;

    public Arquillian(Class<?> testClass) throws InitializationError {
        super(testClass);
        if (State.isRunningInEclipse()) {
            State.runnerStarted();
        }
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> children = super.getChildren();
        // Only sort if InOrder is defined, else keep them in original order returned by parent
        boolean hasDefinedOrder = false;
        for (FrameworkMethod method : children) {
            if (method.getAnnotation(InSequence.class) != null) {
                hasDefinedOrder = true;
            }
        }
        if (hasDefinedOrder) {
            List<FrameworkMethod> sorted = new ArrayList<FrameworkMethod>(children);
            Collections.sort(sorted, new InSequenceSorter());
            return sorted;
        }
        return children;
    }

    @Override
    public void run(final RunNotifier notifier) {
        if (State.hasAnyArquillianRule(this.getTestClass())) {
            throw new RuntimeException(String.format("TestClass: %s contains Arquillian runner and Arquillian Rule."
                + " Arquillian doesn't support @RunWith(Arquillian.class) and ArquillianTestClass or "
                    + "ArquillianTest to use at the same time. You have to decide whether you want use runner:"
                    + " http://arquillian.org/arquillian-core/#how-it-works or rules : http://arquillian.org/arquillian-core/#_how_to_use_it",
                this.getTestClass().getName()));
        }

        if (State.isNotRunningInEclipse()) {
            State.runnerStarted();
        }

        AdaptorManagerWithNotifier adaptorManager = new AdaptorManagerWithNotifier(notifier) {
            protected void setAdaptor(TestRunnerAdaptor testRunnerAdaptor) {
                adaptor = testRunnerAdaptor;
            }

            protected TestRunnerAdaptor getAdaptor() {
                return adaptor;
            }

            protected Description getFailureDescription() {
                return getDescription();
            }
        };
        adaptorManager.initializeAdaptor();
        adaptorManager.prepareDestroyAdaptorProcess();


        // initialization ok, run children
        if (State.hasTestAdaptor()) {
            super.run(notifier);
        }
    }

    /**
     * Override to allow test methods with arguments
     */
    @Override
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic,
        List<Throwable> errors) {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
        for (FrameworkMethod eachTestMethod : methods) {
            eachTestMethod.validatePublicVoid(isStatic, errors);
        }
    }

    /**
     * Override @ClassRule retrieval to prevent from running them inside of a container
     */
    @Override
    protected List<TestRule> classRules() {
        List<JUnitClassRulesFilter> junitClassRulesFilters =
            new JavaSPILoader().all(Arquillian.class.getClassLoader(), JUnitClassRulesFilter.class);
        List<TestRule> result = super.classRules();

        if (!junitClassRulesFilters.isEmpty()) {
            for (final JUnitClassRulesFilter junitClassRulesFilter : junitClassRulesFilters) {
                result = junitClassRulesFilter.filter(result);
            }
        }
        return result;
    }

    /*
    * Override BeforeClass/AfterClass and Before/After handling.
    * 
    * Let super create the Before/After chain against a EmptyStatement so our newly created Statement
    * only contains the method that are of interest to us(@Before..etc). 
    * They can then optionally be executed if we get expected callback.
    * 
    */

    @Override
    protected Statement withBeforeClasses(final Statement originalStatement) {
        final Statement onlyBefores = super.withBeforeClasses(new EmptyStatement());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                adaptor.beforeClass(
                    Arquillian.this.getTestClass().getJavaClass(),
                    new StatementLifecycleExecutor(onlyBefores));
                originalStatement.evaluate();
            }
        };
    }

    @Override
    protected Statement withAfterClasses(final Statement originalStatement) {
        final Statement onlyAfters = super.withAfterClasses(new EmptyStatement());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                multiExecute
                    (
                        originalStatement,
                        new Statement() {
                            @Override
                            public void evaluate() throws Throwable {
                                adaptor.afterClass(
                                    Arquillian.this.getTestClass().getJavaClass(),
                                    new StatementLifecycleExecutor(onlyAfters));
                            }
                        }
                    );
            }
        };
    }

    @Override
    protected Statement withBefores(final FrameworkMethod method, final Object target,
        final Statement originalStatement) {
        final Statement onlyBefores = super.withBefores(method, target, new EmptyStatement());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                adaptor.before(
                    target,
                    method.getMethod(),
                    new StatementLifecycleExecutor(onlyBefores));
                originalStatement.evaluate();
            }
        };
    }

    @Override
    protected Statement withAfters(final FrameworkMethod method, final Object target, final Statement originalStatement) {
        final Statement onlyAfters = super.withAfters(method, target, new EmptyStatement());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                multiExecute
                    (
                        originalStatement,
                        new Statement() {
                            @Override
                            public void evaluate() throws Throwable {
                                adaptor.after(
                                    target,
                                    method.getMethod(),
                                    new StatementLifecycleExecutor(onlyAfters));
                            }
                        }
                    );
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Statement methodBlock(final FrameworkMethod method) {
        Object temp;
        try {
            temp = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable e) {
            return new Fail(e);
        }
        final Object test = temp;
        try {
            Method withRules = BlockJUnit4ClassRunner.class.getDeclaredMethod("withRules",
                FrameworkMethod.class, Object.class, Statement.class);
            withRules.setAccessible(true);

            Statement statement = methodInvoker(method, test);
            statement = possiblyExpectingExceptions(method, test, statement);
            statement = withPotentialTimeout(method, test, statement);

            Statement arounds = withBefores(method, test, statement);
            arounds = withAfters(method, test, arounds);
            final Statement stmtWithLifecycle = arounds;

            adaptor.fireCustomLifecycle(
                new RulesEnrichment(test, getTestClass(), method.getMethod(), LifecycleMethodExecutor.NO_OP));

            final Statement stmtWithRules = (Statement) withRules.invoke(this, method, test, arounds);
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    State.caughtExceptionAfterJunit(null);
                    final AtomicInteger integer = new AtomicInteger();
                    List<Throwable> exceptions = new ArrayList<Throwable>();

                    try {
                        adaptor.fireCustomLifecycle(
                            new BeforeRules(test, getTestClass(), stmtWithRules, method.getMethod(),
                                new LifecycleMethodExecutor() {
                                    @Override
                                    public void invoke() throws Throwable {
                                        integer.incrementAndGet();
                                        stmtWithRules.evaluate();
                                    }
                                }));
                        // If AroundRules (includes lifecycles) were not executed, invoke only lifecycles+test
                        if (integer.get() == 0) {
                            try {
                                stmtWithLifecycle.evaluate();
                            } catch (Throwable t) {
                                State.caughtExceptionAfterJunit(t);
                                throw t;
                            }
                        }
                    } catch (Throwable t) {
                        State.caughtExceptionAfterJunit(t);
                        exceptions.add(t);
                    } finally {
                        try {
                            adaptor.fireCustomLifecycle(
                                new AfterRules(test, method.getMethod(), LifecycleMethodExecutor.NO_OP));
                        } catch (Throwable t) {
                            State.caughtExceptionAfterJunit(t);
                            exceptions.add(t);
                        }
                    }
                    if (exceptions.isEmpty()) {
                        return;
                    }
                    if (exceptions.size() == 1) {
                        throw exceptions.get(0);
                    }
                    throw new MultipleFailureException(exceptions);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Could not create statement", e);
        }
    }

    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                new MethodInvoker() {
                    void invokeMethod(Object... parameters) throws Throwable {
                        try {
                            method.invokeExplosively(test, parameters);
                        } catch (Throwable e) {
                            // Force a way to return the thrown Exception from the Container to the client.
                            State.caughtTestException(e);
                            throw e;
                        }
                    }
                }.invoke(adaptor, method, test);
            }
        };
    }

    /**
     * A helper to safely execute multiple statements in one.<br/>
     * <p>
     * Will execute all statements even if they fail, all exceptions will be kept. If multiple {@link Statement}s
     * fail, a {@link MultipleFailureException} will be thrown.
     *
     * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
     * @version $Revision: $
     */
    private void multiExecute(Statement... statements) throws Throwable {
        List<Throwable> exceptions = new ArrayList<Throwable>();
        for (Statement command : statements) {
            try {
                command.evaluate();
            } catch (Throwable e) {
                exceptions.add(e);
            }
        }
        if (exceptions.isEmpty()) {
            return;
        }
        if (exceptions.size() == 1) {
            throw exceptions.get(0);
        }
        throw new MultipleFailureException(exceptions);
    }

    private static class EmptyStatement extends Statement {
        @Override
        public void evaluate() throws Throwable {
        }
    }

    private static class StatementLifecycleExecutor implements LifecycleMethodExecutor {
        private Statement statement;

        public StatementLifecycleExecutor(Statement statement) {
            this.statement = statement;
        }

        public void invoke() throws Throwable {
            statement.evaluate();
        }
    }
}
