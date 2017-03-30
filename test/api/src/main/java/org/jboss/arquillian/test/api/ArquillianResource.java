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
package org.jboss.arquillian.test.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Arquillian has support for multiple injection points like @EJB, @Resources and @Inject, but there are also
 * non standard component model objects available within the Arquillian runtime that can be of useful during testing.
 * <p>
 * Arquillian can expose these objects to the test case using the @ArquillianResource injection annotation.
 * <p>
 * Usage Example of Field injection:<br/>
 * <pre><code>
 * &#64;ArquillianResource
 * private InitialContext context;
 *
 * &#64;Test
 * public void shouldBeAbleToGetContext() {
 *      context.lookup("");
 * }
 * </code></pre>
 * <p>
 * Usage Example of Argument injection:<br/>
 * <pre><code>
 * &#64;Test
 * public void shouldBeAbleToGetContext(&#64;ArquillianResource InitialContext context) {
 *      context.lookup("");
 * }
 * </code></pre>
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ArquillianResource {

    /**
     * Defines the resource target for this injection. e.g. Servlet.
     *
     * @return The Target Resource Type
     */
    Class<?> value() default ArquillianResource.class;
}
