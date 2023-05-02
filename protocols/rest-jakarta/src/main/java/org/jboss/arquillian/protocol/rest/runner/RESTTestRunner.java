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
package org.jboss.arquillian.protocol.rest.runner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.util.TestRunners;
import org.jboss.arquillian.test.spi.TestResult;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

/**
 * RESTTestRunner
 * <p>
 * The server side executor for the REST protocol impl.
 * <p>
 * Supports multiple output modes ("outputmode"):
 * - html
 * - serializedObject
 * 
 * A conversion of the ServletTestRunner to use RESTful Web Services instead.
 */
@Path("")
public class RESTTestRunner {
    public static final String PARA_METHOD_NAME = "methodName";
    public static final String PARA_CLASS_NAME = "className";
    public static final String PARA_OUTPUT_MODE = "outputMode";
    public static final String PARA_CMD_NAME = "cmd";
    public static final String OUTPUT_MODE_SERIALIZED = "serializedObject";
    public static final String OUTPUT_MODE_HTML = "html";
    public static final String CMD_NAME_TEST = "test";
    public static final String CMD_NAME_EVENT = "event";
    static ConcurrentHashMap<String, Command<?>> events = new ConcurrentHashMap<>();
    static ThreadLocal<String> currentCall = new ThreadLocal<>();

    @POST
    public Response doPost(@QueryParam(PARA_OUTPUT_MODE) String outputMode, @QueryParam(PARA_CLASS_NAME) String className,
            @QueryParam(PARA_METHOD_NAME) String methodName, @QueryParam(PARA_CMD_NAME) String cmd, byte[] cmdObject) throws WebApplicationException, IOException {
        return execute(outputMode, className, methodName, cmd, cmdObject);
    }

    @GET
    public Response doGet(@QueryParam(PARA_OUTPUT_MODE) String outputMode, @QueryParam(PARA_CLASS_NAME) String className,
            @QueryParam(PARA_METHOD_NAME) String methodName, @QueryParam(PARA_CMD_NAME) String cmd) throws WebApplicationException, IOException {
        return execute(outputMode, className, methodName, cmd, null);
    }

    protected Response execute(String outputMode, String className, String methodName, String cmd, byte[] cmdObject)
        throws WebApplicationException, IOException {
        if (outputMode == null) {
            outputMode = OUTPUT_MODE_HTML;
        }
        if (cmd == null) {
            cmd = CMD_NAME_TEST;
        }
        ResponseBuilder response = Response.ok();
        try {

            if (className == null) {
                throw new IllegalArgumentException(PARA_CLASS_NAME + " must be specified");
            }
            if (methodName == null) {
                throw new IllegalArgumentException(PARA_METHOD_NAME + " must be specified");
            }

            currentCall.set(className + methodName);

            if (CMD_NAME_TEST.equals(cmd)) {
                executeTest(response, outputMode, className, methodName);
            } else if (CMD_NAME_EVENT.equals(cmd)) {
                executeEvent(cmdObject, response, className, methodName);
            } else {
                throw new RuntimeException("Unknown value for parameter" + PARA_CMD_NAME + ": " + cmd);
            }
        } catch (Exception e) {
            if (OUTPUT_MODE_SERIALIZED.equalsIgnoreCase(outputMode)) {
                writeObject(createFailedResult(e), response);
            } else {
                response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            }
        } finally {
            currentCall.remove();
        }
        return response.build();
    }

    public void executeTest(ResponseBuilder response, String outputMode, String className, String methodName)
        throws ClassNotFoundException, IOException {
        Class<?> testClass = SecurityActions.getThreadContextClassLoader().loadClass(className);
        TestRunner runner = TestRunners.getTestRunner();
        TestResult testResult = runner.execute(testClass, methodName);
        if (OUTPUT_MODE_SERIALIZED.equalsIgnoreCase(outputMode)) {
            writeObject(testResult, response);
        } else {
            // TODO: implement a html view of the result
            response.type(MediaType.TEXT_HTML_TYPE);
            response.status(Status.OK);
            StringBuilder buffer = new StringBuilder();
            buffer.append("<html>\n");
            buffer.append("<head><title>TCK Report</title></head>\n");
            buffer.append("<body>\n");
            buffer.append("<h2>Configuration</h2>\n");
            buffer.append("<table>\n");
            buffer.append("<tr>\n");
            buffer.append("<td><b>Method</b></td><td><b>Status</b></td>\n");
            buffer.append("</tr>\n");

            buffer.append("</table>\n");
            buffer.append("<h2>Tests</h2>\n");
            buffer.append("<table>\n");
            buffer.append("<tr>\n");
            buffer.append("<td><b>Method</b></td><td><b>Status</b></td>\n");
            buffer.append("</tr>\n");

            buffer.append("</table>\n");
            buffer.append("</body>\n");
            response.entity(buffer.toString());
        }
    }

    public void executeEvent(byte[] cmdObject, ResponseBuilder response, String className,
        String methodName)
        throws ClassNotFoundException, IOException {
        String eventKey = className + methodName;

        if (cmdObject != null && cmdObject.length > 0) {
            response.status(Status.NO_CONTENT);
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(cmdObject));
            Command<?> result = (Command<?>) input.readObject();
            input.close();

            events.put(eventKey, result);
        } else {
            if (events.containsKey(eventKey) && events.get(eventKey).getResult() == null) {
                response.status(Status.OK);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(baos);
                output.writeObject(events.remove(eventKey));
                output.flush();
                response.type(MediaType.APPLICATION_OCTET_STREAM_TYPE);
                response.entity(baos.toByteArray());
                output.close();
            } else {
                response.status(Status.NO_CONTENT);
            }
        }
    }

    private void writeObject(Object object, ResponseBuilder response) {
        try {
            response.status(Status.OK);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
            response.type(MediaType.APPLICATION_OCTET_STREAM_TYPE);
            response.entity(baos.toByteArray());
            oos.close();
        } catch (Exception e) {
            try {
                response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            } catch (Exception e2) {
                throw new RuntimeException("Could not write to output", e2);
            }
        }
    }

    private TestResult createFailedResult(Throwable throwable) {
        return TestResult.failed(throwable);
    }
}
