/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.testenricher.cdi.client;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.arquillian.testenricher.cdi.CDIInjectionEnricher;
import org.jboss.arquillian.testenricher.cdi.container.CDIEnricherRemoteExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * CDIEnricherArchiveAppender
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CDIEnricherArchiveAppender extends CachedAuxilliaryArchiveAppender
{
   @Override
   protected Archive<?> buildArchive()
   {
      return ShrinkWrap.create(JavaArchive.class, "arquillian-testenricher-cdi.jar")
                  .addPackages(false, 
                        CDIInjectionEnricher.class.getPackage(),
                        CDIEnricherRemoteExtension.class.getPackage())
                  // We can't use Extension.class, CDI API might not be available during package time
                  .addAsManifestResource(
                        new StringAsset("org.jboss.arquillian.testenricher.cdi.container.CDIExtension"), 
                        "services/javax.enterprise.inject.spi.Extension")
                  .addAsServiceProvider(RemoteLoadableExtension.class, CDIEnricherRemoteExtension.class);
   }
}
