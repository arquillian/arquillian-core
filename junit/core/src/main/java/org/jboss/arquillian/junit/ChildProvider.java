package org.jboss.arquillian.junit;

import java.util.List;

import org.jboss.arquillian.test.spi.TestClass;
import org.junit.runner.Runner;

public interface ChildProvider {

    List<Runner> children(TestClass testClass);
}
