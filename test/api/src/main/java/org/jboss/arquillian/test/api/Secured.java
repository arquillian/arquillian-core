/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Uses the secured protocol for URL and URI injection.
 *
 * Usage example of field injection:<br />
 * <pre><code>
 * &#64;ArquillianResource
 * &#64;Secured
 * private URL url;
 * </code></pre>
 *
 * @author <a href="http://community.jboss.org/people/silenius">Samuel Santos</a>
 * @version $Revision: $
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Secured {

    /**
     * Defines the name of the protocol to use.
     *
     * @return The scheme name or <code>https</code> if the scheme is undefined
     */
    String scheme() default "https";

    /**
     * Defines the port number on the host.
     *
     * @return The port number or <code>-1</code> if the port is undefined
     */
    int port() default -1;
}
