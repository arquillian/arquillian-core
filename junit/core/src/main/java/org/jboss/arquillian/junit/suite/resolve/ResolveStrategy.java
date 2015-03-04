package org.jboss.arquillian.junit.suite.resolve;

import java.util.List;

public interface ResolveStrategy {

    List<Class<?>> resolve(String[] values);
}
