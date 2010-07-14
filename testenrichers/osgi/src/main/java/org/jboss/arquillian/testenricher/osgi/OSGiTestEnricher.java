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
package org.jboss.arquillian.testenricher.osgi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * The OSGi TestEnricher
 * 
 * The enricher supports the injection of the system BundleContext and the test Bundle.
 * 
 * <pre><code>
 *    @Inject
 *    BundleContext context;
 * 
 *    @Inject
 *    Bundle bundle;
 * </code></pre>
 * 
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiTestEnricher implements TestEnricher
{
   // Provide logging
   private static Logger log = Logger.getLogger(OSGiTestEnricher.class);
   
   public void enrich(Context context, Object testCase)
   {
      Class<? extends Object> testClass = testCase.getClass();
      for (Field field : testClass.getDeclaredFields())
      {
         if (field.isAnnotationPresent(Inject.class))
         {
            if (field.getType().isAssignableFrom(BundleContext.class))
            {
               injectBundleContext(context, testCase, field);
            }
            if (field.getType().isAssignableFrom(Bundle.class))
            {
               injectBundle(context, testCase, field);
            }
         }
      }
   }

   public Object[] resolve(Context context, Method method)
   {
      return null;
   }
   
   private void injectBundleContext(Context context, Object testCase, Field field) 
   {
      try
      {
         field.set(testCase, getSystemBundleContext(context));
      }
      catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Cannot inject BundleContext", ex);
      }
   }

   private void injectBundle(Context context, Object testCase, Field field) 
   {
      try
      {
         field.set(testCase, getTestBundle(context, testCase.getClass()));
      }
      catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Cannot inject Bundle", ex);
      }
   }

   private BundleContext getSystemBundleContext(Context context)
   {
      BundleContext bundleContext = context.get(BundleContext.class);
      if (bundleContext == null)
         bundleContext = getBundleContextFromHolder();
      
      // Make sure this is really the system context
      bundleContext = bundleContext.getBundle(0).getBundleContext();
      return bundleContext;
   }

   private Bundle getTestBundle(Context context, Class<?> testClass)
   {
      Bundle testbundle = context.get(Bundle.class);
      if (testbundle == null)
      {
         // Get the test bundle from PackageAdmin with the test class as key 
         BundleContext bundleContext = getSystemBundleContext(context);
         ServiceReference sref = bundleContext.getServiceReference(PackageAdmin.class.getName());
         PackageAdmin pa = (PackageAdmin)bundleContext.getService(sref);
         testbundle = pa.getBundle(testClass);
      }
      return testbundle;
   }

   /**
    * Get the BundleContext associated with the arquillian-bundle
    */
   private BundleContext getBundleContextFromHolder()
   {
      try
      {
         MBeanServer mbeanServer = findOrCreateMBeanServer();
         ObjectName oname = new ObjectName(BundleContextHolder.OBJECT_NAME);
         BundleContextHolder holder = MBeanServerInvocationHandler.newProxyInstance(mbeanServer, oname, BundleContextHolder.class, false);
         return holder.getBundleContext();
      }
      catch (JMException ex)
      {
         throw new IllegalStateException("Cannot obtain arquillian-bundle context", ex);
      }
   }
   
   /**
    * Find or create the MBeanServer
    */
   public static MBeanServer findOrCreateMBeanServer()
   {
      MBeanServer mbeanServer = null;

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

      return mbeanServer;
   }
   
}
