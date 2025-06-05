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
package org.jboss.arquillian.integration.test.lifecycle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 *
 * @author lprimak
 */
public class FileWriterExtension implements BeforeAllCallback, AutoCloseable,
    ExtensionContext.Store.CloseableResource {
    private static Path TMP_FILE_PATH;
    static final String TMP_FILE_ASSET_NAME = "temporaryFileAsset";

    enum RunsWhere {
        CLIENT,
        SERVER,
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if (!isRunningOnServer()) {
            Path tempDir = Files.createTempDirectory("arquillianLifecycleTest");
            initTmpFileName(tempDir);
            assertTrue(tempDir.toFile().delete(), "Cleanup Failed");
            createTmpFile();
        }
        extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put(this.getClass().getName(), this);
    }

    @Override
    public void close() throws IOException {
        if (isRunningOnServer()) {
            return;
        }
        String contents;
        try (FileReader fileReader = new FileReader(getTmpFilePath().toFile())) {
            char[] buf = new char[1000];
            int length = fileReader.read(buf);
            contents = new String(buf, 0, length - 1);
        }
        Path tempDir = getTmpFilePath().getParent();
        Files.walk(tempDir).map(Path::toFile).forEach(File::delete);
        assertTrue(tempDir.toFile().delete(), "Cleanup Failed");
        assertEquals("before_all,before_each,test_one,after_each,before_each,test_two,after_each,after_all", contents);
    }

    static void initTmpFileName(Path tmpDirBase) {
        TMP_FILE_PATH = tmpDirBase.getParent().resolve(
                tmpDirBase.getFileName() + "-arquillianLifecycleTest")
            .resolve("lifecycleOutput");
    }

    void createTmpFile() throws IOException {
        TMP_FILE_PATH.getParent().toFile().mkdir();
        File tmpFile = TMP_FILE_PATH.toFile();
        tmpFile.delete();
        assertTrue(tmpFile.createNewFile(), "cannot create results file");
    }

    static Path getTmpFilePath() {
        if (isRunningOnServer()) {
            try (InputStream istrm = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(TMP_FILE_ASSET_NAME)) {
                return Paths.get(new BufferedReader(new InputStreamReader(istrm)).readLine());
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        } else {
            return TMP_FILE_PATH;
        }
    }

    static void appendToFile(String str) {
        try (FileWriter fw = new FileWriter(getTmpFilePath().toFile(), true)) {
            fw.append(str + ",");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static void checkRunsWhere(RunsWhere expected) {
        assertEquals(expected, isRunningOnServer() ? RunsWhere.SERVER : RunsWhere.CLIENT);
    }

    static boolean isRunningOnServer() {
        try {
            new InitialContext().lookup("java:comp/env");
            return true;
        } catch (NamingException ex) {
            return false;
        }
    }
}
