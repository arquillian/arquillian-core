package org.jboss.arquillian.api;

import org.jboss.shrinkwrap.api.Archive;

public interface ArchiveGenerator 
{
   // Packaging SPI
   Archive<?> generateArchive(Class<?> testCase);

}
