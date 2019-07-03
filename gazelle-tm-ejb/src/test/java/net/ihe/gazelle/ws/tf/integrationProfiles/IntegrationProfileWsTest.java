package net.ihe.gazelle.ws.tf.integrationProfiles;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.tf.ws.data.IntegrationProfileNamesWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@Ignore
public class IntegrationProfileWsTest extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void integrationProfilesWithReadyTests() {
        IntegrationProfileWs ws = new IntegrationProfileWs();
        try {
            IntegrationProfileNamesWrapper integrationProfile = ws.getIntegrationProfilesNamesWithTests("");
            assertEquals(171, integrationProfile.keywords.size());
        } catch (Exception e) {
        }
    }

    @Test
    public void integrationProfilesWithReadyTestsOfOneDomain() {
        IntegrationProfileWs ws = new IntegrationProfileWs();
        try {
            IntegrationProfileNamesWrapper integrationProfile = ws.getIntegrationProfilesNamesWithTests("EYE");
            assertEquals(5, integrationProfile.keywords.size());
        } catch (Exception e) {
        }
    }
}
