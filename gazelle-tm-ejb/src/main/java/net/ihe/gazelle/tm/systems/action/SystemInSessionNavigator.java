package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.faces.RedirectException;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;

public class SystemInSessionNavigator {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SystemInSessionNavigator.class);
    private static final String NO_ACTIVE_SESSION = "No Active Session !!!!!!!";

    /**
     * Add a new system action (from the management list or from the menu bar) This operation is allowed for some granted users (check the security
     * .drl) (This does not add a button, but rather adds a
     * system !)
     *
     * @return String : JSF page to render
     */
    public String addNewSystemActionRedirect() {
        LOG.debug("addNewSystemActionRedirect");
        if (!Authorizations.REGISTRATION_EDITOR.isGranted()) {
            return null;
        }
        return "/systems/system/createSystemInSession.seam";
    }

    public String editSystemInSessionActionRedirect(SystemInSession sis, String selectedTab) {
        LOG.debug("editSystemInSessionActionRedirect");
        if (!Authorizations.REGISTRATION_EDITOR.isGranted()) {
            return null;
        }
        return "/systems/system/editSystemInSession.seam?id=" + sis.getId() + "&selectedTab=" + selectedTab;
    }

    public String editSystemInSessionSummaryActionRedirect(SystemInSession sis) {
        LOG.debug("editSystemInSessionSummaryActionRedirect");
        if (!Authorizations.REGISTRATION_EDITOR.isGranted()) {
            return null;
        }
        return "/systems/system/editSystemInSession.seam?id=" + sis.getId();
    }

    public String viewSystemInSession(SystemInSession inSystemSession) {
        LOG.debug("viewSystemInSession");
        if (!Authorizations.ALL.isGranted()) {
            return null;
        }
        return "/systems/system/showSystemInSession.seam?id=" + inSystemSession.getId();
    }

    public String viewSystemInSession(SystemInSession inSystemSession, String selectedTab) {
        LOG.debug("viewSystemInSession");
        if (!Authorizations.ALL.isGranted()) {
            return null;
        }
        return "/systems/system/showSystemInSession.seam?id=" + inSystemSession.getId() + "&selectedTab=" + selectedTab;
    }

    public String copySystemInSession() {
        LOG.debug("copySystemInSession");
        if (!Authorizations.ALL.isGranted()) {
            return null;
        }
        return "/systems/system/copySystemInSession.seam";
    }

    public String manageAllSystemActions() {
        LOG.debug("manageAllSystemActions");
        if (TestingSession.getSelectedTestingSession() == null) {
            LOG.error(NO_ACTIVE_SESSION);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.systems.error.noActivatedTestingSession");
        }
        return "/systems/listSystems.xhtml";
    }

    public String redirectToSystemInSessionPage() {
        LOG.debug("redirectToSystemInSessionPage");
        FacesContext fc = FacesContext.getCurrentInstance();

        String systemIdString = fc.getExternalContext().getRequestParameterMap().get("systemId");
        if ((systemIdString != null) && StringUtils.isNumeric(systemIdString)) {
            SystemInSessionQuery systemInSessionQuery = new SystemInSessionQuery();
            systemInSessionQuery.system().id().eq(Integer.valueOf(systemIdString));
            redirectToSystemInSessionPageDo(systemInSessionQuery);
            return null;
        }
        String systemInSessionIdString = fc.getExternalContext().getRequestParameterMap().get("systemInSessionId");
        if ((systemInSessionIdString != null) && StringUtils.isNumeric(systemInSessionIdString)) {
            SystemInSessionQuery systemInSessionQuery = new SystemInSessionQuery();
            systemInSessionQuery.id().eq(Integer.valueOf(systemInSessionIdString));
            redirectToSystemInSessionPageDo(systemInSessionQuery);
            return null;
        }

        String systemString = fc.getExternalContext().getRequestParameterMap().get("system");
        String testingSessionId = fc.getExternalContext().getRequestParameterMap().get("testingSessionId");
        if (systemString != null) {
            SystemInSessionQuery systemInSessionQuery = new SystemInSessionQuery();
            systemInSessionQuery.system().keyword().eq(systemString);
            if (testingSessionId != null) {
                systemInSessionQuery.testingSession().id().eq(Integer.parseInt(testingSessionId));
            }
            redirectToSystemInSessionPageDo(systemInSessionQuery);
            return null;
        }
        return "You have to specify a system attribute, like this : http://xxxxx/systemInSession.seam?system=XXX&testingSessionId=XX";
    }

    public void redirectToSystemInSessionPageDo(SystemInSessionQuery systemInSessionQuery) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("redirectToSystemInSessionPageDo");
        }
        SystemInSession systemInSession = systemInSessionQuery.getUniqueResult();
        if (systemInSession != null) {
            try {
                Redirect redirect = Redirect.instance();
                redirect.setViewId("/systems/system/showSystemInSession.seam");
                redirect.setParameter("id", systemInSession.getId());
                redirect.execute();
            } catch (RedirectException e) {
                LOG.error("" + e);
            }
        }
    }
}
