/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.web;

import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * DSL Grammar to construct / alter Web Application XML Descriptors
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public interface WebAppDescriptor extends Descriptor {

    WebAppDescriptor version(String version);

    WebAppDescriptor displayName(String displayName);

    ServletDef servlet(String clazz, String... urlPatterns);

    ServletDef servlet(String name, String clazz, String[] urlPatterns);
}
