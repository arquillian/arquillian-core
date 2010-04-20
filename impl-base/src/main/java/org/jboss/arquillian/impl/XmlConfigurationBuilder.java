/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.io.InputStream;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ConfigurationException;
import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ServiceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An implementation of {@link ConfigurationBuilder} that loads the configuration
 * from the arquillian.xml file located in the root of the classpath. If not found,
 * it just returns an empty {@link org.jboss.arquillian.spi.Configuration} object.
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @version $Revision: $
 */
public class XmlConfigurationBuilder implements ConfigurationBuilder
{
   
   private static final Logger log = Logger.getLogger(XmlConfigurationBuilder.class.getName());
   
   /**
    * The default XML resource path.
    */
   private static final String DEFAULT_RESOURCE_PATH = "arquillian.xml";

   /**
    * The actual resourcePath
    */
   private String resourcePath;
   
   private ServiceLoader serviceLoader;

   /**
    * Constructor. Initializes with the default resource path and service loader.
    */
   public XmlConfigurationBuilder() 
   {
       this(DEFAULT_RESOURCE_PATH);
   }

   /**
    * Constructor. Initializes with the provided resource path and the default
    * service loader.
    * @param resourcePath the path to the XML configuration file.
    */
   public XmlConfigurationBuilder(String resourcePath) 
   {
       this(resourcePath, new DynamicServiceLoader());
   }
   
   /**
    * Constructor. Initializes with the provided resource path and service loader.
    * @param resourcePath the path to the XML configuration file.
    * @param serviceLoader the ServiceLoader implementation to use.
    */
   public XmlConfigurationBuilder(String resourcePath, ServiceLoader serviceLoader) 
   {
      this.resourcePath = resourcePath;
      this.serviceLoader = serviceLoader;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.ConfigurationBuilder#build()
    */
   @Override
   public Configuration build() throws ConfigurationException
   {      
      // the configuration object we are going to return
      Configuration configuration = new Configuration();
      
      Collection<ContainerConfiguration> containersConfigurations = serviceLoader.all(ContainerConfiguration.class);
      log.fine("Container Configurations: " + containersConfigurations.size());
      
      for(ContainerConfiguration containerConfiguration : containersConfigurations)
      {
         configuration.addContainerConfig(containerConfiguration);
      }
      
      try
      {
         // load the xml configuration file
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
         
         if (inputStream != null) 
         {
            log.info("building configuration from XML file: " + resourcePath);
            Document document = getDocument(inputStream);
            
            // load all the container nodes
            NodeList nodeList = document.getDocumentElement().getElementsByTagNameNS("*", "container");
            for (int i=0; i < nodeList.getLength(); i++) 
            {
               Node containerNode = nodeList.item(i); 
               
               // retrieve the package
               String pkg = containerNode.getNamespaceURI().replaceFirst("urn:arq:", "");
               
               // try to find a ContainerConfiguration that matches the package
               ContainerConfiguration containerConfig = matchContainerConfiguration(containersConfigurations, pkg);
               
               if (containerConfig != null) 
               {
                  // map the nodes
                  mapNodesToProperties(containerConfig, containerNode);
                  
                  // add the ContainerConfiguration to the configuration
               }
            }
         }
         else
         {
            log.fine("No " + resourcePath + " file found");
         }
      }
      catch (Exception e)
      {
         throw new ConfigurationException(e.getMessage(), e);
      }
      return configuration;
   }
   
   /**
    * Fills the properties of the ContainerConfiguration implementation object with the 
    * information from the container XML fragment (the containerNode argument). 
    * @param containerConfig the ContainerConfiguration object to be filled from the XML fragment
    * @param containerNode the XML node that represents the container configuration.
    * @throws Exception if there is a problem filling the ContainerConfiguration object.
    */
   private void mapNodesToProperties(ContainerConfiguration containerConfig, Node containerNode) throws Exception
   {
      // validation
      Validate.notNull(containerConfig, "No container configuration specified");
      Validate.notNull(containerNode, "No container XML Node specified");
      
      log.fine("filling container configuration for class: " + containerConfig.getClass().getName());
      
      // here we will store the properties taken from the child elements of the container node
      Map<String,String> properties = new HashMap<String,String>(); 
      
      NodeList childNodes = containerNode.getChildNodes();
      for (int i=0; i < childNodes.getLength(); i++) 
      {
         Node child = childNodes.item(i);
         
         // only process element nodes
         if (child.getNodeType() == Node.ELEMENT_NODE) 
         {
            properties.putAll(getPropertiesFromNode(child));
         }
      }
      
      // set the properties found in the container XML fragment to the ContainerConfiguration
      for (Map.Entry<String, String> property : properties.entrySet()) 
      {
         Field field = containerConfig.getClass().getDeclaredField(property.getKey());
         field.setAccessible(true);
         Object value = convert(field.getType(), property.getValue());
         field.set(containerConfig, value);
      }
   }
   
   /**
    * Creates all the properties from a single Node element. The element must be a child of the
    * container root element.
    * @param element the XML Node from which we are going to create the properties.
    * @return a Map of properties names and values mapped from the XML Node element.
    */
   private Map<String,String> getPropertiesFromNode(Node element) {
      Map<String,String> properties = new HashMap<String,String>(); 

      // retrieve the attributes of the element 
      NamedNodeMap attributes = element.getAttributes();
      
      // choose the strategy
      if (attributes.getLength() > 0) 
      {
         new TagNameAttributeMapper().map(element, properties);
      }
      else
      {
         new TagNameMapper().map(element, properties);
      }
      
      return properties;
   }
   
   /**
    * Matches a ContainerConfiguration implementation object with the pkg parameter.
    * @param pkg the package prefix used to match the ContainerConfiguration.
    * @return the ContainerConfiguration implementation object that matches the package, 
    * null otherwise.
    */
   private ContainerConfiguration matchContainerConfiguration(Collection<ContainerConfiguration> containerConfigurations, String pkg) 
   {
      log.fine("trying to match a container configuration for package: " + pkg);
      // load all the containers configurations 
      
      ContainerConfiguration containerConfig = null;
      
      // select the container configuration that matches the package
      for (ContainerConfiguration cc : containerConfigurations) 
      {
         if (cc.getClass().getName().startsWith(pkg)) 
         {
            containerConfig = cc;                     
         }
      }
      
      // warn: we didn't find the class
      if (containerConfig == null)
      {
         log.warning("No container configuration found for URI: java:urn:" + pkg);
      }
      
      return containerConfig;
   }
   
   /**
    * Retrieves the DOM document object from the inputStream.
    * @param inputStream the inputStream of the XML file.
    * @return a loaded Document object for DOM manipulation.
    * @throws Exception if the Document object couldn't be created.
    */
   private Document getDocument(InputStream inputStream) throws Exception 
   {
      Validate.notNull(inputStream, "No input stream specified");
      
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.parse(inputStream);
   
      document.getDocumentElement().normalize();
      
      return document;
   }
   
   /**
    * Converts a String value to the specified class.
    * @param clazz
    * @param value
    * @return
    */
   private Object convert(Class<?> clazz, String value) 
   {
      /* TODO create a new Converter class and move this method there for reuse */
      
      if (Integer.class.equals(clazz) || int.class.equals(clazz)) 
      {
         return Integer.valueOf(value);
      } 
      else if (Double.class.equals(clazz) || double.class.equals(clazz)) 
      {
         return Double.valueOf(value);
      } 
      else if (Long.class.equals(clazz) || long.class.equals(clazz))
      {
         return Long.valueOf(value);
      }
      else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz))
      {
         return Boolean.valueOf(value);
      }
      
      return value;
   }
   
   /**
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   private interface PropertiesMapper 
   {
      void map(Node element, Map<String,String> properties);
   }

   /**
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   private class TagNameAttributeMapper implements PropertiesMapper
   {
      @Override
      public void map(Node element, Map<String, String> properties)
      {
         // retrieve the attributes of the element 
         NamedNodeMap attributes = element.getAttributes();
         
         for (int k=0; k < attributes.getLength(); k++)
         {
            Node attribute = attributes.item(k);
            
            // build the property name
            String attributeName = attribute.getNodeName();
            String fullPropertyName = element.getLocalName() + Character.toUpperCase(attributeName.charAt(0)) 
                  + attributeName.substring(1);
           
            // add the property name and its value
            properties.put(fullPropertyName, attribute.getNodeValue());
         }
      }
   }
   
   /**
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   private class TagNameMapper implements PropertiesMapper
   {
      @Override
      public void map(Node element, Map<String, String> properties)
      {
         String value = "";
         
         if (!element.hasChildNodes()) 
         {
            throw new ConfigurationException("Node " + element.getNodeName() + " has no value");
         }
         
         value = element.getChildNodes().item(0).getNodeValue();
         properties.put(element.getLocalName(), value);
      }
      
   }
}
