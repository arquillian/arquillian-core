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
package org.jboss.arquillian.protocol.servlet5;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;

/**
 * ServletMethodExecutor
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletMethodExecutor implements ContainerMethodExecutor {
    public static final String ARQUILLIAN_SERVLET_NAME = "ArquillianServletRunnerEE9";
    public static final String ARQUILLIAN_SERVLET_MAPPING = "/" + ARQUILLIAN_SERVLET_NAME;
    private static final Logger log = Logger.getLogger(ContainerMethodExecutor.class.getName());
    protected ServletURIHandler uriHandler;
    protected CommandCallback callback;
    protected ServletProtocolConfiguration config;

    protected ServletMethodExecutor() {
    }

    public ServletMethodExecutor(ServletProtocolConfiguration config, Collection<HTTPContext> contexts,
        final CommandCallback callback) {
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
        this.uriHandler = new ServletURIHandler(config, contexts);
        this.callback = callback;
    }

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
            final String url = targetBaseURI.toASCIIString() + ARQUILLIAN_SERVLET_MAPPING
                + "?outputMode=serializedObject&className=" + testClass.getName() + "&methodName="
                + urlEncodedMethodName;

            final String eventUrl = targetBaseURI.toASCIIString() + ARQUILLIAN_SERVLET_MAPPING
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

    protected <T> T executeWithRetry(String url, Class<T> type) throws Exception {
        long timeoutTime = System.currentTimeMillis() + 1000;
        boolean interrupted = false;
        while (timeoutTime > System.currentTimeMillis()) {
            T o = execute(url, type, null);
            if (o != null) {
                return o;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        throw new IllegalStateException("Error launching request at " + url + ". No result returned");
    }

    protected <T> T execute(String url, Class<T> returnType, Object requestObject) throws Exception {
        URLConnection connection = new URL(url).openConnection();
        if (!(connection instanceof HttpURLConnection)) {
            throw new IllegalStateException("Not an http connection! " + connection);
        }
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setUseCaches(false);
        httpConnection.setDefaultUseCaches(false);
        httpConnection.setDoInput(true);

        prepareHttpConnection(httpConnection);

        try {

            if (requestObject != null) {
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                httpConnection.setRequestProperty("Content-Type", "application/octet-stream");
            }

            if (requestObject != null) {
                ObjectOutputStream ous = new ObjectOutputStream(httpConnection.getOutputStream());
                try {
                    ous.writeObject(requestObject);
                } catch (Exception e) {
                    throw new RuntimeException("Error sending request Object, " + requestObject, e);
                } finally {
                    ous.flush();
                    ous.close();
                }
            }

            try {
                httpConnection.getResponseCode();
            } catch (ConnectException e) {
                return null; // Could not connect
            }
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ObjectInputStream ois = new ObjectInputStream(httpConnection.getInputStream());
                Object o;
                try {
                    o = ois.readObject();
                } finally {
                    ois.close();
                }

                if (!returnType.isInstance(o)) {
                    throw new IllegalStateException(
                        "Error reading results, expected a " + returnType.getName() + " but got " + o);
                }
                return returnType.cast(o);
            } else if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                return null;
            } else if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw new IllegalStateException(
                    "Error launching test at " + url + ". " +
                        "Got " + httpConnection.getResponseCode() + " (" + httpConnection.getResponseMessage() + ")");
            }
        } finally {
            httpConnection.disconnect();
        }
        return null;
    }

    @SuppressWarnings("UnusedParameters")
    protected void prepareHttpConnection(HttpURLConnection connection) {
    }

    protected Timer createCommandServicePullTimer(final String eventUrl,
            final Lock timerLock, final AtomicBoolean isCanceled) {
        if (config.getPullInMilliSeconds() == null || config.getPullInMilliSeconds() <= 0) {
            log.warning("The Servlet Protocol has been configured with a pullInMilliSeconds interval of " +
                config.getPullInMilliSeconds() + ". The effect of this is that the Command Service has been disabled." +
                " Depending on which features you use, this might cause serious delays. Be on high alert for " +
                " possible timeout runtime exceptions.");
            return null;
        }
        Timer eventTimer = new Timer();
        eventTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerLock.lock();
                try {
                    if (isCanceled.get()) {
                        return;
                    }
                    Object o = execute(eventUrl, Object.class, null);
                    if (o != null) {
                        if (o instanceof Command) {
                            Command<?> command = (Command<?>) o;
                            callback.fired(command);
                            execute(eventUrl, Object.class, command);
                        } else {
                            throw new RuntimeException("Recived a non " + Command.class.getName()
                                + " object on event channel");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    timerLock.unlock();
                }
            }
        }, 0, config.getPullInMilliSeconds());
        return eventTimer;
    }
}
