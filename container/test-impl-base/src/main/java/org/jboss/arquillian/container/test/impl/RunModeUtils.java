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
package org.jboss.arquillian.container.test.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.impl.client.protocol.local.LocalProtocol;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

/**
 * RunModeUtils
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
public final class RunModeUtils {
    private static Logger log = Logger.getLogger(RunModeUtils.class.getName());

    private RunModeUtils() {
    }

    /**
     * Returns if the given test should run as client.
     * <p>
     * Verify @Deployment.testable vs @RunAsClient on Class or Method level
     */
    public static boolean isRunAsClient(Deployment deployment, TestClass testClass, Method testMethod) {
        return isRunAsClient(deployment, isAnnotatedRunAsClient(testClass, testMethod));
    }

    /**
     * Returns if the test the given event belongs to should run as client.
     * <p>
     * Unlike {@link #isRunAsClient(Deployment, TestClass, Method)}, this also honours @RunAsClient on the
     * {@code @Nested} classes enclosing the test method, which the outermost {@link TestEvent#getTestClass()} does not
     * reflect.
     */
    public static boolean isRunAsClient(Deployment deployment, TestEvent event) {
        return isRunAsClient(deployment, isAnnotatedRunAsClient(event));
    }

    private static boolean isRunAsClient(Deployment deployment, boolean annotatedRunAsClient) {
        boolean runAsClient = true;
        if (deployment != null) {
            runAsClient = deployment.getDescription().testable() ? false : true;
            runAsClient = deployment.isDeployed() ? runAsClient : true;

            if (annotatedRunAsClient) {
                runAsClient = true;
            }
        }
        return runAsClient;
    }

    /**
     * Returns if the given test should run as client and also checks for a confusing use case, when the test is not
     * intended to be run as a client test - in this case logs a warning. see: ARQ-1937
     */
    public static boolean isRunAsClientAndCheck(Deployment deployment, TestClass testClass, Method testMethod) {
        return isRunAsClientAndCheck(deployment, testClass, testMethod, isAnnotatedRunAsClient(testClass, testMethod));
    }

    /**
     * Returns if the test the given event belongs to should run as client and also checks for a confusing use case,
     * like {@link #isRunAsClientAndCheck(Deployment, TestClass, Method)}, honouring @RunAsClient on enclosing
     * {@code @Nested} classes like {@link #isRunAsClient(Deployment, TestEvent)}.
     */
    public static boolean isRunAsClientAndCheck(Deployment deployment, TestEvent event) {
        return isRunAsClientAndCheck(deployment, event.getTestClass(), event.getTestMethod(),
            isAnnotatedRunAsClient(event));
    }

    private static boolean isRunAsClientAndCheck(Deployment deployment, TestClass testClass, Method testMethod,
        boolean annotatedRunAsClient) {
        boolean runAsClient = isRunAsClient(deployment, annotatedRunAsClient);

        if (runAsClient && deployment == null) {
            Method[] methods = testClass.getMethods(org.jboss.arquillian.container.test.api.Deployment.class);
            if (methods.length > 0) {
                if (!annotatedRunAsClient) {
                    OperateOnDeployment onDeployment = testClass.getAnnotation(OperateOnDeployment.class);
                    String deploymentName = onDeployment == null ? "_DEFAULT_" : onDeployment.value();

                    for (Method m : methods) {
                        org.jboss.arquillian.container.test.api.Deployment deploymentAnnotation =
                            m.getAnnotation(org.jboss.arquillian.container.test.api.Deployment.class);

                        if (deploymentAnnotation.name().equals(deploymentName) && deploymentAnnotation.testable()) {
                            log.warning(
                                "The test method "
                                    + testClass.getJavaClass().getCanonicalName()
                                    + "#"
                                    + testMethod.getName()
                                    + " will run on the client side,because the "
                                    + deploymentName
                                    + " deployment is not deployed."
                                    + " Please deploy the deployment or mark the test as a client test");
                        }
                    }
                }
            }
        }
        return runAsClient;
    }

    private static boolean isAnnotatedRunAsClient(TestClass testClass, Method testMethod) {
        return testMethod.isAnnotationPresent(RunAsClient.class) || testClass.isAnnotationPresent(RunAsClient.class);
    }

    private static boolean isAnnotatedRunAsClient(TestEvent event) {
        if (isAnnotatedRunAsClient(event.getTestClass(), event.getTestMethod())) {
            return true;
        }
        // The test class of the event is the outermost class; walk the @Nested classes between it and the test instance
        Class<?> clazz = event.getTestInstance().getClass();
        while (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            if (clazz.isAnnotationPresent(RunAsClient.class)) {
                return true;
            }
            clazz = clazz.getEnclosingClass();
        }
        return false;
    }

    /**
     * Check if this Container DEFAULTs to the Local protocol.
     * <p>
     * Hack to get around ARQ-391
     *
     * @return true if DeployableContianer.getDefaultProtocol == Local
     */
    public static boolean isLocalContainer(Container container) {
        if (
            container == null ||
                container.getDeployableContainer() == null ||
                container.getDeployableContainer().getDefaultProtocol() == null) {
            return false;
        }
        if (LocalProtocol.NAME.equals(container.getDeployableContainer().getDefaultProtocol().getName())) {
            return true;
        }
        return false;
    }
}
