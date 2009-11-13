package org.jboss.arquillian.impl.runner.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.arquillian.impl.DeployableTest;

public class InContainerListener implements ServletContextListener
{
   @Override
   public void contextDestroyed(ServletContextEvent event)
   {
   }
   
   @Override
   public void contextInitialized(ServletContextEvent event)
   {
      DeployableTest.setInContainer(true);
   }
}