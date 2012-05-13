/*
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used with the {@code @Deployment} annotation to identify which Arquillian services should be left out of
 * the test archive.
 * 
 * <p>
 * By default, Arquillian adds a collection of services to the {@code Archive} defined by the {@code @Deployment} member, often
 * by wrapping it inside another {@code Archive}. The {@code @ExcludeServices} annotation can be used to disable this behavior
 * altogether or exclude certain services types using a set of built-in constants defined in {@link ServiceType}. This list of
 * service types is extensible so that extensions can honor additional exclusions.
 * <p>
 * 
 * <p>
 * {@code @ExcludeServices} should be used in place of {@code @Deployment(testable = false)}.
 * </p>
 * 
 * <p>
 * Usage Example (disable archive modification):<br/>
 * <pre><code>
 * &#64;Deployment
 * &#64;ExcludeServices
 * public static WebArchive create() {
 *     return ShrinkWrap.create(WebArchive.class).addClass(MyComponent.class);
 * }
 * </code></pre>
 * <p>
 * Usage Example (exclude support for in-container testing):<br/>
 * <pre><code>
 * &#64;Deployment
 * &#64;ExcludeServices(ServiceType.TEST_RUNNER)
 * public static WebArchive create() {
 *     return ShrinkWrap.create(WebArchive.class).addClass(MyComponent.class);
 * }
 * </code></pre>
 * 
 * @since 1.0.1.Final
 * @author <a href="http://community.jboss.org/people/dan.j.allen">Dan Allen</a>
 * @see Deployment
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ExcludeServices {
    String[] value() default { ServiceType.ALL };
}
