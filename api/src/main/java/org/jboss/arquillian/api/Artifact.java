package org.jboss.arquillian.api;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Mark this class as requiring an artifact to be deployed for the
 * container. By default all classes in the current package, as well as support
 * classes, will be deployed. The resultant artifact may not support session beans.
 * <p/>
 * This test may be a unit test (in which case, if the suite is run in
 * standalone mode, the artifact itself won't be deployed, just the contained
 * classes and xml config files).
 *
 * @author Pete Muir
 * @see Classes
 * @see IntegrationTest
 * @see Packaging
 * @see Resources
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Artifact {
	ArtifactType artifactType() default ArtifactType.JSR299;

	boolean addCurrentPackage() default true;
}
