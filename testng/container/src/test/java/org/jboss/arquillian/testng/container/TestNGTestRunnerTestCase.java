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
package org.jboss.arquillian.testng.container;

import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.testng.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.testng.SkipException;

public class TestNGTestRunnerTestCase extends Arquillian {
    @Test
    public void shouldReturnPassedTest() throws Exception {
        TestNGTestRunner runner = new TestNGTestRunner();
        TestResult result =
            runner.execute(ShouldProvideVariousTestResultsToTestRunner.class, "shouldProvidePassingTestToRunner");

        Assert.assertNotNull(result);
        Assert.assertEquals(TestResult.Status.PASSED, result.getStatus());
        Assert.assertNull(result.getThrowable());
    }

    @Test
    public void shouldReturnFailedTest() throws Exception {
        TestNGTestRunner runner = new TestNGTestRunner();
        TestResult result =
            runner.execute(ShouldProvideVariousTestResultsToTestRunner.class, "shouldProvideFailingTestToRunner");

        Assert.assertNotNull(result);
        Assert.assertEquals(TestResult.Status.FAILED, result.getStatus());
        Assert.assertEquals(AssertionError.class, result.getThrowable().getClass());
    }

    @Test
    public void shouldReturnSkippedTest() throws Exception {
        TestNGTestRunner runner = new TestNGTestRunner();
        TestResult result =
            runner.execute(ShouldProvideVariousTestResultsToTestRunner.class, "shouldProvideSkippingTestToRunner");

        Assert.assertNotNull(result);
        Assert.assertEquals(TestResult.Status.SKIPPED, result.getStatus());
        Assert.assertEquals(SkipException.class, result.getThrowable().getClass());
    }

    @Test
    public void shouldReturnFailedTestAfterConfigurationError() throws Exception {
        TestNGTestRunner runner = new TestNGTestRunner();
        TestResult result = runner.execute(ShouldProvideConfigurationFailureToTestRunner.class, "successfulTest");

        Assert.assertNotNull(result);
        Assert.assertEquals(TestResult.Status.FAILED, result.getStatus());
        Assert.assertEquals(ClassNotFoundException.class, result.getThrowable().getClass());
    }

    @Test
    public void shouldReturnExceptionOnPassedTest() throws Exception {
        TestNGTestRunner runner = new TestNGTestRunner();
        TestResult result =
            runner.execute(ShouldProvideVariousTestResultsToTestRunner.class, "shouldProvideExpectedExceptionToRunner");

        Assert.assertNotNull(result);
        Assert.assertEquals(TestResult.Status.PASSED, result.getStatus());
        Assert.assertNotNull(result.getThrowable());
        Assert.assertEquals(IllegalArgumentException.class, result.getThrowable().getClass());
    }

    @Test
    // TODO: this should me moved to new TestNG test suite
    public void shouldBeAbleToUseOtherDataProviders() throws Exception {
        TestNGTestRunner runner = new TestNGTestRunner();
        TestResult result =
            runner.execute(ShouldProvideVariousTestResultsToTestRunner.class, "shouldBeAbleToUseOtherDataProviders");

        Assert.assertNotNull(result);
        Assert.assertEquals(TestResult.Status.PASSED, result.getStatus());
        Assert.assertNull(result.getThrowable());
    }
}
