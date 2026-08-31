/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit5.container;

import static org.jboss.arquillian.junit5.container.JUnitTestBaseClass.Cycle;
import static org.jboss.arquillian.junit5.container.JUnitTestBaseClass.wasCalled;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
public class ClassWithArquillianExtensionWithBeforeAfterEach {

    @BeforeEach
    public void before() throws Exception {
        wasCalled(Cycle.BEFORE);
    }

    @AfterEach
    public void after() throws Exception {
        wasCalled(Cycle.AFTER);
    }

    @Test
    public void shouldBeInvoked() throws Exception {
        wasCalled(Cycle.TEST);
    }
}
