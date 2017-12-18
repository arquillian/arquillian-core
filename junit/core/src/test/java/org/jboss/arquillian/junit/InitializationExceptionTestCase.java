/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.junit;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * ARQ-404 Better reporting when Arquillian fails to initialise
 * <p>
 * Only run first test, ignore the rest
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class InitializationExceptionTestCase extends JUnitTestBaseClass
{
   @Test
   public void shouldKeepInitializationExceptionBetweenTestCases() throws Exception
   {
      String exceptionMessage = "TEST_EXCEPTION_BEFORE_SUITE_FAILING";
      TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
      doThrow(new Exception(exceptionMessage)).when(adaptor).beforeSuite();

      Result result = run(adaptor, ClassWithArquillianRunner.class, ClassWithArquillianRunner.class);
      
      Assert.assertFalse(result.wasSuccessful());
      // both should be marked failed, the second with the real exception as cause 
      Assert.assertEquals(2, result.getFailureCount()); 
      Assert.assertEquals(exceptionMessage, result.getFailures().get(0).getMessage());
      Assert.assertEquals(exceptionMessage, result.getFailures().get(1).getException().getCause().getMessage());
      
      verify(adaptor, times(0)).afterSuite();
   }
}
