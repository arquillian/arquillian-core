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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.junit.event.BeforeRules;
import org.jboss.arquillian.junit.event.RulesEnrichment;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.event.enrichment.AfterEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.BeforeEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.EnrichmentEvent;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;

/**
 * Enriches instance of the Rule that has been applied; of the Statement that is about to be taken; and of the Test that
 * is
 * about to be executed<br/>
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 * @version $Revision: $
 */
public class RulesEnricher {

    private static Logger log = Logger.getLogger(RulesEnricher.class.getName());

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<EnrichmentEvent> enrichmentEvent;

    public void enrichRulesAndTestInstance(@Observes RulesEnrichment event) throws Exception {
        Object testInstance = event.getTestInstance();
        List<Object> toEnrich = getRuleInstances(testInstance);
        if (toEnrich == null) {
            return;
        }
        toEnrich.add(testInstance);
        enrichInstances(toEnrich);
    }

    public void enrichStatement(@Observes BeforeRules event) throws Exception {
        List<Object> toEnrich = new ArrayList<Object>();

        if (RunRules.class.isInstance(event.getStatementInstance())) {
            toEnrich.add(SecurityActions.getField(RunRules.class, "statement").get(event.getStatementInstance()));
        } else {
            toEnrich.add(event.getStatementInstance());
        }
        enrichInstances(toEnrich);
    }

    public void enrichInstances(List<Object> toEnrich) {
        Collection<TestEnricher> testEnrichers = serviceLoader.get().all(TestEnricher.class);
        for (Object instance : toEnrich) {
            enrichmentEvent.fire(new BeforeEnrichment(instance));
            for (TestEnricher enricher : testEnrichers) {
                enricher.enrich(instance);
            }
            enrichmentEvent.fire(new AfterEnrichment(instance));
        }
    }

    /**
     * Retrieves instances of the TestRule and MethodRule classes
     */
    private List<Object> getRuleInstances(Object testInstance) throws Exception {
        List<Object> ruleInstances = new ArrayList<Object>();

        List<Field> fieldsWithRuleAnnotation =
            SecurityActions.getFieldsWithAnnotation(testInstance.getClass(), Rule.class);
        if (fieldsWithRuleAnnotation.isEmpty()) {
            List<Method> methodsWithAnnotation =
                SecurityActions.getMethodsWithAnnotation(testInstance.getClass(), Rule.class);
            if (methodsWithAnnotation.isEmpty()) {
                // there isn't any rule in the test class
                return null;
            } else {
                log.warning("Please note that methods annotated with @Rule are not fully supported in Arquillian. "
                    + "Specifically, if you want to enrich a field in your Rule implementation class.");

                return ruleInstances;
            }
        } else {
            for (Field field : fieldsWithRuleAnnotation) {
                Object fieldInstance = field.get(testInstance);
                if (isRule(fieldInstance)) {
                    ruleInstances.add(fieldInstance);
                }
            }
        }
        return ruleInstances;
    }

    /**
     * Decides whether the instance is a Rule or not
     */
    private boolean isRule(Object instance) {
        return MethodRule.class.isInstance(instance) || TestRule.class.isInstance(instance);
    }
}
