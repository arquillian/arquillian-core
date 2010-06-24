/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.bundle;

// $Id: BasicResponse.java 91197 2009-07-14 09:48:24Z thomas.diesler@jboss.com $

/**
 * A basic {@link Failure} implementation. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2009
 */
public class BasicFailure implements Failure
{
   private static final long serialVersionUID = 1L;

   private String message;
   private Throwable exception;
   private String className;
   private String methodName;
   
   public BasicFailure(String message, Throwable exception)
   {
      this.message = message;
      this.exception = exception;
   }

   public Throwable getException()
   {
      return exception;
   }

   public String getMessage()
   {
      return message;
   }

   public String getClassName()
   {
      return className;
   }

   public void setClassName(String className)
   {
      this.className = className;
   }

   public String getMethodName()
   {
      return methodName;
   }

   public void setMethodName(String methodName)
   {
      this.methodName = methodName;
   }
}
