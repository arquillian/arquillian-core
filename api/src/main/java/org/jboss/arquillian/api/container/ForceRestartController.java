package org.jboss.arquillian.api.container;

import org.jboss.arquillian.api.Controlable;
import org.jboss.tmpdpl.api.shrinkwrap.container.ArchiveContainer;

public class ForceRestartController implements Controlable
{
   private ArchiveContainer container;
   
   public ForceRestartController(ArchiveContainer containers)
   {
      this.container = containers;
   }

   public void start() throws Exception
   {
      try {
         //container.cleanup();
      } catch (Exception e) {
         // no-op
      }
      //container.setup();
   }

   public void stop() throws Exception
   {
      //container.cleanup();      
   }
}
