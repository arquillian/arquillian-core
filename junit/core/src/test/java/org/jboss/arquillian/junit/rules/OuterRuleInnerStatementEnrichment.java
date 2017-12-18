/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit.rules;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertNotNull;

/**
 * Uses Rule as normal outer java class and Statement as inner anonymous class defined within the Rule
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class OuterRuleInnerStatementEnrichment extends AbstractRuleStatementEnrichment {
    @Rule
    public TestingTestRuleInnerStatement testRuleInnerStatement = new TestingTestRuleInnerStatement();
    @Rule
    public TestingMethodRuleInnerStatement methodRuleInnerStatement = new TestingMethodRuleInnerStatement();
    @ArquillianResource
    private ResourceStub resources;

    public TestRule getTestRule() {
        return testRuleInnerStatement;
    }

    public MethodRule getMethodRule() {
        return methodRuleInnerStatement;
    }

    @Test
    public void verifyEnrichment() {
        assertNotNull(resources);
    }
}
