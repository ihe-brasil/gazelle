package net.ihe.gazelle.jira.issue.search;

import net.ihe.gazelle.tm.application.action.ApplicationManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Name("jiraIssuesUpdater")
@Scope(ScopeType.PAGE)
public class JiraIssuesUpdater implements Serializable {

    private static final long serialVersionUID = 123131365132131L;

    private static final Logger LOG = LoggerFactory.getLogger(JiraIssuesUpdater.class);

    public void updateAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateAll");
        }
        ApplicationManager applicationManager = ApplicationManager.instance();
        JiraIssuesJsonClient jiraClient = new JiraIssuesJsonClient(applicationManager.getJiraUrl(),
                applicationManager.getJiraTestIssueProject());
        String error = jiraClient.update();
        FacesMessages.instance().add(StatusMessage.Severity.ERROR, error);
    }

    public void updateJiraFor(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateJiraFor");
        }
        ApplicationManager applicationManager = ApplicationManager.instance();
        JiraIssuesJsonClient jiraClient = new JiraIssuesJsonClient(applicationManager.getJiraUrl(),
                applicationManager.getJiraTestIssueProject());
        jiraClient.update(String.valueOf(testId));
    }
}
