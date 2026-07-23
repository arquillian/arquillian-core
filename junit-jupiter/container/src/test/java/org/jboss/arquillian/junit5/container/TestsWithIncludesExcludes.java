package org.jboss.arquillian.junit5.container;

import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ArquillianTest
public class TestsWithIncludesExcludes {
    @Test
    @Tag("shouldInclude")
    public void functionATest() {
        System.out.println("TestsWithIncludesExcludes.functionATest executed...");
    }

    @Test
    @Tag("shouldInclude")
    public void badTest() {
        System.out.println("TestsWithIncludesExcludes.badTest executed...");
        fail("badTest is expected to fail");
    }

    @Test
    @Tag("shouldInclude")
    public void failedAssumptionShouldAbort() {
        assumeTrue(false);
        fail("fail should be skipped");
    }
    @Test
    @Disabled
    @Tag("shouldInclude")
    public void disabledTestShouldSkip() {
        System.out.println("TestsWithIncludesExcludes.functionCTest executed...");
        fail("fail should not called");
    }

// All tests below should not run for various reasons
    @Test
    @Tag("shouldExclude")
    public void functionBTest() {
        System.out.println("TestsWithIncludesExcludes.functionBTest executed...");
        fail("fail should not called");
    }

}
