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
package org.jboss.arquillian.container.test.impl.client.deployment.tool;

import java.io.File;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * ToolingDeploymentFormatterTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ToolingDeploymentFormatterTestCase {

    @Test
    public void shouldBeAbleToExportArchive() throws Exception {
        String content = ShrinkWrap
            .create(WebArchive.class, "test.jar")
            .addAsResource(new File("src/test/resources/tooling/arquillian.xml"), ArchivePaths.create("resource.xml"))
            .addAsResource("tooling/arquillian.xml", ArchivePaths.create("resource2.xml"))
            .addAsResource(new File("src/test/resources/tooling/arquillian.xml").toURI().toURL(),
                ArchivePaths.create("resource3.xml")).addClass(ToolingDeploymentFormatterTestCase.class)
            .addAsServiceProvider(Service.class, ServiceImpl.class)
            .addAsLibrary(ShrinkWrap.create(JavaArchive.class, "test.jar").addClass(ToolingDeploymentFormatter.class))
            .toString(new ToolingDeploymentFormatter(getClass()));

        // TODO: do some output Assertions..
        Assert.assertNotNull(content);
        System.out.println(content);
    }

    public static interface Service {

    }

    public static class ServiceImpl implements Service {

    }
}
