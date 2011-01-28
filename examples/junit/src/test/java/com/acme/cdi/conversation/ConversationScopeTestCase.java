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
package com.acme.cdi.conversation;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * ConversationScope
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Ignore // Revert until 1.1 Weld is released.. https://jira.jboss.org/browse/ARQ-185
@RunWith(Arquillian.class)
public class ConversationScopeTestCase
{
   @Deployment
   public static JavaArchive createDeployment() {
       return ShrinkWrap.create(JavaArchive.class)
               .addClass(LongRunningBean.class)
               .addManifestResource(
                     EmptyAsset.INSTANCE, 
                     ArchivePaths.create("beans.xml"));
   }

   @Inject 
   private LongRunningBean bean;
   
   @Inject 
   private Conversation conversation;
   
   @Test
   public void firstCall() throws Exception
   {
      bean.addMessage("test1");
      conversation.begin();
   }

   @Test
   public void secondCall() throws Exception
   {
      Assert.assertEquals(1, bean.getMessages().size());
   }
}
