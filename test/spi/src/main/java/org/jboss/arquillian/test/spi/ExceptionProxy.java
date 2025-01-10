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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
 * <li>If the exception class doesn't exist, one of its superclasses is used
 * </li>
 * </ul>
 *
 * @author <a href="mailto:contact@andygibson.net">Andy Gibson</a>
 */
public class ExceptionProxy implements Externalizable {
    // The serialVersionUID of the ExceptionProxy that existed in Arquillian 1.9.1.Final
    private static final long serialVersionUID = 2321010311438950147L;

    // This is the className of the exception in the container passed into TestResult#setThrowable(Throwable)
    private String className;
    // This is the message of the exception in the container passed into TestResult#setThrowable(Throwable)
    private String message;
    // This is the stack trace of the exception in the container passed into TestResult#setThrowable(Throwable)
    private StackTraceElement[] trace;
    // This is a proxy to the cause exception in the container, not used post 1.9.1.Final
    private ExceptionProxy causeProxy;
    // This is the causeProxy#createException() instance
    private Throwable cause;
    // This only exists if the original container exception could be deserialized in the client
    private Throwable original;
    // This would exist if the original exception could not be serialized in the container
    private Throwable serializationProcessException = null;
    // New fields added in 1.9.2.Final
    private Version version;
    // The list of superclasses of the exception class that was serialized
    private List<String> exceptionHierarchy;

    public static class Version implements Serializable {
        private static final long serialVersionUID = 1L;
        int version = 2;
    }

    public ExceptionProxy() {
        version = new Version();
    }

    public ExceptionProxy(Throwable throwable) {
        this.version = new Version();
        this.className = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.trace = throwable.getStackTrace();
        //this.causeProxy = ExceptionProxy.createForException(throwable.getCause());
        this.original = throwable;
        this.exceptionHierarchy = getExceptionHierarchy(throwable);
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
     * message, stack trace and if applicable, and the cause if the cause could be
     * deserialized in the client. Otherwise, this returns an ArquillianProxyException
     *
     * @return The constructed {@link Throwable} instance if one exists, null otherwise
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
        return serializationProcessException;
    }

    // Accessors to the fields of the ExceptionProxy
    public String getClassName() {
        return className;
    }

    public String getMessage() {
        return message;
    }

    public StackTraceElement[] getTrace() {
        return trace;
    }

    public ExceptionProxy getCauseProxy() {
        return causeProxy;
    }

    public Throwable getOriginal() {
        return original;
    }

    public Throwable getSerializationProcessException() {
        return serializationProcessException;
    }

    public Version getVersion() {
        return version;
    }

    public List<String> getExceptionHierarchy() {
        return exceptionHierarchy;
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
        // Read the first object to see if it is the version object
        Object firstObject = in.readObject();
        if (firstObject instanceof Version) {
            version = (Version) firstObject;
            className = (String) in.readObject();
            message = (String) in.readObject();
            trace = (StackTraceElement[]) in.readObject();
            exceptionHierarchy = (List<String>) in.readObject();
            // Try to deserialize the original exception
            try {
                byte[] originalExceptionData = (byte[]) in.readObject();
                if (originalExceptionData != null && originalExceptionData.length > 0) {
                    ByteArrayInputStream originalIn = new ByteArrayInputStream(originalExceptionData);
                    ObjectInputStream input = new ObjectInputStream(originalIn);
                    original = (Throwable) input.readObject();
                }
            } catch (Throwable e) {
                this.serializationProcessException = e;
            }
            // Override with the remote serialization issue cause if exists
            Throwable tmpSerializationProcessException = (Throwable) in.readObject();
            if (tmpSerializationProcessException != null) {
                serializationProcessException = tmpSerializationProcessException;
            }

            // If we were not able to create original from originalExceptionData, try to create it from exceptionHierarchy
            if(original == null) {
                original = buildOriginalException();
            }
        } else {
            serializationProcessException = new IOException("Unknown version of ExceptionProxy");
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(version);
        out.writeObject(className);
        out.writeObject(message);
        out.writeObject(trace);
        out.writeObject(exceptionHierarchy);
        byte[] originalBytes = new byte[0];
        try {
            /* Try to serialize the original exception. Here we do it in a separate try-catch block to avoid
            because default serialization will serialize whatever it can and leave non-serializable fields out.
            We have to make the write of the root exception atomic.
            */
            ByteArrayOutputStream originalOut = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(originalOut);
            output.writeObject(original);
            output.flush();
            originalBytes = originalOut.toByteArray();
        } catch (NotSerializableException e) {
            // ignore, could not serialize original exception
            this.serializationProcessException = e;
        }
        out.writeObject(originalBytes);
        out.writeObject(serializationProcessException);
    }

    @Override
    public String toString() {
        return super.toString() + String.format("[class=%s, message=%s],cause = %s", className, message, causeProxy);
    }

    /**
     * Get the exception hierarchy for the exception class being proxied.
     *
     * @return list of exception types in the hierarchy
     */
    protected List<String> getExceptionHierarchy(Throwable t) {
        List<String> hierarchy = new ArrayList<>();
        Class<?> tclass = t.getClass();
        while(Throwable.class.isAssignableFrom(tclass)) {
            hierarchy.add(tclass.getName());
            tclass = tclass.getSuperclass();
        }
        return hierarchy;
    }
    /**
     * Build the original exception based on the exception class name. This first
     * tries to use a ctor with a message, then a default ctor.
     *
     * @return the original exception
     */
    protected Throwable buildOriginalException() {
        Throwable original = null;
        for(String tclassName : exceptionHierarchy) {
            try {
                Class<? extends Throwable> tclass = Class.forName(tclassName).asSubclass(Throwable.class);
                try {
                    original = tclass.getDeclaredConstructor(String.class).newInstance(message);
                    original.setStackTrace(trace);
                    break;
                } catch (Exception e) {
                    try {
                        original = tclass.getDeclaredConstructor().newInstance();
                        original.setStackTrace(trace);
                        break;
                    } catch (Exception ex) {
                        // ignore, could not load class on client side, try next base class
                    }
                }
            } catch (ClassNotFoundException e) {
                // ignore, could not load class on client side, try next base class
            }
        }
        return original;
    }
}
