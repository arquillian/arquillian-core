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

import java.util.Map;

/**
 * A interface that describes how you can start/stop server instances during test execution.
 * <p>
 * Usage Example:<br/>
 * <pre><code>
 * &#64;Deployment
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class)
 *
 * }
 *
 * &#64;ArquillianResource
 * private ContainerController controller;
 *
 * &#64;Test
 * public void shouldStartServerX() {
 *      controller.start("X")
 * }
 * </code></pre>
 * <p>
 * <pre><code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;arquillian xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xmlns="http://jboss.org/schema/arquillian"
 *   xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd"&gt;
 *
 *      &lt;group qualifier="G"&gt;
 *          &lt;container qualifier="X" mode="manual" /&gt;
 *          &lt;container qualifier="Y" default="true" /&gt;
 *      &lt;/group&gt;
 * &lt;/arquillian&gt;
 * </code></pre>
 * <p>
 * Only containers configured to be in mode manual or custom can be controlled via the ContainerController.
 *
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 */
public interface ContainerController {
    void start(String containerQualifier);

    void start(String containerQualifier, Map<String, String> config);

    void stop(String containerQualifier);

    void kill(String containerQualifier);

    boolean isStarted(String containerQualifier);
}
