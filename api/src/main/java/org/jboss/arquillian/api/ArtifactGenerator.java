package org.jboss.arquillian.api;

import org.jboss.shrinkwrap.api.Archive;

public interface ArtifactGenerator 
{
   // Packaging SPI
   Archive<?> generateArtifact(Class<?> testCase);
   void generateArtifact(Class<?> testCase, Archive<?> baseArtifact);

}
