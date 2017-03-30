package ${enclosing_package};

    import javax.enterprise.inject.spi.BeanManager;
    import javax.inject.Inject;
    import static org.junit.Assert.*;
    import org.jboss.arquillian.api.Deployment;
    import org.jboss.arquillian.junit.Arquillian;
    import org.jboss.shrinkwrap.api.ShrinkWrap;
    import org.jboss.shrinkwrap.api.Archive;
    import org.jboss.shrinkwrap.api.asset.StringAsset;
    import org.junit.Test;
    import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class $ {
    enclosing_type
}
{
@Deployment
public static Archive<?> createTestArchive()
    {
    return ShrinkWrap.create(JavaArchive.class,"test.jar")
    .addManifestResource(new StringAsset(""),"beans.xml");
    }

@Inject BeanManager beanManager;

@Test
public void testCdiBootstrap()
    {
    assertNotNull(beanManager);
    assertFalse(beanManager.getBeans(BeanManager.class).isEmpty());
    }
    }
