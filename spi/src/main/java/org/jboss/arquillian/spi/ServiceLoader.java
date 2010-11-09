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

import java.util.Collection;

/**
 * ServiceLoader
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface ServiceLoader
{
   /**
    * Load multiple service implementations.
    * 
    * @param <T>
    * @param serviceClass The service interface to load a implementations for
    * @return A {@link Collection} of all instances of serviceClass 
    */
   <T> Collection<T>  all(Class<T> serviceClass);
   
   /**
    * Load multiple service implementations.
    * 
    * @param <T>
    * @param classLoader The ClassLoader to use
    * @param serviceClass The service interface to load a implementations for
    * @return A {@link Collection} of all instances of serviceClass 
    */
   <T> Collection<T>  all(ClassLoader classLoader, Class<T> serviceClass);

   /**
    * Load a single service implementation. 
    * 
    * Method should throw {@link IllegalStateException} if multiple instances of serviceClass found.
    * 
    * @param <T>
    * @param serviceClass The service interface to load a implementation for
    * @return A instance of serviceClass
    * @throws IllegalStateException if more then one implementation of serviceClass found
    */
   <T> T onlyOne(Class<T> serviceClass);
   
   /**
    * Load a single service implementation. 
    * 
    * Method should throw {@link IllegalStateException} if multiple instances of serviceClass found.
    * 
    * @param <T>
    * @param classLoader The ClassLoader to use
    * @param serviceClass The service interface to load a implementation for
    * @return A instance of serviceClass
    * @throws IllegalStateException if more then one implementation of serviceClass found
    */
   <T> T onlyOne(ClassLoader classLoader, Class<T> serviceClass);
   
   /**
    * Load a single service implementation. 
    * 
    * Method should returns a new instance of defaultServiceClass if no other instance is found.
    * 
    * @param <T>
    * @param serviceClass The service interface to load a implementation for
    * @param defaultServiceClass If no other implementations found, create a instance of this class
    * @return A instance of serviceClass
    */
   <T> T onlyOne(Class<T> serviceClass, Class<? extends T> defaultServiceClass);

   /**
    * Load a single service implementation. 
    * 
    * Method should returns a new instance of defaultServiceClass if no other instance is found.
    * 
    * @param <T>
    * @param classLoader The ClassLoader to use
    * @param serviceClass The service interface to load a implementation for
    * @param defaultServiceClass If no other implementations found, create a instance of this class
    * @return A instance of serviceClass
    */
   <T> T onlyOne(ClassLoader classLoader, Class<T> serviceClass, Class<? extends T> defaultServiceClass);
}
