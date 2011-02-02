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
package org.jboss.arquillian.spi.client.container;

/**
 * A utility SPI for use with @Excpected. Not all containers have a simple cause chain when it comes to 
 * deployment exceptions. This should transform from the container specific exception to the real cause. 
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface DeploymentExceptionTransformer
{
   /**
    * Transform from a Container specific deployment exception to the real cause.
    * 
    * @param exception The exception caught during deploy
    * @return The transformed Exception or null if exception is unknown/untransformable
    */
   Throwable transform(Throwable exception);
}
