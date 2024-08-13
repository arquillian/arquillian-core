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

import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.RunsWhere.CLIENT;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.RunsWhere.SERVER;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.TMP_FILE_ASSET_NAME;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.appendToFile;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.checkRunsWhere;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.getTmpFilePath;

import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author lprimak
 */
@ExtendWith(FileWriterExtension.class)
@ArquillianTest
public class LifecycleMethodsTest {
    @Inject
    Greeter greeter;

    @Test
    @Order(1)
    void one() {
        assertEquals("one", "one");
        appendToFile("test_one");
        checkRunsWhere(SERVER);
    }

    @Test
    @Order(2)
    void should_create_greeting() {
        assertEquals("Hello, Earthling!", greeter.createGreeting("Earthling"));
        greeter.greet(System.out, "Earthling");
        appendToFile("test_two");
        checkRunsWhere(SERVER);
    }

    @BeforeAll
    static void beforeAll() {
        appendToFile("before_all");
        checkRunsWhere(CLIENT);
    }

    @BeforeEach
    void beforeEach() {
        appendToFile("before_each");
        checkRunsWhere(SERVER);
    }

    @AfterEach
    void afterEeach() {
        appendToFile("after_each");
        checkRunsWhere(SERVER);
    }

    @AfterAll
    static void afterAll() {
        appendToFile("after_all");
        checkRunsWhere(CLIENT);
    }


    @Deployment
    static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(FileWriterExtension.class)
                .addClass(Greeter.class)
                .addAsResource(new StringAsset(getTmpFilePath().toString()), TMP_FILE_ASSET_NAME);
        return jar;
    }
}
