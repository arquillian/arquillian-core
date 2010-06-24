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
package org.jboss.arquillian.bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.impl.context.AbstractEventContext;
import org.jboss.arquillian.junit.JUnitTestRunner;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.testenricher.osgi.OSGiTestEnricher;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * JUnitTestRunner
 * 
 * A Implementation of the Arquillian TestRunner SPI for JUnit.
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JUnitBundleTestRunner extends JUnitTestRunner
{
   @Override
   protected List<RunListener> getRunListeners()
   {
      List<RunListener> listeners = new ArrayList<RunListener>(super.getRunListeners());
      listeners.add(new BundleRunListener());
      return Collections.unmodifiableList(listeners);
   }
   
   class BundleRunListener extends RunListener
   {
      @Override
      public void testRunStarted(Description descr) throws Exception
      {
         Class<?> testClass = descr.getTestClass();
         
         // [TODO] This is a hack. Get the context in some other way
         BundleContext context = ArquillianActivator.bundleContext;
         if (context == null)
            throw new IllegalStateException("Cannot obtain arquillian-bundle context");
         
         ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
         PackageAdmin pa = (PackageAdmin)context.getService(sref);
         Bundle testBundle = pa.getBundle(testClass);
         
         TestRunnerContext arquillianContext = new TestRunnerContext();
         arquillianContext.add(BundleContext.class, context.getBundle(0).getBundleContext());
         arquillianContext.add(Bundle.class, testBundle);

         OSGiTestEnricher enricher = new OSGiTestEnricher();
         enricher.enrich(arquillianContext, testClass);
      }
   }
   
   static class TestRunnerContext extends AbstractEventContext
   {
      @Override
      public Context getParentContext()
      {
         return null;
      }
   }
}
