/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.test.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Define that a Deployment should cause a exception during deployment. If the Container does not throw a exception,
 * or the exception is of the wrong type, a RuntimeException will be thrown and the test failed.
 * If the correct exception is thrown the test will execute as normal.
 * <p>
 * <p>
 * Usage Example:<br/>
 * <pre><code>
 * &#64;Deployment &#64;ShouldThrowException(WeldDeploymentException.class)
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class);
 * }
 * </code></pre>
 * <p>
 * Adding the @ShouldThrowException annotation will force the @{@link Deployment} to be <code>testable = false</code> which again
 * will force a @{@link RunAsClient} test run mode, unless you explicitly mark <code>testable = true</code>
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface ShouldThrowException {
    Class<? extends Exception> value() default Exception.class;

    /**
     * @return whether or not this deployment is intended to be testable, default is false
     */
    boolean testable() default false;
}
