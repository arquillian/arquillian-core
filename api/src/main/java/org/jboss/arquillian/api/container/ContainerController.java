package org.jboss.arquillian.api.container;

import org.jboss.arquillian.api.Controlable;
import org.jboss.tmpdpl.api.shrinkwrap.container.ArchiveContainer;

public class ContainerController implements Controlable
{
   private ArchiveContainer container;

   public ContainerController(ArchiveContainer containers)
   {
      this.container = containers;
   }
   
   public void start() throws Exception
   {
      //container.setup();
   }
   
   public void stop() throws Exception
   {
      //container.cleanup();
   }
}
