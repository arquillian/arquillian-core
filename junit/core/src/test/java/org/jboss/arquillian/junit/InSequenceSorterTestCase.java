/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runners.MethodSorters;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

/**
 * InOrderSorterTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class InSequenceSorterTestCase extends JUnitTestBaseClass {
    @Test
    public void shouldInvokeInOrder() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        final List<String> runOrder = new ArrayList<String>();
        Result result = run(adaptor,
            new RunListener() {
                @Override
                public void testStarted(Description description) throws Exception {
                    runOrder.add(description.getMethodName());
                }
            }, OrderedTestCase.class);

        Assert.assertTrue(result.wasSuccessful());

        Assert.assertEquals("one", runOrder.get(0));
        Assert.assertEquals("two", runOrder.get(1));
        Assert.assertEquals("tree", runOrder.get(2));
    }

    @Test
    public void shouldNotChangeOriginalOrderIfInSequenceNotDefined() throws Exception {
        TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
        executeAllLifeCycles(adaptor);

        final List<String> runOrder = new ArrayList<String>();
        Result result = run(adaptor,
            new RunListener() {
                @Override
                public void testStarted(Description description) throws Exception {
                    runOrder.add(description.getMethodName());
                }
            }, UnOrderedTestCase.class);

        Assert.assertTrue(result.wasSuccessful());

        String[] ordered = new String[] {"Atree", "Btwo", "Cone"};
        boolean inOriginalOrder = true;
        for (int i = 0; i < ordered.length; i++) {
            if (!ordered[i].equals(runOrder.get(i))) {
                inOriginalOrder = false;
            }
        }
        Assert.assertTrue(inOriginalOrder);
    }

    @RunWith(Arquillian.class)
    public static class OrderedTestCase {
        @Test
        @InSequence(2)
        public void two() {
        }

        @Test
        @InSequence(3)
        public void tree() {
        }

        @Test
        @InSequence(1)
        public void one() {
        }
    }

    @RunWith(Arquillian.class)
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class UnOrderedTestCase {
        @Test
        public void Cone() {
        }

        @Test
        public void Atree() {
        }

        @Test
        public void Btwo() {
        }
    }
}