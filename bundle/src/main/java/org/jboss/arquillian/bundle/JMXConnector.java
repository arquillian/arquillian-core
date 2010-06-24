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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The JMX connector is a {@link Connector} that process Husky 
 * requests via an JMX invocation. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 17-May-2009
 */
public class JMXConnector extends AbstractConnector implements JMXConnectorMBean
{
   // Provide Logging
   private static final Logger logger = Logger.getLogger(JMXConnector.class.getName());

   /** The ObjectName for this service: jboss.osgi.husky:service=jmx-connector */
   public static ObjectName OBJECT_NAME;
   static
   {
      try
      {
         OBJECT_NAME = ObjectName.getInstance("jboss.osgi.husky:service=jmx-connector");
      }
      catch (MalformedObjectNameException ex)
      {
         // ignore
      }
   }

   public JMXConnector(BundleContext context)
   {
      super(context);
   }

   @Override
   public void start() throws Exception
   {
      super.start();
      
      MBeanServer mbeanServer = getMBeanServer();
      mbeanServer.registerMBean(this, OBJECT_NAME);

      Properties props = new Properties();
      props.setProperty("transport", "jmx");
      getBundleContext().registerService(Connector.class.getName(), this, props);

      logger.info("JMXConnector registered: " + OBJECT_NAME);
   }

   @Override
   public void stop() throws Exception
   {
      super.stop();
      
      MBeanServer mbeanServer = getMBeanServer();
      if (mbeanServer.isRegistered(OBJECT_NAME))
         mbeanServer.unregisterMBean(OBJECT_NAME);
   }

   @Override
   public InputStream process(InputStream reqStream)
   {
      return super.process(reqStream);
   }

   private MBeanServer getMBeanServer()
   {
      MBeanServer mbeanServer = null;
      BundleContext context = getBundleContext();

      // Check if there is an MBeanServer service already
      ServiceReference sref = context.getServiceReference(MBeanServer.class.getName());
      if (sref != null)
      {
         mbeanServer = (MBeanServer)context.getService(sref);
         logger.fine("Found MBeanServer fom service: " + mbeanServer.getDefaultDomain());
      }
      else
      {
         ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
         if (serverArr.size() > 1)
            logger.warning("Multiple MBeanServer instances: " + serverArr);

         if (serverArr.size() > 0)
         {
            mbeanServer = serverArr.get(0);
            logger.fine("Found MBeanServer: " + mbeanServer.getDefaultDomain());
         }

         if (mbeanServer == null)
         {
            logger.fine("No MBeanServer, create one ...");
            mbeanServer = MBeanServerFactory.createMBeanServer();
         }
      }
      return mbeanServer;
   }
}