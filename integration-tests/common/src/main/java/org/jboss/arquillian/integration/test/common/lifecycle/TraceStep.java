/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.integration.test.common.lifecycle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a single expected step in a lifecycle trace sequence.
 * Used within a framework-specific composed annotation to declare the expected
 * order of lifecycle method invocations during a test run.
 *
 * @see RunsWhere
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface TraceStep {
    /**
     * The name of the lifecycle step.
     */
    String name();

    /**
     * Where this step is expected to execute.
     */
    RunsWhere runsWhere();

    /**
     * Ordering index for this step. Consecutive steps sharing the same
     * {@code order} value form a permutable group whose elements may
     * appear in any order. Steps with unique order values are fixed in
     * position. Values must be non-decreasing, start at 0, and have no
     * gaps.
     */
    int order();
}
