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
package org.jboss.arquillian.container.test.impl.client.protocol.local;

import org.jboss.arquillian.container.test.impl.execution.event.LocalExecutionEvent;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;

/**
 * LocalContainerMethodExecutor
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LocalContainerMethodExecutor implements ContainerMethodExecutor {
    @Inject
    private Event<LocalExecutionEvent> event;

    @Inject
    private Instance<TestResult> testResult;

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.ContainerMethodExecutor#invoke(org.jboss.arquillian.spi.TestMethodExecutor)
     */
    public TestResult invoke(TestMethodExecutor testMethodExecutor) {
      /*
       *  TODO: when we fire a LocalExecutionEvent from a ContainerMethodExecutor, 
       *  both the LocalTestExecutor and RemoteTestExecutor will set the same TestResult. 
       */
        event.fire(new LocalExecutionEvent(testMethodExecutor));
        return testResult.get();
    }
}
