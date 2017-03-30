/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.spi.client.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.shrinkwrap.api.Archive;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DeploymentScenario {
    private final List<Deployment> deployments;

    public DeploymentScenario() {
        this.deployments = new ArrayList<Deployment>();
    }

    public DeploymentScenario addDeployment(DeploymentDescription deployment) {
        Validate.notNull(deployment, "Deployment must be specified");
        validateNotSameNameAndTypeOfDeployment(deployment);
        validateNotSameArchiveAndSameTarget(deployment);

        this.deployments.add(new Deployment(deployment));
        return this;
    }

    public Set<TargetDescription> targets() {
        Set<TargetDescription> targets = new HashSet<TargetDescription>();
        for (Deployment dep : deployments) {
            targets.add(dep.getDescription().getTarget());
        }
        return targets;
    }

    public Set<ProtocolDescription> protocols() {
        Set<ProtocolDescription> protocols = new HashSet<ProtocolDescription>();
        for (Deployment dep : deployments) {
            protocols.add(dep.getDescription().getProtocol());
        }
        return protocols;
    }

    /**
     * Get a {@link DeploymentDescription} with a specific name if it exists.
     *
     * @param target
     *     The name of the {@link DeploymentDescription}
     *
     * @return Defined Deployment or null if not found.
     */
    public Deployment deployment(DeploymentTargetDescription target) {
        Validate.notNull(target, "Target must be specified");
        if (DeploymentTargetDescription.DEFAULT.equals(target)) {
            return findDefaultDeployment();
        }
        return findMatchingDeployment(target);
    }

    public List<Deployment> managedDeploymentsInDeployOrder() {
        List<Deployment> managedDeployment = new ArrayList<Deployment>();
        for (Deployment deployment : deployments) {
            DeploymentDescription desc = deployment.getDescription();
            if (desc.managed()) {
                managedDeployment.add(deployment);
            }
        }
        Collections.sort(managedDeployment, new Comparator<Deployment>() {
            public int compare(Deployment o1, Deployment o2) {
                return new Integer(o1.getDescription().getOrder()).compareTo(o2.getDescription().getOrder());
            }
        });

        return Collections.unmodifiableList(managedDeployment);
    }

    public List<Deployment> deployedDeploymentsInUnDeployOrder() {
        List<Deployment> managedDeployment = new ArrayList<Deployment>();
        for (Deployment deployment : deployments) {
            DeploymentDescription desc = deployment.getDescription();
            if (desc.managed() || deployment.isDeployed()) {
                managedDeployment.add(deployment);
            }
        }
        Collections.sort(managedDeployment, new Comparator<Deployment>() {
            public int compare(Deployment o1, Deployment o2) {
                return new Integer(o2.getDescription().getOrder()).compareTo(o1.getDescription().getOrder());
            }
        });

        return Collections.unmodifiableList(managedDeployment);
    }

    /**
     * Get all {@link DeploymentDescription} defined to be deployed during Test startup for a specific {@link
     * TargetDescription} ordered.
     *
     * @param target
     *     The Target to filter on
     *
     * @return A List of found {@link DeploymentDescription}. Will return a empty list if none are found.
     */
    public List<Deployment> startupDeploymentsFor(TargetDescription target) {
        Validate.notNull(target, "Target must be specified");
        List<Deployment> startupDeployments = new ArrayList<Deployment>();
        for (Deployment deployment : deployments) {
            DeploymentDescription desc = deployment.getDescription();
            if (desc.managed() && target.equals(desc.getTarget())) {
                startupDeployments.add(deployment);
            }
        }
        // sort them by order
        Collections.sort(startupDeployments, new Comparator<Deployment>() {
            public int compare(Deployment o1, Deployment o2) {
                return new Integer(o1.getDescription().getOrder()).compareTo(o2.getDescription().getOrder());
            }
        });

        return Collections.unmodifiableList(startupDeployments);
    }

    public List<Deployment> deploymentsInError() {
        List<Deployment> result = new ArrayList<Deployment>();
        for (Deployment dep : this.deployments) {
            if (dep.hasDeploymentError()) {
                result.add(dep);
            }
        }
        return result;
    }

    public List<Deployment> deployedDeployments() {
        List<Deployment> result = new ArrayList<Deployment>();
        for (Deployment dep : this.deployments) {
            if (dep.isDeployed()) {
                result.add(dep);
            }
        }
        return result;
    }

    /**
     * @return the deployments
     */
    public List<Deployment> deployments() {
        return Collections.unmodifiableList(deployments);
    }

    /**
     * @return
     */
    private Deployment findDefaultDeployment() {
        if (deployments.size() == 1) {
            return deployments.get(0);
        } else if (deployments.size() > 1) {
            // if there are only one Archive deployment, default to it
            List<Deployment> archiveDeployments = archiveDeployments(deployments);
            if (archiveDeployments.size() == 1) {
                return archiveDeployments.get(0);
            }

            // if there is only one managed deployment, default to it
            List<Deployment> managedDeployments = managedDeployments(deployments);
            if (managedDeployments.size() == 1) {
                return managedDeployments.get(0);
            }

            // if there are only one DEFAULT deployment, default to it, else default to the  DEFAULT Archive
            List<Deployment> defaultDeployments = defaultDeployments(deployments);
            if (defaultDeployments.size() == 1) {
                return defaultDeployments.get(0);
            } else if (defaultDeployments.size() > 1) {
                List<Deployment> defaultArchiveDeployments = archiveDeployments(defaultDeployments);
                return defaultArchiveDeployments.get(0);
            }
        }
        return null;
    }

    /**
     * Filters the List of Deployments and returns the ones that are Managed deployments.
     *
     * @param deployments
     *     List to filter
     *
     * @return Filtered list
     */
    private List<Deployment> managedDeployments(List<Deployment> deployments) {
        List<Deployment> managed = new ArrayList<Deployment>();
        for (Deployment deployment : deployments) {
            if (deployment.getDescription().managed()) {
                managed.add(deployment);
            }
        }
        return managed;
    }

    /**
     * Filters the List of Deployments and returns the ones that are DEFAULT deployments.
     *
     * @param deployments
     *     List to filter
     *
     * @return Filtered list
     */
    private List<Deployment> defaultDeployments(List<Deployment> deployments) {
        List<Deployment> defaults = new ArrayList<Deployment>();
        for (Deployment deployment : deployments) {
            if (deployment.getDescription().getName().equals(DeploymentTargetDescription.DEFAULT.getName())) {
                defaults.add(deployment);
            }
        }
        return defaults;
    }

    /**
     * Filters the List of Deployments and returns the ones that are Archive deployments.
     *
     * @param deployments
     *     List to filter
     *
     * @return Filtered list
     */
    private List<Deployment> archiveDeployments(List<Deployment> deployments) {
        List<Deployment> archives = new ArrayList<Deployment>();
        for (Deployment deployment : deployments) {
            if (deployment.getDescription().isArchiveDeployment()) {
                archives.add(deployment);
            }
        }
        return archives;
    }

    /**
     * Validation, names except DEFAULT should be unique. See constructor
     */
    private Deployment findMatchingDeployment(DeploymentTargetDescription target) {
        List<Deployment> matching = findMatchingDeployments(target);
        if (matching.size() == 0) {
            return null;
        }
        if (matching.size() == 1) {
            return matching.get(0);
        }

        // if multiple Deployment of different Type, we get the Archive Deployment
        return archiveDeployments(matching).get(0);
    }

    private List<Deployment> findMatchingDeployments(DeploymentTargetDescription target) {
        List<Deployment> matching = new ArrayList<Deployment>();
        for (Deployment deployment : deployments) {
            if (deployment.getDescription().getName().equals(target.getName())) {
                matching.add(deployment);
            }
        }
        return matching;
    }

    /**
     * Validate that a deployment of same type is not already added
     */
    private void validateNotSameNameAndTypeOfDeployment(DeploymentDescription deployment) {
        for (Deployment existing : deployments) {
            if (existing.getDescription().getName().equals(deployment.getName())) {
                if (
                    (existing.getDescription().isArchiveDeployment() && deployment.isArchiveDeployment()) ||
                        (existing.getDescription().isDescriptorDeployment() && deployment.isDescriptorDeployment())) {
                    throw new IllegalArgumentException("Can not add multiple " +
                        Archive.class.getName() + " deployments with the same name: " + deployment.getName());
                }
            }
        }
    }

    /**
     * Validate that a deployment with a archive of the same name does not have the same taget
     */
    private void validateNotSameArchiveAndSameTarget(DeploymentDescription deployment) {
        if (!deployment.isArchiveDeployment()) {
            return;
        }
        for (Deployment existing : archiveDeployments(deployments)) {
            if (existing.getDescription().getArchive().getName().equals(deployment.getArchive().getName())) {
                if (existing.getDescription().getTarget().equals(deployment.getTarget())) {
                    throw new IllegalArgumentException("Can not add multiple "
                        +
                        Archive.class.getName()
                        + " archive deployments with the same archive name "
                        + deployment.getName()
                        +
                        " that target the same target "
                        + deployment.getTarget());
                }
            }
        }
    }
}