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
package org.jboss.arquillian.config.impl.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.spi.ConfigurationPlaceholderResolver;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.impl.context.SuiteContextImpl;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * ConfigurationRegistrarTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationRegistrarTestCase extends AbstractManagerTestBase {

    @Mock
    private ServiceLoader serviceLoader;

    @Inject
    private Instance<Injector> injectorInst;

    @Inject
    private Instance<ArquillianDescriptor> descInst;

    private ConfigurationRegistrar registrar;

    static void validate(String property, String value, AssertCallback callback) {
        try {
            System.setProperty(property, value);
            callback.validate();
        } finally {
            System.clearProperty(property);
        }
    }

    @Before
    public void injectConfigurationRegistrar() {
        ConfigurationPlaceholderResolver configurationSysPropResolver = new SystemPropertiesConfigurationPlaceholderResolver();

        Mockito.when(serviceLoader.all(ConfigurationPlaceholderResolver.class))
            .thenReturn(Arrays.asList(configurationSysPropResolver));
        bind(SuiteScoped.class, ServiceLoader.class, serviceLoader);
        registrar = injectorInst.get().inject(new ConfigurationRegistrar());
    }

    @Test
    public void shouldBeAbleToLoadEmptyDefaultConfiguration() throws Exception {
        registrar.loadConfiguration(new ManagerStarted());
        ArquillianDescriptor desc = descInst.get();

        Assert.assertEquals(0, desc.getContainers().size());
        Assert.assertEquals(0, desc.getGroups().size());
        Assert.assertEquals(0, desc.getExtensions().size());
    }

    @Test
    public void shouldBeAbleToLoadConfiguredXMLFileResource() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                "registrar_tests/named_arquillian.xml",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        registrar.loadConfiguration(new ManagerStarted());
                        ArquillianDescriptor desc = descInst.get();

                        Assert.assertEquals(1, desc.getContainers().size());
                        Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                        // verify mode = class, override test will set it to suite
                        Assert.assertEquals("class", desc.getContainers().get(0).getMode());
                    }
                });
    }

    @Test
    public void shouldBeAbleToLoadConfiguredXMLClasspathResource() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                "registrar_tests/named_arquillian.xml",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        registrar.loadConfiguration(new ManagerStarted());
                        ArquillianDescriptor desc = descInst.get();

                        Assert.assertEquals(1, desc.getContainers().size());
                        Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                        // verify mode = class, override test will set it to suite
                        Assert.assertEquals("class", desc.getContainers().get(0).getMode());
                    }
                });
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingConfiguredXMLResource() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                "registrar_tests/named_arquillian_SHOULD_NOT_BE_FOUND_.xml",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        registrar.loadConfiguration(new ManagerStarted());
                    }
                });
    }

    @Test
    public void shouldBeAbleToLoadConfiguredPropertiesFileResource() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                "registrar_tests/named_arquillian.properties",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        registrar.loadConfiguration(new ManagerStarted());
                        ArquillianDescriptor desc = descInst.get();

                        Assert.assertEquals(1, desc.getContainers().size());
                        Assert.assertEquals("B", desc.getContainers().get(0).getContainerName());
                    }
                });
    }

    @Test
    public void shouldBeAbleToLoadConfiguredPropertiesClasspathResource() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                "registrar_tests/named_arquillian.properties",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        registrar.loadConfiguration(new ManagerStarted());
                        ArquillianDescriptor desc = descInst.get();

                        Assert.assertEquals(1, desc.getContainers().size());
                        Assert.assertEquals("B", desc.getContainers().get(0).getContainerName());
                        Assert.assertEquals("manual", desc.getContainers().get(0).getMode());
                    }
                });
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingConfiguredPropertiesResource() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                "registrar_tests/named_arquillian_SHOULD_NOT_BE_FOUND_.properties",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        registrar.loadConfiguration(new ManagerStarted());
                    }
                });
    }

    @Test
    public void shouldBeAbleToAddSystemProperties() throws Exception {
        validate(
                "arq.container.C.mode",
                "manual",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        registrar.loadConfiguration(new ManagerStarted());
                        ArquillianDescriptor desc = descInst.get();

                        Assert.assertEquals(1, desc.getContainers().size());
                        Assert.assertEquals("C", desc.getContainers().get(0).getContainerName());
                        Assert.assertEquals("manual", desc.getContainers().get(0).getMode());
                    }
                });
    }

    @Test
    public void shouldBeAbleToOverrideWithSystemProperties() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                "registrar_tests/named_arquillian.xml",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        ConfigurationRegistrarTestCase.validate(
                                "arq.container.A.mode",
                                "suite",
                                new AssertCallback() {
                                    @Override
                                    public void validate() {
                                        registrar.loadConfiguration(new ManagerStarted());
                                        ArquillianDescriptor desc = descInst.get();

                                        Assert.assertEquals(1, desc.getContainers().size());
                                        Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                                        Assert.assertEquals("suite", desc.getContainers().get(0).getMode());
                                    }
                                });
                    }
                });
    }

    @Test
    public void shouldBeAbleToAddToXMLWithProperties() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                "registrar_tests/named_arquillian.xml",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        ConfigurationRegistrarTestCase.validate(
                                ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                                "registrar_tests/named_arquillian.properties",
                                new AssertCallback() {
                                    @Override
                                    public void validate() {
                                        registrar.loadConfiguration(new ManagerStarted());
                                        ArquillianDescriptor desc = descInst.get();

                                        Assert.assertEquals(2, desc.getContainers().size());
                                        Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                                        Assert.assertEquals("B", desc.getContainers().get(1).getContainerName());
                                    }
                                });
                    }
                });
    }

    @Test
    public void shouldBeAbleToOverrideToXMLWithProperties() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                "registrar_tests/named_arquillian.xml",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        ConfigurationRegistrarTestCase.validate(
                                ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                                "registrar_tests/override_named_arquillian.properties",
                                new AssertCallback() {
                                    @Override
                                    public void validate() {
                                        registrar.loadConfiguration(new ManagerStarted());
                                        ArquillianDescriptor desc = descInst.get();

                                        Assert.assertEquals(1, desc.getContainers().size());
                                        Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                                        Assert.assertEquals("suite", desc.getContainers().get(0).getMode());
                                    }
                                });
                    }
                });
    }

    @Test
    public void shouldToOverrideToPropertiesWithSystemEnvironment() throws Exception {
        validate(
                ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                "registrar_tests/named_arquillian.xml",
                new AssertCallback() {
                    @Override
                    public void validate() {
                        ConfigurationRegistrarTestCase.validate(
                                ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                                "registrar_tests/override_named_arquillian.properties",
                                new AssertCallback() {
                                    @Override
                                    public void validate() {
                                        Map<String, String> envVars = new HashMap<String, String>();
                                        envVars.put("arq.container.A.mode", "none");
                                        registrar.setEnvironmentVariables(envVars);
                                        registrar.loadConfiguration(new ManagerStarted());

                                        ArquillianDescriptor desc = descInst.get();

                                        Assert.assertEquals(1, desc.getContainers().size());
                                        Assert.assertEquals("A", desc.getContainers().get(0).getContainerName());
                                        Assert.assertEquals("none", desc.getContainers().get(0).getMode());
                                    }
                                });
                    }
                });
    }

    @Test
    public void shouldBeAbleToOverrideDefaultProtocolTXMLWithPlaceholderReplace() throws Exception {
        validate("env.ENV1", "env1", new AssertCallback() {
            @Override
            public void validate() {
                ConfigurationRegistrarTestCase.validate("env.ENV3", "env3", new AssertCallback() {
                    @Override
                    public void validate() {

                        ConfigurationRegistrarTestCase.validate(
                                ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                                "registrar_tests/property_arquillian.xml",
                                new AssertCallback() {
                                    @Override
                                    public void validate() {
                                        ConfigurationRegistrarTestCase.validate(
                                                ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                                                "registrar_tests/property_arquillian.properties",
                                                new AssertCallback() {
                                                    @Override
                                                    public void validate() {
                                                        registrar.loadConfiguration(new ManagerStarted());
                                                        ArquillianDescriptor desc = descInst.get();

                                                        Assert.assertNotNull(desc.getDefaultProtocol());
                                                        Assert.assertEquals("X BBB X", desc.getDefaultProtocol().getProperty("bbb"));
                                                        Assert.assertEquals("X  X", desc.getDefaultProtocol().getProperty("bbb2"));
                                                    }
                                                });
                                    }
                                });
                    }
                });
            }
        });
    }

    @Test
    public void shouldBeAbleToOverrideContainersToXMLWithPlaceholderReplace() throws Exception {
        validate("env.ENV1", "env1", new AssertCallback() {
            @Override
            public void validate() {
                ConfigurationRegistrarTestCase.validate("env.ENV3", "env3", new AssertCallback() {
                    @Override
                    public void validate() {

                        ConfigurationRegistrarTestCase.validate(
                            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                            "registrar_tests/property_arquillian.xml",
                            new AssertCallback() {
                                @Override
                                public void validate() {
                                    ConfigurationRegistrarTestCase.validate(
                                        ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                                        "registrar_tests/property_arquillian.properties",
                                        new AssertCallback() {
                                            @Override
                                            public void validate() {
                                                registrar.loadConfiguration(new ManagerStarted());
                                                ArquillianDescriptor desc = descInst.get();

                                                Assert.assertEquals(1, desc.getContainers().size());
                                                Assert.assertNotNull(desc.getContainers().get(0));
                                                Assert.assertNotNull("Y AAA Y", desc.getContainers().get(0).getContainerProperty("aaa"));
                                                Assert.assertNotNull("Y  Y", desc.getContainers().get(0).getContainerProperty("aaa2"));

                                            }
                                        });
                                }
                            });
                    }
                });
            }
        });
    }

    @Test
    public void shouldBeAbleToOverrideExtensionsToXMLWithPlaceholderReplace() throws Exception {
        validate("env.ENV1", "env1", new AssertCallback() {
            @Override
            public void validate() {
                ConfigurationRegistrarTestCase.validate("env.ENV3", "env3", new AssertCallback() {
                    @Override
                    public void validate() {

                        ConfigurationRegistrarTestCase.validate(
                            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                            "registrar_tests/property_arquillian.xml",
                            new AssertCallback() {
                                @Override
                                public void validate() {
                                    ConfigurationRegistrarTestCase.validate(
                                        ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                                        "registrar_tests/property_arquillian.properties",
                                        new AssertCallback() {
                                            @Override
                                            public void validate() {
                                                registrar.loadConfiguration(new ManagerStarted());
                                                ArquillianDescriptor desc = descInst.get();

                                                Assert.assertEquals(1, desc.getExtensions().size());
                                                Assert.assertNotNull(desc.getExtensions().get(0));
                                                Assert.assertNotNull("Z DDD Z", desc.getExtensions().get(0).getExtensionProperty("ddd"));
                                                Assert.assertNotNull("Z  Z", desc.getExtensions().get(0).getExtensionProperty("ddd2"));
                                            }
                                        });
                                }
                            });
                    }
                });
            }
        });
    }

    @Test
    public void shouldBeAbleToOverrideGroupsToXMLWithPlaceholderReplace() throws Exception {
        validate("env.ENV1", "env1", new AssertCallback() {
            @Override
            public void validate() {
                ConfigurationRegistrarTestCase.validate("env.ENV3", "env3", new AssertCallback() {
                    @Override
                    public void validate() {

                        ConfigurationRegistrarTestCase.validate(
                            ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY,
                            "registrar_tests/property_arquillian.xml",
                            new AssertCallback() {
                                @Override
                                public void validate() {
                                    ConfigurationRegistrarTestCase.validate(
                                        ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY,
                                        "registrar_tests/property_arquillian.properties",
                                        new AssertCallback() {
                                            @Override
                                            public void validate() {
                                                registrar.loadConfiguration(new ManagerStarted());
                                                ArquillianDescriptor desc = descInst.get();

                                                Assert.assertEquals(2, desc.getGroups().size());
                                                Assert.assertNotNull(desc.getGroups().get(0));
                                                Assert.assertEquals(1, desc.getGroups().get(0).getGroupContainers().size());
                                                Assert.assertEquals("T EEE T", desc.getGroups().get(0).getGroupContainers().get(0).getContainerProperty("eee"));
                                                Assert.assertEquals("T  T", desc.getGroups().get(0).getGroupContainers().get(0).getContainerProperty("eee2"));
                                                Assert.assertEquals(1, desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().size());
                                                Assert.assertEquals("R FFF R", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("fff"));
                                                Assert.assertEquals("R  R", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("fff2"));
                                                Assert.assertEquals("env1", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("ggg1"));
                                                Assert.assertEquals("${env.ENV2}", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("ggg2"));
                                                Assert.assertEquals("G env1 G", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("ggg3"));
                                                Assert.assertEquals("env3 HHH", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("hhh1"));
                                                Assert.assertEquals("${env.ENV4} HHH", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("hhh2"));
                                                Assert.assertEquals("H env1 HHH H", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("hhh3"));
                                                Assert.assertEquals("env1  ${env.ENV2}", desc.getGroups().get(0).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("hhh4"));
                                                Assert.assertNotNull(desc.getGroups().get(1));
                                                Assert.assertEquals(1, desc.getGroups().get(1).getGroupContainers().size());
                                                Assert.assertEquals("WITHOUT ", desc.getGroups().get(1).getGroupContainers().get(0).getProtocols().get(0).getProtocolProperty("foo"));
                                            }
                                        });
                                }
                            });
                    }
                });
            }
        });
    }

    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(SuiteContextImpl.class);
    }

    @Override
    protected void startContexts(Manager manager) {
        super.startContexts(manager);
        manager.getContext(SuiteContext.class).activate();
    }

    public interface AssertCallback {
        void validate();
    }
}
