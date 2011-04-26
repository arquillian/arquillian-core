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
package org.jboss.arquillian.test.spi;

import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.spi.TestRunnerAdaptor;

/**
 * DeployableTestBuilder
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestRunnerAdaptorBuilder
{
   private static final String DEFAULT_EXTENSION_CLASS = "org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader";
   private static final String TEST_RUNNER_IMPL_CLASS = "org.jboss.arquillian.test.impl.EventTestRunnerAdaptor";
   
   private TestRunnerAdaptorBuilder() {}
   
   public static TestRunnerAdaptor build() 
   {
      ManagerBuilder builder = ManagerBuilder.from()
         .extension(SecurityActions.loadClass(DEFAULT_EXTENSION_CLASS));

      return SecurityActions.newInstance(
            TEST_RUNNER_IMPL_CLASS, 
            new Class<?>[] {ManagerBuilder.class}, 
            new Object[] {builder}, 
            TestRunnerAdaptor.class);     
   }
}
