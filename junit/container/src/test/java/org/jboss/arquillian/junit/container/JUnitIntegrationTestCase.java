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
package org.jboss.arquillian.junit.container;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Result;

import static org.mockito.Mockito.mock;

public class JUnitIntegrationTestCase extends JUnitTestBaseClass {

    @Test
    public void should_not_execute_class_rule() throws Exception {
        // given
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        // when
        Result result = run(adaptor, ClassWithArquillianRunnerWithRules.class);

        // then
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertEquals(0, result.getFailureCount());
        Assert.assertEquals(0, result.getIgnoreCount());
        assertCycle(1, Cycle.BEFORE_RULE, Cycle.BEFORE_CLASS, Cycle.BEFORE, Cycle.TEST, Cycle.AFTER, Cycle.AFTER_CLASS,
            Cycle.AFTER_RULE);
        assertCycle(0, Cycle.BEFORE_CLASS_RULE, Cycle.AFTER_CLASS_RULE);
    }
}
