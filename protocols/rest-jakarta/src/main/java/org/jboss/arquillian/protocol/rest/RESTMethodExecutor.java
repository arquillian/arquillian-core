/*
 * Copyright 2022 Red Hat Inc. and/or its affiliates and other contributors
 * identified by the Git commit log. 
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
package org.jboss.arquillian.protocol.rest;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.protocol.servlet5.ServletMethodExecutor;
import org.jboss.arquillian.protocol.servlet5.ServletProtocolConfiguration;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;

public class RESTMethodExecutor extends ServletMethodExecutor {
    public static final String ARQUILLIAN_REST_NAME = "ArquillianRESTRunnerEE9";
    public static final String ARQUILLIAN_REST_MAPPING = "/" + ARQUILLIAN_REST_NAME;

    public RESTMethodExecutor(ServletProtocolConfiguration config, Collection<HTTPContext> contexts,
            CommandCallback callback) {
        if (config == null) {
            throw new IllegalArgumentException("ServletProtocolConfiguration must be specified");
        }
        if (contexts == null || contexts.size() == 0) {
            throw new IllegalArgumentException("HTTPContext must be specified");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback must be specified");
        }
        this.config = config;
        this.uriHandler = new RESTURIHandler(config, contexts);
        this.callback = callback;
    }

    @Override
    public TestResult invoke(final TestMethodExecutor testMethodExecutor) {
        if (testMethodExecutor == null) {
            throw new IllegalArgumentException("TestMethodExecutor must be specified");
        }

        URI targetBaseURI = uriHandler.locateTestServlet(testMethodExecutor.getMethod());

        Class<?> testClass = testMethodExecutor.getInstance().getClass();

        Timer eventTimer = null;
        Lock timerLock = new ReentrantLock();
        AtomicBoolean isCanceled = new AtomicBoolean();
        try {
            String urlEncodedMethodName = URLEncoder.encode(testMethodExecutor.getMethodName(), "UTF-8");
            final String url = targetBaseURI.toASCIIString() + ARQUILLIAN_REST_MAPPING
                + "?outputMode=serializedObject&className=" + testClass.getName() + "&methodName="
                + urlEncodedMethodName;

            final String eventUrl = targetBaseURI.toASCIIString() + ARQUILLIAN_REST_MAPPING
                + "?outputMode=serializedObject&className=" + testClass.getName() + "&methodName="
                + urlEncodedMethodName + "&cmd=event";

            eventTimer = createCommandServicePullTimer(eventUrl, timerLock, isCanceled);
            return executeWithRetry(url, TestResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Error launching test " + testClass.getName() + " "
                + testMethodExecutor.getMethod(), e);
        } finally {
            if (eventTimer != null) {
                eventTimer.cancel();
                timerLock.lock();
                try {
                    isCanceled.set(true);
                } finally {
                    timerLock.unlock();
                }
            }
        }
    }
}
