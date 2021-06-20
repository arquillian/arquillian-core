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
package org.jboss.arquillian.junit5.lifecycle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.jboss.arquillian.junit5.lifecycle.CreateFileTestCase.getTmpFilePath;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author lprimak
 */
// ensure this runs last
public class XCheckFileTestCase {
    @Test
    void check() throws IOException {
        try (FileReader fileReader = new FileReader(getTmpFilePath().toFile())) {
            char[] buf = new char[1000];
            int length = fileReader.read(buf);
            String contents = new String(buf, 0, length - 1);
            assertEquals("before_all,before_each,test_one,after_each,before_each,test_two,after_each,after_all", contents);
        }
        Path tempDir = getTmpFilePath().getParent();
        Files.walk(tempDir).map(Path::toFile).forEach(File::delete);
        assertTrue(tempDir.toFile().delete(), "Cleanup Failed");
    }
}
