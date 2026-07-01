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

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.jboss.arquillian.junit5.container.JUnitTestBaseClass.Cycle;
import static org.jboss.arquillian.junit5.container.JUnitTestBaseClass.wasCalled;

@ExtendWith(ArquillianExtension.class)
public class ClassWithArquillianExtensionAndParameterizedTest {

  @BeforeAll
  public static void beforeClass() throws Throwable {
    wasCalled(Cycle.BEFORE_CLASS);
  }

  @AfterAll
  public static void afterClass() throws Throwable {
    wasCalled(Cycle.AFTER_CLASS);
  }

  @BeforeEach
  public void before() throws Throwable {
    wasCalled(Cycle.BEFORE);
  }

  @AfterEach
  public void after() throws Throwable {
    wasCalled(Cycle.AFTER);
  }

  @ParameterizedTest
  @ValueSource(strings = {"one", "two"})
  public void failingTest() throws Throwable {
    wasCalled(Cycle.TEST);
    Assertions.fail("Intentionally failing the test.");
  }

  @ParameterizedTest
  @ValueSource(strings = {"one", "two"})
  public void succeedingTest() throws Throwable {
    wasCalled(Cycle.TEST);
  }
}
