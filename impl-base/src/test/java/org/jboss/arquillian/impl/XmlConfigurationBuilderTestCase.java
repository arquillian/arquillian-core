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
package org.jboss.arquillian.impl;

import java.util.Collection;
import java.util.Collections;

import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ConfigurationException;
import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.ServiceLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * XmlConfigurationBuilderTestCase
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @version $Revision: $
 */
public class XmlConfigurationBuilderTestCase
{
   /**
    * Should return an empty Configuration object if the file doesn't exists.
    * @throws Exception
    */
   @Test
   public void testNonExistingConfigurationFile() throws Exception
   {
      ConfigurationBuilder builder = new XmlConfigurationBuilder("non-existing.xml");
      Configuration configuration = builder.build();

      Assert.assertNotNull(configuration);
      Assert.assertNull(configuration.getContainerConfig(MockContainerConfiguration.class));
   }
   
   /**
    * Should process correctly a valid configuration file. Property mapper is also tested here.
    * @throws Exception
    */
   @Test
   public void testValidConfigurationFile() throws Exception 
   {
      // create a mock ServiceLoader that returns our MockContainerConfiguration
      ServiceLoader serviceLoader = new MockServiceLoader();
      
      // build the configuration
      ConfigurationBuilder builder = new XmlConfigurationBuilder("arquillian.xml", serviceLoader);
      Configuration configuration = builder.build();
      Assert.assertNotNull(configuration);
      
      // retrieve the container configuration
      MockContainerConfiguration containerConfig = configuration.getContainerConfig(MockContainerConfiguration.class);
      Assert.assertNotNull(containerConfig);
      
      // check that the properties have the correct value
      Assert.assertEquals("hola", containerConfig.getPropertyString());
      Assert.assertEquals(1, containerConfig.getPropertyInt());
      Assert.assertEquals(2L, containerConfig.getPropertyLong());
      Assert.assertEquals(3D, containerConfig.getPropertyDouble(), 0);
      Assert.assertEquals(true, containerConfig.getPropertyBoolean());
   }
   
   /**
    * Should throw a ConfigurationException if 
    * @throws Exception
    */
   @Test(expected=ConfigurationException.class)
   public void testInvalidConfigurationFile() throws Exception
   {
      new XmlConfigurationBuilder("broken_arquillian.xml").build();
   }

   /**
    * Should load the MocContainerConfiguration even if the defined resource file is missing.
    * 
    * @throws Exception
    */
   @Test
   public void testLoadDefaultConfigurationOnMissingFile() throws Exception
   {
      Configuration configuration = new XmlConfigurationBuilder(
            "missing_arquillian.xml", 
            new MockServiceLoader()).build();
      
      ContainerConfiguration containerConfiguration = configuration.getActiveContainerConfiguration();
      Assert.assertNotNull(containerConfiguration);
      
      MockContainerConfiguration mockContainerConfiguration = configuration.getContainerConfig(MockContainerConfiguration.class);
      Assert.assertNotNull(mockContainerConfiguration);
   }
   
   /**
    * Mocks the ServiceLoader to return our MockContainerConfiguration
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   class MockServiceLoader implements ServiceLoader 
   {
      @SuppressWarnings("unchecked")
      @Override
      public <T> Collection<T> all(Class<T> serviceClass)
      {
         return (Collection<T>) Collections.singleton(new MockContainerConfiguration());
      }

      @SuppressWarnings("unchecked")
      @Override
      public <T> T onlyOne(Class<T> serviceClass)
      {
         return (T) new MockContainerConfiguration();
      }
   }
   
   /**
    * Mocks a ContainerConfiguration implementation
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   class MockContainerConfiguration implements ContainerConfiguration
   {
      private String propertyString;
      
      private int propertyInt;
      
      private long propertyLong;
      
      private double propertyDouble;
      
      private boolean propertyBoolean;

      @Override
      public ContainerProfile getContainerProfile()
      {
         return ContainerProfile.STANDALONE;
      }
      
      public String getPropertyString()
      {
         return propertyString;
      }

      public void setPropertyString(String propertyString)
      {
         this.propertyString = propertyString;
      }

      public int getPropertyInt()
      {
         return propertyInt;
      }

      public void setPropertyInt(int propertyInt)
      {
         this.propertyInt = propertyInt;
      }

      public long getPropertyLong()
      {
         return propertyLong;
      }

      public void setPropertyLong(long propertyLong)
      {
         this.propertyLong = propertyLong;
      }

      public double getPropertyDouble()
      {
         return propertyDouble;
      }

      public void setPropertyDouble(double propertyDouble)
      {
         this.propertyDouble = propertyDouble;
      }

      public boolean getPropertyBoolean()
      {
         return propertyBoolean;
      }

      public void setPropertyBoolean(boolean propertyBoolean)
      {
         this.propertyBoolean = propertyBoolean;
      }
      
   }

}
