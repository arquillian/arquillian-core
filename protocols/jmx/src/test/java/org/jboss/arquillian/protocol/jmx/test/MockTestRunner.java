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
package org.jboss.arquillian.protocol.jmx.test;

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.protocol.jmx.JMXCommandService;
import org.jboss.arquillian.test.spi.TestResult;

/**
 * MockTestRunner
 * <p>
 * TestRunner that will return what you want for testing
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class MockTestRunner implements TestRunner {
    public static TestResult wantedResults;

    public static List<Command<?>> commands = new ArrayList<Command<?>>();

    public static List<Object> commandResults = new ArrayList<Object>();

    public static void add(Command<?> command) {
        commands.add(command);
    }

    public static void add(TestResult wantedTestResult) {
        wantedResults = wantedTestResult;
    }

    public static void clear() {
        wantedResults = null;
        commands.clear();
        commandResults.clear();
    }

    public TestResult execute(Class<?> testClass, String methodName) {
        for (Command<?> command : commands) {
            commandResults.add(new JMXCommandService().execute(command));
        }

        return wantedResults;
    }
}
