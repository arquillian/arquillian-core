package org.jboss.arquillian.api.container;

import org.jboss.arquillian.api.Controlable;
import org.jboss.tmpdpl.api.shrinkwrap.container.ArchiveContainer;

public class NoContainerController implements Controlable
{
   public NoContainerController(ArchiveContainer container)
   {
   }
   
   public void start() throws Exception
   {
   }
   
   public void stop() throws Exception
   {
   }
}
