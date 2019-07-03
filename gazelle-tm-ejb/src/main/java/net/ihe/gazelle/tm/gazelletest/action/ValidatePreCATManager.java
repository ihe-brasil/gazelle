package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.model.AuditedObject;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.bean.InfoCookies;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestStatus;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipantsQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingType;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Name("validatePreCATManager")
@Scope(ScopeType.PAGE)
public class ValidatePreCATManager implements Serializable, QueryModifier<TestInstanceParticipants> {

    private static final long serialVersionUID = -4985738996783073135L;
    private static final Logger LOG = LoggerFactory.getLogger(ValidatePreCATManager.class);
    private Filter<TestInstanceParticipants> filter = null;
    private FilterDataModel<TestInstanceParticipants> listTestInstanceParticipants;
    private boolean mainPreCatPage;

    public String getShortcutName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getShortcutName");
        }
        return CookiesPreset.instance().getShortcutName();
    }

    public void setShortcutName(String shortcutName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShortcutName");
        }
        CookiesPreset.instance().setShortcutName(shortcutName);
    }

    public Filter<TestInstanceParticipants> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

            filter = new Filter<TestInstanceParticipants>(getCriterions(), requestParameterMap);
        }
        return filter;
    }

    private HQLCriterionsForFilter<TestInstanceParticipants> getCriterions() {
        TestInstanceParticipantsQuery query = new TestInstanceParticipantsQuery();
        HQLCriterionsForFilter<TestInstanceParticipants> criterions = query.getHQLCriterionsForFilter();

        TMCriterions.addTestingSession(criterions, "testingSession", query.testInstance().testingSession());

        if (Identity.instance().hasRole("admin_role") || Identity.instance().hasRole("monitor_role")) {
            criterions.addPath("institution", query.systemInSessionUser().systemInSession().system()
                    .institutionSystems().institution());
        } else {
            criterions.addPath("institution", query.systemInSessionUser().systemInSession().system()
                    .institutionSystems().institution(), User.loggedInUser().getInstitution(), User.loggedInUser()
                    .getInstitution());
        }
        criterions.addPath("system", query.systemInSessionUser().systemInSession().system());

        TMCriterions.addAIPOCriterionsUsingTestInstance(criterions, query.testInstance(), "testingSession");

        criterions.addPath("test", query.testInstance().test());

        criterions.addPath("status", query.testInstance().lastStatus());

        criterions.addQueryModifier(this);

        return criterions;
    }

    public FilterDataModel<TestInstanceParticipants> getListTestInstanceParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListTestInstanceParticipants");
        }
        if (listTestInstanceParticipants == null) {
            listTestInstanceParticipants = new FilterDataModel<TestInstanceParticipants>(getFilter()) {
                @Override
                protected Object getId(TestInstanceParticipants t) {
                    return t.getId();
                }
            };
        }
        return listTestInstanceParticipants;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<TestInstanceParticipants> queryBuilder,
                            Map<String, Object> filterValuesApplied) {
        TestInstanceParticipantsQuery query = new TestInstanceParticipantsQuery(queryBuilder);
        query.testInstance().test().testStatus().eq(TestStatus.getSTATUS_READY());
        query.testInstance().test().testType().eq(TestType.getTYPE_MESA());
    }

    public boolean isMainPreCatPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMainPreCatPage");
        }
        return mainPreCatPage;
    }

    public void setMainPreCatPage(boolean mainPreCatPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMainPreCatPage");
        }
        this.mainPreCatPage = mainPreCatPage;
        if (!mainPreCatPage) {
            listTestInstanceParticipants.getFilter().getFilterValues()
                    .put("integrationProfileOption", IntegrationProfileOption.getNoneOption());
            listTestInstanceParticipants.getFilter().getFilterValues()
                    .put("testType", AuditedObject.getObjectByKeyword(TestingType.class, "T"));
            listTestInstanceParticipants.getFilter().getFilterValues()
                    .put("institution", Institution.getLoggedInInstitution());
        }
        loadNewMainPage();
    }

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        CookiesPreset.instance().setMainPageCookie(true);
    }

    public void loadNewMainPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadNewMainPage");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext externalContext = fc.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        String cookieUri = getCookieToLoadUri();
        String currentUri = CookiesPreset.instance().getUriWithParam(request, null, getFilter());

        String baseUri = "/testing/test/mesa/validateMesaTest.seam?testingSession=";
        int uriLength = baseUri.length() + getSelectedTestingSession().getId().toString().length();

        if (cookieUri != null && !cookieUri.isEmpty() && !cookieUri.equals("None") && !cookieUri.equals(currentUri)
                && currentUri.length() == uriLength && CookiesPreset.instance().isMainPageCookie()) {
            String viewId = cookieUri;
            CookiesPreset.instance().setMainPageCookie(false);
            try {
                externalContext.redirect(externalContext.getRequestContextPath() + viewId);
            } catch (IOException e) {
                LOG.error("" + e);
            }

        }
    }

    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        TestingSession activatedTestingSession = (TestingSession) getListTestInstanceParticipants().getFilter()
                .getFilterValues().get("testingSession");
        return activatedTestingSession;
    }

    public String createCookie() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createCookie");
        }
        CookiesPreset.instance().createCookie("V2preConnectathonCookie", null, getFilter());
        return "/testing/test/mesa/validateMesaTest.seam" + "?" + getFilter().getUrlParameters();
    }

    public void createCookieToLoadIt(String encodedUri) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createCookieToLoadIt");
        }
        if (encodedUri == null) {
            encodedUri = "None";
        }
        CookiesPreset.instance().createSimpleCookie("cookieToLoadPreCAT", encodedUri);
    }

    public String getPresetName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPresetName");
        }
        return CookiesPreset.instance().getPresetName("V2preConnectathonCookie", null, getFilter());
    }

    public List<InfoCookies> getAllPresets() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllPresets");
        }
        return CookiesPreset.instance().getAllPresets("V2preConnectathonCookie");
    }

    public List<SelectItem> getListOfPresets() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfPresets");
        }
        return CookiesPreset.instance().getListOfPresets(getAllPresets());
    }

    public boolean containCookie() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("containCookie");
        }
        return CookiesPreset.instance().containCookie("V2preConnectathonCookie", null, getFilter());
    }

    public String getCookieToLoadUri() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCookieToLoadUri");
        }
        return CookiesPreset.instance().getCookieUri("cookieToLoadPreCAT");
    }

    public String getUriWithParam(HttpServletRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUriWithParam");
        }
        return CookiesPreset.instance().getUriWithParam(request, null, getFilter());
    }

    public boolean shortcutsIsFull() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("shortcutsIsFull");
        }
        return CookiesPreset.instance().shortcutsIsFull("V2preConnectathonCookie");
    }

    public String getSelectedPreset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedPreset");
        }
        return CookiesPreset.instance().getSelectedPreCatPreset();
    }

    public void setSelectedPreset(String selectedPreset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedPreset");
        }
        CookiesPreset.instance().setSelectedPreCatPreset(selectedPreset);
    }

    public String deleteSelectedCookie(String presetName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedCookie");
        }
        CookiesPreset.instance().deleteSelectedCookie(presetName);
        return "/testing/test/mesa/validateMesaTest.seam";
    }

    public void saveNewMainPage(ValueChangeEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveNewMainPage");
        }
        CookiesPreset.instance().deleteCookieToLoad("cookieToLoadPreCAT");
        if (getSelectedPreset() != null) {
            CookiesPreset.instance().setMainPageCookie(true);
            createCookieToLoadIt((String) e.getNewValue());
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Main page is changed !");
        }
    }
}
