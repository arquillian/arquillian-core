package org.jboss.arquillian.junit.container;

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.junit.JUnitClassRulesFilter;
import org.junit.rules.TestRule;

public class ContainerClassRulesFilter implements JUnitClassRulesFilter {

    /**
     * No @ClassRule should be executed inside of a container since the state between @Test is not kept there.
     */
    public List<TestRule> filter(List<TestRule> scannedRules) {
        return new ArrayList<TestRule>();
    }
}
