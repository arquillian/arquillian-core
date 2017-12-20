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
package org.jboss.arquillian.test.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Wraps a class to be run, providing method validation and annotation searching.
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class TestClass {
    private Class<?> testClass;

    public TestClass(Class<?> testClass) {
        if (testClass == null) {
            throw new IllegalArgumentException("Null testClass");
        }

        this.testClass = testClass;
    }

    public Class<?> getJavaClass() {
        return testClass;
    }

    public String getName() {
        return testClass.getName();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return testClass.isAnnotationPresent(annotation);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return testClass.getAnnotation(annotation);
    }

    public Method getMethod(Class<? extends Annotation> annotation) {
        Method[] methods = getMethods(annotation);
        if (methods.length > 0) {
            return methods[0];
        }
        return null;
    }

    public Method[] getMethods(Class<? extends Annotation> annotation) {
        return SecurityActions.getMethodsWithAnnotation(testClass, annotation).toArray(new Method[0]);
    }
}