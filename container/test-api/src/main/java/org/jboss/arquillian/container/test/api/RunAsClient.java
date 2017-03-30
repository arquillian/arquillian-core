/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The run mode for a test method is determined by the @Deployment annotations member testable.
 * By default testable is true which tells Arquillian to execute the test methods in container. If testable is set to
 * false,
 * Arquillian will execute the test methods on the client side.
 * <p>
 * <p>
 * In some cases it is useful to run different test methods in a test class in different modes,
 * e.g. a client method that calls a Servlet for then to verify some internal state in a in container method.
 * <p>
 * Usage Example:<br/>
 * <pre><code>
 * &#64;Deployment
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class);
 * }
 *
 * &#64;Test &#64;RunAsClient
 * public void shouldExecuteOnClientSide() { ... }
 *
 * &#64;Test
 * public void shouldExecuteInContainer() { ... }
 * </code></pre>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RunAsClient {
}
