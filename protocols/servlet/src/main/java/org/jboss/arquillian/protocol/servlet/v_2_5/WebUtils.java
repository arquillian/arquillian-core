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
package org.jboss.arquillian.protocol.servlet.v_2_5;

import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * Common util for Web.xml 2.5 manipulation
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
class WebUtils {
    private WebUtils() {
    }

    static WebAppDescriptor createNewDescriptor() {
        return mergeWithDescriptor(getDefaultDescriptor());
    }

    static WebAppDescriptor getDefaultDescriptor() {
        return Descriptors.create(WebAppDescriptor.class)
            .version("2.5")
            .displayName("Arquillian Servlet 2.5 Protocol");
    }

    static WebAppDescriptor mergeWithDescriptor(WebAppDescriptor descriptor) {
        // use String v. of desc.servlet(..) so we don't force Servlet API on classpath
        descriptor.servlet(
            ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME,
            "org.jboss.arquillian.protocol.servlet.runner.ServletTestRunner",
            new String[] {ServletMethodExecutor.ARQUILLIAN_SERVLET_MAPPING});
        return descriptor;
    }
}
