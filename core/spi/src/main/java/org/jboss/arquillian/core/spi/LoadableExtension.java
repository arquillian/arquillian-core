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
package org.jboss.arquillian.core.spi;

import org.jboss.arquillian.core.spi.context.Context;

/**
 * LoadableExtension.
 * <p>
 * Loadable extensions are loaded on the local side of Arquillan. For extensions, components, observers etc to run on
 * the remote side, use {@code RemoteLoadableExtension} instead, and provide it via an
 * {@code AuxilliaryArchiveAppender}.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface LoadableExtension {
    /**
     * Implement to register any extensions.
     */
    public void register(ExtensionBuilder builder);

    public interface ExtensionBuilder {
        /**
         * Register a service implementation.
         * <p>
         * The service can be looked up from the {@link ServiceLoader}. When instantiated, it will be injected
         * according to any {@link org.jboss.arquillian.core.api.annotation.Inject} annotated
         * {@link org.jboss.arquillian.core.api.Instance} fields.
         * <p>
         * Note that services are not automatically available for dependency injection, they must be provided
         * explicitly to an {@link org.jboss.arquillian.core.api.InstanceProducer}.
         */
        <T> ExtensionBuilder service(Class<T> service, Class<? extends T> impl);

        /**
         * Override a service.
         */
        <T> ExtensionBuilder override(Class<T> service, Class<? extends T> oldServiceImpl,
            Class<? extends T> newServiceImpl);

        /**
         * Register an observer for events. This observer will be injected according to any
         * {@link org.jboss.arquillian.core.api.annotation.Inject} annotated
         * {@link org.jboss.arquillian.core.api.Instance} fields.
         */
        ExtensionBuilder observer(Class<?> handler);

        /**
         * Register a context.
         */
        ExtensionBuilder context(Class<? extends Context> context);
    }

    public static class Validate {
        public static boolean classExists(String className) {
            try {
                Class.forName(className);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
