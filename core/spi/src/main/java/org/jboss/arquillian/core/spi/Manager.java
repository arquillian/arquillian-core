/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
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

import java.lang.annotation.Annotation;

/**
 * Manager
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface Manager {
    // Event
    void fire(Object event);

    <T> void fire(T event, NonManagedObserver<T> observer);

    // Contextual
    <T> T resolve(Class<T> type);

    <T> void bind(Class<? extends Annotation> scope, Class<T> type, T instance);

    // Injector
    void inject(Object obj);

    // Contexts
    <T> T getContext(Class<T> type);

    // startup
    void start();

    // clean
    void shutdown();

    void addExtension(Class<?> extension) throws Exception;

    void removeExtension(Class<?> extension) throws Exception;
}
