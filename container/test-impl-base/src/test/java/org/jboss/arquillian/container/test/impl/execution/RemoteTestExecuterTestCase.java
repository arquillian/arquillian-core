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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.container.test.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.container.test.impl.execution.event.RemoteExecutionEvent;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.client.protocol.ProtocolConfiguration;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.context.ManagerTestContext;
import org.jboss.arquillian.core.test.context.ManagerTestContextImpl;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * TestExecutorHandlerTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteTestExecuterTestCase extends AbstractContainerTestTestBase {
    @Mock
    private ContainerMethodExecutor executor;

    @Mock
    private Container container;

    @Mock
    private TestMethodExecutor testExecutor;

    @Mock
    private DeploymentDescription deploymentDescription;

    @Mock
    @SuppressWarnings("rawtypes")
    private Protocol protocol;

    @Mock
    private ProtocolDefinition protocolDefinition;

    @Mock
    private ProtocolRegistry protocolRegistry;

    @Mock
    private ProtocolMetaData protocolMetaData;

    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(ManagerTestContextImpl.class);
    }

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(RemoteTestExecuter.class);
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        bind(ApplicationScoped.class, Container.class, container);
        bind(ApplicationScoped.class, DeploymentDescription.class, deploymentDescription);
        bind(ApplicationScoped.class, ProtocolMetaData.class, protocolMetaData);
        bind(ApplicationScoped.class, ProtocolRegistry.class, protocolRegistry);

        Mockito.when(deploymentDescription.getProtocol()).thenReturn(new ProtocolDescription("TEST"));
        Mockito.when(protocolRegistry.getProtocol(Mockito.any(ProtocolDescription.class))).thenReturn(protocolDefinition);
        Mockito.when(protocolDefinition.getProtocol()).thenReturn(protocol);
        Mockito.when(protocol.getExecutor(
            Mockito.any(ProtocolConfiguration.class),
            Mockito.any(ProtocolMetaData.class),
            Mockito.any(CommandCallback.class))).thenAnswer(new Answer<ContainerMethodExecutor>() {
            @Override
            public ContainerMethodExecutor answer(InvocationOnMock invocation) throws Throwable {
                return new TestContainerMethodExecutor((CommandCallback) invocation.getArguments()[2]);
            }
        });

        Mockito.when(testExecutor.getInstance()).thenReturn(this);
        Mockito.when(testExecutor.getMethod()).thenReturn(
            getTestMethod("shouldReactivePreviousContextsOnRemoteEvents"));
    }

    @Test
    public void shouldReactivePreviousContextsOnRemoteEvents() throws Exception {
        getManager().getContext(ManagerTestContext.class).activate();
        fire(new RemoteExecutionEvent(testExecutor));
        assertEventFiredInContext(TestStringCommand.class, ManagerTestContext.class);
    }

    private Method getTestMethod(String name) throws Exception {
        return this.getClass().getMethod(name);
    }

    public class TestContainerMethodExecutor implements ContainerMethodExecutor {
        private CommandCallback callback;

        public TestContainerMethodExecutor(CommandCallback callback) {
            this.callback = callback;
        }

        @Override
        public TestResult invoke(TestMethodExecutor testMethodExecutor) {
            final CountDownLatch latch = new CountDownLatch(1);

            Thread remote = new Thread() {
                public void run() {
                    callback.fired(new TestStringCommand());
                    latch.countDown();
                }

                ;
            };
            remote.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            remote.start();

            try {
                if (!latch.await(200, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Latch never reached");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return TestResult.passed();
        }
    }

    public class TestStringCommand implements Command<String>, Serializable {
        private static final long serialVersionUID = 1L;

        private String result;
        private Throwable throwable;

        @Override
        public String getResult() {
            return result;
        }

        @Override
        public void setResult(String result) {
            this.result = result;
        }

        @Override
        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}