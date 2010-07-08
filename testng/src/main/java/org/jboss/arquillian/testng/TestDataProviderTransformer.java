/**
 * 
 */
package org.jboss.arquillian.testng;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.TestAnnotation;

/**
 * A IAnnotationTransformer that will add the {@link TestEnricherDataProvider} as {@link DataProvider} 
 * to the given test method to enable method argument injection support. 
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestDataProviderTransformer implements IAnnotationTransformer
{

   /* (non-Javadoc)
    * @see org.testng.IAnnotationTransformer#transform(org.testng.annotations.ITestAnnotation, java.lang.Class, java.lang.reflect.Constructor, java.lang.reflect.Method)
    */
   @SuppressWarnings("rawtypes")
   public void transform(ITestAnnotation testAnnotation, Class clazz, Constructor constructor, Method method)
   {
      if (testAnnotation.getDataProviderClass() == null)
      {
         if (testAnnotation instanceof TestAnnotation)
         {
            TestAnnotation annoation = (TestAnnotation) testAnnotation;
            annoation.setDataProviderClass(TestEnricherDataProvider.class);
            annoation.setDataProvider(TestEnricherDataProvider.PROVIDER_NAME);
         }
      }
   }
}
