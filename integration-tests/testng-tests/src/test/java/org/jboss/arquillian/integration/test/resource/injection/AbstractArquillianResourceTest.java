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

import java.net.URI;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.integration.test.common.TestEnvironment;
import org.jboss.arquillian.integration.test.common.app.Greeter;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractArquillianResourceTest extends Arquillian {
    static final String DEPLOYMENT_NAME = "injection";

    @ArquillianResource
    protected URL url;

    @ArquillianResource
    protected URI uri;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
            .addClasses(Greeter.class, AbstractArquillianResourceTest.class, TestEnvironment.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void checkUrl() {
        Assert.assertNotNull(url, "The URL should have been injected");
        Assert.assertEquals(url.getProtocol(), TestEnvironment.protocol());
        checkHost(url.getHost());
        Assert.assertEquals(url.getPort(), TestEnvironment.port());
        Assert.assertEquals(url.getPath(), "/" + DEPLOYMENT_NAME + "/");
    }

    @Test
    public void checkUri() {
        Assert.assertNotNull(uri, "The URI should have been injected");
        checkHost(uri.getHost());
        Assert.assertEquals(uri.getPort(), TestEnvironment.port());
        Assert.assertEquals(uri.getPath(), "/" + DEPLOYMENT_NAME + "/");
    }

    protected void checkHost(final String host) {
        // localhost and 127.0.0.1 should be treated as the same
        final String expectedHost = TestEnvironment.host();
        if ("127.0.0.1".equals(expectedHost)) {
            Assert.assertEquals(host.replace("localhost", "127.0.0.1"), expectedHost);
        } else if ("localhost".equals(expectedHost)) {
            Assert.assertEquals(host.replace("127.0.0.1", "localhost"), expectedHost);
        } else {
            Assert.assertEquals(host, expectedHost);
        }
    }
}
