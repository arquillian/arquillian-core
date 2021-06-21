/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.testenricher.cdi;

import java.lang.reflect.Method;
import java.util.logging.Logger;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.annotation.TestScoped;

/**
 * Enricher that provide JSR-299 CDI class and method argument injection.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CDIInjectionEnricher implements TestEnricher {
    private static final String ANNOTATION_NAME = "jakarta.inject.Inject";

    private static final Logger log = Logger.getLogger(TestEnricher.class.getName());

    @Inject
    private Instance<BeanManager> beanManagerInst;

    @SuppressWarnings("rawtypes")
    @Inject
    @TestScoped // keep it raw, core does not like generics to much
    private InstanceProducer<CreationalContext> creationalContextProducer;

    /**
     * @return the beanManagerInst
     */
    public BeanManager getBeanManager() {
        return beanManagerInst.get();
    }

    @SuppressWarnings("unchecked")
    public CreationalContext<Object> getCreationalContext() {
        CreationalContext<Object> cc = creationalContextProducer.get();
        if (cc == null) {
            cc = getBeanManager().createCreationalContext(null);
            creationalContextProducer.set(cc);
        }
        return cc;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.TestEnricher#enrich(org.jboss.arquillian.spi.Context, java.lang.Object)
     */
    public void enrich(Object testCase) {
        if (SecurityActions.isClassPresent(ANNOTATION_NAME)) {
            injectClass(testCase);
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.TestEnricher#resolve(org.jboss.arquillian.spi.Context, java.lang.reflect.Method)
     */
    public Object[] resolve(Method method) {
        Object[] values = new Object[method.getParameterTypes().length];
        if (SecurityActions.isClassPresent(ANNOTATION_NAME)) {
            BeanManager beanManager = getBeanManager();
            if (beanManager == null) {
                return values;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                try {
                    values[i] = getInstanceByType(beanManager, i, method);
                } catch (Exception e) {
                    log.fine("CDIEnricher tried to lookup method parameter of type "
                        + parameterTypes[i]
                        + " but caught exception: "
                        + e.getMessage());
                }
            }
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstanceByType(BeanManager manager, final int position, final Method method) {
        CreationalContext<?> cc = getCreationalContext();
        return (T) manager.getInjectableReference(new MethodParameterInjectionPoint<T>(method, position, manager), cc);
    }

    protected void injectClass(Object testCase) {
        try {
            BeanManager beanManager = getBeanManager();
            if (beanManager != null) {
                injectNonContextualInstance(beanManager, testCase);
            } else {
                // Better would be to raise an exception if @Inject is present in class and BeanManager cannot be found
                log.fine(
                    "BeanManager cannot be located in context. Either you are using an archive with no beans.xml or the BeanManager has not been produced.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not inject members", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void injectNonContextualInstance(BeanManager manager, Object instance) {
        CreationalContext<Object> creationalContext = getCreationalContext();
        InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) manager
                .getInjectionTargetFactory(manager.createAnnotatedType(instance.getClass()))
                .createInjectionTarget(null);
        injectionTarget.inject(instance, creationalContext);
    }
}
