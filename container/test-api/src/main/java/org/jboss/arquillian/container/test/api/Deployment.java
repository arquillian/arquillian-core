/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.test.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The {@code @Deployment} is used to define which methods should be be considered as deployment producers. Arquillian
 * support
 * two types of deployment units, a {@link Archive} or a {@link Descriptor}.
 * <p>
 * A deployment represent the isolation level of your test, that being a single JavaArchive or a multi module
 * EnterpriseArchive.
 * <p>
 * The deployment producer will be executed to create the deployment before the Test run, this to detect environment
 * problems as soon as
 * possible.
 * <p>
 * <p>
 * Usage Example:<br/>
 * <pre><code>
 * &#64;Deployment
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class);
 * }
 *
 * &#64;Deployment
 * public static Descriptor create() {
 *      return Descriptors.create(HornetQDescriptor.class);
 * }
 * </code></pre>
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Deployment {
    /**
     * Name the deployment so you can reference it using the {@link Deployer} API if managed is false or method is using
     * @OperateOnDeployment
     *
     * @return The name of this Deployment
     */
    String name() default "_DEFAULT_";

    /**
     * Describes whether or not this deployment should be deployed by Arquillian.
     */
    boolean managed() default true;

    /**
     * If multiple deployments are specified against the same target and defined as startup, this control the order of
     * which they
     * will be given to the Container.
     */
    int order() default -1;

    /**
     * Defines if this deployment should be wrapped up based on the protocol so the testcase can be executed incontainer.
     */
    boolean testable() default true;
}
