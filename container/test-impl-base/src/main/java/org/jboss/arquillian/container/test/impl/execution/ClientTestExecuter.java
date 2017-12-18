/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.test.impl.execution;

import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.test.impl.RunModeUtils;
import org.jboss.arquillian.container.test.impl.execution.event.ExecutionEvent;
import org.jboss.arquillian.container.test.impl.execution.event.LocalExecutionEvent;
import org.jboss.arquillian.container.test.impl.execution.event.RemoteExecutionEvent;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.Test;

/**
 * TestExecuter for running on the client side. Can switch between Local and Remote test execution.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClientTestExecuter {
    @Inject
    private Event<ExecutionEvent> executionEvent;

    @Inject
    private Instance<Deployment> deployment;

    public void execute(@Observes Test event) throws Exception {
        boolean runAsClient = RunModeUtils.isRunAsClientAndCheck(
            this.deployment.get(),
            event.getTestClass(),
            event.getTestMethod());

        if (runAsClient) {
            executionEvent.fire(new LocalExecutionEvent(event.getTestMethodExecutor()));
        } else {
            executionEvent.fire(new RemoteExecutionEvent(event.getTestMethodExecutor()));
        }
    }
}