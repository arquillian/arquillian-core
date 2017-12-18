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
package org.jboss.arquillian.container.test.spi;

import java.util.Collection;
import java.util.Map;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.Node;

/**
 * Value object that contains the {@link Archive}s needed for deployment. <br/>
 * <p>
 * With convenience methods for working / manipulating the Archives.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestDeployment {
    private DeploymentDescription deploymentDescription;

    private Archive<?> applicationArchive;

    private Collection<Archive<?>> auxiliaryArchives;

    private Archive<?> archiveForEnrichment;

    /**
     * @param applicationArchive
     *     The user defined {@link Archive}
     * @param auxiliaryArchives
     *     All extra library {@link Archive}s defined by extensions / core / frameworks.
     *
     * @deprecated
     */
    public TestDeployment(Archive<?> applicationArchive, Collection<Archive<?>> auxiliaryArchives) {
        this(null, applicationArchive, auxiliaryArchives);
    }

    /**
     * @param deploymentDescription
     *     The deployment that backs this TestDeployment
     * @param applicationArchive
     *     The user defined {@link Archive}
     * @param auxiliaryArchives
     *     All extra library {@link Archive}s defined by extensions / core / frameworks.
     */
    public TestDeployment(DeploymentDescription deploymentDescription, Archive<?> applicationArchive,
        Collection<Archive<?>> auxiliaryArchives) {
        if (applicationArchive == null) {
            throw new IllegalArgumentException("ApplicationArchive must be specified");
        }
        if (auxiliaryArchives == null) {
            throw new IllegalArgumentException("AuxiliaryArchives must be specified");
        }

        this.deploymentDescription = deploymentDescription;
        this.applicationArchive = applicationArchive;
        this.auxiliaryArchives = auxiliaryArchives;
    }

    public TargetDescription getTargetDescription() {
        return deploymentDescription == null ? null : deploymentDescription.getTarget();
    }

    public ProtocolDescription getProtocolDescription() {
        return deploymentDescription == null ? null : deploymentDescription.getProtocol();
    }

    public String getDeploymentName() {
        return deploymentDescription == null ? null : deploymentDescription.getName();
    }

    /**
     * Convenience method to lookup the user tagged archive for enriching.
     *
     * @return The tagged Archive or ApplicationArchive if none are tagged
     */
    public Archive<?> getArchiveForEnrichment() {
        if (archiveForEnrichment == null) {
            // TODO: Extend to EJBs once they are supported
            Map<ArchivePath, Node> nested = applicationArchive.getContent(Filters.include(".*\\.war"));
            if (!nested.isEmpty()) {
                for (ArchivePath path : nested.keySet()) {
                    try {
                        GenericArchive genericArchive = applicationArchive.getAsType(GenericArchive.class, path);
                        if (Testable.isArchiveToTest(genericArchive)) {
                            if (archiveForEnrichment != null) {
                                throw new UnsupportedOperationException("Multiple marked Archives found in "
                                    + applicationArchive.getName() + ". Can not determine which to enrich");
                            }
                            archiveForEnrichment = genericArchive;
                        }
                    } catch (IllegalArgumentException e) {
                        // no-op, Nested archive is not a ShrinkWrap archive.
                    }
                }
            }
        } else {
            archiveForEnrichment = applicationArchive;
        }
        return archiveForEnrichment;
    }

    public Archive<?> getApplicationArchive() {
        return applicationArchive;
    }

    public Collection<Archive<?>> getAuxiliaryArchives() {
        return auxiliaryArchives;
    }
}
