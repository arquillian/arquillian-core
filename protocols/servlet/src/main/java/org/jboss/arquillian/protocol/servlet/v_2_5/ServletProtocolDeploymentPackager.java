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
package org.jboss.arquillian.protocol.servlet.v_2_5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.protocol.servlet.Processor;
import org.jboss.arquillian.protocol.servlet.ServletUtil;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.ApplicationDescriptor;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.WebAppDescriptor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

import static org.jboss.arquillian.protocol.servlet.ServletUtil.APPLICATION_XML_PATH;
import static org.jboss.arquillian.protocol.servlet.ServletUtil.WEB_XML_PATH;

/**
 * ServletProtocolDeploymentPackager
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletProtocolDeploymentPackager implements DeploymentPackager {

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.DeploymentPackager#generateDeployment(org.jboss.arquillian.spi.TestDeployment)
     */
    public Archive<?> generateDeployment(TestDeployment testDeployment, Collection<ProtocolArchiveProcessor> processors) {
        WebArchive protocol = new ProtocolDeploymentAppender().createAuxiliaryArchive();

        Archive<?> applicationArchive = testDeployment.getApplicationArchive();
        Collection<Archive<?>> auxiliaryArchives = testDeployment.getAuxiliaryArchives();

        Processor processor = new Processor(testDeployment, processors);

        if (Validate.isArchiveOfType(EnterpriseArchive.class, applicationArchive)) {
            return handleArchive(applicationArchive.as(EnterpriseArchive.class), auxiliaryArchives, protocol, processor,
                testDeployment);
        }

        if (Validate.isArchiveOfType(WebArchive.class, applicationArchive)) {
            return handleArchive(applicationArchive.as(WebArchive.class), auxiliaryArchives, protocol, processor);
        }

        if (Validate.isArchiveOfType(JavaArchive.class, applicationArchive)) {
            return handleArchive(applicationArchive.as(JavaArchive.class), auxiliaryArchives, protocol, processor);
        }

        throw new IllegalArgumentException(ServletProtocolDeploymentPackager.class.getName() +
            " can not handle archive of type " + applicationArchive.getClass().getName());
    }

    private Archive<?> handleArchive(WebArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives,
        WebArchive protocol, Processor processor) {
        if (applicationArchive.contains(WEB_XML_PATH)) {
            WebAppDescriptor applicationWebXml = Descriptors.importAs(WebAppDescriptor.class).from(
                applicationArchive.get(WEB_XML_PATH).getAsset().openStream());

            // SHRINKWRAP-187, to eager on not allowing overrides, delete it first
            applicationArchive.delete(WEB_XML_PATH);
            applicationArchive.setWebXML(
                new StringAsset(
                    WebUtils.mergeWithDescriptor(applicationWebXml).exportAsString()));
            applicationArchive.merge(protocol, Filters.exclude(".*web\\.xml.*"));
        } else {
            applicationArchive.merge(protocol);
        }
        applicationArchive.addAsLibraries(auxiliaryArchives.toArray(new Archive<?>[0]));

        processor.process(applicationArchive);
        return applicationArchive;
    }

    /*
     * Wrap the applicationArchive as a EnterpriseArchive and pass it to handleArchive(EnterpriseArchive, ...)
     */
    private Archive<?> handleArchive(JavaArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives,
        WebArchive protocol, Processor processor) {
        return handleArchive(
            ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(applicationArchive),
            auxiliaryArchives,
            protocol,
            processor,
            null);
    }

    private Archive<?> handleArchive(EnterpriseArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives,
        WebArchive protocol, Processor processor, TestDeployment testDeployment) {
        Map<ArchivePath, Node> applicationArchiveWars = applicationArchive.getContent(Filters.include(".*\\.war"));
        if (applicationArchiveWars.size() == 1) {
            ArchivePath warPath = applicationArchiveWars.keySet().iterator().next();
            try {
                handleArchive(
                    applicationArchive.getAsType(WebArchive.class, warPath),
                    new ArrayList<Archive<?>>(),
                    // reuse the War handling, but Auxiliary Archives should be added to the EAR, not the WAR
                    protocol,
                    processor);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Can not manipulate war's that are not of type " + WebArchive.class,
                    e);
            }
        } else if (applicationArchiveWars.size() > 1) {
            Archive<?> archiveToTest = testDeployment.getArchiveForEnrichment();
            if (archiveToTest == null) {
                throw new UnsupportedOperationException("Multiple WebArchives found in "
                    + applicationArchive.getName()
                    + ". Can not determine which to enrich");
            } else if (!archiveToTest.getName().endsWith(".war")) {
                //TODO: Removed throwing an exception when EJB modules are supported as well
                throw new UnsupportedOperationException("Archive to test is not a WebArchive!");
            } else {
                handleArchive(
                    archiveToTest.as(WebArchive.class),
                    new ArrayList<Archive<?>>(),
                    // reuse the War handling, but Auxiliary Archives should be added to the EAR, not the WAR
                    protocol,
                    processor);
            }
        } else {
            // SHRINKWRAP-187, to eager on not allowing overrides, delete it first
            protocol.delete(WEB_XML_PATH);
            applicationArchive
                .addAsModule(
                    protocol.setWebXML(
                        new StringAsset(WebUtils.createNewDescriptor().exportAsString())));

            if (applicationArchive.contains(APPLICATION_XML_PATH)) {
                ApplicationDescriptor applicationXml = Descriptors.importAs(ApplicationDescriptor.class).from(
                    applicationArchive.get(APPLICATION_XML_PATH).getAsset().openStream());

                applicationXml.webModule(protocol.getName(), ServletUtil.calculateContextRoot(protocol.getName()));

                // SHRINKWRAP-187, to eager on not allowing overrides, delete it first
                applicationArchive.delete(APPLICATION_XML_PATH);
                applicationArchive.setApplicationXML(
                    new StringAsset(applicationXml.exportAsString()));
            }

            processor.process(protocol);
        }
        applicationArchive.addAsLibraries(
            auxiliaryArchives.toArray(new Archive<?>[0]));
        return applicationArchive;
    }
}
