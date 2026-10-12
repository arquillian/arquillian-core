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
package org.jboss.arquillian.junit5.container.fixtures;

import java.time.Duration;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A meeting point that proves test classes really do overlap in time.
 *
 * <p>Each participating test class calls {@link #arrive()}, which blocks until every participant has arrived. If the
 * classes are executed one after another instead of concurrently, the first arrival is never joined, the wait runs
 * out and the test fails with a {@link TimeoutException}. A parallel smoke test built this way cannot pass on a
 * sequential run - unlike one that sleeps and hopes.</p>
 */
public final class ParallelRendezvous {

    /**
     * Generous enough that a loaded CI machine will not trip it, short enough that a genuinely sequential run fails
     * rather than hangs.
     */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private static volatile CyclicBarrier barrier;

    private static volatile Duration timeout = DEFAULT_TIMEOUT;

    private ParallelRendezvous() {
    }

    /**
     * Arms the rendezvous for a run in which {@code parties} test classes are expected to overlap.
     */
    public static void expect(int parties) {
        expect(parties, DEFAULT_TIMEOUT);
    }

    /**
     * Arms the rendezvous with an explicit wait.
     *
     * <p>A short wait suits a run that is <em>expected</em> to be sequential: no partner will ever arrive, so the
     * wait is certain to run out and there is no race to lose.</p>
     */
    public static void expect(int parties, Duration waitFor) {
        barrier = new CyclicBarrier(parties);
        timeout = waitFor;
    }

    /**
     * Blocks until every expected participant has arrived.
     *
     * @throws IllegalStateException if the rendezvous was not armed
     * @throws TimeoutException      if the other participants never showed up, i.e. the classes did not run
     *                               concurrently
     */
    public static void arrive() throws InterruptedException, BrokenBarrierException, TimeoutException {
        CyclicBarrier current = barrier;
        if (current == null) {
            throw new IllegalStateException("Rendezvous was not armed; call expect(int) before running the fixtures");
        }
        current.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
