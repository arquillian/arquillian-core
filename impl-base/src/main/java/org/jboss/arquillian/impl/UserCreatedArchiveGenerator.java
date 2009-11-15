package org.jboss.arquillian.impl;

import java.lang.reflect.Method;

import org.jboss.arquillian.api.ArchiveGenerator;
import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;

public class UserCreatedArchiveGenerator implements ArchiveGenerator
{

   @Override
   public Archive<?> generateArchive(Class<?> testCase)
   {
      Method deploymentMethod = findDeploymentMethod(testCase);
      if(deploymentMethod == null) 
      {
         throw new RuntimeException("No static method annotated with " + Deployment.class.getName() + " found");
      }
      try 
      {
         return (Archive<?>)deploymentMethod.invoke(null);
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not get Deploymnet", e);
      }
   }
   
   private Method findDeploymentMethod(Class<?> testCase) {
      
      Method[] methods = testCase.getMethods();
      for(Method method: methods)
      {
         if(method.isAnnotationPresent(Deployment.class)) {
            return method;
         }
      }
      return null;
   }
}
