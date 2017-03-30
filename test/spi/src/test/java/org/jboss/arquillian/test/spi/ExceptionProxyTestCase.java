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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import junit.framework.Assert;
import org.junit.Test;

/**
 * ExceptionProxyTestCase
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

    @Test
    public void shouldSerializeNonSerializableExceptions() throws Exception {
        ExceptionProxy proxy = serialize(ExceptionProxy.createForException(new NonSerializableException()));
        Throwable t = proxy.createException();

        Assert.assertEquals(ArquillianProxyException.class, t.getClass());
        Assert.assertTrue(
            "Verify Proxy message contain root exception of serialization problem",
            t.getMessage().contains("java.io.NotSerializableException"));
        Assert.assertTrue(
            "Verify Proxy message contain root cause of serialization problem",
            t.getMessage().contains("BufferedInputStream"));
        Assert.assertEquals(UnsupportedOperationException.class, t.getCause().getClass());
    }

    @Test
    public void shouldSerializeNonDeSerializableExceptions() throws Exception {
        ExceptionProxy proxy = serialize(ExceptionProxy.createForException(new NonDeserializableExtension("Test")));
        Throwable t = proxy.createException();

        Assert.assertEquals(ArquillianProxyException.class, t.getClass());
        Assert.assertTrue(
            "Verify Proxy message contain root exception of deserialization problem",
            t.getMessage().contains("NonDeserializableExtension"));
        Assert.assertTrue(
            "Verify Proxy message contain root cause of deserialization problem",
            t.getMessage().contains("Could not de-serialize"));
        Assert.assertEquals(UnsupportedOperationException.class, t.getCause().getClass());
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

    private ExceptionProxy serialize(ExceptionProxy proxy) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(output);
        out.writeObject(proxy);

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(output.toByteArray()));
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

    // Simulate org.jboss.weld.exceptions.IllegalArgumentException
    private static class ExtendedIllegalArgumentException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        public ExtendedIllegalArgumentException(Exception throwable) {
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
