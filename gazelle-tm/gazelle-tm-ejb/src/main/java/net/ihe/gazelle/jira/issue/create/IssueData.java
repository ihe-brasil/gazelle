package net.ihe.gazelle.jira.issue.create;

import net.ihe.gazelle.tm.application.action.ApplicationManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

public class IssueData {

    private static final Logger LOG = LoggerFactory.getLogger(IssueData.class);
    private String summary;
    private String gazelleOID;
    private String permanentLink;
    private String description;
    private String gazelleUser;
    private float testId;
    private String version;
    private float revision;

    public IssueData() {
    }

    public IssueData(String summary, String gazelleOID, String permanentLink, String description, float testId,
                     String version, float revision) {
        super();
        this.summary = summary;
        this.gazelleOID = gazelleOID;
        this.permanentLink = permanentLink;
        this.description = description;
        this.testId = testId;
        this.version = version;
        this.revision = revision;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public String toJson() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("toJson");
        }
        JSONObject obj = new JSONObject();
        LinkedHashMap issueFields = new LinkedHashMap();

        obj.put("fields", issueFields);
        issueFields.put("customfield_10860", gazelleOID);
        issueFields.put("customfield_10861", testId);
        issueFields.put("customfield_10865", version);
        issueFields.put("customfield_10864", permanentLink);
        issueFields.put("customfield_10863", revision);
        issueFields.put("customfield_10866", gazelleUser);
        issueFields.put("project", getProjectKey());
        issueFields.put("summary", summary);
        issueFields.put("description", description);
        issueFields.put("issuetype", getIssueType());

        return obj.toJSONString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LinkedHashMap getIssueType() {
        LinkedHashMap issueType = new LinkedHashMap();
        issueType.put("name", "Bug");
        return issueType;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LinkedHashMap getProjectKey() {
        LinkedHashMap project = new LinkedHashMap();
        project.put("key", ApplicationManager.instance().getJiraReportTestIssueProject());
        return project;
    }

    public String getSummary() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSummary");
        }
        return summary;
    }

    public void setSummary(String summary) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSummary");
        }
        this.summary = summary;
    }

    public String getGazelleOID() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGazelleOID");
        }
        return gazelleOID;
    }

    public void setGazelleOID(String gazelleOID) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setGazelleOID");
        }
        this.gazelleOID = gazelleOID;
    }

    public String getPermanentLink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPermanentLink");
        }
        return permanentLink;
    }

    public void setPermanentLink(String permanentLink) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPermanentLink");
        }
        this.permanentLink = permanentLink;
    }

    public String getDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDescription");
        }
        return description;
    }

    public void setDescription(String description) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDescription");
        }
        this.description = description;
    }

    public float getTestId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestId");
        }
        return testId;
    }

    public void setTestId(float testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestId");
        }
        this.testId = testId;
    }

    public String getVersion() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getVersion");
        }
        return version;
    }

    public void setVersion(String version) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setVersion");
        }
        this.version = version;
    }

    public float getRevision() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRevision");
        }
        return revision;
    }

    public void setRevision(float revision) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRevision");
        }
        this.revision = revision;
    }

    public String getGazelleUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGazelleUser");
        }
        return gazelleUser;
    }

    public void setGazelleUser(String gazelleUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setGazelleUser");
        }
        this.gazelleUser = gazelleUser;
    }
}
