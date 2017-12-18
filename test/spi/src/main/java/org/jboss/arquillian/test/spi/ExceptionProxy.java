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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Takes an exception class and creates a proxy that can be used to rebuild the
 * exception. The problem stems from problems serializing exceptions and
 * deserializing them in another application where the exception classes might
 * not exist, or they might exist in different version. This proxy also
 * propagates the stacktrace and the cause exception to create totally portable
 * exceptions. </p> This class creates a serializable proxy of the exception and
 * when unserialized can be used to re-create the exception based on the
 * following rules :
 * <ul>
 * <li>If the exception class exists on the client, the original exception is
 * created</li>
 * <li>If the exception class exists, but doesn't have a suitable constructor
 * then another exception is thrown referencing the original exception</li>
 * <li>If the exception class exists, but is not throwable, another exception is
 * thrown referencing the original exception</li>
 * <li>If the exception class doesn't exist, another exception is raised instead
 * </li>
 * </ul>
 *
 * @author <a href="mailto:contact@andygibson.net">Andy Gibson</a>
 */
public class ExceptionProxy implements Externalizable {

    private static final long serialVersionUID = 2321010311438950147L;

    private String className;

    private String message;

    private StackTraceElement[] trace;

    private ExceptionProxy causeProxy;

    private Throwable cause;

    private Throwable original;

    private Throwable serializationProcessException = null;

    public ExceptionProxy() {
    }

    public ExceptionProxy(Throwable throwable) {
        this.className = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.trace = throwable.getStackTrace();
        this.causeProxy = ExceptionProxy.createForException(throwable.getCause());
        this.original = throwable;
    }

    /**
     * Static method to create an exception proxy for the passed in
     * {@link Throwable} class. If null is passed in, null is returned as the
     * exception proxy
     *
     * @param throwable
     *     Exception to proxy
     *
     * @return An ExceptionProxy representing the exception passed in
     */
    public static ExceptionProxy createForException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return new ExceptionProxy(throwable);
    }

    /**
     * Indicates whether this proxy wraps an exception
     *
     * @return Flag indicating an exception is wrapped.
     */
    public boolean hasException() {
        return className != null;
    }

    /**
     * Constructs an instance of the proxied exception based on the class name,
     * message, stack trace and if applicable, the cause.
     *
     * @return The constructed {@link Throwable} instance
     */
    public Throwable createException() {
        if (!hasException()) {
            return null;
        }
        if (original != null) {
            return original;
        }

        Throwable throwable = createProxyException(
            "Original exception caused: " + (serializationProcessException != null
                ? serializationProcessException.getClass() + ": " + serializationProcessException.getMessage()
                : "Unknown serialization issue"));
        return throwable;
    }

    public ArquillianProxyException createProxyException(String reason) {
        ArquillianProxyException exception = new ArquillianProxyException(message, className, reason, getCause());
        exception.setStackTrace(trace);
        return exception;
    }

    /**
     * Returns the cause of the exception represented by this proxy
     *
     * @return The cause of this exception
     */
    public Throwable getCause() {
        // lazy create cause
        if (cause == null) {
            if (causeProxy != null) {
                cause = causeProxy.createException();
            }
        }
        return cause;
    }

    /**
     * Custom Serialization logic.
     * <p>
     * If possible, we try to keep the original Exception form the Container side.
     * <p>
     * If we can't load the Exception on the client side, return a ArquillianProxyException that keeps the original stack
     * trace etc.
     * <p>
     * We can't use in.readObject() on the Throwable cause, because if a ClassNotFoundException is thrown, the stream is
     * marked with the exception
     * and that stream is the same stream that is deserializing us, so we will fail outside of our control. Store the
     * Throwable cause as a
     * serialized byte array instead, so we can deserialize it outside of our own stream.
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        className = (String) in.readObject();
        message = (String) in.readObject();
        trace = (StackTraceElement[]) in.readObject();
        causeProxy = (ExceptionProxy) in.readObject();

      /*
       * Attempt to deserialize the original Exception. It might fail due to ClassNotFoundExceptions, ignore and move on
       */
        byte[] originalExceptionData = (byte[]) in.readObject();
        if (originalExceptionData != null && originalExceptionData.length > 0) {
            try {
                ByteArrayInputStream originalIn = new ByteArrayInputStream(originalExceptionData);
                ObjectInputStream input = new ObjectInputStream(originalIn);
                //           // Uncomment to run ExceptionProxySerializationTestCase
                //            {
                //               @Override
                //               protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
                //               {
                //                  return Class.forName(desc.getName(), false, Thread.currentThread().getContextClassLoader());
                //               }
                //            };
                original = (Throwable) input.readObject();

                if (causeProxy != null) {
                    // reset the cause, so we can de-serialize them individual
                    Throwable cause = causeProxy.createException();
                    if (original instanceof InvocationTargetException) {
                        SecurityActions.setFieldValue(InvocationTargetException.class, original, "target", cause);
                    } else {
                        SecurityActions.setFieldValue(Throwable.class, original, "cause", cause);
                    }
                }
            } catch (Throwable e) // Possible ClassNotFoundExcpetion / NoClassDefFoundError
            {
                // ignore, could not load class on client side, move on and create a fake 'proxy' later
                serializationProcessException = e;
            }
        }
        // Override with the remote serialization issue cause if exists
        Throwable tmpSerializationProcessException = (Throwable) in.readObject();
        if (tmpSerializationProcessException != null) {
            serializationProcessException = tmpSerializationProcessException;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(className);
        out.writeObject(message);
        out.writeObject(trace);
        out.writeObject(causeProxy);

        byte[] originalBytes = new byte[0];
        if (original != null) {
            try {
                // reset the cause, so we can serialize the exception chain individual
                SecurityActions.setFieldValue(Throwable.class, original, "cause", null);
            } catch (Exception e) {
                // move on, try to serialize anyway
            }

            try {
                ByteArrayOutputStream originalOut = new ByteArrayOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(originalOut);
                output.writeObject(original);
                output.flush();
                originalBytes = originalOut.toByteArray();
            } catch (NotSerializableException e) {
                // in case some class breaks Serialization contract
                serializationProcessException = e;
            }
        }
        out.writeObject(originalBytes);
        out.writeObject(serializationProcessException);
    }

    @Override
    public String toString() {
        return super.toString() + String.format("[class=%s, message=%s],cause = %s", className, message, causeProxy);
    }
}
