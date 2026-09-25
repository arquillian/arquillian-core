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
package org.jboss.arquillian.container.test.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that the method(s) should be run on the client. By default, methods are run on the server (container).
 * <p>
 * Annotating a {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method will cause it to run on the client before <b>all</b> the
 * test methods in the class, regardless of their run mode (un-annotated {@code @BeforeAll} methods will also run before all test
 * methods). <i>JUnit</i> allows multiple {@code @BeforeAll} methods in the same class; this can be used to have
 * {@code @BeforeAll} methods both on the client and on the server. The order of execution is as per <i>JUnit</i>'s execution
 * rules, that is, {@code @RunAsClient} confers no special ordering rules - {@code @BeforeAll} client methods may be run before or
 * after {@code @BeforeAll} server methods.<br>
 * The same applies for {@link org.junit.jupiter.api.AfterAll @AfterAll} methods.
 * <p>
 * A use case for the above is to setup both the client and the server for tests, such as clearing the database on the server and
 * setting up an HTTP client on the client. (Note that the order of these operations is interchangeable.)
 * <p>
 * Similarly, annotating a {@link org.junit.jupiter.api.Test @Test} (or {@link org.junit.jupiter.api.RepeatedTest @RepeatedTest}
 * or {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest}) method will run it on the client with no special
 * ordering treatment. The standard {@link org.junit.jupiter.api.Order @Order} annotation applies both for client-run and
 * server-run methods together.
 * <p>
 * A use case for the above is to execute a client call to an API and test its result, and then test the change in the container:
 * 
 * <pre><code>
 * &#64;Order(1)
 * &#64;RunAsClient
 * &#64;Test
 * void testUpdateEmailAddressRequest() {
 *     // call the server and receive a response
 *     // assert that the response status code is correct 
 * }
 *
 * &#64;Order(2)
 * &#64;Test
 * void testEmailAddressUpdated() {
 *     // assert that the DB entry for the email address has changed
 * }
 * </code></pre>
 *
 * Annotating a {@link org.junit.jupiter.api.BeforeEach @BeforeEach} method will cause it to run before each {@code @RunAsClient}
 * test method, whereas un-annotated {@code @BeforeEach} methods will run before each un-annotated (server) method.<br>
 * The same applies for {@link org.junit.jupiter.api.AfterEach @AfterEach} methods.
 * <p>
 * If used on a class, all its methods will be run on the client. There is no per-method "opt-out" annotation, such as
 * {@code @RunOnServer} unlike the "opt-in" {@code @RunAsClient} for un-annotated classes.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @see Testable
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RunAsClient {
}
