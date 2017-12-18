package org.jboss.arquillian.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;


public class MethodRuleChain implements MethodRule {

    private static final MethodRuleChain EMPTY_CHAIN
        = new MethodRuleChain(Collections.<MethodRule>emptyList());

    private final List<MethodRule> rules;


    private MethodRuleChain(List<MethodRule> rules) {
        this.rules = rules;
    }


    private static MethodRuleChain emptyChain() {
        return MethodRuleChain.EMPTY_CHAIN;
    }


    static MethodRuleChain outer(MethodRule outerRule) {
        return MethodRuleChain.emptyChain().around(outerRule);
    }

    MethodRuleChain around(MethodRule enclosedRule) {
        final List<MethodRule> rules = new ArrayList<MethodRule>();

        rules.add(enclosedRule);
        rules.addAll(this.rules);
        return new MethodRuleChain(rules);
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        for (final MethodRule r : rules) {
            base = r.apply(base, method, target);
        }
        return base;
    }
}

