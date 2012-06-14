package org.jboss.arquillian.container.test.api;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * When testing an ear deployment containing multiple wars using the servlet protocol
 * this class allows to define the archive under test. This means that the test is running
 * in the context of this web module.
 * 
 * 
 * @author robert.panzer
 * @version $Revision: $
 */
public final class Testable {
    
    public static final ArchivePath MARKER_FILE_PATH = ArchivePaths.create("META-INF/arquillian.ArchiveUnderTest");

    private Testable() {}
    
    /**
     * Mark the given archive as the archive under test so that the test are running in its context when using the Servlet protocol.
     * 
     * <p>Usage Example:<br/>
     * <code><pre>
     * &#64;Deployment
     * public static EnterpriseArchive create() {
     *    EnterpriseArchive earArchive = ...
     *    WebArchive warArchive = ...
     *    earArchive.addAsModule( Testable.archiveToTest(warArchive) );
     *    return earArchive;
     * }
     * </pre></code>
     * @param archive
     * @return
     */
    public static WebArchive archiveToTest(WebArchive archive) {
        return archive.addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("arquillian.ArchiveUnderTest"));
    }
    
}
