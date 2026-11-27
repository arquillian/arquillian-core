/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021 Red Hat Inc. and/or its affiliates and other contributors
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.junit5.IdentifiedTestException;
import org.jboss.arquillian.junit5.container.fixtures.ClassWithAbortingTemplates;
import org.jboss.arquillian.junit5.container.fixtures.ClassWithAssumptionInBeforeAll;
import org.jboss.arquillian.junit5.container.fixtures.ClassWithFailedTemplateAndAbortInAfterAll;
import org.jboss.arquillian.junit5.container.fixtures.SameMethodNameNestedFixture;
import org.jboss.arquillian.junit5.container.fixtures.TestScenarios;
import org.jboss.arquillian.test.spi.TestResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

public class JUnitJupiterTestRunnerTestCase {

  @Test
  public void shouldReturnExceptionToClientIfFailingOnWrongExceptionThrown() {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldFailExpectedWrongException");

    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertNotNull(result.getThrowable());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
  }

  @Test
  public void shouldReportFailedToClientIfIncompleteExecutionExceptionThrown() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldFailOnIncompleteExecution");

    // JUnit only treats TestAbortedException as an abort, so other IncompleteExecutionExceptions remain failures
    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
  }

  @Test
  public void shouldReportSkippedToClientIfAssumptionFailsAtClassLevel() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(ClassWithAssumptionInBeforeAll.class, "test");

    Assertions.assertEquals(TestResult.Status.SKIPPED, result.getStatus());
    Assertions.assertInstanceOf(TestAbortedException.class, result.getThrowable());
  }

  @Test
  public void shouldReportContainerFailureEvenIfAnotherContainerAbortsLater() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(ClassWithFailedTemplateAndAbortInAfterAll.class, "parameterizedTest");

    // The later abort of the class container must not replace the earlier failure of the template container, or the
    // client would rethrow the abort and report the test as aborted rather than failed
    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertFalse(result.getThrowable() instanceof TestAbortedException,
        () -> "Expected the template failure, but got " + result.getThrowable());
  }

  @Test
  public void shouldNotReturnExceptionToClientIfAssumptionPassing() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldPassOnAssumption");

    Assertions.assertEquals(TestResult.Status.PASSED, result.getStatus());
    Assertions.assertNull(result.getThrowable());
  }

  @Test
  public void shouldReportSkippedToClientIfAssumptionFailing() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldSkipOnAssumption");

    Assertions.assertEquals(TestResult.Status.SKIPPED, result.getStatus());
    Assertions.assertNotNull(result.getThrowable());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
    Assertions.assertInstanceOf(TestAbortedException.class, result.getThrowable().getCause());
  }

  @Test
  public void shouldReportFailedToClientIfAssumptionFailsAndAfterThrows() throws Exception {
    TestScenarios.exceptionThrownInAfter = new Exception("Not expected");

    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldSkipOnAssumption");

    // The test aborted, but the lifecycle failed, so the overall result must not be downgraded to skipped
    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
  }

  @Test
  public void shouldReportSkippedOnlyForTheAbortedRepetition() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(ClassWithAbortingTemplates.class, "repeatedTest");

    // Only the aborted repetition is collected, under its own invocation, so the client can tell it apart
    Assertions.assertEquals(TestResult.Status.SKIPPED, result.getStatus());
    Map<String, Throwable> collected = collectedExceptions(result);
    Assertions.assertEquals(Set.of(invocationId("repeatedTest(org.junit.jupiter.api.RepetitionInfo)", 2)), collected.keySet());
    Assertions.assertInstanceOf(TestAbortedException.class, collected.values().iterator().next());
  }

  @Test
  public void shouldReportSkippedOnlyForTheAbortedParameterizedInvocation() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(ClassWithAbortingTemplates.class, "parameterizedTest");

    Assertions.assertEquals(TestResult.Status.SKIPPED, result.getStatus());
    Map<String, Throwable> collected = collectedExceptions(result);
    Assertions.assertEquals(Set.of(invocationId("parameterizedTest(java.lang.String)", 2)), collected.keySet());
    Assertions.assertInstanceOf(TestAbortedException.class, collected.values().iterator().next());
  }

  @Test
  public void shouldReportFailedButKeepTheAbortWhenAnotherRepetitionFails() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(ClassWithAbortingTemplates.class, "abortingAndFailingTest");

    // The template as a whole failed, but the abort of repetition 1 must still be collected as an abort
    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Map<String, Throwable> collected = collectedExceptions(result);
    String template = "abortingAndFailingTest(org.junit.jupiter.api.RepetitionInfo)";
    Assertions.assertEquals(Set.of(invocationId(template, 1), invocationId(template, 2)), collected.keySet());
    Assertions.assertInstanceOf(TestAbortedException.class, collected.get(invocationId(template, 1)));
    Assertions.assertInstanceOf(AssertionError.class, collected.get(invocationId(template, 2)));
  }

  @Test
  public void shouldNotReturnExceptionToClientIfExpectedRulePassing() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldPassOnException");

    Assertions.assertEquals(TestResult.Status.PASSED, result.getStatus());
    Assertions.assertNull(result.getThrowable());
  }

  @Test
  public void shouldReturnExceptionToClientIfExpectedRuleFailing() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldFailOnException");

    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertNotNull(result.getThrowable());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
  }

  @Test
  public void shouldReturnExceptionThrownInBeforeToClientWhenTestFails() throws Exception {
    Exception expectedException = new Exception("Expected");
    TestScenarios.exceptionThrownInBefore = expectedException;

    Exception unexpectedException = new Exception("Not expected");
    TestScenarios.exceptionThrownInAfter = unexpectedException;

    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldFailOnException");

    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
  }

  @Test
  public void shouldReturnAssertionErrorToClientWhenAfterThrowsException() throws Exception {
    Exception unexpectedException = new Exception("Not expected");
    TestScenarios.exceptionThrownInAfter = unexpectedException;

    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldFailOnException");

    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertNotNull(result.getThrowable());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
  }

  @Test
  public void shouldReturnExceptionThrownInAfterClientWhenTestSucceeds() throws Exception {
    Exception expectedException = new Exception("Expected");
    TestScenarios.exceptionThrownInAfter = expectedException;

    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldSucceed");

    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
  }

  @Test
  public void shouldNotReturnExceptionToClientIfAssumptionIsPassingInNestedClass() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.NestedTestScenarios.class, "shouldPassOnAssumptionInNested");

    Assertions.assertEquals(TestResult.Status.PASSED, result.getStatus());
    Assertions.assertNull(result.getThrowable());
  }

  @Test
  public void shouldRunOnlyTheMethodOfTheGivenClassWhenNestedClassesShareItsName() {
    assertRunsOnly(SameMethodNameNestedFixture.class);
    assertRunsOnly(SameMethodNameNestedFixture.InnerTests.class);
    assertRunsOnly(SameMethodNameNestedFixture.InnerTests.DeeperTests.class);
  }

  private static Map<String, Throwable> collectedExceptions(TestResult result) {
    Assertions.assertInstanceOf(IdentifiedTestException.class, result.getThrowable());
    return ((IdentifiedTestException) result.getThrowable()).getCollectedExceptions();
  }

  private static String invocationId(String template, int invocation) {
    return "[engine:junit-jupiter]/[class:" + ClassWithAbortingTemplates.class.getName() + "]"
        + "/[test-template:" + template + "]/[test-template-invocation:#" + invocation + "]";
  }

  private static void assertRunsOnly(Class<?> testClass) {
    SameMethodNameNestedFixture.INVOKED.clear();

    TestResult result = new JUnitJupiterTestRunner().execute(testClass, "test");

    Assertions.assertEquals(TestResult.Status.PASSED, result.getStatus(), () -> String.valueOf(result.getThrowable()));
    Assertions.assertEquals(List.of(testClass), SameMethodNameNestedFixture.INVOKED);
  }
}
