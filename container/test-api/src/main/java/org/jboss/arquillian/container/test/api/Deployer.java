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
package org.jboss.arquillian.container.test.api;

import java.io.InputStream;

/**
 * The Deployer interface describes how to deploy manually controlled {@link Deployment}'s during test execution.
 * <p>
 * By default {@link Deployment}'s are managed by Arquillian, which mean Arquillian will perform the deploy and undeploy
 * operations for you automatically in the background. In some cases you might want a more fine grained control over the
 * deployment lifecycle. e.g. You might need to test some kind of auto discovery feature of your application that happens
 * during startup?
 * In this case you can define a {@link Deployment} to not be {@link Deployment#managed()} = false and
 * use the Deployer to manually deploy and undeploy them at your own will.
 * <p>
 * Usage Example:<br/>
 * <pre><code>
 * &#64;Deployment(name = "X", managed = false)
 * public static WebArchive create() {
 *      return ShrinkWrap.create(WebArchive.class);
 * }
 *
 * &#64;ArquillianResource
 * private Deployer deployer;
 *
 * &#64;Test &#64;RunAsClient
 * public void shouldDeployX() {
 *      deployer.deploy("X");
 * }
 * </code></pre>
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface Deployer {
    /**
     * Deploy the named deployment. <br/>
     * The operation will block until deploy is complete.
     * <p>
     * NOTE: If you want to run a test in a container, you cannot deploy a deployment from this test on the same container
     * that the test is
     * running in.
     * </p>
     * This is NOT correct for a test running in a container:<br/>
     * <pre><code>
     * &#64;Test &#64;OperatesOnDeployment("X")
     * public void deployTest() {
     *     deployer.deploy("X");
     * }
     * </code></pre>
     * <p>
     * If you run the test in this way for the very first deployment the test will be launched on the client side. If you
     * try to redeploy
     * a deployment from the container an exception will be thrown. In these cases please use the annotation {@link
     * RunAsClient} either
     * on the test method  or on the whole test class to be sure, that the test is running on the client side.
     * </p>
     * <p>
     * NOTE: You can still (re)deploy a deployment on a different container than the test is running in.
     * </p>
     * This IS correct for a test running in a container: <br/>
     * <pre><code>
     * &#64;Test &#64;OperatesOnDeployment("X")
     * public void deployTest() {
     *     deployer.deploy("Y");
     * }
     * </code></pre>
     *
     * @param name
     *     The name of the deployment
     */
    public void deploy(String name);

    /**
     * Get the Deployment byte content.
     *
     * @param name
     *     The name of the Deployment as defined by Deployment
     *
     * @return a Zipped Stream of the Archive
     */
    public InputStream getDeployment(String name);

    /**
     * UnDeploy a named deployment.<br/>
     * The operation will block until deploy is complete.
     *
     * @param name
     *     The name of the deployment
     */
    public void undeploy(String name);
}
