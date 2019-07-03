package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.tm.ws.data.domain.DomainsWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBException;

import static org.junit.Assert.assertEquals;

@Ignore
public class DomainsWsTest extends AbstractTestQueryJunit4 {

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
    public void returnsListOfAllDomains() throws JAXBException {
        DomainsWs ws = new DomainsWs();
        DomainsWrapper domains = ws.getDomains();
        assertEquals(16, domains.getKeywords().size());
    }
}
