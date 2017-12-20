package org.jboss.arquillian.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * The MethodRuleChain rule allows ordering of MethodRules. You create a
 * {@code MethodRuleChain} with {@link #outer(MethodRule)} and subsequent calls of
 * {@link #around(MethodRule)}:
 *
 * <pre>
 * public static class MethodRuleChainUse {
 * 	&#064;Rule
 * 	public MethodRuleChain chain= MethodRuleChain.outer(new ArquillianTest())
 * 	                                .around(new TestWatchman())
 *
 * 	&#064;Test
 * 	public void example() {
 * 		assertTrue(true);
 *     }
 * }
 * </pre>
 *
 * ArquillianTest will evaluate first, then TestWatchman will evaluate.
 *
 */

public class MethodRuleChain implements MethodRule {

    private static final MethodRuleChain EMPTY_CHAIN
        = new MethodRuleChain(Collections.<MethodRule>emptyList());

    private final List<MethodRule> rules;

    private MethodRuleChain(List<MethodRule> rules) {
        this.rules = rules;
    }

    /**
     * Returns a {@code MethodRuleChain} without a {@link MethodRule}. This method may
     * be the starting point of a {@code MethodRuleChain}.
     *
     * @return a {@code MethodRuleChain} without a {@link MethodRule}.
     */
    public static MethodRuleChain emptyChain() {
        return MethodRuleChain.EMPTY_CHAIN;
    }

    /**
     * Returns a {@code MethodRuleChain} with a single {@link MethodRule}. This method
     * is the usual starting point of a {@code MethodRuleChain}.
     *
     * @param outerRule the outer rule of the {@code MethodRuleChain}.
     * @return a {@code MethodRuleChain} with a single {@link MethodRule}.
     */
    public static MethodRuleChain outer(MethodRule outerRule) {
        return MethodRuleChain.emptyChain().around(outerRule);
    }

    /**
     * Create a new {@code MethodRuleChain}, which encloses the {@code nextRule} with
     * the rules of the current {@code MethodRuleChain}.
     *
     * @param enclosedRule the rule to enclose.
     * @return a new {@code MethodRuleChain}.
     */
    public MethodRuleChain around(MethodRule enclosedRule) {
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

