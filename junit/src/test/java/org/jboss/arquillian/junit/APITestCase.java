package org.jboss.arquillian.junit;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class APITestCase
{
   @Deployment
   public static WebArchive createDeployment() 
   {
      return Archives.create("test.war", WebArchive.class)
            .addClass(APITestCase.class);
   }
   
   @Test
   public void myTestCase() throws Exception {
      System.out.println("test run");
   }
   
   @Test
   public void myFailingTestCase() throws Exception {
      Assert.assertTrue(false);      
   }

   @Test(expected = IllegalArgumentException.class)
   public void myExpectedFailingTestCase() throws Exception {
      throw new IllegalArgumentException("This is ok");
   }
}
