/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
