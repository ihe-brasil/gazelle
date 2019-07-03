package net.ihe.gazelle.jira.issue.search;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;

@Ignore
public class JiraIssuesUpdaterTest extends AbstractTestQueryJunit4 {
    private EntityManager em;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void updateFromWrongUrl() throws ParseException {
        JiraIssuesJsonClient updater = new JiraIssuesJsonClient("http://gazelle.ihe.net/jir", "TES,GZL");
        assertEquals("Cannot connect to jira server at url: http://gazelle.ihe.net/jir", updater.update());
    }

    @Test
    public void updateFromJira() throws ParseException {
        JiraIssuesJsonClient updater = new JiraIssuesJsonClient("http://gazelle.ihe.net/jira", "TES");
        assertEquals("Update completed", updater.update());
    }

    @Test
    public void updateFromJiraWithOneProjectFail() throws ParseException {
        JiraIssuesJsonClient updater = new JiraIssuesJsonClient("http://gazelle.ihe.net/jira", "TES,GZL");
        assertEquals("Jira issues in gazelle cannot be updated for the project: GZL", updater.update());
    }

    @Test
    public void updateFromJiraWithTwoProjectFail() throws ParseException {
        JiraIssuesJsonClient updater = new JiraIssuesJsonClient("http://gazelle.ihe.net/jira", "TES,GZL,AS");
        assertEquals("Jira issues in gazelle cannot be updated for the following projects: GZL, AS", updater.update());
    }

    @Test
    public void updateWrongJiraKey() throws ParseException {
        JiraIssuesJsonClient updater = new JiraIssuesJsonClient("http://gazelle.ihe.net/jira", "TOTO");
        assertEquals("There is no Jira project with the following key: TOTO", updater.update());
    }

    @Test
    public void updateWrongJiraKeys() throws ParseException {
        JiraIssuesJsonClient updater = new JiraIssuesJsonClient("http://gazelle.ihe.net/jira", "TOTO,TUTU");
        assertEquals("There are no Jira projects with the following keys: TOTO, TUTU", updater.update());
    }

    @Test
    public void updateOneTestFromJira() throws ParseException {
        JiraIssuesJsonClient updater = new JiraIssuesJsonClient("http://gazelle.ihe.net/jira", "TES");
        updater.update("94");
    }

    @Test
    public void updateOneTestFromJiraWithoutIssue() throws ParseException {
        JiraIssuesJsonClient updater = new JiraIssuesJsonClient("http://gazelle.ihe.net/jira", "TES");
        updater.update("95");
    }
}