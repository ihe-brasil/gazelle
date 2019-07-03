package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.tf.ws.data.ActorsNameWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Ignore
public class ActorWsTest extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected String getDb() {
        return "gazelle-junit";
    }

    @Test
    public void testGetAllActorsNames() throws Exception {
        ActorWs ws = new ActorWs();
        ActorsNameWrapper actorsNames = ws.getActorsNames(null, null, null);
        assertEquals(296, actorsNames.getNames().size());
    }

    @Test
    public void testGetDentalActorsNames() throws Exception {
        ActorWs ws = new ActorWs();
        ActorsNameWrapper actorsNames = ws.getActorsNames("DENT", null, null);
        assertEquals(2, actorsNames.getNames().size());
    }

    @Test
    public void testGetITIActorsNames() throws Exception {
        ActorWs ws = new ActorWs();
        ActorsNameWrapper actorsNames = ws.getActorsNames("ITI", null, null);
        assertEquals(92, actorsNames.getNames().size());
    }

    @Test
    public void testGetActorsNamesWithAuditMessage() throws Exception {
        ActorWs ws = new ActorWs();
        ActorsNameWrapper actorsNames = ws.getActorsNamesWithAuditMessage("ITI", null, null);
        assertEquals(50, actorsNames.getNames().size());
    }

    @Test
    public void testGetActorsNamesWithAuditMessageAndITI18() throws Exception {
        ActorWs ws = new ActorWs();
        ActorsNameWrapper actorsNames = ws.getActorsNamesWithAuditMessage(null, null, "ITI-18");
        System.out.println(actorsNames);
        assertEquals(2, actorsNames.getNames().size());
    }
}

