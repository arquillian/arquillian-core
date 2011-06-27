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
package org.jboss.arquillian.container.test.impl.client.container.command;

import java.util.Map;
import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;


/**
 * StartContainerCommand
 *
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 */
public class StartContainerCommand extends AbstractCommand<String>
{
   private static final long serialVersionUID = 1L;

   private String containerQualifier;
   
   private Map<String, String> configuration;
   
   public StartContainerCommand(String containerQualifier)
   {
      this.containerQualifier = containerQualifier;
      this.configuration = null;
   }
   
   public StartContainerCommand(String containerQualifier, Map<String, String> config)
   {
      this.containerQualifier = containerQualifier;
      this.configuration = config;
   }
   
   /**
    * @return the containerQualifier
    */
   public String getContainerQualifier()
   {
      return containerQualifier;
   }
   
   public Map<String, String> getConfiguration()
   {
      return configuration;
   }
}
