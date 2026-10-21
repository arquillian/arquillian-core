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
package org.jboss.arquillian.junit5.container.fixtures;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * A test method name shared by a test class and the {@code @Nested} classes within it. The in-container runner
 * receives just a class and a method name, so it must tell these apart by the class the method runs in.
 */
public class SameMethodNameNestedFixture {

    public static final List<Class<?>> INVOKED = new CopyOnWriteArrayList<>();

    @Test
    public void test() {
        INVOKED.add(SameMethodNameNestedFixture.class);
    }

    @Nested
    public class InnerTests {

        @Test
        public void test() {
            INVOKED.add(InnerTests.class);
        }

        @Nested
        public class DeeperTests {

            @Test
            public void test() {
                INVOKED.add(DeeperTests.class);
            }
        }
    }
}
