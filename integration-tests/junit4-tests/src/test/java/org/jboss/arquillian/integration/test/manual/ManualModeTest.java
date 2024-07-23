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

package org.jboss.arquillian.integration.test.manual;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.integration.test.common.TestEnvironment;
import org.jboss.arquillian.integration.test.common.app.EchoResource;
import org.jboss.arquillian.integration.test.common.app.RestActivator;
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
@RunAsClient
public class ManualModeTest {
    private static final String CONTAINER_NAME = "default";
    private static final String DEPLOYMENT_NAME = "manual-mode";

    @Deployment(name = DEPLOYMENT_NAME, managed = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
            .addClasses(RestActivator.class, EchoResource.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private static ContainerController controller;

    @ArquillianResource
    private static Deployer deployer;

    @Test
    public void startConnectAndStop() throws Exception {
        Assert.assertNotNull(controller);
        Assert.assertFalse("This is a manual mode test and the server should not have been started.", controller.isStarted(CONTAINER_NAME));
        // Check that we can start the server and the server is running
        controller.start(CONTAINER_NAME);
        Assert.assertTrue("The server should have been started", controller.isStarted(CONTAINER_NAME));

        // Deploy the application and make a REST request
        deployer.deploy(DEPLOYMENT_NAME);
        assertAppDeployed();

        // Undeploy the application and stop the server
        deployer.undeploy(DEPLOYMENT_NAME);
        controller.stop(CONTAINER_NAME);
    }

    private void assertAppDeployed() throws Exception {
        final String msg = "Test message";
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(TestEnvironment.uri(DEPLOYMENT_NAME, TestEnvironment.REST_PATH, "echo"))
            .POST(HttpRequest.BodyPublishers.ofString(msg))
            .build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(200, response.statusCode());
        Assert.assertEquals(msg, response.body());
    }
}
