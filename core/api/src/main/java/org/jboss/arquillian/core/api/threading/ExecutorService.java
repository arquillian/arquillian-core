/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.core.api.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * Simple ExecutorService for executing background tasks within callers
 * original context.
 */
public interface ExecutorService {

    /**
     * Submits a value-returning task for execution and returns a
     * Future representing the pending results of the task. The
     * Future's <tt>get</tt> method will return the task's result upon
     * successful completion.
     * <p>
     * The contextual information of the caller is preserved onto the
     * new thread used to execute the Callable.
     *
     * @param task
     *     the task to submit
     *
     * @return a Future representing pending completion of the task
     *
     * @throws RejectedExecutionException
     *     if the task cannot be
     *     scheduled for execution
     * @throws NullPointerException
     *     if the task is null
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * Create a snapshot of the current active Contexts.
     * <p>
     * The snapshot can later be used to activate and deactivate
     * the previously activate Contexts.
     * <p>
     * Used to manually recreate the current env on a new Thread.
     *
     * @return A representation of the current activate Contexts
     */
    ContextSnapshot createSnapshotContext();
}
