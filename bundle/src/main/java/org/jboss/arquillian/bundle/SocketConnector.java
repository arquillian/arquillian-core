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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The socket connector is a {@link Connector} that process Husky 
 * requests via an socket invocations.
 * 
 * Both, the test runner process as well as the remote target process must 
 * configure the properties {@link #PROP_SOCKET_CONNECTOR_HOST} and 
 * {@link #PROP_SOCKET_CONNECTOR_PORT}. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 17-May-2009
 */
public class SocketConnector extends AbstractConnector
{
   // Provide Logging
   private static final Logger logger = Logger.getLogger(SocketConnector.class.getName());
   
   /** The Husky socket connector host poperty: 'org.jboss.osgi.husky.runtime.connector.host' */
   public static final String PROP_SOCKET_CONNECTOR_HOST = Connector.class.getName().toLowerCase() + ".host";
   /** The Husky socket connector port poperty: 'org.jboss.osgi.husky.runtime.connector.port' */
   public static final String PROP_SOCKET_CONNECTOR_PORT = Connector.class.getName().toLowerCase() + ".port";
   
   private ServiceRegistration sreg;
   private ListenerThread listenerThread;

   public SocketConnector(BundleContext context)
   {
      super(context);
   }

   public static boolean isRemoteConnection(BundleContext context)
   {
      return getHost(context) != null && getPort(context) != null;
   }

   private static String getPort(BundleContext context)
   {
      String port = context.getProperty(PROP_SOCKET_CONNECTOR_PORT);
      return port;
   }

   private static String getHost(BundleContext context)
   {
      String host = context.getProperty(PROP_SOCKET_CONNECTOR_HOST);
      return host;
   }

   @Override
   public void start() throws Exception
   {
      super.start();
      
      BundleContext context = getBundleContext();
      String host = getHost(context);
      String port = getPort(context);

      Properties props = new Properties();
      props.setProperty("transport", "socket");
      props.setProperty("host", host);
      props.setProperty("port", port);

      listenerThread = new ListenerThread(host, new Integer(port));
      listenerThread.start();

      sreg = context.registerService(Connector.class.getName(), this, props);
      logger.info("SocketConnector registered: " + props);
   }

   @Override
   public void stop() throws Exception
   {
      super.stop();
      
      if (sreg != null)
         sreg.unregister();

      if (listenerThread != null)
         listenerThread.stopListener();
   }

   class ListenerThread extends Thread
   {
      private ServerSocket serverSocket;

      public ListenerThread(String host, int port)
      {
         super("ConnectorThread");
         try
         {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(host, port));
         }
         catch (IOException ex)
         {
            throw new IllegalStateException("Cannot create server socket", ex);
         }
      }

      public void stopListener()
      {
         try
         {
            logger.fine("Stop SocketConnector");
            serverSocket.close();
         }
         catch (IOException ex)
         {
            // ignore
         }
      }

      @Override
      public void run()
      {
         while (serverSocket.isClosed() == false)
         {
            Socket socket = null;
            try
            {
               logger.fine("Waiting for connection ...");
               socket = serverSocket.accept();
               logger.fine("Connection accepted");
            }
            catch (IOException ex)
            {
               if (serverSocket.isClosed())
                  break;
            }

            if (socket != null)
            {
               try
               {
                  InputStream resStream = process(socket.getInputStream());
                  Util.copyStream(resStream, socket.getOutputStream());
               }
               catch (Exception ex)
               {
                  logger.log(Level.SEVERE, "Cannot process request", ex);
               }
               finally
               {
                  try
                  {
                     socket.close();
                  }
                  catch (IOException ex)
                  {
                     logger.log(Level.SEVERE, "Cannot close socket", ex);
                  }
               }
            }
         }
      }
   }
}