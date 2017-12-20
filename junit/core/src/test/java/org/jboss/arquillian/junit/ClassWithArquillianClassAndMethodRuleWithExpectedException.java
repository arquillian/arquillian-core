package org.jboss.arquillian.junit;

import org.jboss.arquillian.junit.JUnitTestBaseClass.Cycle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.jboss.arquillian.junit.JUnitTestBaseClass.wasCalled;

public class ClassWithArquillianClassAndMethodRuleWithExpectedException
{
   @ClassRule
   public static ArquillianTestClass arquillianTestClass = new ArquillianTestClass();

   @Rule
   public ArquillianTest arquillianTest = new ArquillianTest();

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      wasCalled(Cycle.BEFORE_CLASS);
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      wasCalled(Cycle.AFTER_CLASS);
   }

   @Before
   public void before() throws Throwable
   {
      wasCalled(Cycle.BEFORE);
   }

   @After
   public void after() throws Throwable
   {
      wasCalled(Cycle.AFTER);
   }

   @Test(expected = IllegalArgumentException.class)
   public void shouldBeInvoked() throws Throwable
   {
      wasCalled(Cycle.TEST);
      throw new IllegalArgumentException();
   }
}
