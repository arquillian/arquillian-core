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
package org.jboss.arquillian.spi.client.protocol.metadata;

import java.net.URI;

/**
 * Servlet
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class Servlet
{
   private String name;
   private String contextRoot;

   private HTTPContext context;
   
   public Servlet(String name, String contextRoot)
   {
      this.name = name;
      this.contextRoot = cleanContextRoot(contextRoot);
   }
   
   /**
    * @param context the context to set
    */
   void setParent(HTTPContext context)
   {
      this.context = context;
   }
   
   
   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * @return the contextRoot
    */
   public String getContextRoot()
   {
      return contextRoot;
   }

   private String cleanContextRoot(String contextRoot)
   {
      if(!contextRoot.startsWith("/"))
      {
         return "/" + contextRoot;
      }
      return contextRoot;
   }

   public URI getBaseURI()
   {
      return URI.create("http://" + context.getHost() + ":" + context.getPort() + contextRoot + "/");
   }

   /**
    * @return BaseURI + name
    */
   public URI getFullURI()
   {
      return URI.create("http://" + context.getHost() + ":" + context.getPort() + contextRoot + "/" + name);
   }

   @Override
   public String toString()
   {
      return "Servlet [contextRoot=" + contextRoot + ", name=" + name + "]";
   }
}
