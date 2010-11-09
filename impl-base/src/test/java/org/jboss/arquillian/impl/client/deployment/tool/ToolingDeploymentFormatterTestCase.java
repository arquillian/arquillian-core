/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.impl.client.deployment.tool;

import java.io.File;

import junit.framework.Assert;

import org.jboss.arquillian.impl.DynamicServiceLoader;
import org.jboss.arquillian.impl.client.deployment.tool.ToolingDeploymentFormatter;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * ToolingDeploymentFormatterTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ToolingDeploymentFormatterTestCase
{

   @Test
   public void shouldBeAbleToExportArchive() throws Exception
   {
      String content = ShrinkWrap.create(WebArchive.class, "test.jar")
                        .addResource(new File("src/test/resources/arquillian.xml"), ArchivePaths.create("resource.xml"))
                        .addResource("arquillian.xml", ArchivePaths.create("resource2.xml"))
                        .addResource(new File("src/test/resources/arquillian.xml").toURI().toURL(), ArchivePaths.create("resource3.xml"))
                        .addClass(ToolingDeploymentFormatterTestCase.class)
                        .addServiceProvider(ServiceLoader.class, DynamicServiceLoader.class)
                        .addLibrary(
                              ShrinkWrap.create(JavaArchive.class, "test.jar")
                                 .addClass(ToolingDeploymentFormatter.class)
                        )
                        .toString(new ToolingDeploymentFormatter(getClass()));
      
      // TODO: do some output Assertions..
      Assert.assertNotNull(content);
      System.out.println(content);
   }
}
