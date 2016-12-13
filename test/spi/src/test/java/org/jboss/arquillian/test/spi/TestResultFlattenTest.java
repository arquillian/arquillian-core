/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
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

package org.jboss.arquillian.test.spi;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.test.spi.TestResult.failed;

public class TestResultFlattenTest
{

   public static final String LINE_SEPARATOR = System.getProperty("line.separator");

   @Test
   public void should_flatten_successful_test_results() throws Exception
   {
      // given
      final TestResult p1 = TestResult.passed();
      final TestResult p2 = TestResult.passed();
      final List<TestResult> testResults = asList(p1, p2);

      // when
      TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getStatus()).isEqualTo(TestResult.Status.PASSED);
   }

   @Test
   public void should_combine_descriptions() throws Exception
   {
      // given
      final TestResult p1 = TestResult.passed("First test passed");
      final TestResult p2 = TestResult.passed("Second test passed");
      final List<TestResult> testResults = asList(p1, p2);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getDescription()).isEqualTo(String.format("PASSED: 'First test passed'%nPASSED: 'Second test passed'%n"));
   }

   @Test
   public void should_treat_skipped_and_passed_tests_as_passed_overall() throws Exception
   {
      // given
      final TestResult passed = TestResult.passed("Test passed");
      final TestResult skipped = TestResult.skipped();
      final List<TestResult> testResults = asList(skipped, passed);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getStatus()).isEqualTo(TestResult.Status.PASSED);
      assertThat(result.getDescription()).isEqualTo(String.format("SKIPPED: ''%nPASSED: 'Test passed'%n"));
   }

   @Test
   public void should_combine_skipped_and_passed_tests_descriptions() throws Exception
   {
      // given
      final TestResult passed = TestResult.passed("First test passed");
      final TestResult skipped = TestResult.skipped("Not implemented yet");
      final List<TestResult> testResults = asList(skipped, passed);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getDescription()).isEqualTo(String.format("SKIPPED: 'Not implemented yet'%nPASSED: 'First test passed'%n"));
   }

   @Test
   public void should_mark_combined_test_result_as_skipped_if_only_skipped_test_results_are_present()throws Exception
   {
      // given
      final TestResult s1 = TestResult.skipped();
      final TestResult s2 = TestResult.skipped();
      final List<TestResult> testResults = asList(s1, s2);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getStatus()).isEqualTo(TestResult.Status.SKIPPED);
   }

   @Test
   public void should_mark_test_result_as_failed_if_there_is_at_least_one_failure() throws Exception
   {
      // given
      final TestResult p1 = TestResult.passed("First test passed");
      final TestResult s1 = TestResult.skipped("Not implemented yet");
      final TestResult f1 = failed(new RuntimeException("Exception"));
      final List<TestResult> testResults = asList(p1, s1, f1);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getStatus()).isEqualTo(TestResult.Status.FAILED);
   }

   @Test
   public void should_mark_test_result_as_failed_if_there_are_only_failures() throws Exception
   {
      //given
      final TestResult e1 = failed(new RuntimeException("Exception 1"));
      final TestResult e2 = failed(new RuntimeException("Exception 2"));
      final List<TestResult> testResults = asList(e1, e2);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getStatus()).isEqualTo(TestResult.Status.FAILED);
   }

   // maybe 2 tests? one with failure one with skipped? open question
   @Test
   public void should_propagate_exception_when_only_one_cause_reported() throws Exception
   {
      // given
      final RuntimeException cause = new RuntimeException("Exception 1");
      final TestResult f1 = failed(cause);
      final List<TestResult> testResults = asList(f1);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getThrowable()).isEqualTo(cause);
   }

   @Test
   public void should_combine_exceptions_for_all_failures() throws Exception
   {
      // given
      final RuntimeException ex1 = new RuntimeException("Exception 1");
      final TestResult e1 = failed(ex1);
      final RuntimeException ex2 = new RuntimeException("Exception 2");
      final TestResult e2 = failed(ex2);
      final List<TestResult> testResults = asList(e1, e2);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getThrowable()).isInstanceOf(CombinedException.class);
      assertThat(((CombinedException) result.getThrowable()).getCauses()).containsExactly(ex1, ex2);
   }

   @Test
   public void should_create_exception_messages_in_order() throws Exception
   {
      // given
      final RuntimeException ex1 = new RuntimeException("Exception 1");
      final TestResult e1 = failed(ex1);
      final RuntimeException ex2 = new RuntimeException("Exception 2");
      final TestResult e2 = failed(ex2);
      final List<TestResult> testResults = asList(e2, e1);

      // when
      final TestResult result = TestResult.flatten(testResults);

      // then
      assertThat(result.getThrowable().getMessage()).isEqualTo("Exception 1: '[java.lang.RuntimeException] Exception 2'"
            + LINE_SEPARATOR
            + "Exception 2: '[java.lang.RuntimeException] Exception 1'"
            + LINE_SEPARATOR);
   }

}