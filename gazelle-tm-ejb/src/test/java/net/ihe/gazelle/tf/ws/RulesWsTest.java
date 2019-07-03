package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.tf.ws.data.RuleWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBException;

import static org.junit.Assert.assertEquals;

@Ignore
public class RulesWsTest extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void getRuleById() throws JAXBException {
        RulesWs ws = new RulesWs();
        RuleWrapper rule = ws.getRule("1231");
        assertEquals(1231, rule.getId());
    }
}
