package org.jboss.arquillian.junit5.container;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Junit5 Suite with failures and excludes")
@IncludeTags("shouldInclude")
@ExcludeTags({"shouldExclude", "failOnFirst", "failOnSecond"})
@SelectClasses({
    TestsWithIncludesExcludes.class
})
public class ExampleSuite {
}
