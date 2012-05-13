/*
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.test.api;

/**
 * A collection of constants that represent groups of Arquillian services that are
 * added to the deployment archive to facilitate or enhance in-container testing.
 * 
 * @since 1.0.1.Final
 * @author <a href="http://community.jboss.org/people/dan.j.allen">Dan Allen</a>
 * @see Deployment
 */
public interface ServiceType {
    /**
     * Represents all Arquillian services that are added to a deployment.
     */
    public static final String ALL = "*";
    
    /**
     * Provides the test runner infrastructure and test class to run tests in-container over the specified protocol.
     */
    public static final String TEST_RUNNER = "test-runner";
    
    /**
     * Instrumentation services for analyzing and managing code running inside the container.
     * 
     * <p>Instrumentation is the addition of code for the purpose of gathering data to be utilized by tools. Since the changes are
     * purely additive, these tools do not modify application state or behavior. Examples include monitoring agents, profilers,
     * coverage analyzers, and event loggers.</p>
     * 
     * <p>Not currently honored as an exclusion.</p>
     */
    public static final String INSTRUMENTATION = "instrumentation";
    
    /**
     * <p>Adds additional component model services to the deployment such as CDI for Servlet containers or Spring libraries.</p>
     * 
     * <p>Not currently honored as an exclusion.</p>
     */
    public static final String DEPLOYERS = "deployers";
}