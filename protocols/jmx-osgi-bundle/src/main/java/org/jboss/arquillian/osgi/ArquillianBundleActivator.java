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
package org.jboss.arquillian.osgi;

// $Id$

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.jboss.arquillian.protocol.jmx.JMXTestRunner;
import org.jboss.arquillian.protocol.jmx.JMXTestRunner.TestClassLoader;
import org.jboss.arquillian.testenricher.osgi.BundleContextHolder;
import org.jboss.arquillian.testenricher.osgi.OSGiTestEnricher;
import org.jboss.logging.Logger;
import org.osgi.framework.Bundle;
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
public class ArquillianBundleActivator implements BundleActivator
{
   // Provide logging
   private static Logger log = Logger.getLogger(ArquillianBundleActivator.class);

   private  JMXTestRunner testRunner;
   
   public void start(final BundleContext context) throws Exception
   {
      TestClassLoader loader = new TestClassLoader()
      {
         public Class<?> loadTestClass(String className) throws ClassNotFoundException
         {
            Bundle bundle = context.getBundle();
            return bundle.loadClass(className);
         }
      };
      
      // Register the JMXTestRunner
      MBeanServer mbeanServer = getMBeanServer(context);
      testRunner = new JMXTestRunner(loader);
      testRunner.registerMBean(mbeanServer);

      // Register the BundleContextHolder
      BundleContextHolder holder = new BundleContextHolder()
      {
         public BundleContext getBundleContext()
         {
            return context;
         }
      };
      StandardMBean holderMBean = new StandardMBean(holder, BundleContextHolder.class);
      mbeanServer.registerMBean(holderMBean, new ObjectName(BundleContextHolder.OBJECT_NAME));
   }

   public void stop(BundleContext context) throws Exception
   {
      // Unregister the JMXTestRunner
      MBeanServer mbeanServer = getMBeanServer(context);
      testRunner.unregisterMBean(mbeanServer);

      // Unregister the BundleContextHolder
      mbeanServer.unregisterMBean(new ObjectName(BundleContextHolder.OBJECT_NAME));
   }

   private MBeanServer getMBeanServer(BundleContext context)
   {
      // Check if the MBeanServer is registered as an OSGi service 
      ServiceReference sref = context.getServiceReference(MBeanServer.class.getName());
      if (sref != null)
      {
         MBeanServer mbeanServer = (MBeanServer)context.getService(sref);
         log.debug("Found MBeanServer fom service: " + mbeanServer.getDefaultDomain());
         return mbeanServer;
      }

      // Find or create the MBeanServer
      return OSGiTestEnricher.findOrCreateMBeanServer();
   }
}