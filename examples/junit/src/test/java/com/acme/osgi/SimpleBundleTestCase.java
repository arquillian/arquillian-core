package com.acme.osgi;

import java.io.InputStream;

import org.jboss.arquillian.api.Deployment;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(org.jboss.arquillian.junit.Arquillian.class)
public class SimpleBundleTestCase
{
   @Deployment
   public static JavaArchive createTestArchive()
   {
      final JavaArchive archive = ShrinkWrap.create("example-bundle.jar", JavaArchive.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleManifestVersion(2);
            return builder.openStream();
         }
      });
      return archive;
   }

   @Test
   public void testBundleAccess()
   {
   }
}
