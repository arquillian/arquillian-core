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
package org.jboss.arquillian.container.test.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Config class enables users to use fluent API for creating a list of
 * properties which should be overridden in the existing Arquillian
 * configuration. It holds a map of properties that can be retrieved via
 * {@link Config#map()} and the results should be passed e.g. to
 * {@link ContainerController#start(String, Map)}
 *
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 */
public class Config {
    private Map<String, String> props;

    public Config() {
        this.props = new HashMap<String, String>();
    }

    public Map<String, String> getProperties() {
        return props;
    }

    public Config add(String name, String value) {
        props.put(name, value);
        return this;
    }

    public Map<String, String> map() {
        return props;
    }
}
