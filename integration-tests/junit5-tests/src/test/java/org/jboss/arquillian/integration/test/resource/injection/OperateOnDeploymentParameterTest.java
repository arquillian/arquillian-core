/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2026 Red Hat, Inc., and individual contributors
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.integration.test.common.app.EchoResource;
import org.jboss.arquillian.integration.test.common.app.RestActivator;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests that a method level {@link OperateOnDeployment} selects the deployment used to resolve the parameters of the
 * test method. The annotation is not a qualifier on the parameters, so the resource providers can't select the
 * deployment themselves.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@ArquillianTest
@RunAsClient
public class OperateOnDeploymentParameterTest {
    private static final String DEPLOYMENT_NAME_1 = "operate-on-first";
    private static final String DEPLOYMENT_NAME_2 = "operate-on-second";

    @Deployment(name = DEPLOYMENT_NAME_1)
    public static WebArchive createFirstDeployment() {
        return createDeployment(DEPLOYMENT_NAME_1);
    }

    @Deployment(name = DEPLOYMENT_NAME_2)
    public static WebArchive createSecondDeployment() {
        return createDeployment(DEPLOYMENT_NAME_2);
    }

    @Test
    @OperateOnDeployment(DEPLOYMENT_NAME_1)
    public void checkFirstUri(@ArquillianResource final URI uri) {
        Assertions.assertNotNull(uri);
        Assertions.assertTrue(uri.toString().contains("/" + DEPLOYMENT_NAME_1), () -> String.format("Unexpected URI: %s", uri));
    }

    @Test
    @OperateOnDeployment(DEPLOYMENT_NAME_2)
    public void checkSecondUri(@ArquillianResource final URI uri) {
        Assertions.assertNotNull(uri);
        Assertions.assertTrue(uri.toString().contains("/" + DEPLOYMENT_NAME_2), () -> String.format("Unexpected URI: %s", uri));
    }

    private static WebArchive createDeployment(final String name) {
        return ShrinkWrap.create(WebArchive.class, name + ".war")
            .addClasses(RestActivator.class, EchoResource.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
