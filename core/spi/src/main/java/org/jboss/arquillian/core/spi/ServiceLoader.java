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
package org.jboss.arquillian.core.spi;

import java.util.Collection;

/**
 * ServiceLoader.
 * <p>
 * This is the mechanism that can be used to load services registered by {@link LoadableExtension}'s. For example,
 * to make a service available for dependency injection:
 * <p>
 * <pre>
 * &#064;Inject
 * private Instance&lt;ServiceLoader&gt; serviceLoader;
 *
 * &#064;Inject
 * &#064;ApplicationScoped
 * private InstanceProducer&lt;MyService&gt; myService;
 *
 * public void provideMyService(&#064;Observes ManagerStarted event) {
 *     MyService service = serviceLoader.get().onlyOne(MyService.class, MyDefaultService.class);
 *     myService.set(service);
 * }
 * </pre>
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface ServiceLoader {
    /**
     * Load multiple service implementations.
     *
     * @param serviceClass
     *     The service interface to load a implementations for
     *
     * @return A {@link Collection} of all instances of serviceClass
     */
    <T> Collection<T> all(Class<T> serviceClass);

    /**
     * Load a single service implementation.
     * <p>
     * Method should throw {@link IllegalStateException} if multiple instances of serviceClass found.
     *
     * @param serviceClass
     *     The service interface to load a implementation for
     *
     * @return A instance of serviceClass
     *
     * @throws IllegalStateException
     *     if more then one implementation of serviceClass found
     */
    <T> T onlyOne(Class<T> serviceClass);

    /**
     * Load a single service implementation.
     * <p>
     * Method should returns a new instance of defaultServiceClass if no other instance is found.
     *
     * @param serviceClass
     *     The service interface to load a implementation for
     * @param defaultServiceClass
     *     If no other implementations found, create a instance of this class
     *
     * @return A instance of serviceClass
     */
    <T> T onlyOne(Class<T> serviceClass, Class<? extends T> defaultServiceClass);
}
