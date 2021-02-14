package org.jboss.arquillian.junit5.container;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
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
  public void shouldSkipOnAssumption() throws Exception {
    Assumptions.assumeTrue(false);
  }

  @Test
  public void shouldPassOnAssumption() throws Exception {
    Assumptions.assumeTrue(true);
  }

  @Test
  public void shouldPassOnException() throws Exception {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      throw new IllegalArgumentException();
    });
  }

  @Test
  public void shouldFailOnException() throws Exception {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
    });
  }

  @Test
  public void shouldFailExpectedWrongException() throws Exception {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      throw new UnsupportedOperationException();
    });
  }
}