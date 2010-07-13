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
package org.jboss.arquillian.impl.context;

import java.util.Collection;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.spi.ClassContextAppender;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.SuiteContextAppender;
import org.jboss.arquillian.spi.TestContextAppender;

/**
 * A ProfileBuilder that combines the defined 'internal' Profile with the SPI loadable profiles.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServiceLoadableProfileBuilder implements ProfileBuilder
{
   private ServiceLoader serviceLoader;
   private ProfileBuilder containerProfileBuilder;
   
   public ServiceLoadableProfileBuilder(ServiceLoader serviceLoader, ProfileBuilder containerProfilebuilder)
   {
      Validate.notNull(serviceLoader, "ServiceLoader must be specified");
      Validate.notNull(containerProfilebuilder, "ProfileBuilder must be specified");
      
      this.serviceLoader = serviceLoader;
      this.containerProfileBuilder = containerProfilebuilder;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ProfileBuilder#buildSuiteContext(org.jboss.arquillian.impl.context.SuiteContext)
    */
   public void buildSuiteContext(SuiteContext context)
   {
      containerProfileBuilder.buildSuiteContext(context);
      Collection<SuiteContextAppender> appenders = serviceLoader.all(SuiteContextAppender.class);
      for(SuiteContextAppender appender : appenders)
      {
         appender.append(context);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ProfileBuilder#buildClassContext(org.jboss.arquillian.impl.context.ClassContext, java.lang.Class)
    */
   public void buildClassContext(ClassContext context, Class<?> testClass)
   {
      containerProfileBuilder.buildClassContext(context, testClass);
      Collection<ClassContextAppender> appenders = serviceLoader.all(ClassContextAppender.class);
      for(ClassContextAppender appender : appenders)
      {
         appender.append(context);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.context.ProfileBuilder#buildTestContext(org.jboss.arquillian.impl.context.TestContext, java.lang.Object)
    */
   public void buildTestContext(TestContext context, Object testInstance)
   {
      containerProfileBuilder.buildTestContext(context, testInstance);
      Collection<TestContextAppender> appenders = serviceLoader.all(TestContextAppender.class);
      for(TestContextAppender appender : appenders)
      {
         appender.append(context);
      }
   }
}
