package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.configurations.model.HostQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jlabbe on 09/07/15.
 */
@Ignore
public class NetworkConfigurationManagerTestMigration extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected String getDb() {
        return "gazelle-dev";
    }

    @Override
    protected String getHbm2ddl() {
        return "update";
    }

    @Test
    public void displayDeleteButton() {
        NetworkConfigurationManager mgr = new NetworkConfigurationManager();
        HostQuery q = new HostQuery();
        q.hostname().eq("iasi1");
        Host uniqueResult = q.getUniqueResult();
        assertFalse(mgr.displayDeleteButton(uniqueResult));
        q = new HostQuery();
        q.hostname().eq("forcare21");
        uniqueResult = q.getUniqueResult();
        assertTrue(mgr.displayDeleteButton(uniqueResult));
    }

    protected boolean getShowSql() {
        return false;
    }
}