package org.jboss.arquillian.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Mark this test as requiring specific packaging when deployed to a container
 * for intgration testing.
 * 
 * @see IntegrationTest
 * @see Artifact
 * 
 * @author Pete Muir
 *
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Packaging
{
   
   PackagingType value() default PackagingType.WAR;
   
}
