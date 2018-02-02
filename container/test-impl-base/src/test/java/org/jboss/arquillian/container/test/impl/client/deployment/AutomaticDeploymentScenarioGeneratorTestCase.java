package org.jboss.arquillian.container.test.impl.client.deployment;

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.container.test.api.DeploymentConfiguration;
import org.jboss.arquillian.container.test.spi.client.deployment.AutomaticDeployment;
import org.jboss.arquillian.container.test.spi.util.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutomaticDeploymentScenarioGeneratorTestCase {

    @Mock
    AutomaticDeploymentScenarioGenerator.AutomaticDeploymentLocator automaticDeploymentLocator;

    @Mock
    AutomaticDeployment automaticDeployment;

    @Mock
    ServiceLoader serviceLoader;

    @Before
    public void setUpTest() {

        DeploymentConfiguration.DeploymentContentBuilder content = new DeploymentConfiguration.DeploymentContentBuilder(ShrinkWrap.create(
            JavaArchive.class));

        when(automaticDeployment.generateDeploymentScenario(any(TestClass.class))).thenReturn(content.get());

        final List<AutomaticDeployment> deploymentContents = new ArrayList<AutomaticDeployment>();
        deploymentContents.add(automaticDeployment);

        when(automaticDeploymentLocator.find()).thenReturn(serviceLoader);
        when(serviceLoader.iterator()).thenReturn(deploymentContents.iterator());
    }

    @Test
    public void shouldGetDeploymentDescriptorFromSpi() {

        // given
        final AutomaticDeploymentScenarioGenerator automaticDeploymentScenarioGenerator = new AutomaticDeploymentScenarioGenerator();
        automaticDeploymentScenarioGenerator.automaticDeploymentLocator = automaticDeploymentLocator;

        // when

        final List<DeploymentDescription> deploymentDescriptions =
            automaticDeploymentScenarioGenerator.generate(new TestClass(EmptyTest.class));

        // then
        Assert.assertTrue(deploymentDescriptions.get(0).isArchiveDeployment());

    }

    @Test
    public void shouldRunBeforeDeploymentMethods() {

        // given
        final AutomaticDeploymentScenarioGenerator automaticDeploymentScenarioGenerator = new AutomaticDeploymentScenarioGenerator();
        automaticDeploymentScenarioGenerator.automaticDeploymentLocator = automaticDeploymentLocator;

        // when
        final List<DeploymentDescription> deploymentDescriptions =
            automaticDeploymentScenarioGenerator.generate(new TestClass(BeforeDeploymentTest.class));

        // then
        Assert.assertTrue(deploymentDescriptions.get(0).isArchiveDeployment());
        Assert.assertTrue(deploymentDescriptions.get(0).getArchive().contains("hello.txt"));

    }

    private static class BeforeDeploymentTest {
        @BeforeDeployment
        public static Archive addDeploymentContent(Archive archive) {
            archive.add(new StringAsset("Hello"), "hello.txt");
            return archive;
        }
    }

    private static class EmptyTest {}

}
