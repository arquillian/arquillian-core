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

package org.jboss.arquillian.integration.test.common.ext;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MetaInfServices
public class TestExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.observer(ContainerStartedObserver.class)
            .service(ResourceProvider.class, TestArquillianResourceProvider.class)
            .service(AuxiliaryArchiveAppender.class, TestDeploymentAppender.class);
    }
}
