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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When using multiple containers within the same test suite, you can use the @TargetsContainer annotation to specify
 * which container a deployment should be deployed to.
 * <p>
 * <p>
 * Usage Example:<br/>
 * <pre><code>
 * &#64;Deployment &#64;TargetsContainer("X")
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class);
 * }
 *
 * &#64;Deployment
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class)
 * }
 * </code></pre>
 * <p>
 * The TargetsContainer name refers to the container qualifier defined in the Arquillian configuration.
 * <p>
 * <pre><code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;arquillian xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xmlns="http://jboss.org/schema/arquillian"
 *   xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd"&gt;
 *
 *      &lt;group qualifier="G"&gt;
 *          &lt;container qualifier="X" /&gt;
 *          &lt;container qualifier="Y" default="true" /&gt;
 *      &lt;/group&gt;
 * &lt;/arquillian&gt;
 * </code></pre>
 * <p>
 * If a container is defined as default=true in configuration, the @TargetsContainer annotation can be emitted
 * when targeting that container. Only one container can be set as default within a group.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Documented
@Retention(RUNTIME)
@java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface TargetsContainer {
    /**
     * The name of the target container as defined in configuration.
     *
     * @return The target name.
     */
    String value();
}
