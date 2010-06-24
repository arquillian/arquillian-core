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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;

/**
 * An abstract {@link Connector} implementation 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2009
 */
public abstract class AbstractConnector implements Connector
{
   // Provide logging
   private static final Logger log = Logger.getLogger(AbstractConnector.class.getName());
   
   private BundleContext context;

   public AbstractConnector(BundleContext context)
   {
      this.context = context;
   }

   public BundleContext getBundleContext()
   {
      return context;
   }

   protected void start() throws Exception
   {
   }

   protected void stop() throws Exception
   {
   }

   public Response process(final Request req) throws Throwable
   {
      throw new IllegalStateException("Cannot find listener to handle: " + req.getClassName());
   }

   protected InputStream process(InputStream reqStream)
   {
      Request request = null;
      Response response = null;
      try
      {
         // Unmarshall the Request
         ObjectInputStream ois = new ObjectInputStream(reqStream);
         request = (Request)ois.readObject();

         log.fine("Start invoke: " + request);
         
         // Field the request through the abstract connector
         response = process(request);
      }
      catch (Throwable th)
      {
         response = new BasicResponse();
         BasicFailure failure = new BasicFailure(th.getMessage(), th);
         if (request != null)
         {
            failure.setClassName(request.getClassName());
            failure.setMethodName(request.getMethodName());
         }
         response.addFailure(failure);
      }
      finally
      {
         log.fine("End invoke: " + response);
      }

      // Marshall the Response
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(response);
         oos.close();

         return new ByteArrayInputStream(baos.toByteArray());
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot marshall response", ex);
      }
   }
}