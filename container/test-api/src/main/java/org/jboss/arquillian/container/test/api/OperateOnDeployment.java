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
 * Defines that the target should operate within the context of the referenced deployment.
 * <p>
 * When using multiple {@link Deployment}'s within the same TestCase you need to specify which deployment the
 * individual test methods should operate on.
 * <p>
 * Within the context of a deployment you will have access to the meta data provided by the
 * deployment and the container where it is deployed, e.g. URLs
 * <p>
 * Usage Example for test method:<br/>
 * <pre><code>
 * &#64;Deployment(name = "X")
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class)
 *          .addClass(MyServletX.class);
 * }
 *
 * &#64;Deployment(name = "Y")
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class)
 *          .addClass(MyServletY.class);
 * }
 *
 * &#64;Test &#64;OperatesOnDeployment("X")
 * public void shouldExecuteInX() { ... }
 *
 * &#64;Test &#64;OperatesOnDeployment("Y")
 * public void shouldExecuteInY() { ... }
 * </code></pre>
 * <p>
 * Additionally you can reference another deployments metadata from within another context by qualifiing
 * OperateOnDeployment on ArquillianResource injection points.
 * <p>
 * <p>
 * Usage Example for ArquillianResource:<br/>
 * <pre><code>
 * &#64;Deployment(name = "X")
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class)
 *          .addClass(MyServletX.class);
 * }
 * &#64;Deployment(name = "Y")
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class)
 *          .addClass(MyServletY.class);
 * }
 *
 * &#64;Test &#64;OperatesOnDeployment("X")
 * public void shouldExecuteInX() { ... }
 *
 * &#64;Test &#64;OperatesOnDeployment("Y") &#64;RunAsClient
 * public void shouldExecuteInY(&#64;ArquillianResource &#64;OperateOnDeployment("X") URL deploymentXURLContext) { ... }
 * </code></pre>
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface OperateOnDeployment {
    /**
     * Refer to the deployment name this should operate on.
     *
     * @return The Deployment name this method operates on
     *
     * @see Deployment#name()
     */
    String value();
}
