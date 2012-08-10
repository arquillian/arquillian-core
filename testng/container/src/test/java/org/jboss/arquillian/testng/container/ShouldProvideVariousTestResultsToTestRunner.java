package org.jboss.arquillian.testng.container;

import org.testng.Assert;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;

public class ShouldProvideVariousTestResultsToTestRunner
{
   @org.testng.annotations.Test(expectedExceptions = IllegalArgumentException.class)
   public void shouldProvideExpectedExceptionToRunner() throws Exception
   {
      throw new IllegalArgumentException();
   }

   @org.testng.annotations.Test
   public void shouldProvidePassingTestToRunner() throws Exception
   {
      Assert.assertTrue(true);
   }

   @org.testng.annotations.Test
   public void shouldProvideFailingTestToRunner() throws Exception
   {
      Assert.fail("Failing by design");
   }

   @DataProvider(name = "xx")
   public static Object[][] getCurrentMethod(Method m)
   {
      return new Object[][]{new Object[]{m}};
   }

   @org.testng.annotations.Test(dataProvider = "xx")
   public void shouldBeAbleToUseOtherDataProviders(Method m) throws Exception
   {
      Assert.assertNotNull(m);
   }
}
