/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
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

import java.net.URI;
import java.net.URL;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.integration.test.common.app.EchoResource;
import org.jboss.arquillian.integration.test.common.app.RestActivator;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
@RunAsClient
public class ManualModeInjectionTest {
    private static final String CONTAINER_NAME = "default";
    static final String DEPLOYMENT_NAME = "manual-mode";

    @ArquillianResource
    private static ContainerController controller;

    @ArquillianResource
    private static Deployer deployer;

    @Deployment(name = DEPLOYMENT_NAME, managed = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
            .addClasses(RestActivator.class, EchoResource.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeEach
    public void startAndDeploy() {
        controller.start(CONTAINER_NAME);
        deployer.deploy(DEPLOYMENT_NAME);
    }

    @AfterEach
    public void stop() {
        if (controller.isStarted(CONTAINER_NAME)) {
            deployer.undeploy(DEPLOYMENT_NAME);
            controller.stop(CONTAINER_NAME);
        }
    }

    @Test
    public void checkUri(@ArquillianResource final URI uri) {
        Assertions.assertNotNull(uri);
        Assertions.assertTrue(uri.toString().contains("/" + DEPLOYMENT_NAME));
    }

    @Test
    public void checkUrl(@ArquillianResource final URL url) {
        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.toString().contains("/" + DEPLOYMENT_NAME));
    }
}
