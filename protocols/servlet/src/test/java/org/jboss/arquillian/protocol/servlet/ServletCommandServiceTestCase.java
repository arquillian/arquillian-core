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
package org.jboss.arquillian.protocol.servlet;

import org.jboss.arquillian.protocol.servlet.test.MockTestRunner;
import org.jboss.arquillian.protocol.servlet.test.TestCommand;
import org.jboss.arquillian.protocol.servlet.test.TestCommandCallback;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.junit.Assert;
import org.junit.Test;

/**
 * ServletCommandServiceTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletCommandServiceTestCase extends AbstractServerBase
{
   @Test
   public void shouldBeAbleToTransfereCommand() throws Exception
   {
      TestCommandCallback.result = "Weee";
      MockTestRunner.add(new TestResult(Status.PASSED, null));
      MockTestRunner.command = new TestCommand();
      
      ServletMethodExecutor executor = new ServletMethodExecutor(createBaseURL(), new TestCommandCallback());
      TestResult result = executor.invoke(new MockTestExecutor());

      Assert.assertEquals(
            "Should have returned a passed test",
            MockTestRunner.wantedResults.getStatus(),
            result.getStatus());
      
      Assert.assertNull(
            "Exception should have been thrown",
            result.getThrowable());

      Assert.assertEquals(
            "Should have returned command",
            TestCommandCallback.result,
            MockTestRunner.commandResult);
   }

}
