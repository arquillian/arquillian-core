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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TestScenarios {

  public static Exception exceptionThrownInBefore;

  public static Exception exceptionThrownInAfter;

  @BeforeEach
  public void throwExceptionInBefore() throws Exception {
    if (exceptionThrownInBefore != null) {
      Exception e = exceptionThrownInBefore;
      exceptionThrownInBefore = null;
      throw e;
    }
  }

  @AfterEach
  public void throwExceptionInAfter() throws Exception {
    if (exceptionThrownInAfter != null) {
      Exception e = exceptionThrownInAfter;
      exceptionThrownInAfter = null;
      throw e;
    }
  }

  @Test
  public void shouldSucceed() {
    Assertions.assertTrue(true);
  }

  @Test
  public void shouldSkipOnAssumption() {
    Assumptions.assumeTrue(false);
  }

  @Test
  public void shouldPassOnAssumption() {
    Assumptions.assumeTrue(true);
  }

  @Test
  public void shouldPassOnException() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      throw new IllegalArgumentException();
    });
  }

  @Test
  public void shouldFailOnException() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
    });
  }

  @Test
  public void shouldFailExpectedWrongException() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      throw new UnsupportedOperationException();
    });
  }

  @Nested
  class NestedTestScenarios {

    @Test
    public void shouldPassOnAssumptionInNested() {
      Assumptions.assumeTrue(true);
    }
  }
}
