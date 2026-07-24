/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.integration.test.common.lifecycle;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Manages temporary trace files for lifecycle test validation.
 * Handles creation, writing, reading, and cleanup of trace files
 * that record the order and location of lifecycle method invocations.
 */
public final class TraceFileManager {

    public static final String TMP_FILE_ASSET_NAME = "temporaryFileAsset";

    private static final String ARQUILLIAN_LIFECYCLE_TEST = "arquillianLifecycleTest";

    private static final String LIFECYCLE_OUTPUT = "lifecycleOutput";

    private static final ThreadLocal<Path> tmpFilePath = new ThreadLocal<>();

    private TraceFileManager() {
    }

    public static Path createTmpFile() throws IOException {
        Path tempDir = Files.createTempDirectory(ARQUILLIAN_LIFECYCLE_TEST);
        tmpFilePath.set(tempDir.resolve(LIFECYCLE_OUTPUT));
        Files.createFile(tmpFilePath.get());
        return tmpFilePath.get();
    }

    public static Path getTmpFilePath() {
        if (RunsWhere.isRunningOnServer()) {
            try (InputStream istrm = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(TMP_FILE_ASSET_NAME)) {
                return Paths.get(new BufferedReader(new InputStreamReader(istrm)).readLine());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            return tmpFilePath.get();
        }
    }

    public static void appendToFile(String stepName) {
        RunsWhere location = RunsWhere.getCurrentRunningLocation();
        try (FileWriter fw = new FileWriter(getTmpFilePath().toFile(), true)) {
            fw.append(stepName + ":" + location + ",");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String readFromFile(Path path) {
        try {
            String content = Files.readString(path);
            if (content.isEmpty()) {
                return "";
            }
            return content.substring(0, content.length() - 1);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void cleanupTmpDir(Path tempDir) throws IOException {
        try (Stream<Path> paths = Files.walk(tempDir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
        tmpFilePath.remove();
        if (Files.exists(tempDir)) {
            throw new IOException("Cleanup Failed: " + tempDir);
        }
    }
}
