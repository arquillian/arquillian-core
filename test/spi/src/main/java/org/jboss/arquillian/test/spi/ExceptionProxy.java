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
package org.jboss.arquillian.test.spi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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
public class ExceptionProxy implements Externalizable
{

   private static final long serialVersionUID = 2321010311438950147L;

   private String className;

   private String message;

   private StackTraceElement[] trace;

   private ExceptionProxy causeProxy;

   private Throwable cause;

   private Throwable original;

   public ExceptionProxy() {}

   public ExceptionProxy(Throwable throwable)
   {
      this.className = throwable.getClass().getName();
      this.message = throwable.getMessage();
      this.trace = throwable.getStackTrace();
      this.causeProxy = ExceptionProxy.createForException(throwable.getCause());
      this.original = throwable;
   }

   /**
    * Indicates whether this proxy wraps an exception
    *
    * @return Flag indicating an exception is wrapped.
    */
   public boolean hasException()
   {
      return className != null;
   }

   /**
    * Constructs an instance of the proxied exception based on the class name,
    * message, stack trace and if applicable, the cause.
    *
    * @return The constructed {@link Throwable} instance
    */
   public Throwable createException()
   {
      if (!hasException())
      {
         return null;
      }
      if(original != null)
      {
         return original;
      }

      Throwable throwable = createProxyException("Original exception not deserilizable, ClassNotFoundException"); //constructExceptionForClass(clazz);
      throwable.setStackTrace(trace);
      return throwable;
   }

   public ArquillianProxyException createProxyException(String reason)
   {
      ArquillianProxyException exception = new ArquillianProxyException(message, className, reason, getCause());
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
   private Throwable constructExceptionForClass(Class<?> clazz)
   {
      Object object = buildObjectFromClassConstructors(clazz);

      if (object == null)
      {
         return createProxyException("Could not find suitable constructor");
      }

      if (!(object instanceof Throwable))
      {
         return createProxyException("Proxy references non-Throwable type");
      }
      return (Throwable) object;
   }

   private Object buildObjectFromClassConstructors(Class<?> clazz)
   {
      // try the (String,Throwable) constructor first
      Object object = buildExceptionFromConstructor(clazz, new Class<?>[] {String.class, Throwable.class}, new Object[] {message, getCause()});
      if (object != null)
      {
         return object;
      }

      // try the (String,Exception) constructor
      object = buildExceptionFromConstructor(clazz, new Class<?>[] {String.class, Exception.class}, new Object[] {message, getCause()});
      if (object != null)
      {
         return object;
      }

      // try the (Throwable) constructor
      object = buildExceptionFromConstructor(clazz, new Class<?>[]{Throwable.class}, new Object[] {getCause()});
      if (object != null)
      {
         return object;
      }

      // try the (Exception) constructor
      object = buildExceptionFromConstructor(clazz, new Class<?>[]{Exception.class}, new Object[] {getCause()});
      if (object != null)
      {
         return object;
      }

      // try the (Object) constructor
      object = buildExceptionFromConstructor(clazz, new Class<?>[]{Object.class}, new Object[] {message});
      if (object != null)
      {
         return object;
      }

      // try the (String, Object) constructor
      object = buildExceptionFromConstructor(clazz, new Class<?>[]{String.class}, new Object[] {message});
      if (object != null)
      {
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
   private <T> T buildExceptionFromConstructor(Class<T> clazz, Class<?>[] signature, Object[] params) {
		Constructor<?> constructor = null;
		// try the message,cause constructor first
        Class<?> nextSource = clazz;
//        while (nextSource != Object.class && constructor == null)
//        {
      		try
      		{
      			constructor = nextSource.getDeclaredConstructor(signature);
      		}
      		catch (SecurityException e)
      		{
      			// we'll try the next signature
      		}
      		catch (NoSuchMethodException e)
      		{
      			// we'll try the next signature
      		}
//      		nextSource = nextSource.getSuperclass();
//        }
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
   public static ExceptionProxy createForException(Throwable throwable)
   {
      if (throwable == null)
      {
         return null;
      }
      return new ExceptionProxy(throwable);
   }

   /**
    * Returns the cause of the exception represented by this proxy
    *
    * @return The cause of this exception
    */
   public Throwable getCause()
   {
      // lazy create cause
      if (cause == null)
      {
         if (causeProxy != null)
         {
            cause = causeProxy.createException();
         }
      }
      return cause;
   }

   /**
    * Custom Serialization logic.
    *
    * If possible, we try to keep the original Exception form the Container side.
    *
    * If we can't load the Exception on the client side, return a ArquillianProxyException that keeps the original stack trace etc.
    *
    * We can't use in.readObject() on the Throwable cause, because if a ClassNotFoundException is thrown, the stream is marked with the exception
    * and that stream is the same stream that is deserializing us, so we will fail outside of our control. Store the Throwable cause as a
    * serialized byte array instead, so we can deserialize it outside of our own stream.
    */
   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      className = (String)in.readObject();
      message = (String)in.readObject();
      trace = (StackTraceElement[])in.readObject();
      causeProxy = (ExceptionProxy)in.readObject();

      /*
       * Attempt to deserialize the original Exception. It might fail due to ClassNotFoundExceptions, ignore and move on
       */
      byte[] originalExceptionData = (byte[])in.readObject();
      if(originalExceptionData != null && originalExceptionData.length > 0)
      {
         try
         {
            ByteArrayInputStream originalIn = new ByteArrayInputStream(originalExceptionData);
            ObjectInputStream input = new ObjectInputStream(originalIn) ;
            /*// need to active for testing
            {

               @Override
               protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
               {
                  return Class.forName(desc.getName(), false, Thread.currentThread().getContextClassLoader());
               }
            };
            */
            original = (Throwable)input.readObject();
            try
            {
               // reset the cause, so we can de-serialize them individual
               SecurityActions.setFieldValue(Throwable.class, original, "cause", causeProxy.createException());
            }
            catch (Exception e)
            {
               // move on, try to serialize anyway
            }
         }
         catch (ClassNotFoundException e)
         {
            // ignore, could not load class on client side, move on and create a fake 'proxy' later
         }
      }
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeObject(className);
      out.writeObject(message);
      out.writeObject(trace);
      out.writeObject(causeProxy);

      byte[] originalBytes = new byte[0];
      if(original != null)
      {
         try
         {
            // reset the cause, so we can serialize the exception chain individual
            SecurityActions.setFieldValue(Throwable.class, original, "cause", null);
         }
         catch (Exception e)
         {
            // move on, try to serialize anyway
         }

         ByteArrayOutputStream originalOut = new ByteArrayOutputStream();
         ObjectOutputStream output = new ObjectOutputStream(originalOut);
         output.writeObject(original);
         output.flush();
         originalBytes = originalOut.toByteArray();
      }
      out.writeObject(originalBytes);
   }

   @Override
   public String toString()
   {
      return super.toString() + String.format("[class=%s, message=%s],cause = %s", className, message, causeProxy);
   }
}
