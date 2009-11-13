package org.jboss.arquillian.junit;

import java.lang.reflect.Method;

import java.util.List;

import org.jboss.arquillian.api.TestMethodExecutor;
import org.jboss.arquillian.impl.DeployableTest;
import org.jboss.arquillian.impl.DeployableTestBuilder;
import org.jboss.arquillian.impl.runner.servlet.InContainerListener;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.descriptor.WebArchiveDescriptor;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

// TODO: where to put start/stop container..
public class Arquillian extends BlockJUnit4ClassRunner
{
   private static DeployableTest deployableTest;
   
   private Archive<?> archive = null;
   
   public Arquillian(Class<?> klass) throws InitializationError
   {
      super(klass);
      deployableTest = DeployableTestBuilder.build(null);
   }


   @Override
   // TODO: exclude @Integration test classes
   protected List<FrameworkMethod> computeTestMethods()
   {
      return super.computeTestMethods();
   }

   @Override
   protected Statement withBeforeClasses(Statement statement)
   {
      final Statement originalStatement = super.withBeforeClasses(statement);
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            archive = deployableTest.generateArtifact(
                  Arquillian.this.getTestClass().getJavaClass());
            
            if(archive instanceof WebArchive) {
               WebArchive webArchive = (WebArchive)archive;
               webArchive.addPackages(
                     true,
                     Package.getPackage("org.junit"),
                     Package.getPackage("org.jboss.arquillian.api"), 
                     Package.getPackage("org.jboss.arquillian.impl"),
                     Package.getPackage("org.jboss.arquillian.junit"));
               
               webArchive.as(WebArchiveDescriptor.class)
                  .addListener(InContainerListener.class)
                  .addServlet(ServletTestRunner.class, "/*");
            }
            if(archive instanceof JavaArchive) {
               EnterpriseArchive ear = Archives.create("test.ear", EnterpriseArchive.class);

               WebArchive war = Archives.create("test.war", WebArchive.class)
                     .addPackages(
                        true,
                        Package.getPackage("org.junit"),
                        Package.getPackage("org.jboss.arquillian.api"), 
                        Package.getPackage("org.jboss.arquillian.impl"),
                        Package.getPackage("org.jboss.arquillian.junit"))
                     .addClass(Arquillian.this.getTestClass().getJavaClass());
               
               war.as(WebArchiveDescriptor.class)
                  .addListener(InContainerListener.class)
                  .addServlet(ServletTestRunner.class, "/*");
                  
               ear.addModule(war)
                  .addModule(archive);
             
               archive = ear;
            }
            
            deployableTest.getContainerController().start();
            deployableTest.getDeployer().deploy(archive);
            originalStatement.evaluate();
         }
      };
   }
   
   @Override
   protected Statement withAfterClasses(Statement statement)
   {
      final Statement originalStatement = super.withAfterClasses(statement);
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            originalStatement.evaluate();
            deployableTest.getDeployer().undeploy(archive);
            deployableTest.getContainerController().stop();
         }
      };
   }
   
   @Override
   protected Statement methodInvoker(final FrameworkMethod method, final Object test)
   {
      return new Statement()
      {
         @Override
         public void evaluate() throws Throwable
         {
            deployableTest.run(new TestMethodExecutor()
            {
               public void invoke() throws Throwable
               {
                  method.invokeExplosively(test);
               }
               
               public Method getMethod()
               {
                  return method.getMethod();
               }
               
               public Object getInstance()
               {
                  return test;
               }
            });
         }
      };
   }
}
