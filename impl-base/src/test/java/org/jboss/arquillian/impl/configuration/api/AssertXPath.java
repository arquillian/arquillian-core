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
package org.jboss.arquillian.impl.configuration.api;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple helper for Xpath related Assertions.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class AssertXPath
{
   /**
    * Util class, should not be constructed. 
    */
   private AssertXPath() {}
   
   /**
    * Assert that the specified XPath Expression resolves to the specified values.
    * <br/><br/>
    * Assertions:<br/>
    * "ExpectedValue count should match found Node count" <br/> 
    * "XPath content should match expected value" <br/>
    * 
    * @param xml The XML to assert against
    * @param expression XPath expression to extract
    * @param expectedValue The Expected values found by expression
    * @throws Exception XML/XPath related parse exceptions
    */
   public static void assertXPath(String xml, String expression, String... expectedValue)
      throws Exception
   {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new ByteArrayInputStream(xml.getBytes()));
      
      XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(expression);
   
      NodeList nodes = (NodeList)xPathExpression.evaluate(doc, XPathConstants.NODESET);
      Assert.assertEquals(
            "ExpectedValue count should match found Node count", 
            expectedValue.length, 
            nodes.getLength());
   
      for(int i = 0; i < nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         Assert.assertEquals(
               "XPath content should match expected value",
               expectedValue[i], 
               node.getTextContent());
      }
   }

}
