package com.acme.ejb;

import javax.ejb.EJB;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TemperatureConverterTestCase extends Arquillian {

   @EJB
   private TemperatureConverter converter;

   @Deployment
   public static JavaArchive createTestArchive() {
      return Archives.create("test.jar", JavaArchive.class)
         .addClasses(TemperatureConverter.class, TemperatureConverterBean.class);
   }

   @Test
   public void testConvertToCelcius() {
      Assert.assertEquals(converter.convertToCelcius(32d), 0d);
      Assert.assertEquals(converter.convertToCelcius(212d), 100d);
   }

   @Test
   public void testConvertToFarenheight() {
      Assert.assertEquals(converter.convertToFarenheight(0d), 32d);
      Assert.assertEquals(converter.convertToFarenheight(100d), 212d);
   }

   @Test
   public void testIsTransactional() {
      Assert.assertTrue(converter.isTransactional());
   }

}
