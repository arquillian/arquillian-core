/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.test.impl.client.deployment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PrivilegedAction;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.impl.client.deployment.command.DeployDeploymentCommand;
import org.jboss.arquillian.container.test.impl.client.deployment.command.GetDeploymentCommand;
import org.jboss.arquillian.container.test.impl.client.deployment.command.UnDeployDeploymentCommand;
import org.jboss.arquillian.container.test.spi.command.CommandService;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;

import static java.security.AccessController.doPrivileged;

/**
 * ContainerDeployer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerDeployer implements Deployer {
    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Override
    public void deploy(final String name) {
        doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                getCommandService().execute(new DeployDeploymentCommand(name));
                return null;
            }
        });
    }

    @Override
    public void undeploy(final String name) {
        doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                getCommandService().execute(new UnDeployDeploymentCommand(name));
                return null;
            }
        });
    }

    @Override
    public InputStream getDeployment(final String name) {
        return doPrivileged(new PrivilegedAction<InputStream>() {
            public InputStream run() {
                return new ByteArrayInputStream(
                    getCommandService().execute(
                        new GetDeploymentCommand(name)));
            }
        });
    }

    private CommandService getCommandService() {
        ServiceLoader loader = serviceLoader.get();
        if (loader == null) {
            throw new IllegalStateException("No " + ServiceLoader.class.getName() + " found in context");
        }
        CommandService service = loader.onlyOne(CommandService.class);
        if (service == null) {
            throw new IllegalStateException("No " + CommandService.class.getName() + " found in context");
        }
        return service;
    }
}
