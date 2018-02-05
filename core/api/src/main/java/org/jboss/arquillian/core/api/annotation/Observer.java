package org.jboss.arquillian.core.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation to specify one or more Arquillian observers (class that contains a method with annotation {@link Observes})
 * for test class - it will observe all events starting with {@code org.jboss.arquillian.test.spi.event.suite.BeforeClass}
 * and ending with {@code org.jboss.arquillian.test.spi.event.suite.AfterClass}.
 * These observers will behave as any other Arquillian observer - it will be supported by dependency injection for Arquillian
 * extensions. The observer specified in this annotation has to have a non-parametric constructor.
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Observer {

    Class<?>[] value();
}
