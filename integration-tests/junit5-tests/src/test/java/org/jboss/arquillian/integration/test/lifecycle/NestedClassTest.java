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
package org.jboss.arquillian.integration.test.lifecycle;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.TMP_FILE_ASSET_NAME;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.appendToFile;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.checkRunsWhere;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.getTmpFilePath;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.RunsWhere.CLIENT;
import static org.jboss.arquillian.integration.test.lifecycle.FileWriterExtension.RunsWhere.SERVER;

@Disabled("https://github.com/arquillian/arquillian-core/issues/773")
@ExtendWith(FileWriterExtension.class)
@ArquillianTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExpectedTrace("before_all,"
        + "outer_before_each,outer_test,outer_after_each,"
        + "outer_before_each,inner_before_each,nested_test,inner_after_each,outer_after_each,"
        + "after_all")
class NestedClassTest {

    @Deployment
    static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(FileWriterExtension.class, ExpectedTrace.class)
                .addAsResource(new StringAsset(getTmpFilePath().toString()), TMP_FILE_ASSET_NAME)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeAll
    static void beforeAll() {
        appendToFile("before_all");
        checkRunsWhere(CLIENT);
    }

    @BeforeEach
    void outerBeforeEach() {
        appendToFile("outer_before_each");
        // runs on both server and client
    }

    @AfterEach
    void outerAfterEach() {
        appendToFile("outer_after_each");
        // runs on both server and client
    }

    @Test
    @Order(1)
    void outerTest() {
        appendToFile("outer_test");
        checkRunsWhere(SERVER);
    }

    @Nested
    @Order(2)
    class InnerTest {

        @BeforeEach
        void innerBeforeEach() {
            appendToFile("inner_before_each");
            checkRunsWhere(SERVER);
        }

        @AfterEach
        void innerAfterEach() {
            appendToFile("inner_after_each");
            checkRunsWhere(SERVER);
        }

        @Test
        void nestedTest() {
            appendToFile("nested_test");
            checkRunsWhere(SERVER);
        }
    }

    @AfterAll
    static void afterAll() {
        appendToFile("after_all");
        checkRunsWhere(CLIENT);
    }
}