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

import org.jboss.arquillian.junit5.IdentifiedTestException;
import org.jboss.arquillian.test.spi.TestResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JUnitJupiterTestRunnerTestCase {

  @Test
  public void shouldReturnExceptionToClientIfFailingOnWrongExceptionThrown() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldFailExpectedWrongException");

    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertNotNull(result.getThrowable());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
  }

  @Test
  public void shouldNotReturnExceptionToClientIfAsumptionPassing() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldPassOnAssumption");

    Assertions.assertEquals(TestResult.Status.PASSED, result.getStatus());
    Assertions.assertNull(result.getThrowable());
  }

  @Test
  public void shouldReturnExceptionToClientIfAsumptionFailing() throws Exception {
    JUnitJupiterTestRunner runner = new JUnitJupiterTestRunner();
    TestResult result = runner.execute(TestScenarios.class, "shouldSkipOnAssumption");

    Assertions.assertEquals(TestResult.Status.FAILED, result.getStatus());
    Assertions.assertNotNull(result.getThrowable());
    Assertions.assertEquals(IdentifiedTestException.class, result.getThrowable().getClass());
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
}
