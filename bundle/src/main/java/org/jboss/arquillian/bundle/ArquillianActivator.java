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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;

/**
 * This is the Husky {@link BundleActivator}.
 * 
 * It unconditionally starts the {@link JMXConnector}.
 * 
 * If the {@link SocketConnector#PROP_SOCKET_CONNECTOR_HOST} and 
 * {@link SocketConnector#PROP_SOCKET_CONNECTOR_PORT} properites are set it also
 * starts the {@link SocketConnector}.
 * 
 * Finally it starts the {@link HuskyExtender}, which is a {@link BundleListener}
 * that looks for manifest headers called  {@link ManifestProcessor#HEADER_TEST_PACKAGE}. 
 * 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 17-May-2009
 */
public class ArquillianActivator implements BundleActivator
{
   private SocketConnector socketConnector;
   private JMXConnector jmxConnector;

   public void start(BundleContext context) throws Exception
   {
      jmxConnector = new JMXConnector(context);
      jmxConnector.start();

      if (SocketConnector.isRemoteConnection(context))
      {
         socketConnector = new SocketConnector(context);
         socketConnector.start();
      }
   }

   public void stop(BundleContext context) throws Exception
   {
      if (socketConnector != null)
      {
         socketConnector.stop();
         socketConnector = null;
      }

      if (jmxConnector != null)
      {
         jmxConnector.stop();
         jmxConnector = null;
      }
   }
}