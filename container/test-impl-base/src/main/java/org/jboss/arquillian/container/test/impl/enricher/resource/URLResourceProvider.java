/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;

/**
 * URLResourceProvider
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class URLResourceProvider implements ResourceProvider
{
   @Inject
   private Instance<ProtocolMetaData> protocolMetadata;

   /* (non-Javadoc)
    * @see org.jboss.arquillian.testenricher.arquillian.ResourceProvider#lookup(java.lang.reflect.Field, org.jboss.arquillian.api.ArquillianResource)
    */
   @Override
   public Object lookup(ArquillianResource resource)
   {
      ProtocolMetaData metaData = protocolMetadata.get();
      if(metaData == null)
      {
         return null;
      }
      if(metaData.hasContext(HTTPContext.class))
      {
         HTTPContext context = metaData.getContext(HTTPContext.class);
         if(resource.value() != null && resource.value() != ArquillianResource.class)
         {
            // TODO: we need to check for class. Not all containers have ServletClass available.
            Servlet servlet = context.getServletByName(resource.value().getSimpleName());
            if( servlet == null)
            {
               servlet = context.getServletByName(resource.value().getName());
               //throw new RuntimeException("No Servlet named " + resource.value().getSimpleName() + " found in metadata");
            }
            if(servlet == null)
            {
               return null;
            }
            return toURL(servlet);
         }
         // TODO: evaluate, if all servlets are in the same context, and only one context exists, we can find the context         
         else if(allInSameContext(context.getServlets()))
         {
            return toURL(context.getServlets().get(0));
         }
         else
         {
            return toURL(context);
         }
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.arquillian.container.test.impl.enricher.resource.ResourceProvider#canProvide(java.lang.Class)
    */
   @Override
   public boolean canProvide(Class<?> type)
   {
      return URL.class.isAssignableFrom(type);
   }

   private boolean allInSameContext(List<Servlet> servlets)
   {
      Set<String> context = new HashSet<String>();
      for (Servlet servlet : servlets)
      {
         context.add(servlet.getContextRoot());
      }
      return context.size() == 1;
   }

   private URL toURL(Servlet servlet)
   {
      try
      {
         return servlet.getBaseURI().toURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not convert Servlet to URL, " + servlet, e);
      }
   }

   private URL toURL(HTTPContext context)
   {
      try
      {
         return new URI("http", null, context.getHost(), context.getPort(), null, null, null).toURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not convert Servlet to URL, " + context, e);
      }
   }
}
