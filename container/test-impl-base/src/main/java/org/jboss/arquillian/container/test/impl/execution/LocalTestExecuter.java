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
package org.jboss.arquillian.container.test.impl.execution;

import org.jboss.arquillian.container.test.impl.execution.event.LocalExecutionEvent;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.execution.ExecUtils;

/**
 * A Handler for executing the Test Method.<br/>
 * <br/>
 * <b>Imports:</b><br/>
 * {@link Injector}<br/>
 * {@link ServiceLoader}<br/>
 * <br/>
 * <b>Exports:</b><br/>
 * {@link TestResult}<br/>
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LocalTestExecuter {
    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    @TestScoped
    private InstanceProducer<TestResult> testResult;

    public void execute(@Observes LocalExecutionEvent event) throws Exception {
        TestResult result = TestResult.passed();
        try {
            Object[] args = ExecUtils.enrichArguments(
                event.getExecutor().getMethod(),
                serviceLoader.get().all(TestEnricher.class));
            event.getExecutor().invoke(args);
        } catch (Throwable e) {
            result = TestResult.failed(e);
        } finally {
            result.setEnd(System.currentTimeMillis());
        }
        testResult.set(result);
    }

}
