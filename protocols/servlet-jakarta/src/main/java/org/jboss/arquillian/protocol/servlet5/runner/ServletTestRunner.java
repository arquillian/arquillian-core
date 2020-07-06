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
package org.jboss.arquillian.protocol.servlet5.runner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.util.TestRunners;
import org.jboss.arquillian.test.spi.TestResult;

/**
 * ServletTestRunner
 * <p>
 * The server side executor for the Servlet protocol impl.
 * <p>
 * Supports multiple output modes ("outputmode"):
 * - html
 * - serializedObject
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletTestRunner extends HttpServlet {
    public static final String PARA_METHOD_NAME = "methodName";
    public static final String PARA_CLASS_NAME = "className";
    public static final String PARA_OUTPUT_MODE = "outputMode";
    public static final String PARA_CMD_NAME = "cmd";
    public static final String OUTPUT_MODE_SERIALIZED = "serializedObject";
    public static final String OUTPUT_MODE_HTML = "html";
    public static final String CMD_NAME_TEST = "test";
    public static final String CMD_NAME_EVENT = "event";
    private static final long serialVersionUID = 1L;
    static ConcurrentHashMap<String, Command<?>> events;
    static ThreadLocal<String> currentCall;
    private static ThreadLocal<ServletContext> currentServletContext;

    public static ServletContext getCurrentServletContext() {
        return currentServletContext.get();
    }

    @Override
    public void init() throws ServletException {
        events = new ConcurrentHashMap<String, Command<?>>();
        currentCall = new ThreadLocal<String>();
        currentServletContext = new ThreadLocal<ServletContext>();
    }

    @Override
    public void destroy() {
        events.clear();
        currentCall.remove();
        currentServletContext.remove();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        execute(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        execute(request, response);
    }

    protected void execute(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String outputMode = OUTPUT_MODE_HTML;
        String cmd = CMD_NAME_TEST;
        try {
            String className = null;
            String methodName = null;

            if (request.getParameter(PARA_OUTPUT_MODE) != null) {
                outputMode = request.getParameter(PARA_OUTPUT_MODE);
            }
            className = request.getParameter(PARA_CLASS_NAME);
            if (className == null) {
                throw new IllegalArgumentException(PARA_CLASS_NAME + " must be specified");
            }
            methodName = request.getParameter(PARA_METHOD_NAME);
            if (methodName == null) {
                throw new IllegalArgumentException(PARA_METHOD_NAME + " must be specified");
            }

            if (request.getParameter(PARA_CMD_NAME) != null) {
                cmd = request.getParameter(PARA_CMD_NAME);
            }

            currentServletContext.set(getServletContext());
            currentCall.set(className + methodName);

            if (CMD_NAME_TEST.equals(cmd)) {
                executeTest(response, outputMode, className, methodName);
            } else if (CMD_NAME_EVENT.equals(cmd)) {
                executeEvent(request, response, className, methodName);
            } else {
                throw new RuntimeException("Unknown value for parameter" + PARA_CMD_NAME + ": " + cmd);
            }
        } catch (Exception e) {
            if (OUTPUT_MODE_SERIALIZED.equalsIgnoreCase(outputMode)) {
                writeObject(createFailedResult(e), response);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } finally {
            currentCall.remove();
            currentServletContext.remove();
        }
    }

    public void executeTest(HttpServletResponse response, String outputMode, String className, String methodName)
        throws ClassNotFoundException, IOException {
        Class<?> testClass = SecurityActions.getThreadContextClassLoader().loadClass(className);
        TestRunner runner = TestRunners.getTestRunner();
        TestResult testResult = runner.execute(testClass, methodName);
        if (OUTPUT_MODE_SERIALIZED.equalsIgnoreCase(outputMode)) {
            writeObject(testResult, response);
        } else {
            // TODO: implement a html view of the result
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter writer = response.getWriter();
            writer.write("<html>\n");
            writer.write("<head><title>TCK Report</title></head>\n");
            writer.write("<body>\n");
            writer.write("<h2>Configuration</h2>\n");
            writer.write("<table>\n");
            writer.write("<tr>\n");
            writer.write("<td><b>Method</b></td><td><b>Status</b></td>\n");
            writer.write("</tr>\n");

            writer.write("</table>\n");
            writer.write("<h2>Tests</h2>\n");
            writer.write("<table>\n");
            writer.write("<tr>\n");
            writer.write("<td><b>Method</b></td><td><b>Status</b></td>\n");
            writer.write("</tr>\n");

            writer.write("</table>\n");
            writer.write("</body>\n");
        }
    }

    public void executeEvent(HttpServletRequest request, HttpServletResponse response, String className,
        String methodName)
        throws ClassNotFoundException, IOException {
        String eventKey = className + methodName;

        if (request.getContentLength() > 0) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(request.getInputStream()));
            Command<?> result = (Command<?>) input.readObject();

            events.put(eventKey, result);
        } else {
            if (events.containsKey(eventKey) && events.get(eventKey).getResult() == null) {
                response.setStatus(HttpServletResponse.SC_OK);
                ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
                output.writeObject(events.remove(eventKey));
                output.flush();
                output.close();
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        }
    }

    private void writeObject(Object object, HttpServletResponse response) {
        try {
            // Set HttpServletResponse status BEFORE getting the output stream
            response.setStatus(HttpServletResponse.SC_OK);
            ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
            oos.writeObject(object);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (Exception e2) {
                throw new RuntimeException("Could not write to output", e2);
            }
        }
    }

    private TestResult createFailedResult(Throwable throwable) {
        return TestResult.failed(throwable);
    }
}
