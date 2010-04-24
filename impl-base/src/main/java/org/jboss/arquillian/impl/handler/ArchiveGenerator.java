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
package org.jboss.arquillian.impl.handler;

import org.jboss.arquillian.impl.DeploymentGenerator;
import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.shrinkwrap.api.Archive;

/**
 * A Handler for generate the {@link Archive} used for deployment. <br/>
 * <br/>
 * 
 *  <b>Imports:</b><br/>
 *   {@link DeploymentGenerator}<br/>
 *  <br/>
 *  <b>Exports:</b><br/>
 *   {@link Archive}<br/>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * 
 * @see DeploymentGenerator
 * @see Archive 
 */
public class ArchiveGenerator implements EventHandler<ClassEvent>
{
   
   public void callback(Context context, ClassEvent event) throws Exception
   {
      DeploymentGenerator generator = context.get(DeploymentGenerator.class);
      Validate.stateNotNull(generator, "No " + DeploymentGenerator.class.getName() + " found in context");
      
      Archive<?> deployment = generator.generate(event.getTestClass());
      
      context.add(Archive.class, deployment);
   }
}
