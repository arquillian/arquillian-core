/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.testenricher.cdi.container;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.testenricher.cdi.CDIInjectionEnricher;
import org.jboss.arquillian.testenricher.cdi.CreationalContextDestroyer;

/**
 * CDIEnricherRemoteExtension
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CDIEnricherRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        // only load if BeanManager is on ClassPath
        if (Validate.classExists("jakarta.enterprise.inject.spi.BeanManager")) {
            builder.service(TestEnricher.class, CDIInjectionEnricher.class);
            builder.observer(BeanManagerProducer.class)
                .observer(CreationalContextDestroyer.class);
        }
    }
}
