package org.jboss.arquillian.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.naming.InitialContext;

import org.jboss.arquillian.api.Artifact;
import org.jboss.arquillian.api.ArtifactGenerator;
import org.jboss.arquillian.api.Controlable;
import org.jboss.arquillian.api.Deployer;
import org.jboss.arquillian.api.Packaging;
import org.jboss.arquillian.api.TestMethodExecutor;
import org.jboss.shrinkwrap.api.Archive;

public class DeployableTest
{
   private static boolean inContainer = false;
   
   public static boolean isInContainer()
   {
      return inContainer;
   }

   public static void setInContainer(boolean inContainer)
   {
      DeployableTest.inContainer = inContainer;
   }

   private Controlable containerController;
   private Deployer containerDeployer;
   
   public DeployableTest(Controlable containerController, Deployer containerDeployer)
   {
      this.containerController = containerController;
      this.containerDeployer = containerDeployer;
   }
   
   public Controlable getContainerController() 
   {
      return containerController;
   }

   public Deployer getDeployer() 
   {
      return containerDeployer;
   }

   
   // TODO: throws MissingArtifactSupportException
   // TODO: SPI lookup based on artifactType
   // TODO: loadArtifactGenerator() ?
   public ArtifactGenerator getArtifactGenerator(Artifact artifact) 
   {
      if(DeployableTest.isInContainer()) 
      {
         return new NullArtifactGenerator();
      }
      return new UserCreatedArtifactGenerator();
   }

   public ArtifactGenerator getArtifactGenerator(Packaging packaging) 
   {
      if(DeployableTest.isInContainer()) 
      {
         return new NullArtifactGenerator();
      }
      return new UserCreatedArtifactGenerator();
   }
   
   public Archive<?> generateArtifact(Class<?> testCase) 
   {
//      ArtifactGenerator artifactGenerator = getArtifactGenerator(
//            this.getClass().getAnnotation(Artifact.class));
//      
//      ArtifactGenerator packagingGenerator = getArtifactGenerator(
//            this.getClass().getAnnotation(Packaging.class));
//      
//      TCKArtifact artifact = packagingGenerator.generateArtifact(this.getClass());
//      artifactGenerator.generateArtifact(this.getClass(), artifact);
//      
//      return artifact;
      return getArtifactGenerator((Artifact)null).generateArtifact(testCase);
   }

   public void run(TestMethodExecutor executor) throws Throwable 
   {
      if(inContainer) 
      {
         injectClass(executor.getInstance());
         executor.invoke();
      } 
      else 
      {
         new ServletMethodExecutor(executor).invoke();
      }
   }
   
   void injectClass(Object testCase) 
   {
      try 
      {
         Class<? extends Annotation> ejbAnnotationClass = (Class<? extends Annotation>)Thread.currentThread()
                     .getContextClassLoader().loadClass("javax.ejb.EJB");
         
         for(Field field : testCase.getClass().getDeclaredFields()) 
         {
            if(field.isAnnotationPresent(ejbAnnotationClass)) 
            {
               Object ejb = lookupEJB(field);
               field.setAccessible(true);
               field.set(testCase, ejb);
            }
         }
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not inject members", e);
      }
   }
   
   private Object lookupEJB(Field field) throws Exception 
   {
      InitialContext context = new InitialContext();
      return context.lookup("test/" + field.getType().getSimpleName() + "Bean/local");
   }
   
   void invokeMethod(Method testMethod, Class<?> testCase) 
   {
   }

}
