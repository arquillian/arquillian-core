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

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.arquillian.protocol.jmx.JMXTestRunner;
import org.jboss.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * This is the Arquillian {@link BundleActivator}.
 * 
 * It unconditionally starts the {@link JMXTestRunner}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 17-May-2009
 */
public class ArquillianActivator implements BundleActivator
{
   // Provide logging
   private static Logger log = Logger.getLogger(ArquillianActivator.class);

   // An thread local association 
   static BundleContext bundleContext;
   
   @Override
   public void start(BundleContext context) throws Exception
   {
      ArquillianActivator.bundleContext = context;
      
      // Register the JMX TestRunner
      MBeanServer mbeanServer = getMBeanServer(context);
      JMXTestRunner.register(mbeanServer);
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
      // Unregister the JMX TestRunner
      MBeanServer mbeanServer = getMBeanServer(context);
      JMXTestRunner.unregister(mbeanServer);
      
      ArquillianActivator.bundleContext = null;
   }

   private MBeanServer getMBeanServer(BundleContext context)
   {
      MBeanServer mbeanServer = null;
      
      // Check if there is an MBeanServer service already
      ServiceReference sref = context.getServiceReference(MBeanServer.class.getName());
      if (sref != null)
      {
         mbeanServer = (MBeanServer)context.getService(sref);
         log.debug("Found MBeanServer fom service: " + mbeanServer.getDefaultDomain());
         return mbeanServer;
      }

      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         log.warn("Multiple MBeanServer instances: " + serverArr);

      if (serverArr.size() > 0)
      {
         mbeanServer = serverArr.get(0);
         log.debug("Found MBeanServer: " + mbeanServer.getDefaultDomain());
      }

      if (mbeanServer == null)
      {
         log.debug("No MBeanServer, create one ...");
         mbeanServer = MBeanServerFactory.createMBeanServer();
      }
      
      // Register the MBeanServer under the system context
      BundleContext syscontext = context.getBundle(0).getBundleContext();
      syscontext.registerService(MBeanServer.class.getName(), mbeanServer, null);
      
      return mbeanServer;
   }
}