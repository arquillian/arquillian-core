package com.acme.ejb;

import javax.ejb.EJB;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class TemperatureConverterTestCase extends Arquillian {

   @EJB
   private TemperatureConverter converter;

   @Deployment
   public static JavaArchive createTestArchive() {
      return ShrinkWrap.create("test.jar", JavaArchive.class)
         .addClasses(TemperatureConverter.class, TemperatureConverterBean.class);
   }

   @Test
   public void testConvertToCelsius() {
      Assert.assertEquals(converter.convertToCelsius(32d), 0d);
      Assert.assertEquals(converter.convertToCelsius(212d), 100d);
   }

   @Test
   public void testConvertToFarenheit() {
      Assert.assertEquals(converter.convertToFarenheit(0d), 32d);
      Assert.assertEquals(converter.convertToFarenheit(100d), 212d);
   }

   @Test
   public void testIsTransactional() {
      Assert.assertTrue(converter.isTransactional());
   }

}
