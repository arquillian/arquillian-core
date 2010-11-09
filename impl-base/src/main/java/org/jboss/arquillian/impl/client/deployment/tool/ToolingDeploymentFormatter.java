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
package org.jboss.arquillian.impl.client.deployment.tool;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import org.jboss.arquillian.impl.Validate;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.jboss.shrinkwrap.api.formatter.Formatter;
import org.jboss.shrinkwrap.impl.base.asset.ArchiveAsset;
import org.jboss.shrinkwrap.impl.base.asset.ClassAsset;
import org.jboss.shrinkwrap.impl.base.asset.ClassLoaderAsset;

/**
 * ToolingDeploymentFormatter
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ToolingDeploymentFormatter implements Formatter
{
   private Class<?> testClass;
   
   public ToolingDeploymentFormatter(Class<?> testClass)
   {
      Validate.notNull(testClass, "TestClass must be specified");
      this.testClass = testClass;
   }
   
   public String format(Archive<?> archive) throws IllegalArgumentException
   {
      StringBuilder xml = new StringBuilder();
      
      xml.append("<?xml version=\"1.0\"?>\n<deployment")
      .append(" name=\"").append(archive.getName()).append("\"")
      .append(" testclass=\"").append(testClass.getName()).append("\"")
      .append(">\n");

      formatNode(archive.get(ArchivePaths.root()), xml); 
      
      xml.append("</deployment>").append("\n");
      return xml.toString();
   }
   
   public void formatNode(Node node, StringBuilder xml)
   {
      if(node.getAsset() != null)
      {
         String source = findResourceLocation(node.getAsset());
         
         xml.append("\t<asset")
            .append(" type=\"").append(node.getAsset().getClass().getSimpleName()).append("\"")
            .append(" path=\"").append(node.getPath().get()).append("\"");
            if(source != null)
            {
               xml.append(" source=\"").append(source).append("\"");
            }
         
         if(node.getAsset().getClass() == ArchiveAsset.class)
         {
            xml.append(">");
            xml.append("\n");
            formatNode(
                  ((ArchiveAsset)node.getAsset()).getArchive().get(ArchivePaths.root()), 
                  xml);
            xml.append("</asset>").append("\n");
         } 
         else 
         {
            xml.append("/>").append("\n");
         }
         
      }
      else 
      {
         xml.append("\t<asset type=\"Directory\" path=\"").append(node.getPath().get()).append("\"/>\n");
      }
      for(Node child : node.getChildren())
      {
         formatNode(child, xml);
      }
   }
   
   private String findResourceLocation(Asset asset) 
   {
      Class<?> assetClass = asset.getClass();
      
      if(assetClass == FileAsset.class)
      {
         return getInternalFieldValue(File.class, "file", asset).getAbsolutePath();
      }
      if(assetClass == ClassAsset.class)
      {
         return getInternalFieldValue(Class.class, "clazz", asset).getName();
      }
      if(assetClass == UrlAsset.class)
      {
         return getInternalFieldValue(URL.class, "url", asset).toExternalForm();
      }
      if(assetClass == ClassLoaderAsset.class)
      {
         return getInternalFieldValue(String.class, "resourceName", asset);
      }
      return null;
   }
   
   @SuppressWarnings("unchecked")
   private <T> T getInternalFieldValue(Class<T> type, String fieldName, Object obj) 
   {
      try
      {
         Field field = obj.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         
         return (T)field.get(obj);
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not extract field " + fieldName + " on " + obj, e);
      }
   }
   
//   private static class Deployment 
//   {
//      
//   }
//   
//   private static class Node 
//   {
//      
//   }
//   
//   private static class Source 
//   {
//      
//   }
//   
//   private static class Content 
//   {
//      
//   }
}
