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
package org.jboss.arquillian.test.spi.event.suite;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Base for events fired in the Test execution cycle.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 */
public class TestEvent extends ClassEvent {
    private Object testInstance;
    private Method testMethod;

    /**
     * @param testInstance
     *     The test case instance
     * @param testMethod
     *     The test method
     *
     * @throws IllegalArgumentException
     *     if testInstance is null
     * @throws IllegalArgumentException
     *     if testMethod is null
     */
    public TestEvent(Object testInstance, Method testMethod) {
        super(validateAndExtractClass(testInstance, testMethod));

        this.testInstance = testInstance;
        this.testMethod = testMethod;
    }

    /**
     * Validates the test instance and method, and extracts the top-level test class.
     *
     * <p>Arquillian's class-scoped context — which holds the {@code DeploymentScenario}, container
     * references, and other class-level state — is keyed on the class passed to
     * {@link ClassEvent#ClassEvent(Class)}. For top-level test classes this is simply
     * {@code testInstance.getClass()}. For JUnit 5 {@code @Nested} inner classes, however, the
     * runtime class of the test instance is the inner class itself, while the deployment and
     * class-scoped state belong to the outermost enclosing class (the one that declares the
     * {@code @Deployment} method). If the inner class were used as the context key, Arquillian
     * would activate an empty class context, find no deployment, and default to
     * {@code runAsClient = true} — running the test on the client instead of in the container.</p>
     *
     * <p>To fix this, the method walks up the enclosing-class chain while the class is a
     * non-static member class ({@code isMemberClass() && !isStatic()}). This is safe for all
     * supported test frameworks:</p>
     * <ul>
     *   <li><b>JUnit 5</b> — {@code @Nested} classes are required to be non-static; JUnit 5
     *       ignores the annotation on static inner classes. So the condition
     *       {@code isMemberClass() && !isStatic()} matches exactly the set of {@code @Nested}
     *       classes, without introducing a compile-time dependency on the JUnit 5 API in this
     *       framework-agnostic SPI module.</li>
     *   <li><b>JUnit 4 / TestNG</b> — neither framework supports non-static inner classes as
     *       test classes, so {@code isMemberClass()} is always {@code false} and the loop is
     *       never entered.</li>
     * </ul>
     *
     * @param testInstance the test instance
     * @param testMethod   the test method
     * @return the outermost enclosing class, or the instance's own class if it is not a non-static member class
     */
    private static Class<?> validateAndExtractClass(Object testInstance, Method testMethod) {
        Validate.notNull(testInstance, "TestInstance must be specified");
        Validate.notNull(testMethod, "TestMethod must be specified");

        Class<?> clazz = testInstance.getClass();
        while (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            // This must be a @Nested class - return the enclosing class
            clazz = clazz.getEnclosingClass();
        }
        return clazz;
    }

    public Object getTestInstance() {
        return testInstance;
    }

    public Method getTestMethod() {
        return testMethod;
    }
}
