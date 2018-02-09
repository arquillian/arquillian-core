/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.config.impl.extension;

import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.impl.AssertXPath;
import org.jboss.arquillian.config.spi.ConfigurationPlaceholderResolver;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.impl.context.SuiteContextImpl;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test Cases to ensure that the arquillian.xml loaded by
 * {@link ConfigurationRegistrar} has EL expressions applied
 * for system property replacement
 * <p>
 * ARQ-148
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class SyspropReplacementInArqXmlTestCase extends AbstractReplacementInArqXmlTestBase {

    /**
     * Name of the arquillian.xml to test
     */
    private static final String NAME_ARQ_XML = "arquillian_sysprop.xml";

    /**
     * Name of the system property for EL expressions
     */
    private static final String SYSPROP_ARQ_CONTAINER = "arquillian.container";

    private static final String VALUE_EL_OVERRIDE = "ALR";

    /**
     * Sets the name of the arquillian.xml under test
     */
    @BeforeClass
    public static void setSysprops() {
        // Set a sysprop to denote the name of the arquillian.xml under test
        System.setProperty(ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY, NAME_ARQ_XML);

        // Set a sysprop to override the name of the qualifier
        System.setProperty(SYSPROP_ARQ_CONTAINER, VALUE_EL_OVERRIDE);
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void clearSysprops() {
        System.clearProperty(ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY);
        System.clearProperty(SYSPROP_ARQ_CONTAINER);
    }

    /**
     * Ensures that we can load an arquillian.xml and perform sysprop
     * EL replacement upon it.
     */
    @Test
    public void syspropReplacementInArqXml() throws Exception {

        // when
        final String xml = desc.get().exportAsString();

        // then
        AssertXPath.assertXPath(xml, "/arquillian/container/@qualifier", VALUE_EL_OVERRIDE);
    }

}
