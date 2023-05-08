/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.testenricher.cdi;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.testenricher.cdi.client.BeansXMLProtocolProcessor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * BeansXMLProtocolProcessorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class BeansXMLProtocolProcessorTestCase {

    @Test
    public void shouldAddBeansXMLWhenFoundInWebArchive() {
        WebArchive deployment = ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        runAndAssertWebArchiveProtocol(deployment, true);
    }

    @Test
    public void shouldAddBeansXMLWhenFoundInJavaArchive() {
        JavaArchive deployment = ShrinkWrap.create(JavaArchive.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        runAndAssertJavaArchiveProtocol(deployment, true);
    }

    @Test
    public void shouldNotAddBeansXMLIfNotFoundInWebArchive() {
        WebArchive deployment = ShrinkWrap.create(WebArchive.class);

        runAndAssertWebArchiveProtocol(deployment, false);
    }

    @Test
    public void shouldAddBeansXMLWhenFoundInEnterpriseModule() {
        EnterpriseArchive deployment = ShrinkWrap.create(EnterpriseArchive.class).addAsModule(
            ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml"));

        runAndAssertWebArchiveProtocol(deployment, true);
    }

    @Test
    public void shouldNotAddBeansXMLIfNotFoundInEnterpriseModule() {
        EnterpriseArchive deployment = ShrinkWrap.create(EnterpriseArchive.class).addAsModule(
            ShrinkWrap.create(WebArchive.class));

        runAndAssertWebArchiveProtocol(deployment, false);
    }

    @Test
    public void shouldNotAddBeansXMLIfArchivesAreEqual() {
        WebArchive protocol = ShrinkWrap.create(WebArchive.class);

        new BeansXMLProtocolProcessor().process(
            new TestDeployment(null, protocol, new ArrayList<Archive<?>>()), protocol);

        Assert.assertFalse(protocol.contains("WEB-INF/beans.xml"));
    }

    @Test // ARQ-1421 this does not fail on SW 1.0 since it silently ignore overwriting existing paths.
    public void shouldNotOverwriteBeansXMLIfArchivesAreTheSameAndContainBeansXml() throws Exception {
        String beansXmlContent = "test";
        WebArchive deployment = ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource(new StringAsset(beansXmlContent), "beans.xml");

        WebArchive protocol = deployment.as(WebArchive.class);

        new BeansXMLProtocolProcessor().process(
            new TestDeployment(null, deployment, new ArrayList<Archive<?>>()), protocol);

        Assert.assertTrue(protocol.contains("WEB-INF/beans.xml"));

        byte[] buf = new byte[beansXmlContent.length()];
        new BufferedInputStream(protocol.get("WEB-INF/beans.xml").getAsset().openStream()).read(buf);

        Assert.assertEquals(beansXmlContent, new String(buf));
    }

    @Test // ARQ-1421
    public void shouldNotOverwriteBeansXMLIfProtocolWebArchiveContainBeansXml() throws Exception {
        String beansXmlContent = "test";
        WebArchive deployment = ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        WebArchive protocol = ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource(new StringAsset(beansXmlContent), "beans.xml");

        new BeansXMLProtocolProcessor().process(
            new TestDeployment(null, deployment, new ArrayList<Archive<?>>()), protocol);

        Assert.assertTrue(protocol.contains("WEB-INF/beans.xml"));

        byte[] buf = new byte[beansXmlContent.length()];
        new BufferedInputStream(protocol.get("WEB-INF/beans.xml").getAsset().openStream()).read(buf);

        Assert.assertEquals(beansXmlContent, new String(buf));
    }

    @Test // ARQ-1421
    public void shouldNotOverwriteBeansXMLIfProtocolJavaArchiveContainBeansXml() throws Exception {
        String beansXmlContent = "test";
        JavaArchive deployment = ShrinkWrap.create(JavaArchive.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        JavaArchive protocol = ShrinkWrap.create(JavaArchive.class)
            .addAsManifestResource(new StringAsset(beansXmlContent), "beans.xml");

        new BeansXMLProtocolProcessor().process(
            new TestDeployment(null, deployment, new ArrayList<Archive<?>>()), protocol);

        Assert.assertTrue(protocol.contains("META-INF/beans.xml"));

        byte[] buf = new byte[beansXmlContent.length()];
        new BufferedInputStream(protocol.get("META-INF/beans.xml").getAsset().openStream()).read(buf);

        Assert.assertEquals(beansXmlContent, new String(buf));
    }

    public void runAndAssertWebArchiveProtocol(Archive<?> deployment, boolean shouldBeFound) {
        runAndAsset(deployment, ShrinkWrap.create(WebArchive.class), shouldBeFound, "WEB-INF/beans.xml");
    }

    public void runAndAssertJavaArchiveProtocol(Archive<?> deployment, boolean shouldBeFound) {
        runAndAsset(deployment, ShrinkWrap.create(JavaArchive.class), shouldBeFound, "META-INF/beans.xml");
    }

    public void runAndAsset(Archive<?> deployment, Archive<?> protocol, boolean shouldBeFound, String expectedLocation) {
        new BeansXMLProtocolProcessor().process(
            new TestDeployment(null, deployment, new ArrayList<Archive<?>>()), protocol);

        System.out.println(protocol.toString(true));
        Assert.assertEquals(
            "Verify beans.xml was " + (!shouldBeFound ? "not " : "") + "found in " + expectedLocation,
            shouldBeFound, protocol.contains(expectedLocation));
    }
}
