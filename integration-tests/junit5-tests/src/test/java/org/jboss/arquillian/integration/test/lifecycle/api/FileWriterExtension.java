/*
 * Copyright 2021 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.integration.test.lifecycle.api;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;

import org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceFileManager;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceStep;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceValidator;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit Jupiter extension that records lifecycle method invocations to a
 * temporary file and validates them against the expected trace declared
 * via {@link ArquillianIntegrationTest}.
 *
 * @author lprimak
 */
public class FileWriterExtension implements BeforeAllCallback, AutoCloseable {

    private Path instanceTmpFilePath;
    private Class<?> testClass;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        Class<?> clazz = extensionContext.getRequiredTestClass();
        if (clazz.getAnnotation(ArquillianIntegrationTest.class) == null) {
            return;
        }
        testClass = clazz;
        if (RunsWhere.isRunningOnClient()) {
            instanceTmpFilePath = TraceFileManager.createTmpFile();
        }
        extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
                .put(this.getClass().getName() + "." + testClass.getName(), this);
    }

    @Override
    public void close() throws IOException {
        if (RunsWhere.isRunningOnServer()) {
            return;
        }
        String contents = TraceFileManager.readFromFile(instanceTmpFilePath);
        TraceFileManager.cleanupTmpDir(instanceTmpFilePath.getParent());

        TraceStep[] expectedSteps = testClass.getAnnotation(ArquillianIntegrationTest.class).value();
        TraceValidator.validate(testClass.getSimpleName(), expectedSteps, contents)
                .ifPresent(error -> fail(error));
    }

    public static Path getTmpFilePath() {
        return TraceFileManager.getTmpFilePath();
    }

    public static void appendToFile(String str) {
        TraceFileManager.appendToFile(str);
    }
}
