package org.jboss.arquillian.testenricher.cdi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.testenricher.cdi.beans.Cat;
import org.jboss.arquillian.testenricher.cdi.beans.CatService;
import org.jboss.arquillian.testenricher.cdi.beans.Dog;
import org.jboss.arquillian.testenricher.cdi.beans.DogService;
import org.jboss.arquillian.testenricher.cdi.beans.Service;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.manager.api.WeldManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CDIInjectionEnricherTestCase
{
   private WeldBootstrap bootstrap;
   private WeldManager manager;
   private CDIInjectionEnricher enricher;
   
   @Before
   public void setup() throws Exception 
   {
      Deployment deployment = createDeployment(Service.class, Cat.class, CatService.class, Dog.class, DogService.class);
      bootstrap = new WeldBootstrap();
      bootstrap.startContainer(Environments.SE, deployment, new ConcurrentHashMapBeanStore())
                  .startInitialization()
                  .deployBeans()
                  .validateBeans()
                  .endInitialization();

      manager = bootstrap.getManager(deployment.getBeanDeploymentArchives().iterator().next());

      enricher = new CDIInjectionEnricher() 
      {
         @Override
         protected BeanManager lookupBeanManager(Context context)
         {
            return manager;
         }
      };
   }
   
   @After
   public void teardown() throws Exception
   {
      bootstrap.shutdown();
   }
   
   @Test
   public void shouldInjectClassMembers() throws Exception
   {
      TestClass testClass = new TestClass();
      enricher.injectClass(null, testClass);
      testClass.testMethod(testClass.dogService, testClass.catService);
   }
   
   @Test
   public void shouldInjectMethodArguments() throws Exception
   {
      Method testMethod = TestClass.class.getMethod("testMethod", Service.class, Service.class);
      
      Object[] resolvedBeans = enricher.resolve(null, testMethod);

      TestClass testClass = new TestClass();
      testMethod.invoke(testClass, resolvedBeans);
   }
   
   @Test
   public void shouldInjectMethodArgumentsEvent() throws Exception
   {
      Method testMethod = TestClass.class.getMethod("testEvent", Event.class, Event.class);
      
      Object[] resolvedBeans = enricher.resolve(null, testMethod);

      TestClass testClass = new TestClass();
      testMethod.invoke(testClass, resolvedBeans);
   }

   @Test
   public void shouldInjectMethodArgumentsInstance() throws Exception
   {
      Method testMethod = TestClass.class.getMethod("testInstance", Instance.class, Instance.class);
      
      Object[] resolvedBeans = enricher.resolve(null, testMethod);

      TestClass testClass = new TestClass();
      testMethod.invoke(testClass, resolvedBeans);
   }

   private static class TestClass 
   {
      @Inject
      Service<Dog> dogService;

      @Inject
      Service<Cat> catService;
      
      public void testMethod(Service<Dog> dogService, Service<Cat> catService)
      {
         Assert.assertNotNull(catService);
         Assert.assertNotNull(dogService);
         
         Assert.assertEquals(CatService.class, catService.getClass());
         Assert.assertEquals(DogService.class, dogService.getClass());
      }
      
      @SuppressWarnings("unused") // used only via reflection
      public void testEvent(Event<Dog> dogEvent, Event<Cat> catEvent)
      {
         Assert.assertNotNull(dogEvent);
         Assert.assertNotNull(catEvent);
      }

      @SuppressWarnings("unused") // used only via reflection
      public void testInstance(Instance<Dog> dogEvent, Instance<Cat> catEvent)
      {
         Assert.assertNotNull(dogEvent);
         Assert.assertNotNull(catEvent);
      }
   }
   
   private Deployment createDeployment(final Class<?>... classes)
   {
      final BeanDeploymentArchive beanArchive = new BeanDeploymentArchive()
      {
         private ServiceRegistry registry = new SimpleServiceRegistry();
         
         public ServiceRegistry getServices()
         {
            return registry; 
         }
         
         public String getId()
         {
            return "test.jar";
         }
         
         public Collection<EjbDescriptor<?>> getEjbs()
         {
            return Collections.emptyList();
         }
         
         public Collection<URL> getBeansXml()
         {
            try
            {
               return Arrays.asList(new URL(null, "memory://beans.xml", new URLStreamHandler()
               {
                  @Override
                  protected URLConnection openConnection(URL u) throws IOException
                  {
                     return new URLConnection(u)
                     {
                        public void connect() throws IOException {}
                        
                        public InputStream getInputStream() throws IOException
                        {
                           return new ByteArrayInputStream("<beans/>".getBytes());
                        }
                     };
                  }
               }));
            } 
            catch (Exception e) 
            {
               throw new RuntimeException(e);
            }
         }
         
         public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
         {
            return Collections.emptyList();
         }
         
         public Collection<Class<?>> getBeanClasses()
         {
            return Arrays.asList(classes);
         }
      };
      final Deployment deployment = new Deployment() 
      {
         public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
         {
            return Arrays.asList((BeanDeploymentArchive)beanArchive);
         }
         
         public ServiceRegistry getServices()
         {
            return beanArchive.getServices();
         }
         
         public BeanDeploymentArchive loadBeanDeploymentArchive(    
               Class<?> beanClass)
         {
            return beanArchive;
         }
      };
      return deployment;
   }
}
