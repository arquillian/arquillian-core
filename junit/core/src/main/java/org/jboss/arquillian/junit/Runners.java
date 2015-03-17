package org.jboss.arquillian.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.junit.suite.BelongsTo;
import org.jboss.arquillian.junit.suite.Suite;
import org.jboss.arquillian.junit.suite.resolve.ResolveStrategy;
import org.jboss.arquillian.test.spi.TestClass;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

final class Runners {

    public static Runner runners(Class<?> testClass) throws InitializationError {
        RunnerGenerator generator = getGenerator(testClass);
        if (generator != null) {
            return generator.runners(testClass);
        } else {
            return new ArquillianTestRunner(testClass);
        }
    }

    public static Runner runners(TestClass suiteTestClass, Class<?> testClass) throws InitializationError {
        RunnerGenerator generator = getGenerator(testClass);
        if (generator != null) {
            return generator.runners(suiteTestClass, testClass);
        } else {
            return new ArquillianTestRunner(new TestClass(suiteTestClass, testClass));
        }
    }

    public static RunnerGenerator getGenerator(Class<?> testClass) {
        if (testClass.isAnnotationPresent(BelongsTo.class)) {
            return new BelongsToRunnerGenerator();
        }
        if (testClass.isAnnotationPresent(Suite.class)) {
            return new SuiteRunnerGenerator();
        }
        return null;
    }

    public interface RunnerGenerator {
        public Runner runners(Class<?> testClass) throws InitializationError;

        public Runner runners(TestClass suiteTestClass, Class<?> testClass) throws InitializationError;
    }

    public static class BelongsToRunnerGenerator implements RunnerGenerator {

        private static Map<Class<?>, BelongsToProvider> providers = new HashMap<Class<?>, BelongsToProvider>();
        private static Map<Class<?>, TestClass> testClasses = new HashMap<Class<?>, TestClass>();

        public static void clean() {
            providers.clear();
            testClasses.clear();
        }

        @Override
        public Runner runners(Class<?> testClass) throws InitializationError {
            Class<?> wasDiscovered = null;
            Class<?> currentLevel = testClass.getAnnotation(BelongsTo.class).value();
            Class<?> previousLevel = null;
            while(currentLevel != null) {
                if (!providers.containsKey(currentLevel)) {
                    wasDiscovered = currentLevel;
                    providers.put(currentLevel, new BelongsToProvider());
                    testClasses.put(currentLevel, new TestClass(currentLevel));
                }
                if(previousLevel != null) {
                    if(!testClasses.get(previousLevel).hasParent()) {
                        testClasses.put(previousLevel, new TestClass(testClasses.get(currentLevel), previousLevel));
                    }
                    // Only add it the first time we see it, else we get duplicate sub suite definitions
                    if(currentLevel == wasDiscovered) {
                        BelongsToProvider val = providers.get(currentLevel);
                        val.addRunner(new ArquillianSuiteRunner(testClasses.get(previousLevel), providers.get(previousLevel)));
                    }
                }

                previousLevel = currentLevel;
                if(currentLevel.isAnnotationPresent(BelongsTo.class)) {
                    BelongsTo parent = currentLevel.getAnnotation(BelongsTo.class);
                    currentLevel = parent.value();
                } else {
                    break;
                }
            }
            Class<?> nearestParent = testClass.getAnnotation(BelongsTo.class).value();
            BelongsToProvider val = providers.get(nearestParent);
            val.addRunner(new ArquillianTestRunner(new TestClass(testClasses.get(nearestParent), testClass)));

            // If top level was discovered then this was the first time we saw the Suite, else return an empty runner
            // as the actual runner will be part of another sub suite
            if (wasDiscovered == currentLevel) {
                return new ArquillianSuiteRunner(testClasses.get(currentLevel), providers.get(currentLevel));
            } else {
                return new Runner() {

                    @Override
                    public void run(RunNotifier notifier) {
                    }

                    @Override
                    public Description getDescription() {
                        return Description.EMPTY;
                    }
                };
            }
        }

        @Override
        public Runner runners(TestClass suiteTestClass, Class<?> testClass) throws InitializationError {
            throw new UnsupportedOperationException("No parent suite allowed with @BelongsTo");
        }
    }

    public static class BelongsToProvider implements ChildProvider {
        private List<Runner> runners = new ArrayList<Runner>();

        public void addRunner(Runner runner) {
            runners.add(runner);
        }

        @Override
        public List<Runner> children(TestClass testClass) {
            BelongsToRunnerGenerator.clean();
            return runners;
        }
    }

    public static class SuiteRunnerGenerator implements RunnerGenerator {

        @Override
        public Runner runners(Class<?> testClass) throws InitializationError {
            return new ArquillianSuiteRunner(testClass,
                    new SuiteScannerProvider());
        }

        @Override
        public Runner runners(TestClass suiteTestClass, Class<?> testClass) throws InitializationError {
            return new ArquillianSuiteRunner(new TestClass(suiteTestClass, testClass), new SuiteScannerProvider());
        }
    }

    public static class SuiteScannerProvider implements ChildProvider {

        public List<Runner> children(TestClass testClass) {
            if (!testClass.isAnnotationPresent(Suite.class)) {
                throw new IllegalArgumentException("Missing "
                        + Suite.class.getName()
                        + " annotation for given TestClass "
                        + testClass.getName());
            }
            Suite suite = testClass.getAnnotation(Suite.class);
            try {
                List<Runner> result = new ArrayList<Runner>();

                Class<? extends ResolveStrategy> strategyClass = suite.strategy();
                ResolveStrategy strategy = strategyClass.newInstance();

                for (Class<?> resolved : strategy.resolve(suite.value())) {
                    Runner runner = Runners.runners(testClass, resolved);
                    result.add(runner);
                }
                return result;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
