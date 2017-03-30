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
import java.util.List;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * ArchiveDeploymentToolingExporterTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Ignore // not implemented
@RunWith(MockitoJUnitRunner.class)
public class ArchiveDeploymentToolingExporterTestCase extends AbstractContainerTestTestBase {
    private static final String EXPORT_FOLDER = "target/";
    @Mock
    private DeployableContainer<?> deployableContainer;
    @Mock
    private DeploymentDescription deployment;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ArchiveDeploymentToolingExporter.class);
    }

    @Test
    public void shouldThrowIllegalStateExceptionOnMissingDeploymentGenerator() throws Exception {
        System.setProperty(ArchiveDeploymentToolingExporter.ARQUILLIAN_TOOLING_DEPLOYMENT_FOLDER, EXPORT_FOLDER);

        //context.add(Archive.class, ShrinkWrap.create(JavaArchive.class, "test.jar"));

        fire(new BeforeDeploy(deployableContainer, deployment));

        File exportedFile = new File(EXPORT_FOLDER + getClass().getName() + ".xml");

        Assert.assertTrue(exportedFile.exists());

        exportedFile.delete();
    }
}
