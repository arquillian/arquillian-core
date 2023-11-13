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
package org.jboss.arquillian.protocol.servlet5.v_5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.protocol.servlet5.TestUtil;
import org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.api.application.ApplicationDescriptor;
import org.jboss.arquillian.protocol.servlet5.runner.ServletTestRunner;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;

/**
 * ServletProtocolDeploymentPackagerTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletProtocolDeploymentPackagerTestCase {
    @Test
    public void shouldHandleJavaArchive() throws Exception {
        Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                null,
                ShrinkWrap.create(JavaArchive.class, "applicationArchive.jar"),
                createAuxiliaryArchives()),
            processors());

        Assert.assertTrue(
            "Verify that a defined JavaArchive using EE6 JavaArchive protocol is build as WebArchive",
            Validate.isArchiveOfType(WebArchive.class, archive));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/arquillian-jakarta-servlet-protocol.jar")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/auxiliaryArchive2.jar")));

        Assert.assertTrue(
            "Verify that the applicationArchive is placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/applicationArchive.jar")));

        Assert.assertTrue(
            "Verify protocol Processor SPI was called",
            DummyProcessor.wasCalled);
    }

    @Test
    public void shouldHandleWebArchive() throws Exception {
        Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                null,
                ShrinkWrap.create(WebArchive.class, "applicationArchive.war"),
                createAuxiliaryArchives()),
            processors());

        Assert.assertTrue(
            "Verify that a defined WebArchive using EE9 JavaArchive protocol is build as WebArchive",
            Validate.isArchiveOfType(WebArchive.class, archive));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/arquillian-jakarta-servlet-protocol.jar")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/auxiliaryArchive1.jar")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /WEB-INF/lib",
            archive.contains(ArchivePaths.create("/WEB-INF/lib/auxiliaryArchive2.jar")));

        Assert.assertTrue(
            "Verify protocol Processor SPI was called",
            DummyProcessor.wasCalled);
    }

    @Test
    public void shouldHandleEnterpriseArchive() throws Exception {
        Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                null,
                ShrinkWrap.create(EnterpriseArchive.class, "applicationArchive.ear"),
                createAuxiliaryArchives()),
            processors());

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /",
            archive.contains(ArchivePaths.create(JarNameProcessor.jarName)));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive1.jar")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));

        Assert.assertTrue(
            "Verify protocol Processor SPI was called",
            DummyProcessor.wasCalled);
    }

    @Test
    public void shouldHandleEnterpriseArchiveWithApplicationXML() throws Exception {
        Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                null,
                ShrinkWrap.create(EnterpriseArchive.class, "applicationArchive.ear")
                    .setApplicationXML(createApplicationDescriptor()),
                createAuxiliaryArchives()),
            processors());

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /",
            archive.contains(ArchivePaths.create(JarNameProcessor.jarName)));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive1.jar")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));

        String applicationXmlContent =
            TestUtil.convertToString(archive.get("META-INF/application.xml").getAsset().openStream());
        Assert.assertTrue(
            "verify that the arquillian-jakarta-servlet-protocol.war was added to the application.xml",
            applicationXmlContent.contains(String.format("<web-uri>%s</web-uri>", JarNameProcessor.jarName)));

        // ARQ-670
        Assert.assertTrue(
            "verify that the arquillian-jakarta-servlet-protocol.war has correct context-root in application.xml",
            applicationXmlContent.contains(String.format("<context-root>%s</context-root>",
                    JarNameProcessor.jarName.replaceFirst(".war$", ""))));

        Assert.assertTrue(
            "Verify protocol Processor SPI was called",
            DummyProcessor.wasCalled);
    }

    @Test
    public void shouldHandleEnterpriseArchiveWithWebArchive() throws Exception {
        WebArchive applicationWar = ShrinkWrap.create(WebArchive.class, "applicationArchive.war");

        Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                null,
                ShrinkWrap.create(EnterpriseArchive.class, "applicationArchive.ear")
                    .addAsModule(applicationWar),
                createAuxiliaryArchives()),
            processors());

        Assert.assertFalse(
            "Verify that the auxiliaryArchives was not added",
            archive.contains(ArchivePaths.create("arquillian-jakarta-servlet-protocol.war")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive1.jar")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));

        String webXmlContent = TestUtil.convertToString(
            applicationWar.get("WEB-INF/lib/arquillian-jakarta-servlet-protocol.jar/META-INF/web-fragment.xml").getAsset().openStream());
        Assert.assertTrue(
            "verify that the ServletTestRunner servlet was added to the web.xml of the existing web archive",
            webXmlContent.contains(ServletTestRunner.class.getName()));

        Assert.assertTrue(
            "Verify protocol Processor SPI was called",
            DummyProcessor.wasCalled);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnUnknownArchiveType() throws Exception {
        new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(null, ShrinkWrap.create(ResourceAdapterArchive.class), new ArrayList<Archive<?>>()),
            processors()
        );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionOnEnterpriseArchiveWithMultipleWebArchive() throws Exception {
        new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                null,
                ShrinkWrap.create(EnterpriseArchive.class, "applicationArchive.ear")
                    .addAsModule(ShrinkWrap.create(WebArchive.class))
                    .addAsModule(ShrinkWrap.create(WebArchive.class)),
                createAuxiliaryArchives()),
            processors());
    }

    @Test
    public void shouldHandleEnterpriseArchiveWithMultipleWebArchiveAndOneMarkedWebArchive() throws Exception {
        WebArchive testableArchive = Testable.archiveToTest(ShrinkWrap.create(WebArchive.class));

        Archive<?> archive = new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                null,
                ShrinkWrap.create(EnterpriseArchive.class, "applicationArchive.ear")
                    .addAsModule(testableArchive)
                    .addAsModule(ShrinkWrap.create(WebArchive.class)),
                createAuxiliaryArchives()),
            processors());

        Assert.assertFalse(
            "Verify that the auxiliaryArchives was not added",
            archive.contains(ArchivePaths.create("arquillian-jakarta-servlet-protocol.war")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive1.jar")));

        Assert.assertTrue(
            "Verify that the auxiliaryArchives are placed in /lib",
            archive.contains(ArchivePaths.create("/lib/auxiliaryArchive2.jar")));

        String webXmlContent = TestUtil.convertToString(
            testableArchive.get("WEB-INF/lib/arquillian-jakarta-servlet-protocol.jar/META-INF/web-fragment.xml").getAsset().openStream());
        Assert.assertTrue(
            "verify that the ServletTestRunner servlet was added to the web.xml of the existing web archive",
            webXmlContent.contains(ServletTestRunner.class.getName()));

        Assert.assertTrue(
            "Verify protocol Processor SPI was called",
            DummyProcessor.wasCalled);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionOnEnterpriseArchiveWithMultipleMarkedWebArchives() throws Exception {
        new ServletProtocolDeploymentPackager().generateDeployment(
            new TestDeployment(
                null,
                ShrinkWrap.create(EnterpriseArchive.class, "applicationArchive.ear")
                    .addAsModule(Testable.archiveToTest(ShrinkWrap.create(WebArchive.class)))
                    .addAsModule(Testable.archiveToTest(ShrinkWrap.create(WebArchive.class))),
                createAuxiliaryArchives()),
            processors());
    }

    private Collection<Archive<?>> createAuxiliaryArchives() {
        List<Archive<?>> archives = new ArrayList<Archive<?>>();
        archives.add(ShrinkWrap.create(JavaArchive.class, "auxiliaryArchive1.jar"));
        archives.add(ShrinkWrap.create(JavaArchive.class, "auxiliaryArchive2.jar"));

        return archives;
    }

    private Asset createApplicationDescriptor() {
        return new StringAsset(
            Descriptors.create(ApplicationDescriptor.class)
                .version("6")
                .ejbModule("test.jar")
                .exportAsString());
    }

    private Collection<ProtocolArchiveProcessor> processors() {
        List<ProtocolArchiveProcessor> pros = new ArrayList<ProtocolArchiveProcessor>();
        pros.add(new DummyProcessor());
        pros.add(new JarNameProcessor());
        return pros;
    }

    private static class DummyProcessor implements ProtocolArchiveProcessor {
        public static boolean wasCalled = false;

        public DummyProcessor() {
            wasCalled = false;
        }

        @Override
        public void process(TestDeployment testDeployment, Archive<?> protocolArchive) {
            wasCalled = true;
        }
    }

    private static class JarNameProcessor implements ProtocolArchiveProcessor {
        static String jarName;

        @Override
        public void process(TestDeployment testDeployment, Archive<?> protocolArchive) {
            jarName = protocolArchive.getName();
        }
    }
}
