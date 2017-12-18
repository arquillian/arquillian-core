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
package org.jboss.arquillian.test.spi.enricher.resource;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.jboss.arquillian.test.api.ArquillianResource;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * ResourceProvider
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface ResourceProvider {
    boolean canProvide(Class<?> type);

    Object lookup(ArquillianResource resource, Annotation... qualifiers);

    /**
     * This annotation is put to {@link ResourceProvider#lookup(ArquillianResource, Annotation...)} qualifiers parameter
     * so
     * implementation of ResourceProvider can enrich it knowing it enriches class scoped ArquillianResource.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME) @interface ClassInjection {
    }

    /**
     * This annotation is put to {@link ResourceProvider#lookup(ArquillianResource, Annotation...)} qualifiers parameter
     * so
     * implementation of ResourceProvider can enrich it knowing it enriches method scoped ArquillianResource.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     */
    @Documented
    @Retention(RUNTIME) @interface MethodInjection {
    }
}
