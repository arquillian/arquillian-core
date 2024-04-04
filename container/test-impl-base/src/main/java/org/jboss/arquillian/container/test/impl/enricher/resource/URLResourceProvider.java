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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.api.Secured;

/**
 * URLResourceProvider
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="http://community.jboss.org/people/silenius">Samuel Santos</a>
 * @version $Revision: $
 */
public class URLResourceProvider extends OperatesOnDeploymentAwareProvider {
    @Inject
    private Instance<ProtocolMetaData> protocolMetadata;

    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(URL.class);
    }

    @Override
    public Object doLookup(ArquillianResource resource, Annotation... qualifiers) {
        return locateURL(resource, qualifiers);
    }

    private Object locateURL(ArquillianResource resource, Annotation[] qualifiers) {
        ProtocolMetaData metaData = protocolMetadata.get();
        if (metaData == null) {
            return null;
        }

        TargetsContainer targets = locateTargetQualification(qualifiers);
        Secured secured = locateSecureQualification(qualifiers);
        if (metaData.hasContext(HTTPContext.class)) {
            HTTPContext context = null;
            if (targets != null) {
                context = locateNamedHttpContext(metaData, targets.value());
            } else {
                context = metaData.getContexts(HTTPContext.class).iterator().next();
            }

            if (resource.value() != null && resource.value() != ArquillianResource.class) {
                // TODO: we need to check for class. Not all containers have ServletClass available.
                Servlet servlet = context.getServletByName(resource.value().getSimpleName());
                if (servlet == null) {
                    servlet = context.getServletByName(resource.value().getName());
                    //throw new RuntimeException("No Servlet named " + resource.value().getSimpleName() + " found in metadata");
                }
                if (servlet == null) {
                    return null;
                }
                return toURL(servlet, secured);
            }
            // TODO: evaluate, if all servlets are in the same context, and only one context exists, we can find the context
            else if (allInSameContext(context.getServlets())) {
                return toURL(context.getServlets().get(0), secured);
            } else {
                return toURL(context, secured);
            }
        }
        return null;
    }

    private HTTPContext locateNamedHttpContext(ProtocolMetaData metaData, String value) {
        for (HTTPContext context : metaData.getContexts(HTTPContext.class)) {
            if (value.equals(context.getName())) {
                return context;
            }
        }
        throw new IllegalArgumentException(
            "Could not find named context " + value + " in metadata. " +
                "Please verify your @" + TargetsContainer.class.getName() + " definition");
    }

    private TargetsContainer locateTargetQualification(Annotation[] qualifiers) {
        for (Annotation qualifier : qualifiers) {
            if (TargetsContainer.class.isAssignableFrom(qualifier.annotationType())) {
                return TargetsContainer.class.cast(qualifier);
            }
        }
        return null;
    }

    private Secured locateSecureQualification(Annotation[] qualifiers) {
        for(Annotation qualifier : qualifiers) {
            if(Secured.class.isAssignableFrom(qualifier.annotationType())) {
                return Secured.class.cast(qualifier);
            }
        }
        return null;
    }

    private boolean allInSameContext(List<Servlet> servlets) {
        Set<String> context = new HashSet<String>();
        for (Servlet servlet : servlets) {
            context.add(servlet.getContextRoot());
        }
        return context.size() == 1;
    }

    private URL toURL(Servlet servlet, Secured secured) {
        try {
            URI baseURI = servlet.getBaseURI();
            String scheme = (secured == null) ? baseURI.getScheme() : secured.scheme();
            int port = (secured == null) ? baseURI.getPort() : secured.port();
            return new URI(scheme, baseURI.getUserInfo(), baseURI.getHost(),
                port, baseURI.getPath(), baseURI.getQuery(),
                baseURI.getFragment()).toURL();
        } catch (Exception e) {
            throw new RuntimeException("Could not convert Servlet to URL, " + servlet, e);
        }
    }

    private URL toURL(HTTPContext context, Secured secured) {
        try {
            String scheme = (secured == null) ? context.getScheme() : secured.scheme();
            int port = (secured == null) ? context.getPort() : secured.port();
            return new URI(scheme, null, context.getHost(), port, null, null, null).toURL();
        } catch (Exception e) {
            throw new RuntimeException("Could not convert HTTPContext to URL, " + context, e);
        }
    }
}
