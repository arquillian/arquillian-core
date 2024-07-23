/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.arquillian.integration.test.resource.injection;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class InContainerArquillianResourceTest extends AbstractArquillianResourceTest {

    @ArquillianResource
    protected Context context;

    @ArquillianResource
    protected InitialContext initialContext;

    @Test
    public void checkContext() throws Exception {
        Assert.assertNotNull("The Context should have been injected", context);
        final Object bm = context.lookup("java:comp/BeanManager");
        Assert.assertNotNull(bm);
    }

    @Test
    public void checkContextParameter(@ArquillianResource final Context context) throws Exception {
        Assert.assertNotNull("The Context should have been injected", context);
        final Object bm = context.lookup("java:comp/BeanManager");
        Assert.assertNotNull(bm);
    }

    @Test
    public void checkInitialContext() throws Exception {
        Assert.assertNotNull("The InitialContext should have been injected", initialContext);
        final Object bm = initialContext.lookup("java:comp/BeanManager");
        Assert.assertNotNull(bm);
    }

    @Test
    public void checkInitialContextParameter(@ArquillianResource final InitialContext initialContext) throws Exception {
        Assert.assertNotNull("The InitialContext should have been injected", initialContext);
        final Object bm = initialContext.lookup("java:comp/BeanManager");
        Assert.assertNotNull(bm);
    }
}
