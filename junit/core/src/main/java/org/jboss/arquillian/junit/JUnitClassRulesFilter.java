package org.jboss.arquillian.junit;

import java.util.List;
import org.junit.rules.TestRule;

/**
 * Interface for filtering the already scanned @ClassRules
 */
public interface JUnitClassRulesFilter {

    /**
     * Filter the given @ClassRules
     *
     * @param scannedRules List of @ClassRules to be filtered
     * @return Filtered the given list of @ClassRules
     */
    List<TestRule> filter(List<TestRule> scannedRules);
}
