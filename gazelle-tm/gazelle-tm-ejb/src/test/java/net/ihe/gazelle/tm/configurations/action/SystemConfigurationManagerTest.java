package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.preferences.PreferenceService;
import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.configurations.model.HostQuery;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingSessionQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PreferenceService.class})
@PowerMockIgnore({"org.*", "javax.*", "net.ihe.gazelle.*"})
@Ignore
public class SystemConfigurationManagerTest extends AbstractTestQueryJunit4 {

    private static final Logger LOG = LoggerFactory.getLogger(SystemConfigurationManagerTest.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.mockStatic(PreferenceService.class);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void test() {
        mockPreferenceService();
        HostQuery hQuery = new HostQuery();
        hQuery.id().eq(19118);
        Host host = hQuery.getUniqueResult();

        SystemInSessionQuery sQuery = new SystemInSessionQuery();
        sQuery.id().eq(1396);
        SystemInSession systemInSession = sQuery.getUniqueResult();
    }

    public void generateConfiguration() {
        mockPreferenceService();
        ConfigurationsOverview config = new ConfigurationsOverview();
        TestingSessionQuery query = new TestingSessionQuery();
        query.id().eq(32);

        TestingSession testingSession = query.getUniqueResult();
        config.generateConfigurations(null, null, testingSession);
    }

    private void mockPreferenceService() {
        Mockito.when(PreferenceService.getObject("is_product_registry")).thenReturn(false);
    }

    @Override
    protected String getDb() {
        return "gazelle-na";
    }
}
