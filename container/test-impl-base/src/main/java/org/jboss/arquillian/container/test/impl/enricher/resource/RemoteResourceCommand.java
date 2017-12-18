/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.lang.annotation.Annotation;
import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;
import org.jboss.arquillian.test.api.ArquillianResource;

public class RemoteResourceCommand extends AbstractCommand<Object> {

    private static final long serialVersionUID = 1L;

    private Class<?> type;
    private ArquillianResource resource;
    private Annotation[] annotations;

    public RemoteResourceCommand(Class<?> type, ArquillianResource resource, Annotation[] annotations) {
        this.type = type;
        this.resource = resource;
        this.annotations = annotations;
    }

    public Class<?> getType() {
        return type;
    }

    public ArquillianResource getResource() {
        return resource;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }
}
