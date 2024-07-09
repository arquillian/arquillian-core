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
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
abstract class AbstractArquillianResourceTest {
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
        Assert.assertNotNull("The URL should have been injected", url);
        Assert.assertEquals(TestEnvironment.protocol(), url.getProtocol());
        checkHost(url.getHost());
        Assert.assertEquals(TestEnvironment.port(), url.getPort());
        Assert.assertEquals("/" + DEPLOYMENT_NAME + "/", url.getPath());
    }

    @Test
    public void checkParameterUrl(@ArquillianResource final URL url) {
        Assert.assertNotNull("The URL should have been injected", url);
        Assert.assertEquals(TestEnvironment.protocol(), url.getProtocol());
        checkHost(url.getHost());
        Assert.assertEquals(TestEnvironment.port(), url.getPort());
        Assert.assertEquals("/" + DEPLOYMENT_NAME + "/", url.getPath());
    }

    @Test
    public void checkUri() {
        Assert.assertNotNull("The URI should have been injected", uri);
        checkHost(uri.getHost());
        Assert.assertEquals(TestEnvironment.port(), uri.getPort());
        Assert.assertEquals("/" + DEPLOYMENT_NAME + "/", uri.getPath());
    }

    @Test
    public void checkParameterUri(@ArquillianResource final URI uri) {
        Assert.assertNotNull("The URI should have been injected", uri);
        checkHost(uri.getHost());
        Assert.assertEquals(TestEnvironment.port(), uri.getPort());
        Assert.assertEquals("/" + DEPLOYMENT_NAME + "/", uri.getPath());
    }

    protected void checkHost(final String host) {
        // localhost and 127.0.0.1 should be treated as the same
        final String expectedHost = TestEnvironment.host();
        if ("127.0.0.1".equals(expectedHost)) {
            Assert.assertEquals(expectedHost, host.replace("localhost", "127.0.0.1"));
        } else if ("localhost".equals(expectedHost)) {
            Assert.assertEquals(expectedHost, host.replace("127.0.0.1", "localhost"));
        } else {
            Assert.assertEquals(expectedHost, host);
        }
    }
}
