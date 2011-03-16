/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.spi;

import java.io.Serializable;
import java.lang.reflect.Constructor;

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
 * 
 * @author <a href="mailto:contact@andygibson.net">Andy Gibson</a>
 * 
 */
public class ExceptionProxy implements Serializable {

	private static final long serialVersionUID = 2321010311438950147L;

	private String className;
	private String message;
	private StackTraceElement[] trace;
	private ExceptionProxy causeProxy;
	private Throwable cause;

	public ExceptionProxy(Throwable throwable) {
		this.className = throwable.getClass().getName();
		this.message = throwable.getMessage();
		this.trace = throwable.getStackTrace();
		this.causeProxy = ExceptionProxy.createForException(throwable
				.getCause());
	}

	@Override
	public String toString() {
		return super.toString()
				+ String.format("[class=%s, message=%s],cause = %s", className,
						message, causeProxy);

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

		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			return createProxyException("Exception class not found on client");
		}

		Throwable throwable = constructExceptionForClass(clazz);
		throwable.setStackTrace(trace);
		return throwable;
	}

	public ArquillianProxyException createProxyException(String reason) {
		ArquillianProxyException exception = new ArquillianProxyException(message, className, reason,
				getCause());
		exception.setStackTrace(trace);
		return exception;
	}

	/**
	 * Constructs an instance of the passed in class if a suitable constructor
	 * is found. If the constructor subclasses {@link Throwable} it is returned.
	 * If a Class instance is not constructed ot is not a {@link Throwable} then
	 * another exception is returned indicating this.
	 * 
	 * @param clazz
	 *            Class to construct
	 * @return Instance of the Throwable class.
	 */
	private Throwable constructExceptionForClass(Class<?> clazz) {
		Object object = buildObjectFromClassConstructors(clazz);

		if (object == null) {
			return createProxyException("Could not find suitable constructor");
		}

		if (!(object instanceof Throwable)) {			
			return createProxyException("Proxy references non-Throwable type");
		}
		return (Throwable) object;
	}

	private Object buildObjectFromClassConstructors(Class<?> clazz) {
		// try the (String,Throwable) constructor first
		Object object = buildExceptionFromConstructor(clazz, new Class<?>[] {
				String.class, Throwable.class }, new Object[] { message,
				getCause() });
		if (object != null) {
			return object;
		}

		// try the (String,Exception) constructor first
		object = buildExceptionFromConstructor(clazz, new Class<?>[] {
				String.class, Exception.class }, new Object[] { message,
				getCause() });
		if (object != null) {
			return object;
		}

		// try the (String) constructor next
		object = buildExceptionFromConstructor(clazz,
				new Class<?>[] { String.class }, new Object[] { message });
		if (object != null) {
			return object;
		}

		return null;
	}

	/**
	 * Attempt to build an exception of the given class type using the
	 * constructor signature and parameters passed in. If no constructor matches
	 * or the constructor throws an exception, then null is returned.
	 * 
	 * @param clazz
	 *            Class to construct
	 * @param signature
	 *            Array of class types to match the signature on
	 * @param params
	 *            Parameter values to pass to the constructor if found.
	 * @return The object instance created using the constructor
	 */
	private <T> T buildExceptionFromConstructor(Class<T> clazz,
			Class<?>[] signature, Object[] params) {
		Constructor<?> constructor = null;
		// try the message,cause constructor first
		try {
			constructor = clazz.getConstructor(signature);
		} catch (SecurityException e) {
			// we'll try the next signature
		} catch (NoSuchMethodException e) {
			// we'll try the next signature
		}
		// if we found a working constructor, use it
		if (constructor != null) {
			try {
				@SuppressWarnings("unchecked")
				T result = (T) constructor.newInstance(params);
				return result;
			} catch (Throwable e) {
				return null;
			}
		}
		// no matching constructor, no result
		return null;

	}

	/**
	 * Static method to create an exception proxy for the passed in
	 * {@link Throwable} class. If null is passed in, null is returned as the
	 * exception proxy
	 * 
	 * @param throwable
	 *            Exception to proxy
	 * @return An ExceptionProxy representing the exception passed in
	 */
	public static ExceptionProxy createForException(Throwable throwable) {
		if (throwable == null) {
			return null;
		}
		return new ExceptionProxy(throwable);
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
}
