package net.ihe.gazelle.tm.gazelletest.action;

import junit.framework.Assert;
import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.junit.DatabaseManager;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOptionQuery;
import net.ihe.gazelle.tf.model.factories.ActorIntegrationProfileOptionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

@Ignore
public class ActorIntegrationProfileOptionTest extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        cleanData();
        super.tearDown();
    }

    private void cleanData() {
        ActorIntegrationProfileOptionFactory.cleanActorIntegrationProfileOptions();
    }

    private int get_ActorIntegrationProfileOption_Table_Size() {
        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
        return query.getList().size();
    }

    public void test_ActorIntegrationProfileOption_has_a_valid_factory() {
        int initSize = get_ActorIntegrationProfileOption_Table_Size();

        ActorIntegrationProfileOption Aipo = ActorIntegrationProfileOptionFactory
                .createActorIntegrationProfileOptionWithMandatoryFields();
        DatabaseManager.writeObject(Aipo);

        int endSize = get_ActorIntegrationProfileOption_Table_Size();
        Assert.assertEquals("1 ActorIntegrationProfileOption should be stored", 1, howManyNewItems(initSize, endSize));
    }

    private int howManyNewItems(int initSize, int endSize) {
        return endSize - initSize;
    }

    @Override
    protected String getDb() {
        return "gazelle-junit";
    }
}
