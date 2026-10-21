/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
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

package org.jboss.arquillian.integration.test.resource.injection;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.integration.test.common.TestEnvironment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link ArquillianResource} fields of the class enclosing a {@code @Nested} test class are injected,
 * both in the container and on the client, whether {@link RunAsClient} is on the test method or on the nested class.
 *
 * @author Radoslav Husar
 */
@ArquillianTest
public class NestedArquillianResourceTest {
    private static final String DEPLOYMENT_NAME = "nested-injection";

    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
            .addClass(TestEnvironment.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeEach
    public void checkUrlBeforeEach() {
        Assertions.assertNotNull(url, "The URL should have been injected before @BeforeEach");
    }

    @Nested
    class NestedTest {

        @Test
        public void checkUrl() {
            assertUrl();
        }

        @Test
        @RunAsClient
        public void checkUrlAsClient() {
            assertUrl();
        }
    }

    @Nested
    @RunAsClient
    class NestedClientTest {

        @Test
        public void checkUrl() {
            assertUrl();
        }
    }

    private void assertUrl() {
        Assertions.assertNotNull(url, "The URL should have been injected");
        Assertions.assertEquals("/" + DEPLOYMENT_NAME + "/", url.getPath());
    }
}
