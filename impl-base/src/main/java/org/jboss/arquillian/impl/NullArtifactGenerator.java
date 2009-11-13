package org.jboss.arquillian.impl;

import org.jboss.arquillian.api.ArtifactGenerator;
import org.jboss.shrinkwrap.api.Archive;

public class NullArtifactGenerator implements ArtifactGenerator
{

   @Override
   public Archive<?> generateArtifact(Class<?> testCase)
   {
      return null;
   }
   
   @Override
   public void generateArtifact(Class<?> testCase, Archive<?> baseArtifact)
   {
      
   }
}
