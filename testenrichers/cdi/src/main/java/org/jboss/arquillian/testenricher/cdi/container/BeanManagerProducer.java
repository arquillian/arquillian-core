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
package org.jboss.arquillian.testenricher.cdi.container;

import java.util.logging.Logger;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * BeanManagerLookup
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
public class BeanManagerProducer {
    private static final String STANDARD_BEAN_MANAGER_JNDI_NAME = "java:comp/BeanManager";

    private static final String SERVLET_BEAN_MANAGER_JNDI_NAME = "java:comp/env/BeanManager";

    // TODO: Hack until BeanManager binding fixed in JBoss AS
    private static final String JBOSSAS_BEAN_MANAGER_JNDI_NAME = "BeanManager";

    private static final String[] BEAN_MANAGER_JNDI_NAMES =
        {STANDARD_BEAN_MANAGER_JNDI_NAME, SERVLET_BEAN_MANAGER_JNDI_NAME, JBOSSAS_BEAN_MANAGER_JNDI_NAME};

    private static final Logger log = Logger.getLogger(BeanManagerProducer.class.getName());

    @Inject
    @ApplicationScoped
    private InstanceProducer<BeanManager> beanManagerProducer;

    public void findBeanManager(@Observes Context context) {
        BeanManager manager = lookup(context);
        if (manager != null) {
            beanManagerProducer.set(manager);
        }
    }

    private BeanManager lookup(Context context) {
        for (String beanManagerJndiName : BEAN_MANAGER_JNDI_NAMES) {
            try {
                return (BeanManager) context.lookup(beanManagerJndiName);
            } catch (Exception e) {
                log.fine("Tried to lookup the BeanManager with name " + beanManagerJndiName + " but caught exception: "
                    + e.getMessage());
            }
        }

        BeanManager beanManager = CDIExtension.getBeanManager();

        if (beanManager != null) {
            return beanManager;
        }

        log.info("BeanManager not found.");
        return null;
    }
}
