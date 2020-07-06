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
package org.jboss.arquillian.protocol.servlet5;

import java.net.URI;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;

/**
 * ServletUtil
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class ServletUtil {
    public static final ArchivePath WEB_XML_PATH = ArchivePaths.create("WEB-INF/web.xml");
    public static final ArchivePath APPLICATION_XML_PATH = ArchivePaths.create("META-INF/application.xml");

    private ServletUtil() {
    }

    public static URI determineBaseURI(ServletProtocolConfiguration config, HTTPContext context, String servletName) {
        String scheme = config.getScheme();
        String host = config.getHost();
        Integer port = config.getPort();

        // TODO: can not set contextRoot in config, change to prefixContextRoot
        String contextRoot = null; //protocolConfiguration.getContextRoot();

        Servlet servlet = context.getServletByName(servletName);
        if (servlet != null) {
            // use the context where the Arquillian servlet is found
            if (scheme == null) {
                scheme = "http";
            }
            if (host == null) {
                host = context.getHost();
            }
            if (port == null) {
                port = context.getPort();
            }
            contextRoot = servlet.getContextRoot();
        } else {
            throw new IllegalArgumentException(
                servletName + " not found. " +
                    "Could not determine ContextRoot from ProtocolMetadata, please contact DeployableContainer developer.");
        }
        return URI.create(scheme + "://" + host + ":" + port + contextRoot);
    }

    public static String calculateContextRoot(String archiveName) {
        String correctedName = archiveName;
        if (correctedName.startsWith("/")) {
            correctedName = correctedName.substring(1);
        }
        if (correctedName.indexOf(".") != -1) {
            correctedName = correctedName.substring(0, correctedName.lastIndexOf("."));
        }
        return correctedName;
    }
}
