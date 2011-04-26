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
package org.jboss.arquillian.spi.client.deployment;

import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Used by enrichers to add Enricher specific information to the Protocols archive. <br/>
 * <br/>
 * Example:
 *  A BeanManager is only available if the caller is a BeanArchive. For Enterprise archives where the EJB module
 *  is a BeanArchive, the Protocol WAR is added without a beans.xml. The result is that the Protocol can't see the EJBs BeanManager.
 * 
 *  This extension point allowed the CDI enricher to add a beans.xml to the protocol.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface ProtocolArchiveProcessor
{
   /**
    *  
    * @param testDeployment The user defined deployment + auxilliary archives
    * @param protocolArchive The archive where the protocol is bundled. 
    */
   void process(TestDeployment testDeployment, Archive<?> protocolArchive);
}
