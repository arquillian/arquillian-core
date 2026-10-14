/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit5.container;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;

/**
 * Hands out {@link TestRunnerAdaptor} instances that record every lifecycle call they receive into one shared,
 * globally ordered log.
 *
 * <p>Mockito mocks cannot answer the questions the manager-scope tests ask. Those tests need to know <em>which</em>
 * adaptor - and therefore which Arquillian {@code Manager} - served a given test class, and in what order calls
 * happened across adaptors. Recording into a single sequence-numbered log answers both, and does so from several
 * threads at once, which cross-thread Mockito verification does not do cleanly.</p>
 *
 * <p>Pass {@link #supplier()} to
 * {@link JUnitTestBaseClass#run(Supplier, java.util.Map, Class[]) JUnitTestBaseClass.run(...)}: every
 * {@code TestRunnerAdaptorBuilder.build()} call then yields a fresh recording adaptor, so counting adaptors counts
 * managers.</p>
 */
public final class AdaptorRecorder {

    /**
     * The lifecycle calls the manager-scope tests care about.
     */
    public enum Event {
        BEFORE_SUITE, BEFORE_CLASS, BEFORE, TEST, AFTER, AFTER_CLASS, AFTER_SUITE, SHUTDOWN
    }

    /**
     * One recorded lifecycle call.
     *
     * @param sequence  position in the global order of all calls across all adaptors
     * @param adaptorId identifies the adaptor, and therefore the manager, that received the call
     * @param testClass the class the call relates to, or {@code null} for suite-level calls
     */
    public static final class Record {
        private final int sequence;

        private final int adaptorId;

        private final Event event;

        private final Class<?> testClass;

        private final String threadName;

        private Record(int sequence, int adaptorId, Event event, Class<?> testClass) {
            this.sequence = sequence;
            this.adaptorId = adaptorId;
            this.event = event;
            this.testClass = testClass;
            this.threadName = Thread.currentThread().getName();
        }

        public int getSequence() {
            return sequence;
        }

        public int getAdaptorId() {
            return adaptorId;
        }

        public Class<?> getTestClass() {
            return testClass;
        }

        public String getThreadName() {
            return threadName;
        }

        @Override
        public String toString() {
            return String.format("#%d adaptor=%d %s %s [%s]", sequence, adaptorId, event,
                testClass == null ? "-" : testClass.getSimpleName(), threadName);
        }
    }

    private final AtomicInteger sequence = new AtomicInteger();

    private final AtomicInteger adaptorIds = new AtomicInteger();

    private final List<Record> records = new CopyOnWriteArrayList<>();

    private volatile Supplier<? extends Throwable> beforeSuiteFailure;

    private volatile Supplier<? extends Exception> afterSuiteFailure;

    private volatile Supplier<? extends RuntimeException> shutdownFailure;

    /**
     * Makes every adaptor handed out from here fail its {@code beforeSuite()} with a freshly supplied throwable.
     *
     * <p>{@code Error}s are allowed, not just exceptions: a container boot that dies on a {@code NoClassDefFoundError}
     * has to be cached and reported like any other failed boot.</p>
     *
     * @param failure supplies the exception or error to throw; called once per adaptor that reaches
     *                {@code beforeSuite()}
     *
     * @return this recorder
     */
    public AdaptorRecorder failBeforeSuiteWith(Supplier<? extends Throwable> failure) {
        this.beforeSuiteFailure = failure;
        return this;
    }

    /**
     * Makes every adaptor handed out from here fail its {@code afterSuite()} with a freshly supplied exception.
     *
     * @param failure supplies the exception to throw; called once per adaptor that reaches {@code afterSuite()}
     *
     * @return this recorder
     */
    public AdaptorRecorder failAfterSuiteWith(Supplier<? extends Exception> failure) {
        this.afterSuiteFailure = failure;
        return this;
    }

    /**
     * Makes every adaptor handed out from here fail its {@code shutdown()} with a freshly supplied exception.
     * {@code shutdown()} declares no checked exceptions, so this one has to be unchecked.
     *
     * @param failure supplies the exception to throw; called once per adaptor that reaches {@code shutdown()}
     *
     * @return this recorder
     */
    public AdaptorRecorder failShutdownWith(Supplier<? extends RuntimeException> failure) {
        this.shutdownFailure = failure;
        return this;
    }

    /**
     * @return a factory producing one new recording adaptor per call, all logging into this recorder
     */
    public Supplier<TestRunnerAdaptor> supplier() {
        return RecordingAdaptor::new;
    }

    /**
     * @return how many adaptors - and therefore how many Arquillian managers - were created
     */
    public int getAdaptorCount() {
        return adaptorIds.get();
    }

    /**
     * @return how many times {@code event} was recorded across all adaptors
     */
    public long count(Event event) {
        return records.stream().filter(r -> r.event == event).count();
    }

    /**
     * @return how many times {@code event} was recorded on the given adaptor
     */
    public long count(int adaptorId, Event event) {
        return records.stream().filter(r -> r.adaptorId == adaptorId && r.event == event).count();
    }

    private List<Integer> adaptorIdsFor(Event event, Class<?> testClass) {
        return records.stream()
            .filter(r -> r.event == event && testClass.equals(r.testClass))
            .map(Record::getAdaptorId)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * @return the one adaptor that recorded {@code event} for {@code testClass}
     *
     * @throws AssertionError if no adaptor, or more than one, did
     */
    public int adaptorIdFor(Event event, Class<?> testClass) {
        List<Integer> ids = adaptorIdsFor(event, testClass);
        if (ids.size() != 1) {
            throw new AssertionError(
                String.format("Expected exactly one adaptor to record %s for %s, but found %s.%n%s",
                    event, testClass.getSimpleName(), ids, describe()));
        }
        return ids.get(0);
    }

    /**
     * @return the sequence number of the single occurrence of {@code event} on {@code adaptorId}
     *
     * @throws AssertionError if it did not happen exactly once
     */
    public int sequenceOf(int adaptorId, Event event) {
        List<Record> matches = records.stream()
            .filter(r -> r.adaptorId == adaptorId && r.event == event)
            .collect(Collectors.toList());
        if (matches.size() != 1) {
            throw new AssertionError(String.format("Expected %s exactly once on adaptor %d, but found %d.%n%s",
                event, adaptorId, matches.size(), describe()));
        }
        return matches.get(0).sequence;
    }

    /**
     * @return the distinct test classes {@code event} was recorded for, in the order first seen
     */
    public List<Class<?>> testClassesFor(Event event) {
        return records.stream()
            .filter(r -> r.event == event && r.testClass != null)
            .map(Record::getTestClass)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * @return the adaptor ids, ordered by when each adaptor received {@link Event#BEFORE_SUITE}
     */
    public List<Integer> adaptorIdsInStartOrder() {
        return records.stream()
            .filter(r -> r.event == Event.BEFORE_SUITE)
            .sorted(Comparator.comparingInt(Record::getSequence))
            .map(Record::getAdaptorId)
            .collect(Collectors.toList());
    }

    /**
     * @return the distinct names of the threads that ran {@link Event#TEST} for {@code testClass}
     */
    public List<String> testThreadsFor(Class<?> testClass) {
        return records.stream()
            .filter(r -> r.event == Event.TEST && testClass.equals(r.testClass))
            .map(Record::getThreadName)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * @return the whole log, one record per line, for inclusion in assertion failure messages
     */
    public String describe() {
        return records.stream().map(Record::toString).collect(Collectors.joining(System.lineSeparator()));
    }

    private void record(int adaptorId, Event event, Class<?> testClass) {
        records.add(new Record(sequence.incrementAndGet(), adaptorId, event, testClass));
    }

    private final class RecordingAdaptor implements TestRunnerAdaptor {
        private final int id = adaptorIds.incrementAndGet();

        @Override
        public void beforeSuite() throws Exception {
            record(id, Event.BEFORE_SUITE, null);
            Supplier<? extends Throwable> failure = beforeSuiteFailure;
            if (failure == null) {
                return;
            }
            Throwable thrown = failure.get();
            if (thrown instanceof Error) {
                throw (Error) thrown;
            }
            if (thrown instanceof Exception) {
                throw (Exception) thrown;
            }
            throw new IllegalArgumentException("Cannot throw " + thrown + " from beforeSuite()", thrown);
        }

        @Override
        public void afterSuite() throws Exception {
            record(id, Event.AFTER_SUITE, null);
            Supplier<? extends Exception> failure = afterSuiteFailure;
            if (failure != null) {
                throw failure.get();
            }
        }

        @Override
        public void beforeClass(Class<?> testClass, LifecycleMethodExecutor executor) throws Exception {
            record(id, Event.BEFORE_CLASS, testClass);
            invoke(executor);
        }

        @Override
        public void afterClass(Class<?> testClass, LifecycleMethodExecutor executor) throws Exception {
            record(id, Event.AFTER_CLASS, testClass);
            invoke(executor);
        }

        @Override
        public void before(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception {
            record(id, Event.BEFORE, testInstance.getClass());
            invoke(executor);
        }

        @Override
        public void after(Object testInstance, Method testMethod, LifecycleMethodExecutor executor) throws Exception {
            record(id, Event.AFTER, testInstance.getClass());
            invoke(executor);
        }

        @Override
        public TestResult test(TestMethodExecutor testMethodExecutor) {
            record(id, Event.TEST, testMethodExecutor.getInstance().getClass());
            try {
                testMethodExecutor.invoke();
                return TestResult.passed();
            } catch (Throwable t) {
                return TestResult.failed(t);
            }
        }

        @Override
        public <T extends TestLifecycleEvent> void fireCustomLifecycle(T event) {
            // Nothing to record: the manager-scope tests do not distinguish custom lifecycle events, and firing the
            // event's executor here would run the test method a second time.
        }

        @Override
        public void shutdown() {
            record(id, Event.SHUTDOWN, null);
            Supplier<? extends RuntimeException> failure = shutdownFailure;
            if (failure != null) {
                throw failure.get();
            }
        }

        @Override
        public String toString() {
            return "RecordingAdaptor#" + id;
        }

        private void invoke(LifecycleMethodExecutor executor) throws Exception {
            try {
                executor.invoke();
            } catch (Exception | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new IllegalStateException(t);
            }
        }
    }
}
