package org.jboss.weld.deployments;

import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.ByteArrayAsset;

public class DeploymentTest
{

   @Inject
   BeanManager beanManager;

   protected static JavaArchive createCDIArchive(Class<?>... classes)
   {
      return createCDIArchive("test.jar", classes);
   }

   protected static JavaArchive createCDIArchive(String name, Class<?>... classes)
   {
      return ShrinkWrap.create(name, JavaArchive.class).addClasses(classes).addClass(DeploymentTest.class).addManifestResource(new ByteArrayAsset(new byte[0]), "beans.xml");
   }

   protected static WebArchive createWebArchive(Archive<?>... archives)
   {
      return ShrinkWrap.create("test.war", WebArchive.class).addLibraries(archives).addWebResource(new ByteArrayAsset(new byte[0]), "beans.xml");
   }

   protected static EnterpriseArchive createEnterpriseArchive(Archive<?>... archives)
   {
      return ShrinkWrap.create("test.ear", EnterpriseArchive.class).addLibraries(archives);
   }
  
   @SuppressWarnings("unchecked")
   public <T> T getNamedBean(String name, Class<T> clazz)
   {
      Set<Bean<?>> beans = beanManager.getBeans(name);
      Assert.assertEquals(1, beans.size());
      return (T) beanManager.getReference(
            beans.iterator().next(), 
            clazz, 
            beanManager.createCreationalContext(null));
   }
}
