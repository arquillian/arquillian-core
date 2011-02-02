/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.testenricher.cdi;

import java.util.ArrayList;

import junit.framework.Assert;

import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;


/**
 * BeansXMLProtocolProcessorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class BeansXMLProtocolProcessorTestCase
{

   @Test
   public void shouldAddBeansXMLWhenFoundInWebArchive() 
   {
      WebArchive deployment = ShrinkWrap.create(WebArchive.class)
                              .addWebResource(EmptyAsset.INSTANCE, "beans.xml");
      
      runAndAssert(deployment, true);
   }

   @Test
   public void shouldNotAddBeansXMLIfNotFoundInWebArchive() 
   {
      WebArchive deployment = ShrinkWrap.create(WebArchive.class);
      
      runAndAssert(deployment, false);
   }
   
   @Test
   public void shouldAddBeansXMLWhenFoundInEnterpriseModule()
   {
      EnterpriseArchive deployment = ShrinkWrap.create(EnterpriseArchive.class)
                                       .addModule(
                                             ShrinkWrap.create(WebArchive.class)
                                             .addWebResource(EmptyAsset.INSTANCE, "beans.xml"));
      
      runAndAssert(deployment, true);
   }

   @Test
   public void shouldNotAddBeansXMLIfNotFoundInEnterpriseModule()
   {
      EnterpriseArchive deployment = ShrinkWrap.create(EnterpriseArchive.class)
                                       .addModule(
                                             ShrinkWrap.create(WebArchive.class));
      
      runAndAssert(deployment, false);
   }

   @Test
   public void shouldNotAddBeansXMLIfArchivesAreEqual()
   {
      WebArchive protocol = ShrinkWrap.create(WebArchive.class);

      new BeansXMLProtocolProcessor().process(
            new TestDeployment(protocol, new ArrayList<Archive<?>>()), protocol);
      
      
      Assert.assertFalse(protocol.contains("WEB-INF/beans.xml"));
   }

   public void runAndAssert(Archive<?> deployment, boolean shouldBeFound)
   {
      WebArchive protocol = ShrinkWrap.create(WebArchive.class);
      
      new BeansXMLProtocolProcessor().process(
            new TestDeployment(deployment, new ArrayList<Archive<?>>()), protocol);
      
      
      Assert.assertEquals(
            "Verify beans.xml was " + (!shouldBeFound ? "not ":"") + "found",
            shouldBeFound, protocol.contains("WEB-INF/beans.xml"));
   }
}
