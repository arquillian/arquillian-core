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
package org.jboss.arquillian.container.spi.client.protocol.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ProtocolMetaData
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ProtocolMetaData {
    private List<Object> contexts = new ArrayList<Object>();

    public boolean hasContext(Class<?> clazz) {
        for (Object obj : contexts) {
            if (clazz.isInstance(obj)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public <T> T getContext(Class<T> clazz) {
        for (Object obj : contexts) {
            if (clazz.isInstance(obj)) {
                return clazz.cast(obj);
            }
        }
        return null;
    }

    public <T> Collection<T> getContexts(Class<T> clazz) {
        List<T> filteredContexts = new ArrayList<T>();
        for (Object obj : contexts) {
            if (clazz.isInstance(obj)) {
                filteredContexts.add(clazz.cast(obj));
            }
        }
        return filteredContexts;
    }

    public ProtocolMetaData addContext(Object obj) {
        contexts.add(obj);
        return this;
    }

    /**
     * @return unmodifiable list of contexts
     */
    public List<Object> getContexts() {
        return Collections.unmodifiableList(contexts);
    }

    @Override
    public String toString() {
        return "ProtocolMetaData [contexts=" + toString(contexts) + "]";
    }

    private String toString(List<Object> contexts) {
        StringBuilder sb = new StringBuilder();
        if (contexts != null) {
            for (Object obj : contexts) {
                sb.append('\n').append(obj);
            }
        }
        return sb.toString();
    }
}
