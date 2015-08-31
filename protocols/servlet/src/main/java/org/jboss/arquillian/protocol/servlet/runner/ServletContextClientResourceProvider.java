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
package org.jboss.arquillian.protocol.servlet.runner;


import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;

/**
 * ServletContextResourceProvider
 *
 * @author asotobu
 */
public class ServletContextClientResourceProvider extends OperatesOnDeploymentAwareProvider {

    @Inject
    Instance<ServletContext> servletContextInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return javax.servlet.ServletContext.class.isAssignableFrom(type);
    }

    @Override
    public Object doLookup(ArquillianResource arquillianResource, Annotation... annotations) {

        if(servletContextInstance.get() == null) {
            throw new IllegalStateException("ServletContext is null");
        }

        return servletContextInstance.get();
    }
}
