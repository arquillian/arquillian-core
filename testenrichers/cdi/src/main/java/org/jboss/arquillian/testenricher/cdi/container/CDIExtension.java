package org.jboss.arquillian.testenricher.cdi.container;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

public class CDIExtension implements Extension
{
   private static BeanManager beanManager;

   void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager)
   {
      setBeanManager(beanManager);
   }

   void beforeShutdown(@Observes BeforeShutdown beforeShutdown)
   {
      setBeanManager(null);
   }

   public static BeanManager getBeanManager()
   {
      return beanManager;
   }

   private static void setBeanManager(BeanManager beanManager)
   {
      CDIExtension.beanManager = beanManager;
   }
}
