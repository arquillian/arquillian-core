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
package org.jboss.arquillian.test.spi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Assert;
import org.junit.Test;

/**
 * ExceptionProxyTestCase
 * Updated for https://github.com/arquillian/arquillian-core/issues/641
 * where the exception seen by a client that did not have the exception class
 * thrown from a server was not on the client classpath.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ExceptionProxyTestCase {
    public static String MSG = "_TEST_";

    @Test(expected = IllegalArgumentException.class)
    public void shouldProxyIllegalArgumentException() throws Throwable {
        proxy(new IllegalArgumentException(MSG));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldProxyExtendedIllegalArgumentException() throws Throwable {
        proxy(new ExtendedIllegalArgumentException(new Exception(MSG)));
    }

    @Test(expected = UnsatisfiedResolutionException.class)
    public void shouldProxyUnsatisfiedResolutionException() throws Throwable {
        proxy(new UnsatisfiedResolutionException(new Exception(MSG)));
    }

    /**
     * This tests that an exception that fails to serialize can be proxied and
     * the client can see the message
     * @throws Exception
     */
    @Test
    public void shouldSerializeNonSerializableExceptions() throws Exception {
        ExceptionProxy proxy = serialize(ExceptionProxy.createForException(new NonSerializableException()));
        Throwable t = proxy.createException();

        Assert.assertEquals(NonSerializableException.class, t.getClass());
        Assert.assertTrue(
            "NonSerializableException should have a message",
            t.getMessage().contains("UnsupportedOperationException"));
        // Since the cause is set by the NonSerializableException.ctor, this should be seen
        Assert.assertEquals(UnsupportedOperationException.class, t.getCause().getClass());
        // The proxy should have a serializationProcessException
        Assert.assertTrue(
            "Verify Proxy message contain root cause of deserialization problem",
            proxy.getSerializationProcessException().getMessage().contains("BufferedInputStream"));
    }

    /**
     * This tests that an exception that fails to de-serialize
     * can be proxied and the client can see the message
     * @throws Exception
     */
    @Test
    public void shouldSerializeNonDeSerializableExceptions() throws Exception {
        ExceptionProxy proxy = serialize(ExceptionProxy.createForException(new NonDeserializableExtension("Test")));
        Throwable t = proxy.createException();

        Assert.assertEquals(NonDeserializableExtension.class, t.getClass());
        Assert.assertTrue(
            "The exception should have original message",
            t.getMessage().contains("Test"));
        // Since the cause is set by the NonDeserializableExtension.ctor, this should be seen
        Assert.assertEquals(UnsupportedOperationException.class, t.getCause().getClass());
        // The proxy should have a serializationProcessException
        Assert.assertTrue(
            "Verify Proxy message contain root cause of deserialization problem",
            proxy.getSerializationProcessException().getMessage().contains("Could not de-serialize"));
    }

    @Test
    public void shouldRecreateInvocationTargetExceptions() throws Exception {
        ExceptionProxy proxy = serialize(ExceptionProxy.createForException(
            new InvocationTargetException(new RuntimeException(new ClassNotFoundException()))));
        Throwable t = proxy.createException();

        Assert.assertEquals(InvocationTargetException.class, t.getClass());
        Assert.assertEquals(RuntimeException.class, t.getCause().getClass());
        Assert.assertEquals(ClassNotFoundException.class, t.getCause().getCause().getClass());
    }

    /**
     * Test that the client can handle a server exception that is not on the client classpath by
     * being able to see the common exception supertypes that are on the client classpath.
     * @throws Throwable
     */
    @Test
    public void handleExceptionClassNotOnClientClasspath() throws Throwable {
        // Create the exception using a classloader that is not the client classloader
        Throwable serverException = causeServerException();
        System.out.println("Loaded server exception: " + serverException);
        ExceptionProxy proxy = serialize(ExceptionProxy.createForException(serverException));
        Throwable t = proxy.createException();
        System.out.println("Client exception from proxy: " + t);
        System.out.println("Client exception trace from proxy:");
        t.printStackTrace();
        Assert.assertEquals(IException.class, t.getClass());
        //Assert.assertEquals(ClassNotFoundException.class, t.getCause().getClass());
    }

    private Throwable causeServerException() throws Exception {
        // Create a ClassLoader for the target/serveronly-classes dir
        File serverOnlyClasses = new File("target/serveronly-classes");
        Assert.assertTrue("target/serveronly-classes should exist", serverOnlyClasses.exists());
        URL[] serveronlyCP = {serverOnlyClasses.toURL()};
        URLClassLoader classLoader = new URLClassLoader(serveronlyCP, getClass().getClassLoader());
        Class<IBean> exClass = (Class<IBean>) classLoader.loadClass("org.jboss.arquillian.test.spi.serveronly.SomeBean");
        IBean bean = exClass.newInstance();
        Throwable exception = null;
        try {
            bean.invoke();
        } catch (Exception e) {
            exception = e;
        }
        return exception;
    }

    private ExceptionProxy serialize(ExceptionProxy proxy) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(output);
        out.writeObject(proxy);
        out.close();
        byte[] data = output.toByteArray();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        return (ExceptionProxy) in.readObject();
    }

    private void proxy(Throwable throwable) throws Throwable {
        //printConstructors(throwable);

        throw ExceptionProxy.createForException(throwable).createException();
    }

    /**
     * @param throwable
     */
    @SuppressWarnings("unused")
    private void printConstructors(Throwable throwable) throws Exception {
        System.out.println("Declared-Constructors for: " + throwable.getClass());
        for (Constructor<?> constructor : throwable.getClass().getDeclaredConstructors()) {
            System.out.println(constructor);
        }
    }

    /** Simulate org.jboss.weld.exceptions.IllegalArgumentException
     * Note, this does not simulate the case of weld implementation classes not
     * being on the test client classpath, which is the norm.
     */
    private static class ExtendedIllegalArgumentException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        ExtendedIllegalArgumentException(Exception throwable) {
            super(throwable);
        }
    }

    // simulate javax.enterprise.inject
    public static class UnsatisfiedResolutionException extends ResolutionException {
        private static final long serialVersionUID = 5350603312442756709L;

        public UnsatisfiedResolutionException() {
            super();
        }

        public UnsatisfiedResolutionException(String message, Throwable throwable) {
            super(message, throwable);
        }

        public UnsatisfiedResolutionException(String message) {
            super(message);
        }

        public UnsatisfiedResolutionException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class ResolutionException extends InjectionException {
        private static final long serialVersionUID = -6280627846071966243L;

        public ResolutionException() {
            super();
        }

        public ResolutionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ResolutionException(String message) {
            super(message);
        }

        public ResolutionException(Throwable cause) {
            super(cause);
        }
    }

    public static class InjectionException extends RuntimeException {
        private static final long serialVersionUID = -2132733164534544788L;

        public InjectionException() {
        }

        public InjectionException(String message, Throwable throwable) {
            super(message, throwable);
        }

        public InjectionException(String message) {
            super(message);
        }

        public InjectionException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class NonSerializableException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        private InputStream input;

        public NonSerializableException() {
            super(new UnsupportedOperationException());
            input = System.in;
        }
    }

    public static class NonDeserializableExtension extends RuntimeException implements Externalizable {

        public NonDeserializableExtension() {
        }

        public NonDeserializableExtension(String message) {
            super(message, new UnsupportedOperationException());
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeChars(getMessage());
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            throw new RuntimeException("Could not de-serialize");
        }
    }
}
