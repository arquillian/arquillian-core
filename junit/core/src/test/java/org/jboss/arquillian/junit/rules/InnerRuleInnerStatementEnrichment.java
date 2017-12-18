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
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;

/**
 * Uses Rule and Statement as inner anonymous classes.
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class InnerRuleInnerStatementEnrichment extends AbstractRuleStatementEnrichment {
    @ArquillianResource
    private ResourceStub testResources;

    @Rule
    public TestRule testRule = new TestRule() {
        @ArquillianResource
        private ResourceStub ruleResources;

        @Override
        public Statement apply(final Statement base, Description description) {
            performRuleAssertion(testResources, ruleResources);

            return new Statement() {
                @ArquillianResource
                private ResourceStub statementResources;

                @Override
                public void evaluate() throws Throwable {
                    performStatementAssertion(testResources, ruleResources, statementResources);
                    base.evaluate();
                }
            };
        }
    };

    @Rule
    public MethodRule methodRule = new MethodRule() {
        @ArquillianResource
        private ResourceStub ruleResources;

        @Override
        public Statement apply(final Statement base, FrameworkMethod method, Object target) {
            performRuleAssertion(testResources, ruleResources);

            return new Statement() {
                @ArquillianResource
                private ResourceStub statementResources;

                @Override
                public void evaluate() throws Throwable {
                    performStatementAssertion(testResources, ruleResources, statementResources);
                    base.evaluate();
                }
            };
        }
    };

    private void performRuleAssertion(ResourceStub testResources, ResourceStub ruleResources) {
        ResourceAssertion.assertNotNullAndNotEqual(testResources, ruleResources);
    }

    private void performStatementAssertion(ResourceStub testResources, ResourceStub ruleResources,
        ResourceStub statementResources) {
        performRuleAssertion(testResources, ruleResources);
        ResourceAssertion.assertNotNullAndNotEqual(testResources, statementResources);
        ResourceAssertion.assertNotNullAndNotEqual(statementResources, ruleResources);
    }

    public TestRule getTestRule() {
        return testRule;
    }

    public MethodRule getMethodRule() {
        return methodRule;
    }

    @Test
    public void verifyEnrichment() {
        assertNotNull(testResources);
    }
}
