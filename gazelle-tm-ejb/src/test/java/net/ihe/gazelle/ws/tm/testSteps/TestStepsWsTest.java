package net.ihe.gazelle.ws.tm.testSteps;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

@Ignore
public class TestStepsWsTest extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void test() {
        TestStepsWs ws = new TestStepsWs();
        try {
            assertFalse(ws.getTestStep(1501628) == null);
        } catch (Exception e) {
        }
    }

    protected String getDb() {
        return "gazelle-k";
    }
}
