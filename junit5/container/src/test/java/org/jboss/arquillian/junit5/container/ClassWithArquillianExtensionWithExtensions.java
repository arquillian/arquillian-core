package org.jboss.arquillian.junit5.container;

import static org.jboss.arquillian.junit5.container.JUnitTestBaseClass.Cycle;
import static org.jboss.arquillian.junit5.container.JUnitTestBaseClass.wasCalled;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

@ExtendWith(ArquillianExtension.class)
@ExtendWith(ClassWithArquillianExtensionWithExtensions.MethodRule.class)
@ExtendWith(ClassWithArquillianExtensionWithExtensions.ClassRule.class)
public class ClassWithArquillianExtensionWithExtensions {

  public static class ClassRule implements AfterAllCallback, BeforeAllCallback {
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
      wasCalled(Cycle.AFTER_CLASS_RULE);
    }
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
      wasCalled(Cycle.BEFORE_CLASS_RULE);
    }
  }

  public static class MethodRule implements AfterEachCallback, BeforeEachCallback {
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
      wasCalled(Cycle.AFTER_RULE);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
      wasCalled(Cycle.BEFORE_RULE);
    }
  }

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

  @Test
  public void shouldBeInvoked() throws Throwable {
    wasCalled(Cycle.TEST);
  }
}
