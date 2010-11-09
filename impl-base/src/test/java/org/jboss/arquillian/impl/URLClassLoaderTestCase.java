/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.impl;

import java.net.URLClassLoader;

import org.jboss.arquillian.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.shrinkwrap.dependencies.Dependencies;
import org.junit.Ignore;
import org.junit.Test;

/**
 * URLClassLoaderTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class URLClassLoaderTestCase
{

   @Test @Ignore // dev test
   public void create() throws Exception
   {
      ClassLoader cl = new URLClassLoader(MapObject.convert(
            Dependencies.artifact("org.jboss.arquillian.container:arquillian-jbossas-remote-6:1.0.0-SNAPSHOT")
                        .artifact("org.jboss.jbossas:jboss-as-client:pom:6.0.0.20100911-M5").resolveAsFiles())
                  );
      
      Thread.currentThread().setContextClassLoader(cl);
      
      Class<?> deployableContainerClass = cl.loadClass("org.jboss.arquillian.container.jbossas.remote_6.JBossASRemoteContainer");
      
      DeployableContainer container = new DynamicServiceLoader().onlyOne(cl, DeployableContainer.class);
      container.setup((ContainerConfiguration)cl.loadClass("org.jboss.arquillian.container.jbossas.remote_6.JBossASConfiguration").newInstance());
      container.start();
   }
}
