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

import java.io.Serializable;



/**
 * An abstraction of a test failure that is associated with a test {@link Response}. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2009
 */
public interface Failure extends Serializable
{
   /**
    * Get the name of the test class that failed
    */
   String getClassName();
   
   /**
    * Get the name of the test method that failed
    */
   String getMethodName();
   
   /**
    * Get the test failure message
    */
   String getMessage();
   
   /**
    * Get the exceoption that caused the failure.
    * In case of a failure due to an assertion, this will be 
    * RuntimeException or Error thrown by the test framework. 
    */
   Throwable getException();
}