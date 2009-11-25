package org.jboss.arquillian.protocol.servlet;

import junit.framework.Assert;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Paths;
import org.junit.Test;


public class ProtocolDeploymentAppenderTestCase
{

   @Test
   public void shouldGenerateDependencies() throws Exception {
      
      Archive<?> archive = new ProtocolDeploymentAppender().createArchive();
      
      Assert.assertTrue(
            "Should have added web.xml",
            archive.contains(Paths.create("WEB-INF/web.xml"))
      );
      
      System.out.println(archive.toString(true));
   }
}
