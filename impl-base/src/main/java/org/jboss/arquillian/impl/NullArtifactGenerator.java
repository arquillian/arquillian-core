package org.jboss.arquillian.impl;

import org.jboss.arquillian.api.ArchiveGenerator;
import org.jboss.shrinkwrap.api.Archive;

public class NullArtifactGenerator implements ArchiveGenerator
{

   @Override
   public Archive<?> generateArchive(Class<?> testCase)
   {
      return null;
   }
}
