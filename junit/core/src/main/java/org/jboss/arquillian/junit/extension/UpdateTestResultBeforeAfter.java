/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.junit.extension;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.junit.State;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;
import org.junit.internal.AssumptionViolatedException;

/**
 * Update the TestResult based on result After going through the JUnit chain.
 *
 * This will give the correct TestResult in After, even with validation outside
 * of Arquillians control, e.g. ExpectedExceptions.
 */
class UpdateTestResultBeforeAfter
{
    public void update(@Observes(precedence = 99) EventContext<AfterTestLifecycleEvent> context, TestResult result)
    {
        if(State.caughtExceptionAfterJunit() != null)
        {
            if(State.caughtExceptionAfterJunit() instanceof AssumptionViolatedException)
            {
                result.setStatus(TestResult.Status.SKIPPED);
            }
            else
            {
                result.setStatus(TestResult.Status.FAILED);
            }
            result.setThrowable(State.caughtExceptionAfterJunit());
        }
        else
        {
            result.setStatus(Status.PASSED);
            result.setThrowable(null);
        }
        context.proceed();
    }
}

