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

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.integration.test.common.TestEnvironment;
import org.jboss.arquillian.integration.test.common.app.Greeter;
import org.jboss.arquillian.integration.test.common.ext.TestArquillianResource;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
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
    @RunAsClient
    // Disabling this seems to be required as the lookup to inject the resource is done for some reason in Payara
    @DisabledIfSystemProperty(named = "javax.naming.Context.parameter", matches = "skip")
    public void containerResource(@ArquillianResource TestArquillianResource resource) {
        Assertions.assertNotNull(resource, "Expected the TestArquillianResource to be injected");
        Assertions.assertEquals("ContainerScoped", resource.containerName());
    }

    @Test
    public void checkUrl() {
        Assertions.assertNotNull(url, "The URL should have been injected");
        Assertions.assertEquals(TestEnvironment.protocol(), url.getProtocol());
        checkHost(url.getHost());
        Assertions.assertEquals(TestEnvironment.port(), url.getPort());
        Assertions.assertEquals("/" + DEPLOYMENT_NAME + "/", url.getPath());
    }

    @Test
    public void checkParameterUrl(@ArquillianResource final URL url) {
        Assertions.assertNotNull(url, "The URL should have been injected");
        Assertions.assertEquals(TestEnvironment.protocol(), url.getProtocol());
        checkHost(url.getHost());
        Assertions.assertEquals(TestEnvironment.port(), url.getPort());
        Assertions.assertEquals("/" + DEPLOYMENT_NAME + "/", url.getPath());
    }

    @Test
    public void checkUri() {
        Assertions.assertNotNull(uri, "The URI should have been injected");
        checkHost(uri.getHost());
        Assertions.assertEquals(TestEnvironment.port(), uri.getPort());
        Assertions.assertEquals("/" + DEPLOYMENT_NAME + "/", uri.getPath());
    }

    @Test
    public void checkParameterUri(@ArquillianResource final URI uri) {
        Assertions.assertNotNull(uri, "The URI should have been injected");
        checkHost(uri.getHost());
        Assertions.assertEquals(TestEnvironment.port(), uri.getPort());
        Assertions.assertEquals("/" + DEPLOYMENT_NAME + "/", uri.getPath());
    }

    @Test
    public void deployerInjection(@ArquillianResource final Deployer deployer) {
        Assertions.assertNotNull(deployer, "The deployer should have been injected");
    }

    @Test
    public void containerControllerInjection(@ArquillianResource final ContainerController container) {
        Assertions.assertNotNull(container, "The container should have been injected");
        Assertions.assertTrue(container.isStarted("default"));
    }

    protected void checkHost(final String host) {
        // localhost and 127.0.0.1 should be treated as the same
        final String expectedHost = TestEnvironment.host();
        if ("127.0.0.1".equals(expectedHost)) {
            Assertions.assertEquals(expectedHost, host.replace("localhost", "127.0.0.1"));
        } else if ("localhost".equals(expectedHost)) {
            Assertions.assertEquals(expectedHost, host.replace("127.0.0.1", "localhost"));
        } else {
            Assertions.assertEquals(expectedHost, host);
        }
    }
}
