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
package org.jboss.arquillian.core.test.context;

import java.lang.annotation.Annotation;
import org.jboss.arquillian.core.spi.HashObjectStore;
import org.jboss.arquillian.core.spi.context.AbstractContext;
import org.jboss.arquillian.core.spi.context.ObjectStore;

/**
 * ManagerTestContextImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ManagerTestContextImpl extends AbstractContext<String> implements ManagerTestContext {
    private static final String SUITE_CONTEXT_ID = "managerTest";

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.spi.context.NonIdBoundContext#activate()
     */
    @Override
    public void activate() {
        super.activate(SUITE_CONTEXT_ID);
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.spi.context.NonIdBoundContext#destroy()
     */
    @Override
    public void destroy() {
        super.destroy(SUITE_CONTEXT_ID);
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.spi.context.Context#getScope()
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return ManagerTestScoped.class;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.spi.context.AbstractContext#createNewObjectStore()
     */
    @Override
    protected ObjectStore createNewObjectStore() {
        return new HashObjectStore();
    }
}
