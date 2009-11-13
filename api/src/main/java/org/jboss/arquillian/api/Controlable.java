package org.jboss.arquillian.api;

// TODO: throws ControllerException
public interface Controlable
{
   void start() throws Exception;
   
   void stop() throws Exception;
   
}
