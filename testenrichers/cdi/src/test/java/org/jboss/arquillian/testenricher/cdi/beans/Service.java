package org.jboss.arquillian.testenricher.cdi.beans;

public interface Service<T>
{
   boolean wasReleased();
   
   void release();
}
