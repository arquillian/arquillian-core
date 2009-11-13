package com.acme.ejb;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GreetingManagerTest
{
   @Deployment
   public static JavaArchive createDeployment() {
      return Archives.create("test.jar", JavaArchive.class)
               .addClasses(
                     GreetingManager.class,
                     GreetingManagerBean.class);
   }
   
   @EJB
   private GreetingManager greetingManager;
   
   @Test
   public void shouldGreetUser() throws Exception {
      
      String userName = "Devoxx";
      
      Assert.assertEquals(
            "Hello " + userName,
            greetingManager.greet(userName));
   }
}
