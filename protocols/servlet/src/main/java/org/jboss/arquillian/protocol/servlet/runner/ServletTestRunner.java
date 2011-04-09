/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.protocol.servlet.runner;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunner;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.util.TestRunners;

/**
 * ServletTestRunner
 * 
 * The server side executor for the Servlet protocol impl.
 * 
 * Supports multiple output modes ("outputmode"):
 *  - html
 *  - serializedObject 
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletTestRunner extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   public static final String PARA_METHOD_NAME = "methodName";
   public static final String PARA_CLASS_NAME = "className";
   public static final String PARA_OUTPUT_MODE = "outputMode";
   
   public static final String OUTPUT_MODE_SERIALIZED = "serializedObject";
   public static final String OUTPUT_MODE_HTML = "html";
   
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      String outputMode = OUTPUT_MODE_HTML;
      try 
      {
         String className = null;
         String methodName = null;

         if (request.getParameter(PARA_OUTPUT_MODE) != null)
         {
            outputMode = request.getParameter(PARA_OUTPUT_MODE);
         }
         className = request.getParameter(PARA_CLASS_NAME);
         if (className == null)
         {
            throw new IllegalArgumentException(PARA_CLASS_NAME + " must be specified");
         }
         methodName = request.getParameter(PARA_METHOD_NAME);
         if ( methodName == null)
         {
            throw new IllegalArgumentException(PARA_METHOD_NAME + " must be specified");
         }
         
         Class<?> testClass = SecurityActions.getThreadContextClassLoader().loadClass(className);
         
         TestRunner runner = TestRunners.getTestRunner();
         
         TestResult testResult = runner.execute(testClass, methodName);

         if(OUTPUT_MODE_SERIALIZED.equalsIgnoreCase(outputMode)) 
         {
            writeObject(testResult, response);
         } 
         else 
         {
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
      catch(Exception e) 
      {
         if(OUTPUT_MODE_SERIALIZED.equalsIgnoreCase(outputMode)) 
         {
            writeObject(createFailedResult(e), response);
         } 
         else 
         {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());  
         }
      }
   }
   
   private void writeObject(Object object, HttpServletResponse response) 
   {
      try 
      {
         // Set HttpServletResponse status BEFORE getting the output stream
         response.setStatus(HttpServletResponse.SC_OK);
         ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
         oos.writeObject(object);
         oos.flush();
         oos.close();
      } 
      catch (Exception e) 
      {
         try 
         {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
         } 
         catch (Exception e2) 
         {
            throw new RuntimeException("Could not write to output", e2);
         }
      }
   }
   
   private TestResult createFailedResult(Throwable throwable)
   {
      return new TestResult(Status.FAILED, throwable);
   }
}
