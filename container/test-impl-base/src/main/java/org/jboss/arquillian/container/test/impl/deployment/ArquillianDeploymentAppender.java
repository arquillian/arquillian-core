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
package org.jboss.arquillian.container.test.impl.deployment;

import org.jboss.arquillian.container.test.impl.ContainerTestRemoteExtension;
import org.jboss.arquillian.container.test.impl.RemoteExtensionLoader;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.arquillian.core.spi.ExtensionLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Appender to package up Arquillian api/spi/impl and ShrinkWrap api/spi/impl
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianDeploymentAppender extends CachedAuxilliaryArchiveAppender
{
   @Override
   protected Archive<?> buildArchive()
   {
      return ShrinkWrap.create(JavaArchive.class, "arquillian-core.jar")
                        .addPackages(
                              true,
                              "org.jboss.arquillian.core",
                              //"org.jboss.arquillian.container.api",
                              "org.jboss.arquillian.container.spi",
                              "org.jboss.arquillian.container.impl",
                              "org.jboss.arquillian.container.test.api",
                              "org.jboss.arquillian.container.test.spi",
                              "org.jboss.arquillian.container.test.impl",
                              "org.jboss.arquillian.config",
                              "org.jboss.arquillian.test",
                              "org.jboss.shrinkwrap.api",
                              "org.jboss.shrinkwrap.descriptor.api")
                        .addAsServiceProvider(RemoteLoadableExtension.class, ContainerTestRemoteExtension.class)
                        .addAsServiceProvider(ExtensionLoader.class, RemoteExtensionLoader.class);
   }
}