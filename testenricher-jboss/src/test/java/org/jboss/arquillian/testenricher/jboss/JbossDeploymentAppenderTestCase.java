package org.jboss.arquillian.testenricher.jboss;

import junit.framework.Assert;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Paths;
import org.junit.Test;


public class JbossDeploymentAppenderTestCase
{
   
   @Test
   public void shouldGenerateDependencies() throws Exception {

      Archive<?> archive = new JbossDeploymentAppender().createArchive();
      System.out.println(archive.toString(true));
      
      Assert.assertTrue(
            "Should have added TestEnricher SPI", 
            archive.contains(Paths.create("/META-INF/services/org.jboss.arquillian.spi.TestEnricher")));

      Assert.assertTrue(
            "Should have added TestEnricher EJB impl", 
            archive.contains(Paths.create("/org/jboss/arquillian/testenricher/jboss/EJBInjectionEnricher.class")));

      Assert.assertTrue(
            "Should have added TestEnricher Resource impl", 
            archive.contains(Paths.create("/org/jboss/arquillian/testenricher/jboss/ResourceInjectionEnricher.class")));

      Assert.assertTrue(
            "Should have added TestEnricher CDI impl", 
            archive.contains(Paths.create("/org/jboss/arquillian/testenricher/jboss/CDIInjectionEnricher.class")));

      Assert.assertTrue(
            "Should have added TestEnricher Impl dep", 
            archive.contains(Paths.create("/org/jboss/arquillian/testenricher/jboss/SecurityActions.class")));
   }
}
