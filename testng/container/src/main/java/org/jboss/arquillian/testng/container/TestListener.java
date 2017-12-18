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
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestListener
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestListener implements ITestListener {

    private ITestContext context;

    public void onFinish(ITestContext paramITestContext) {
        context = paramITestContext;
    }

    public void onStart(ITestContext paramITestContext) {
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult paramITestResult) {
    }

    public void onTestFailure(ITestResult paramITestResult) {
    }

    public void onTestSkipped(ITestResult paramITestResult) {
    }

    public void onTestStart(ITestResult paramITestResult) {
    }

    public void onTestSuccess(ITestResult paramITestResult) {
    }

    public TestResult getTestResult() {
        if (context.getFailedConfigurations().size() > 0) {
            return TestResult.failed(
                context.getFailedConfigurations().getAllResults().iterator().next().getThrowable());
        } else if (context.getFailedTests().size() > 0) {
            return TestResult.failed(
                context.getFailedTests().getAllResults().iterator().next().getThrowable());
        } else if (context.getSkippedTests().size() > 0) {
            return TestResult.skipped().setThrowable(context.getSkippedTests().getAllResults().iterator().next().getThrowable());
        }
        if (context.getPassedTests().size() > 0) {
            return TestResult.passed().setThrowable(
                context.getPassedTests().getAllResults().iterator().next().getThrowable());
        }
        return TestResult.failed(
            new RuntimeException("Unknown test result: " + context).fillInStackTrace());
    }
}
