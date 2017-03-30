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

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Verify the that JUnit integration adaptor fires the expected events even when Handlers are failing.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class JUnitIntegrationTestCase extends JUnitTestBaseClass {
    @Test
    public void shouldCallAllMethodsWithRealAdapter() throws Exception {
        System.setProperty("arquillian.debug", "true");
        TestRunnerAdaptor adaptor = spy(TestRunnerAdaptorBuilder.build());

        Result result = run(adaptor, ArquillianClass1.class);

        Assert.assertTrue(result.wasSuccessful());
        assertCycle(1, Cycle.values());

        verify(adaptor, times(1)).beforeSuite();
        verify(adaptor, times(1)).afterSuite();
    }
}
