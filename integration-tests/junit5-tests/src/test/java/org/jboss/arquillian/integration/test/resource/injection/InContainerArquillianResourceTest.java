/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.arquillian.integration.test.resource.injection;

import java.net.URL;
import java.nio.file.Path;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.arquillian.integration.test.common.TestEnvironment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class InContainerArquillianResourceTest extends AbstractArquillianResourceTest {

    @ArquillianResource
    protected Context context;

    @ArquillianResource
    protected InitialContext initialContext;

    @Test
    public void checkContext() throws Exception {
        Assertions.assertNotNull(context, "The Context should have been injected");
        final Object bm = context.lookup("java:comp/BeanManager");
        Assertions.assertNotNull(bm);
    }

    @Test
    @DisabledIfSystemProperty(named = "javax.naming.Context.parameter", matches = "skip")
    public void checkContextParameter(@ArquillianResource final Context context) throws Exception {
        Assertions.assertNotNull(context, "The Context should have been injected");
        final Object bm = context.lookup("java:comp/BeanManager");
        Assertions.assertNotNull(bm);
    }

    @Test
    public void checkInitialContext() throws Exception {
        Assertions.assertNotNull(initialContext, "The InitialContext should have been injected");
        final Object bm = initialContext.lookup("java:comp/BeanManager");
        Assertions.assertNotNull(bm);
    }

    @Test
    @DisabledIfSystemProperty(named = "javax.naming.Context.parameter", matches = "skip")
    public void checkInitialContextParameter(@ArquillianResource final InitialContext initialContext) throws Exception {
        Assertions.assertNotNull(initialContext, "The InitialContext should have been injected");
        final Object bm = initialContext.lookup("java:comp/BeanManager");
        Assertions.assertNotNull(bm);
    }

    @Test
    public void checkMultipleParameters(@ArquillianResource final URL url, @TempDir final Path tempDir) {
        Assertions.assertNotNull(url, "The URL should have been injected");
        Assertions.assertEquals(TestEnvironment.protocol(), url.getProtocol());
        checkHost(url.getHost());
        Assertions.assertEquals(TestEnvironment.port(), url.getPort());
        Assertions.assertEquals("/" + DEPLOYMENT_NAME + "/", url.getPath());
        Assertions.assertNotNull(tempDir, "The temp dir should have been injected");
    }
}
