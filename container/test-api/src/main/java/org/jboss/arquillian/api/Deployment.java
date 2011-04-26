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
package org.jboss.arquillian.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines that this method returns a deployment definition, either a {@link Archive} or a {@link Descriptor}.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Deployment 
{
   /**
    * Name the deployment so you can reference it using the {@link Deployer} API if managed is false or target is using @OperateOnDeployment
    * 
    * @return The name of this Deployment
    */
   String name() default "NO-NAME";
   
   /**
    * Describes whether or not this deployment should be deployed by Arquillian.
    * 
    * @return
    */
   boolean managed() default true;
   
   /**
    * If multiple deployments are specified against the same target and defined as startup, this control the order of which they
    * will be given to the Container.
    * 
    * @return
    */
   int order() default -1;
   
   /**
    * Defines if this deployment should be wrapped up based on the protocol so the testcase can be executed incontainer. 
    * 
    * @return
    */
   boolean testable() default true;
}
