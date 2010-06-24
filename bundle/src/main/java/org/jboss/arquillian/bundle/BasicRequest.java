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

// $Id$

/**
 * A basic {@link Request} implementation. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2009
 */
public class BasicRequest implements Request
{
   private static final long serialVersionUID = 1L;
   
   private String className;
   private String methodName;

   public BasicRequest(String className, String methodName)
   {
      if (className == null)
         throw new IllegalArgumentException("Null className");
      if (methodName == null)
         throw new IllegalArgumentException("Null methodName");
      
      this.className = className;
      this.methodName = methodName;
   }

   public String getClassName()
   {
      return className;
   }

   public String getMethodName()
   {
      return methodName;
   }

   @Override
   public String toString()
   {
      String testName = className;
      int dotIndex = testName.lastIndexOf(".");
      if (dotIndex > 0)
         testName = testName.substring(dotIndex + 1);
      testName = testName + "." + methodName;
      return "BasicRequest[" + testName + "]";
   }
}
