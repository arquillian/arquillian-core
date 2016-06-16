/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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

import org.jboss.arquillian.junit.JUnitTestBaseClass.Cycle;
import org.junit.*;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.jboss.arquillian.junit.JUnitTestBaseClass.wasCalled;

/*
 * Predfined TestClass
 */
public class ArquillianClass2WithExceptionInBeforeRule
{
   @ClassRule
   public static ArquillianClassRule arquillianClassRuleRule = new ArquillianClassRule();

   @Rule
   public ArquillianRule arquillianRule = new ArquillianRule();

   @Rule
   public MethodRule rule = new MethodRule() {
      @Override
      public Statement apply(final Statement base, FrameworkMethod method, Object target) {
         return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                throw new RuntimeException("BeforeRuleException");
            }
        };
      }
   };

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      wasCalled(Cycle.BEFORE_CLASS);
   }

   @AfterClass
   public static void afterClass() throws Throwable
   {
      wasCalled(Cycle.AFTER_CLASS);
   }

   @Before
   public void before() throws Throwable
   {
      wasCalled(Cycle.BEFORE);
   }

   @After
   public void after() throws Throwable
   {
      wasCalled(Cycle.AFTER);
   }

   @Test
   public void shouldBeInvoked() throws Throwable
   {
      wasCalled(Cycle.TEST);
   }
}
