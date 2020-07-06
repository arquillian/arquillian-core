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
package org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.application;

import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * DSL Grammar to construct / alter Application XML Descriptors
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface ApplicationDescriptor extends Descriptor {

    ApplicationDescriptor version(String version);

    ApplicationDescriptor displayName(String displayName);

    ApplicationDescriptor description(String description);

    ApplicationDescriptor libraryDirectory(String libraryDirectory);

    ApplicationDescriptor webModule(String uri, String contextRoot);

    ApplicationDescriptor ejbModule(String uri);

    ApplicationDescriptor javaModule(String uri);

    ApplicationDescriptor connectorModule(String uri);

    ApplicationDescriptor securityRole(String roleName);

    ApplicationDescriptor securityRole(String roleName, String description);
}
