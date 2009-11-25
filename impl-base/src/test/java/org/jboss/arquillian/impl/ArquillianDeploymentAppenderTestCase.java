package org.jboss.arquillian.impl;

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;


public class ArquillianDeploymentAppenderTestCase
{

   @Test
   public void shouldGenerateDependencies() throws Exception {
      
      Archive<?> archive = new ArquillianDeploymentAppender().createArchive();
      
      System.out.println(archive.toString(true));
   }
}
