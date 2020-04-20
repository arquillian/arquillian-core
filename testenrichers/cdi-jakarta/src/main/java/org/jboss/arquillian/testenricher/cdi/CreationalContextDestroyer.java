/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.testenricher.cdi;

import jakarta.enterprise.context.spi.CreationalContext;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.After;

/**
 * CreationalContextDestroyer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CreationalContextDestroyer {
    @SuppressWarnings("rawtypes")
    @Inject
    private Instance<CreationalContext> creationalContext;

    public void destory(@Observes EventContext<After> event) {
        try {
            event.proceed();
        } finally {
            CreationalContext<Object> cc = creationalContext.get();
            if (cc != null) {
                cc.release();
            }
        }
    }
}
