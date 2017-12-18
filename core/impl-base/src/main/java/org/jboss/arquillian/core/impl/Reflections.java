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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.annotation.Scope;

/**
 * Reflections
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
final class Reflections {
    private Reflections() {
    }

    public static List<Method> getObserverMethods(Class<?> clazz) {
        List<Method> observerMethods = new ArrayList<Method>();
        if (clazz == null) {
            return observerMethods;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (isObserverMethod(method)) {
                observerMethods.add(method);
            }
        }
        observerMethods.addAll(getObserverMethods(clazz.getSuperclass()));
        return observerMethods;
    }

    /**
     * @param class1
     * @return
     */
    public static List<Field> getFieldInjectionPoints(Class<?> clazz) {
        List<Field> injectionPoints = new ArrayList<Field>();
        if (clazz == null) {
            return injectionPoints;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (isInjectionPoint(field)) {
                injectionPoints.add(field);
            }
        }
        injectionPoints.addAll(getFieldInjectionPoints(clazz.getSuperclass()));
        return injectionPoints;
    }

    /**
     * @param class1
     * @return
     */
    public static List<Field> getEventPoints(Class<?> clazz) {
        List<Field> eventPoints = new ArrayList<Field>();
        if (clazz == null) {
            return eventPoints;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (isEventPoint(field)) {
                eventPoints.add(field);
            }
        }
        eventPoints.addAll(getEventPoints(clazz.getSuperclass()));
        return eventPoints;
    }

    public static Class<? extends Annotation> getScope(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.isAnnotationPresent(Scope.class)) {
                return annotationType;
            }
        }
        return null;
    }

    public static <T> T createInstance(Class<T> clazz) throws Exception {
        return SecurityActions.newInstance(clazz, new Class<?>[0], new Object[0]);
    }

    public static boolean isType(Type type, Class<?> clazz) {
        if (type instanceof Class<?>) {
            return type == clazz;
        } else if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getRawType() == clazz;
        }
        return false;
    }

    public static Class<?> getType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getType(((ParameterizedType) type).getActualTypeArguments()[0]);
        } else if (type instanceof WildcardType) {
            for (Type wildType : ((WildcardType) type).getUpperBounds()) {
                Type upperType = getType(wildType);
                if (upperType != null) {
                    return getType(upperType);
                }
            }
            for (Type wildType : ((WildcardType) type).getLowerBounds()) {
                Type lowerType = getType(wildType);
                if (lowerType != null) {
                    return getType(lowerType);
                }
            }
        }
        return null;
    }

    //-------------------------------------------------------------------------------------||
    // Internal Helper Methods ------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    /**
     * @param field
     * @return
     */
    private static boolean isEventPoint(Field field) {
        return field.isAnnotationPresent(Inject.class) && field.getType() == Event.class;
    }

    /**
     * @param field
     * @return
     */
    private static boolean isInjectionPoint(Field field) {
        if (field.isAnnotationPresent(Inject.class)) {
            if (field.getType() == Instance.class) {
                return true;
            }
            if (field.getType() == InstanceProducer.class) {
                if (Reflections.getScope(field) != null) {
                    return true;
                } else {
                    // TODO: join extension validation points.
                    throw new RuntimeException("A InjectionPoint of type "
                        + InstanceProducer.class.getName()
                        + " must define a "
                        + Scope.class.getName()
                        + " annotation, bad definition for field: "
                        + field);
                }
            }
        }
        return false;
    }

    /**
     * @param method
     * @return
     */
    private static boolean isObserverMethod(Method method) {
        if (method.getParameterTypes().length < 1 || method.getParameterAnnotations().length < 1) {
            return false;
        }

        if (method.isBridge()) {
            return false;
        }

        if (containsAnnotation(Observes.class, method.getParameterAnnotations()[0])) {
            return true;
        }
        return false;
    }

    /**
     * @param annotations
     * @return
     */
    private static boolean containsAnnotation(Class<? extends Annotation> match, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == match) {
                return true;
            }
        }
        return false;
    }
}