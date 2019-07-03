package net.ihe.gazelle.jira.issue.create;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.core.util.Base64;
import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.jira.issue.search.JiraIssuesUpdater;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.messages.MessageManager;
import net.ihe.gazelle.tm.messages.SimpleMessageSource;
import net.ihe.gazelle.users.model.User;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JiraManager {

    private final static Logger LOG = LoggerFactory.getLogger(JiraManager.class);
    private final JiraClientCallback callback;
    private IssueData issue;

    public JiraManager(JiraClientCallback callback) {
        issue = new IssueData();
        this.callback = callback;
    }

    public void createIssue(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createIssue");
        }
        if (test != null) {

            issue.setGazelleOID(ApplicationPreferenceManager.getStringValue("app_instance_oid"));
            issue.setPermanentLink(
                    ApplicationPreferenceManager.instance().getApplicationUrl() + "test.seam?id=" + test.getId());
            issue.setTestId(test.getId());
            issue.setVersion(test.getVersion());
            issue.setGazelleUser(User.loggedInUser().getUsername());

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            List<Number> revNumbers = auditReader.getRevisions(Test.class, test.getId());
            issue.setRevision((Integer) revNumbers.get(revNumbers.size() - 1));

            sendJira(test.getKeyword());
            JiraIssuesUpdater jiu = new JiraIssuesUpdater();
            jiu.updateJiraFor(test.getId());
        }
    }

    public void createIssue(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createIssue");
        }
        if (testInstance != null) {
            Test test = testInstance.getTest();

            issue.setGazelleOID(ApplicationPreferenceManager.getStringValue("app_instance_oid"));
            issue.setPermanentLink(
                    ApplicationPreferenceManager.instance().getApplicationUrl() + "test.seam?id=" + test.getId());
            issue.setTestId(test.getId());
            issue.setGazelleUser(User.loggedInUser().getUsername());

            if (testInstance.getTestVersion() != null) {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                AuditReader auditReader = AuditReaderFactory.get(entityManager);
                Test testVer = auditReader.find(Test.class, test.getId(), testInstance.getTestVersion());
                //Test revision is stored in testVersion field of a test instance
                issue.setRevision(testInstance.getTestVersion());
                issue.setVersion(testVer.getVersion());
            }

            String description = issue.getDescription();
            description = description + "\n" + ApplicationPreferenceManager.instance().getApplicationUrl()
                    + "testing/test/test/TestInstance.seam?id=" + testInstance.getId();
            issue.setDescription(description);

            sendJira(test.getKeyword());
        } else {

        }
    }

    private void sendJira(String testKeyword) {
        try {
            String auth = new String(Base64.encode("test_issue_reporter:gazelle66"), StandardCharsets.UTF_8);
            String jiraResult = JiraRestClient.createIssue(auth, issue);
            JSONParser parser = new JSONParser();
            String issueUrl;
            String issueKey = "";
            try {
                JSONObject obj = (JSONObject) parser.parse(jiraResult);
                issueKey = (String) obj.get("key");
                issueUrl = ApplicationManager.instance().getJiraUrl() + "/browse/" + issueKey;
            } catch (ParseException e) {
                issueUrl = "";
                issueKey = "";
                throw new IllegalArgumentException(e);
            }

            FacesMessages.instance()
                    .add(StatusMessage.Severity.INFO, issue.getSummary() + " successfully created, <a class='alert-link' target='_blank' href='"
                            + issueUrl + "'>" + issueKey + "</a>");
            callback.issueCreatedCallback();
        } catch (AuthenticationException e) {
            LOG.error(e.getMessage());
            MessageManager.addMessage(SimpleMessageSource.INSTANCE, User.loggedInUser(), "jlabbe",
                    "Jira issue: " + issue.getSummary() + "On test: " + testKeyword
                            + " could not be created due to authentication against jira", null);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, issue.getSummary() + " could not be created");
        } catch (IllegalArgumentException e) {
            LOG.error("Error trying to post issue on jira, content:" + issue.toJson());
            LOG.error(e.getMessage());
            MessageManager.addMessage(SimpleMessageSource.INSTANCE, User.loggedInUser(), "jlabbe",
                    "Jira issue: " + issue.getSummary() + "On test: " + testKeyword + " could not be created, details: "
                            + e.getMessage(), null);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, issue.getSummary() + " could not be created");
        } catch (ClientHandlerException e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, issue.getSummary() + " could not be created");
            MessageManager.addMessage(SimpleMessageSource.INSTANCE, User.loggedInUser(), "jlabbe",
                    "Jira issue: " + issue.getSummary() + "On test: " + testKeyword
                            + " could not be created, could be due to a bad configuration in preferences", null);
            LOG.error(e.getMessage());
        }
    }

    public IssueData getIssue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIssue");
        }
        return issue;
    }

    public void setIssue(IssueData issue) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIssue");
        }
        this.issue = issue;
    }
}
