package org.jboss.arquillian.junit;

import junit.framework.Assert;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Paths;
import org.junit.Test;


public class JUnitDeploymentAppenderTestCase
{

   @Test
   public void shouldGenerateDependencies() throws Exception {
      
      Archive<?> archive = new JUnitDeploymentAppender().createArchive();
      
      Assert.assertTrue(
            "Should have added TestRunner SPI",
            archive.contains(Paths.create("/META-INF/services/org.jboss.arquillian.spi.TestRunner")));
      
      Assert.assertTrue(
            "Should have added TestRunner Impl",
            archive.contains(Paths.create("/org/jboss/arquillian/junit/JUnitTestRunner.class")));

      System.out.println(archive.toString(true));      
   }
}
