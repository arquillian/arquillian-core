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
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.spi.InjectionPoint;
import org.jboss.arquillian.core.spi.InvocationException;

/**
 * FieldInjectionPointImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class InjectionPointImpl implements InjectionPoint {
    private Object target;
    private Field field;
    private Class<? extends Annotation> scope;

    //-------------------------------------------------------------------------------------||
    // Public Factory Methods -------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    InjectionPointImpl(Object target, Field field, Class<? extends Annotation> scope) {
        this.target = target;
        this.field = field;
        this.scope = scope;
    }

    public static InjectionPointImpl of(Object target, Field field) {
        return new InjectionPointImpl(target, field, Reflections.getScope(field));
    }

    //-------------------------------------------------------------------------------------||
    // Required Implementations - InjectionPoint ------------------------------------------||
    //-------------------------------------------------------------------------------------||

    /* (non-Javadoc)
     * @see org.jboss.arquillian.api.Typed#getType()
     */
    @Override
    public Type getType() {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        if (type.getActualTypeArguments()[0] instanceof ParameterizedType) {
            ParameterizedType first = (ParameterizedType) type.getActualTypeArguments()[0];
            return first.getRawType();
        } else {
            return type.getActualTypeArguments()[0];
        }
    }

    public Class<? extends Annotation> getScope() {
        return scope;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.api.InjectionPoint#set(org.jboss.arquillian.api.Instance)
     */
    @Override
    public void set(Instance<?> value) throws InvocationException {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(target, value);
        } catch (Exception e) {
            throw new InvocationException(e.getCause());
        }
    }
}
