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
 * Defines how Arquillian should communicate with and prepare the @{@link Deployment} for in container testing.
 * <p>
 * Arquillian will use what the container has defined as it's default protocol unless you specify other wise. You can
 * override this behavior
 * by using @OverProtocol on the @Deployment method.
 * <p>
 * <p>
 * Usage Example:<br/>
 * <pre><code>
 * &#64;Deployment &#64;OverProtocol("MyCustomProtocol")
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class);
 * }
 * </code></pre>
 * <p>
 * You can also override the default behavior on a global level using arquillian.xml. This will apply to
 * all containers and all deployments in your test suite.
 * <p>
 * <p>
 * Usage Example:<br/>
 * <pre><code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;arquillian xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xmlns="http://jboss.org/schema/arquillian"
 *   xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd"&gt;
 *
 *      &lt;defaultProtocol type="Servlet 3.0" /&gt;
 * &lt;/arquillian&gt;
 * </code></pre>
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface OverProtocol {
    /**
     * A String reference to the protocol name
     */
    String value();
}
