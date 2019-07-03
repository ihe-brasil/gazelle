package net.ihe.gazelle.jira.issue.search;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.jira.JiraIssuesToTest;
import net.ihe.gazelle.tm.gazelletest.jira.JiraIssuesToTestQuery;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class JiraIssuesJsonClient {

    private static final String TEST_ID = "customfield_10861";
    //customfield_10861 = Test Id

    private static final Logger LOG = LoggerFactory.getLogger(JiraIssuesJsonClient.class);
    private JSONArray jsonIssues;
    private EntityManager em;

    private int startAt;
    private int maxResults;

    private String jiraTestIssueProject;
    private String jiraBaseUrl;

    public JiraIssuesJsonClient(String jiraBaseUrl, String jiraTestIssueProject) {
        super();
        em = EntityManagerService.provideEntityManager();
        startAt = 0;
        maxResults = 0;
        this.jiraTestIssueProject = jiraTestIssueProject;
        this.jiraBaseUrl = jiraBaseUrl;
    }

    private void fetchIssues(String projectKey, String testId) throws JiraUnknownProjectKey, ConnectException {
        String url = jiraBaseUrl + "/rest/api/2/search?jql=project+%3D+%22" + projectKey + "%22";
        if (testId != null) {
            url = url + addTestIdToJqlFilter(testId);
        }
        url = url + "&fields=id,key,status,summary,status," + TEST_ID + ",&startAt=" + startAt;

        Request request = new Request(url);

        ClientResponse<String> performRequest = request.performRequest();
        jsonIssues = parseJson(performRequest.getEntity());
    }

    private String addTestIdToJqlFilter(String testId) {
        return "+AND+%22Test+Id%22+%3D+%22" + testId + "%22";
    }

    public void cleanExistingLinks(String testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanExistingLinks");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        JiraIssuesToTestQuery query = new JiraIssuesToTestQuery();
        if (testId != null && "".equals(testId)) {
            query.test().id().eq(Integer.valueOf(testId));
        }
        List<JiraIssuesToTest> listDistinct = query.getListDistinct();
        for (JiraIssuesToTest jiraIssuesToTest : listDistinct) {
            em.remove(jiraIssuesToTest);
        }
    }

    public String update() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("update");
        }
        return update(null);
    }

    public String update(String testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("update");
        }
        String error = "";
        String connectionError = "";
        String[] split = jiraTestIssueProject.split(",");
        List<String> projectsInError = new ArrayList<String>();
        List<String> unknownJiraProjects = new ArrayList<String>();

        cleanExistingLinks(testId);
        for (String projectKey : split) {
            startAt = 0;
            try {
                do {
                    fetchIssues(projectKey, testId);
                    getIssues(projectKey);
                    prepareForNextFetch();
                } while (!jsonIssues.isEmpty());
            } catch (JiraMissingJsonField e) {
                projectsInError.add(projectKey);
                LOG.error("Jira issues in gazelle cannot be updated for the project: " + projectKey
                        + ", because the jira project is missing a " + e.getMessage() + " field");
            } catch (JiraUnknownProjectKey e) {
                unknownJiraProjects.add(projectKey);
                LOG.error("There is no Jira project with: " + projectKey + " key");
            } catch (ConnectException e) {
                LOG.error("Cannot connect to jira server: " + jiraBaseUrl);
                connectionError = "Cannot connect to jira server at url: " + jiraBaseUrl;
            }
        }
        error = connectionError;
        if (projectsInError.size() != 0) {
            if (projectsInError.size() == 1) {
                error = "Jira issues in gazelle cannot be updated for the project: " + projectsInError.get(0) + ". ";
            } else {
                error = "Jira issues in gazelle cannot be updated for the following projects: "
                        + StringUtils.join(projectsInError, ", ") + ". ";
            }
        }
        if (unknownJiraProjects.size() != 0) {
            if (unknownJiraProjects.size() == 1) {
                error = error + "There is no Jira project with the following key: " + unknownJiraProjects.get(0) + ".";
            } else {
                error = error + "There are no Jira projects with the following keys: "
                        + StringUtils.join(unknownJiraProjects, ", ") + ".";
            }
        }
        if (error.length() == 0) {
            error = "Update completed";
        }
        return error;
    }

    /*Jira api limits the amount of issues returned
     * We need to perform multiple queries to get all of them
     *
     * StartAt points to the first element that will be retrieved
     *
     * StartAt
     * |             contains 50 issues             |
     *
     *                                              New StartAt
     * |             contains 50 issues             |              next 50 issues             |
     *
     * */
    private void prepareForNextFetch() {
        startAt = startAt + maxResults;
    }

    private List<JiraIssuesToTest> getIssues(String projectKey) throws JiraMissingJsonField {
        List<JiraIssuesToTest> issues = new ArrayList<JiraIssuesToTest>();

        for (Object jsonIssue : jsonIssues) {
            JSONObject issue = (JSONObject) jsonIssue;
            JiraIssuesToTest jiraIssuesToTest;

            jiraIssuesToTest = getFieldsFromJson(issue);

            JiraIssuesToTest alreadyExists = jiraIssuesToTest.alreadyExists();
            if (alreadyExists != null) {
                jiraIssuesToTest.setId(alreadyExists.getId());
            }
            em.merge(jiraIssuesToTest);
            em.flush();
        }

        return issues;
    }

    private JSONArray parseJson(String jiraResponse) throws JiraUnknownProjectKey {
        JSONParser parser = new JSONParser();
        JSONArray jsonIssues;
        try {
            Object obj = parser.parse(jiraResponse);
            JSONObject json = (JSONObject) obj;
            if (json.containsKey("errorMessages")) {
                throw new JiraUnknownProjectKey(TEST_ID);
            }
            Object object = json.get("issues");
            maxResults = ((Long) json.get("maxResults")).intValue();
            jsonIssues = (JSONArray) object;
        } catch (ParseException e) {
            jsonIssues = new JSONArray();
        }
        return jsonIssues;
    }

    private JiraIssuesToTest getFieldsFromJson(JSONObject issue) throws JiraMissingJsonField {
        String key = (String) issue.get("key");

        JSONObject fields = (JSONObject) issue.get("fields");
        String summary = (String) fields.get("summary");

        if (!fields.containsKey(TEST_ID)) {
            throw new JiraMissingJsonField(TEST_ID);
        }
        Double testId = (Double) fields.get(TEST_ID);

        JSONObject statusField = (JSONObject) fields.get("status");
        String status = (String) statusField.get("name");
        Test test = em.find(Test.class, testId.intValue());

        return new JiraIssuesToTest(key, summary, status, test);
    }

    private static class JiraMissingJsonField extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 104451906192473235L;

        JiraMissingJsonField(String message) {
            super(message);
        }
    }

    private static class JiraUnknownProjectKey extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 104451906192473235L;

        JiraUnknownProjectKey(String message) {
            super(message);
        }
    }
}
