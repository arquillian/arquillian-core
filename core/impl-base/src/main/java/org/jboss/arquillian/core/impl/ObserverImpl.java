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
package org.jboss.arquillian.core.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.logging.Logger;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.InvocationException;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ObserverMethod;

/**
 * ObjectObserver
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ObserverImpl implements ObserverMethod, Comparable<ObserverMethod> {
    private static Logger log = Logger.getLogger(ObserverMethod.class.getName());
    private Object target;
    private Method method;

    //-------------------------------------------------------------------------------------||
    // Public Factory Methods -------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    ObserverImpl(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    public static ObserverImpl of(Object extension, Method observerMethod) {
        return new ObserverImpl(extension, observerMethod);
    }

    //-------------------------------------------------------------------------------------||
    // Required Implementations - ObserverMethod ------------------------------------------||
    //-------------------------------------------------------------------------------------||

    /* (non-Javadoc)
     * @see org.jboss.arquillian.api.Typed#getType()
     */
    @Override
    public Type getType() {
        return method.getGenericParameterTypes()[0];
    }

    /**
     * @return the method
     */
    @Override
    public Method getMethod() {
        return method;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.api.ObserverMethod#invoke(java.lang.Object)
     */
    @Override
    public boolean invoke(Manager manager, Object event) {
        try {
            Object[] arguments = resolveArguments(manager, event);
            if (containsNull(arguments)) {
                return false;
            }
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            method.invoke(target, arguments);
            return true;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable cause = ((InvocationTargetException) e).getTargetException();
                // Exception already wrapped by another Observer down the chain.
                if (cause instanceof InvocationException) {
                    throw (InvocationException) cause;
                }
                throw new InvocationException(cause);
            }
            throw new InvocationException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ObserverMethod o) {
        if (o == null) {
            return 1;
        }
        Integer a = getPresedence(getMethod());
        Integer b = getPresedence(o.getMethod());
        return b.compareTo(a);
    }

    private Integer getPresedence(Method method) {
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == Observes.class) {
                    return ((Observes) annotation).precedence();
                }
            }
        }
        return 0;
    }

    /**
     * Resolve all Observer method arguments. Unresolved argument types wil be null.
     */
    private Object[] resolveArguments(Manager manager, Object event) {
        final Class<?>[] argumentTypes = getMethod().getParameterTypes();
        int numberOfArguments = argumentTypes.length;

        // we know that the first Argument is always the Event, and it will be there else this wouldn't be a Observer method
        Object[] arguments = new Object[numberOfArguments];
        arguments[0] = event;

        for (int i = 1; i < numberOfArguments; i++) {
            final Class<?> argumentType = argumentTypes[i];
            arguments[i] = manager.resolve(argumentType);
            if (RuntimeLogger.DEBUG && arguments[i] == null) {
                log.warning(String.format("Argument %d (of type %s) for %s#%s is null. Observer method won't be invoked.", i + 1,
                    argumentType.getSimpleName(), getMethod().getDeclaringClass().getName(), getMethod().getName()));
            }
        }
        return arguments;
    }

    /**
     * Check that all arguments were resolved. Do not invoke if not.
     */
    private boolean containsNull(Object[] arguments) {
        for (Object argument : arguments) {
            if (argument == null) {
                return true;
            }
        }
        return false;
    }
}
