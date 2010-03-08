package com.acme.ejb;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(org.jboss.arquillian.junit.Arquillian.class)
public class TemperatureConverterTestCase 
{

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
