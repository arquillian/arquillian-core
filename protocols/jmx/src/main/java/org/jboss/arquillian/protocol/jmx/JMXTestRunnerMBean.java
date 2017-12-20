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
package org.jboss.arquillian.protocol.jmx;

import java.util.Map;
import javax.management.NotificationBroadcaster;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.test.spi.TestResult;

/**
 * An MBean to run test methods in container.
 *
 * @author thomas.diesler@jboss.com
 */
public interface JMXTestRunnerMBean extends NotificationBroadcaster {

    /**
     * The ObjectName for this service: jboss.arquillian:service=jmx-test-runner
     */
    String OBJECT_NAME = "jboss.arquillian:service=jmx-test-runner";

    /**
     * Runs a test method on the given test class
     *
     * @param className
     *     the test class name
     * @param methodName
     *     the test method name
     *
     * @return a serialized {@link TestResult}
     *
     * @deprecated
     */
    @Deprecated
    public byte[] runTestMethod(String className, String methodName);

    /**
     * Runs a test method on the given test class
     *
     * @param className
     *     the test class name
     * @param methodName
     *     the test method name
     * @param protocol
     *     configuration properties
     *
     * @return a serialized {@link TestResult}
     */
    public byte[] runTestMethod(String className, String methodName, Map<String, String> protocolProps);

    /**
     * Broadcast {@link Command} commands to any listeners
     *
     * @param command
     *     Command object containing the request
     */
    void send(Command<?> command);

    /**
     * Receive {@link Command} results
     *
     * @return command Command object containing the result, null if none received (yet)
     */
    Command<?> receive();

    /**
     * Client side to push a {@link Command} result back to container.
     *
     * @param eventId
     *     used to correlate the result
     * @param command
     *     Command object containing the result, serialized
     */
    void push(String eventId, byte[] command);
}
