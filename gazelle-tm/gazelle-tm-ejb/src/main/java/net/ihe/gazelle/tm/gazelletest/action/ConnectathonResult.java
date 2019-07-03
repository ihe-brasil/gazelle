package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.model.AuditedObject;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.filter.modifier.SystemNotTool;
import net.ihe.gazelle.tm.filter.valueprovider.InstitutionFixer;
import net.ihe.gazelle.tm.gazelletest.bean.*;
import net.ihe.gazelle.tm.gazelletest.model.definition.MetaTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRolesQuery;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Name("connectathonResult")
@Scope(ScopeType.PAGE)
public class ConnectathonResult implements Serializable, QueryModifier<SystemAIPOResultForATestingSession> {

    private static final long serialVersionUID = -1591495429879130165L;
    private static final Logger LOG = LoggerFactory.getLogger(ConnectathonResult.class);
    private ConnectathonResultDataModel datamodel;
    private Map<Integer, Boolean> detailsShown = new HashMap<Integer, Boolean>();
    private Map<IntegerKey, List<SAPResultDetailItem>> details = new WeakHashMap<IntegerKey, List<SAPResultDetailItem>>();

    private boolean mainCatPage;

    private Integer selectedTestPeerTypeId;

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

    public boolean isMainCatPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMainCatPage");
        }
        return mainCatPage;
    }

    public void setMainCatPage(boolean mainCatPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMainCatPage");
        }
        this.mainCatPage = mainCatPage;
        if (!mainCatPage) {
            if (!(Identity.instance().hasRole("admin_role")) && !(Identity.instance().hasRole("vendor_role"))
                    && !(Identity.instance().hasRole("vendor_admin_role"))) {
                datamodel.getFilter().getFilterValues()
                        .put("testType", AuditedObject.getObjectByKeyword(TestingType.class, "T"));
            }
            if (!(Identity.instance().hasRole("admin_role"))) {
                datamodel.getFilter().getFilterValues().put("institution", Institution.getLoggedInInstitution());
            }
        }
        loadNewMainPage();
    }

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        datamodel = new ConnectathonResultDataModel(createFilter(requestParameterMap));
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
        String currentUri = CookiesPreset.instance().getUriWithParam(request, getFilter(), null);

        String baseUri = "/testing/test/cat.seam?testSession=";
        int uriLength = baseUri.length() + getSelectedTestingSession().getId().toString().length();

        if ((cookieUri != null) && !cookieUri.isEmpty() && !cookieUri.equals("None") && !cookieUri.equals(currentUri)
                && (currentUri.length() == uriLength) && CookiesPreset.instance().isMainPageCookie()) {
            String viewId = cookieUri;
            CookiesPreset.instance().setMainPageCookie(false);
            try {
                externalContext.redirect(externalContext.getRequestContextPath() + viewId);
            } catch (IOException e) {
                LOG.error("" + e);
            }

        }
    }

    private Filter<SystemAIPOResultForATestingSession> createFilter(Map<String, String> requestParameterMap) {
        SystemAIPOResultForATestingSessionQuery query = new SystemAIPOResultForATestingSessionQuery();
        HQLCriterionsForFilter<SystemAIPOResultForATestingSession> hqlCriterions = query.getHQLCriterionsForFilter();

        TMCriterions.addTestingSession(hqlCriterions, "testSession", query.testSession());
        TMCriterions.addAIPOCriterions(hqlCriterions, query.systemActorProfile().actorIntegrationProfileOption());
        hqlCriterions.addPath("institution", query.systemActorProfile().system().institutionSystems().institution(),
                null, InstitutionFixer.INSTANCE);
        hqlCriterions.addPath("system", query.systemActorProfile().system());
        hqlCriterions.addQueryModifier(new SystemNotTool(query.systemActorProfile().system()));
        hqlCriterions.addQueryModifier(this);
        hqlCriterions.addPath("statusResult", query.status());
        hqlCriterions.addPath("testType", query.systemActorProfile().testingType());
        hqlCriterions.addPath("testTypeReal", query.systemActorProfile().aipo().testParticipants().roleInTest()
                .testRoles().test().testType());
        return new Filter<SystemAIPOResultForATestingSession>(hqlCriterions, requestParameterMap);
    }

    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        TestingSession activatedTestingSession = (TestingSession) datamodel.getFilter().getFilterValues()
                .get("testSession");
        return activatedTestingSession;
    }

    public Integer getSelectedTestPeerTypeId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestPeerTypeId");
        }
        return this.selectedTestPeerTypeId;
    }

    public void setSelectedTestPeerTypeId(Integer selectedTestPeerTypeId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestPeerTypeId");
        }
        this.selectedTestPeerTypeId = selectedTestPeerTypeId;
        this.datamodel.setSelectedTestPeerTypeId(selectedTestPeerTypeId);
    }

    public void updateResults() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateResults");
        }
        TestingSession activatedTestingSession = getSelectedTestingSession();
        if (activatedTestingSession != null) {
            ConnectathonStatisticsBuilder connectathonStatisticsBuilder = new ConnectathonStatisticsBuilder(
                    activatedTestingSession);
            connectathonStatisticsBuilder.updateStatistics();
            activatedTestingSession.setResultsCompute(new Date());
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(activatedTestingSession);
        }
    }

    public FilterDataModel<SystemAIPOResultForATestingSession> getDatamodel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDatamodel");
        }
        return datamodel;
    }

    public Filter<SystemAIPOResultForATestingSession> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        return datamodel.getFilter();
    }

    public boolean isSystemSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isSystemSelected");
        }
        return datamodel.getFilter().getFilterValues().get("system") != null;
    }

    public SystemInSession getSelectedSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystem");
        }
        System sys = (System) datamodel.getFilter().getFilterValues().get("system");
        TestingSession ts = (TestingSession) datamodel.getFilter().getRealFilterValue("testSession");
        SystemInSession sis = SystemInSession.getSystemInSessionForSession(sys, ts);
        return sis;
    }

    public boolean isShowReport() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowReport");
        }
        if (datamodel.getFilter().getRealFilterValue("institution") == null) {
            return false;
        }
        if (datamodel.getFilter().getRealFilterValue("testSession") == null) {
            return false;
        }
        return true;
    }

    public void closeDetails() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeDetails");
        }
        detailsShown.clear();
        details.clear();
    }

    public int getColumnCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getColumnCount");
        }
        return isSystemSelected() ? 13 : 14;
    }

    public String getTestInstancePermalink(Integer id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstancePermalink");
        }
        return ApplicationPreferenceManager.instance().getApplicationUrl() + "testInstance.seam?id="
                + Integer.toString(id);
    }

    public List<StatusResults> getListStatusResults() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListStatusResults");
        }
        return StatusResults.getListStatusResults();
    }

    public void mergeResult(SystemAIPOResultForATestingSession systemAIPOResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeResult");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(systemAIPOResult);
    }

    public void showTestsFor(SystemAIPOResultForATestingSession systemAIPOResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTestsFor");
        }
        boolean showingTestsFor = isShowingTestsFor(systemAIPOResult);
        showingTestsFor = !showingTestsFor;
        detailsShown.put(systemAIPOResult.getId(), Boolean.valueOf(showingTestsFor));
    }

    public boolean isShowingTestsFor(SystemAIPOResultForATestingSession systemAIPOResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowingTestsFor");
        }
        Boolean curValue = detailsShown.get(systemAIPOResult.getId());
        if (curValue == null) {
            return false;
        }
        return curValue;
    }

    public void cleanRefresh() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanRefresh");
        }
        getFilter().modified();
        closeDetails();
    }

    public String printTestCount(SystemAIPOResultForATestingSession systemAIPOResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("printTestCount");
        }
        return printTestCount(systemAIPOResult, false);
    }

    public String printTestCount(SystemAIPOResultForATestingSession systemAIPOResult, boolean isInteroperability) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("printTestCount");
        }
        int count = 0;
        List<SAPResultDetailItem> list = getDetails(systemAIPOResult, isInteroperability);
        if (list != null) {
            for (SAPResultDetailItem sapResultDetailItem : list) {
                if (sapResultDetailItem.isMetaTest()) {
                    if (StringUtils.trimToNull(sapResultDetailItem.getLink2()) != null) {
                        count++;
                    }
                } else {
                    count++;
                }
            }
        }
        if (count == 0) {
            return StatusMessage.getBundleMessage("net.ihe.gazelle.tm.cat.TestsFound0",
                    "net.ihe.gazelle.tm.cat.TestsFound0");
        } else if (count == 1) {
            return StatusMessage.getBundleMessage("net.ihe.gazelle.tm.cat.TestsFound1",
                    "net.ihe.gazelle.tm.cat.TestsFound1");
        } else {
            return Interpolator.instance().interpolate(
                    StatusMessage.getBundleMessage("net.ihe.gazelle.tm.cat.TestsFoundn",
                            "net.ihe.gazelle.tm.cat.TestsFoundn"), count);
        }
    }

    public List<SAPResultDetailItem> getDetails(SystemAIPOResultForATestingSession systemAIPOResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDetails");
        }
        return getDetails(systemAIPOResult, false);
    }

    public List<SAPResultDetailItem> getDetails(SystemAIPOResultForATestingSession systemAIPOResult,
                                                boolean isInteroperability) {
        List<SAPResultDetailItem> detail = null;
        if (systemAIPOResult != null) {
            detail = computeDetail(systemAIPOResult, isInteroperability);
        }
        return detail;
    }

    /**
     * AGB: change from private to public to use the method in reporting4TestingSession.xhtml
     *
     * @param systemAIPOResult
     * @return
     */
    public List<SAPResultDetailItem> computeDetail(SystemAIPOResultForATestingSession systemAIPOResult,
                                                   boolean isInteroperability) {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        return computeDetail(systemAIPOResult, entityManager, isInteroperability);
    }

    public List<SAPResultDetailItem> computeDetail(SystemAIPOResultForATestingSession systemAIPOResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("computeDetail");
        }
        return computeDetail(systemAIPOResult, false);
    }

    public List<SAPResultDetailItem> computeDetail(SystemAIPOResultForATestingSession systemAIPOResult,
                                                   EntityManager entityManager, boolean isInteroperability) {

        LOG.debug("computeDetail " + systemAIPOResult.getTestSession().getDescription() + "getTestRoles "
                + systemAIPOResult.getTestSession().getTestTypes());

        List<SAPResultDetailItem> result = new ArrayList<SAPResultDetailItem>();

        SystemActorProfiles systemActorProfile = systemAIPOResult.getSystemActorProfile();
        System system = systemActorProfile.getSystem();

        List<TestRoles> rolesWithTests = getTestRolesWithTests(systemAIPOResult, systemActorProfile, system,
                isInteroperability);

        for (TestRoles testRoles : rolesWithTests) {
            SAPResultDetailTestRoleItem detailTR = new SAPResultDetailTestRoleItem(testRoles, systemAIPOResult);
            result.add(detailTR);
        }

        List<TestRoles> rolesWithMetaTests = getTestRolesWithMetaTests(systemAIPOResult, systemActorProfile, system,
                isInteroperability);

        for (TestRoles testRoles : rolesWithMetaTests) {
            SAPResultDetailTestRoleItem detailTR = new SAPResultDetailTestRoleItem(testRoles, systemAIPOResult);
            result.add(detailTR);
            SAPResultDetailMetaTestItem.addAndCreateIfNeeded(detailTR, result);
        }

        for (SAPResultDetailItem detailItem : result) {
            if (detailItem instanceof SAPResultDetailTestRoleItem) {
                SAPResultDetailTestRoleItem sapResultDetailTestRoleItem = (SAPResultDetailTestRoleItem) detailItem;
                MetaTest metaTest = sapResultDetailTestRoleItem.getTestRoles().getMetaTest();
                if (metaTest != null) {
                    for (SAPResultDetailItem detailItem2 : result) {
                        if (detailItem2 instanceof SAPResultDetailMetaTestItem) {
                            SAPResultDetailMetaTestItem sapResultDetailMetaTestItem = (SAPResultDetailMetaTestItem) detailItem2;
                            MetaTest metaTest2 = sapResultDetailMetaTestItem.getMetaTest();
                            if (metaTest.getId().equals(metaTest2.getId())) {
                                sapResultDetailTestRoleItem.setResultMetaTest(sapResultDetailMetaTestItem);
                            }
                        }
                    }
                }
            }
        }

        List<TestRoles> roles = getTestRoles(systemAIPOResult, systemActorProfile, system, isInteroperability);

        Set<Integer> roleInTestIds = new HashSet<Integer>();
        for (TestRoles testRoles : roles) {
            roleInTestIds.add(testRoles.getRoleInTest().getId());
        }

        TestInstanceParticipantsQuery tipQuery = new TestInstanceParticipantsQuery();
        List<TestType> testTypes = new ArrayList<TestType>(systemAIPOResult.getTestSession().getTestTypes());
        LOG.debug(systemAIPOResult.getTestSession().getDescription() + systemAIPOResult.getTestSession().getId()
                + "getTestRoles " + testTypes.toString());
        if (isInteroperability && TestingSession.getSelectedTestingSession().isTestingInteroperability()) {
            tipQuery.testInstance().test().testType().id().eq(TestType.getTYPE_INTEROPERABILITY().getId());
        } else {
            if (testTypes.contains(TestType.getTYPE_INTEROPERABILITY())) {
                testTypes.remove(TestType.getTYPE_INTEROPERABILITY());
            }
            tipQuery.testInstance().test().testType().in(testTypes);
        }

        tipQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption()
                .eq(systemActorProfile.getActorIntegrationProfileOption());
        tipQuery.systemInSessionUser().systemInSession().system().id().eq(system.getId());
        tipQuery.systemInSessionUser().systemInSession().testingSession().eq(systemAIPOResult.getTestSession());
        tipQuery.roleInTest().id().in(roleInTestIds);
        List<TestInstanceParticipants> tip = tipQuery.getList();

        for (TestInstanceParticipants testInstanceParticipants : tip) {
            for (SAPResultDetailItem detailItem : result) {
                detailItem.tryAddTestInstance(testInstanceParticipants.getTestInstance());
            }
        }

        TestingSession activatedTestingSession = systemAIPOResult.getTestSession();

        for (SAPResultDetailItem detailItem : result) {

            AIPOSystemPartners partners = detailItem.getPartners();
            List<TestRoles> testRolesList = detailItem.getTestRolesList();
            PartnersStatisticsBuilder.get(activatedTestingSession).addPartners(entityManager, partners,
                    systemActorProfile, testRolesList);
        }

        Collections.sort(result);

        return result;
    }

    private List<TestRoles> getTestRolesWithMetaTests(SystemAIPOResultForATestingSession systemAIPOResult,
                                                      SystemActorProfiles systemActorProfile, System system, boolean isInteroperability) {
        TestRolesQuery trQuery = new TestRolesQuery();

        trQuery.roleInTest().testParticipantsList().tested().eq(Boolean.TRUE);
        List<TestType> testTypes = new ArrayList<TestType>(systemAIPOResult.getTestSession().getTestTypes());
        LOG.debug(systemAIPOResult.getTestSession().getDescription() + systemAIPOResult.getTestSession().getId()
                + " getTestRolesWithMetaTests " + testTypes.toString());
        if (isInteroperability && TestingSession.getSelectedTestingSession().isTestingInteroperability()) {
            trQuery.test().testType().id().eq(TestType.getTYPE_INTEROPERABILITY().getId());
        } else {
            if (testTypes.contains(TestType.getTYPE_INTEROPERABILITY())) {
                testTypes.remove(TestType.getTYPE_INTEROPERABILITY());
            }
            trQuery.test().testType().in(testTypes);
        }
        trQuery.test().testStatus().keyword().eq("ready");

        Integer selectedTestPeerTypeId = datamodel.getSelectedTestPeerTypeId();
        if ((selectedTestPeerTypeId != null) && (selectedTestPeerTypeId != -1)) {
            trQuery.test().testPeerType().id().eq(selectedTestPeerTypeId);
        }
        trQuery.metaTest().isNotEmpty();
        trQuery.roleInTest().addFetch();
        trQuery.roleInTest().testParticipantsList().addFetch();
        trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption().addFetch();
        trQuery.roleInTest().testParticipantsList().aipo().addFetch();

        trQuery.roleInTest().testParticipantsList().aipo().systemActorProfiles().system().eq(system);

        trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption()
                .eq(systemActorProfile.getActorIntegrationProfileOption());

        trQuery.test().keyword().order(true);

        List<TestRoles> roles = trQuery.getList();
        return roles;
    }

    private List<TestRoles> getTestRolesWithTests(SystemAIPOResultForATestingSession systemAIPOResult,
                                                  SystemActorProfiles systemActorProfile, System system, boolean isInteroperability) {
        TestRolesQuery trQuery = new TestRolesQuery();

        trQuery.roleInTest().testParticipantsList().tested().eq(Boolean.TRUE);
        List<TestType> testTypes = new ArrayList<TestType>(systemAIPOResult.getTestSession().getTestTypes());
        LOG.debug(systemAIPOResult.getTestSession().getDescription() + systemAIPOResult.getTestSession().getId()
                + " getTestRolesWithTests " + testTypes.toString());
        if (isInteroperability && TestingSession.getSelectedTestingSession().isTestingInteroperability()) {
            trQuery.test().testType().id().eq(TestType.getTYPE_INTEROPERABILITY().getId());
        } else {
            if (testTypes.contains(TestType.getTYPE_INTEROPERABILITY())) {
                testTypes.remove(TestType.getTYPE_INTEROPERABILITY());
            }
            trQuery.test().testType().in(testTypes);
        }

        trQuery.test().testStatus().keyword().eq("ready");

        Integer selectedTestPeerTypeId = datamodel.getSelectedTestPeerTypeId();
        if ((selectedTestPeerTypeId != null) && (selectedTestPeerTypeId != -1)) {
            trQuery.test().testPeerType().id().eq(selectedTestPeerTypeId);
        }
        trQuery.metaTest().isEmpty();
        trQuery.roleInTest().addFetch();
        trQuery.roleInTest().testParticipantsList().addFetch();
        trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption().addFetch();
        trQuery.roleInTest().testParticipantsList().aipo().addFetch();

        trQuery.roleInTest().testParticipantsList().aipo().systemActorProfiles().system().eq(system);

        trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption()
                .eq(systemActorProfile.getActorIntegrationProfileOption());

        trQuery.test().keyword().order(true);

        List<TestRoles> roles = trQuery.getList();
        return roles;
    }

    private List<TestRoles> getTestRoles(SystemAIPOResultForATestingSession systemAIPOResult,
                                         SystemActorProfiles systemActorProfile, System system, boolean isInteroperability) {
        TestRolesQuery trQuery = new TestRolesQuery();

        trQuery.roleInTest().testParticipantsList().tested().eq(Boolean.TRUE);
        List<TestType> testTypes = new ArrayList<TestType>(systemAIPOResult.getTestSession().getTestTypes());
        LOG.debug(systemAIPOResult.getTestSession().getDescription() + systemAIPOResult.getTestSession().getId()
                + " getTestRoles " + testTypes.toString());
        trQuery.test().testStatus().keyword().eq("ready");

        if (isInteroperability && TestingSession.getSelectedTestingSession().isTestingInteroperability()) {
            trQuery.test().testType().id().eq(TestType.getTYPE_INTEROPERABILITY().getId());
        } else {
            if (testTypes.contains(TestType.getTYPE_INTEROPERABILITY())) {
                testTypes.remove(TestType.getTYPE_INTEROPERABILITY());
            }
            trQuery.test().testType().in(testTypes);
        }

        Integer selectedTestPeerTypeId = datamodel.getSelectedTestPeerTypeId();
        if ((selectedTestPeerTypeId != null) && (selectedTestPeerTypeId != -1)) {
            trQuery.test().testPeerType().id().eq(selectedTestPeerTypeId);
        }

        trQuery.roleInTest().addFetch();
        trQuery.roleInTest().testParticipantsList().addFetch();
        trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption().addFetch();
        trQuery.roleInTest().testParticipantsList().aipo().addFetch();

        trQuery.roleInTest().testParticipantsList().aipo().systemActorProfiles().system().eq(system);

        trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption()
                .eq(systemActorProfile.getActorIntegrationProfileOption());

        trQuery.test().keyword().order(true);

        List<TestRoles> roles = trQuery.getList();
        return roles;
    }

    public AIPOSystemPartners getPartners(SystemAIPOResultForATestingSession systemAIPO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPartners");
        }
        TestingSession selectedTestingSession = getSelectedTestingSession();
        AIPOSystemPartners result = new AIPOSystemPartners(selectedTestingSession);
        if (selectedTestingSession != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            PartnersStatisticsBuilder.get(selectedTestingSession).addPartners(entityManager, result,
                    systemAIPO.getSystemActorProfile());
        }
        return result;
    }

    public String startTestInstance(SAPResultDetailItem entry) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("startTestInstance");
        }
        if (entry instanceof SAPResultDetailTestRoleItem) {
            SAPResultDetailTestRoleItem item = (SAPResultDetailTestRoleItem) entry;
            TestRoles testRoles = item.getTestRoles();
            SystemAIPOResultForATestingSession systemAIPOResult = item.getSystemAIPOResult();
            SystemInSession systemInSession = SystemInSession.getSystemInSessionForSession(systemAIPOResult
                    .getSystemActorProfile().getSystem(), systemAIPOResult.getTestSession());

            Contexts.getSessionContext().set("startTestInstanceSISid", systemInSession.getId());
            Contexts.getSessionContext().set("startTestInstanceTRid", testRoles.getId());

            return "/testing/test/test/StartTestInstance.seam"; // DONE
        }
        return null;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<SystemAIPOResultForATestingSession> queryBuilder,
                            Map<String, Object> filterValuesApplied) {
        SystemAIPOResultForATestingSessionQuery query = new SystemAIPOResultForATestingSessionQuery(queryBuilder);

        HQLRestriction enabledRestriction = HQLRestrictions.or(query.enabled().eqRestriction(true), query.enabled()
                .isNullRestriction());
        query.addRestriction(query.systemActorProfile().system().systemsInSession().acceptedToSession()
                .eqRestriction(true));
        query.addRestriction(query.systemActorProfile().system().systemsInSession().testingSession()
                .eqRestriction(TestingSession.getSelectedTestingSession()));
        query.addRestriction(enabledRestriction);
    }

    public List<Object> getEmptyList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEmptyList");
        }
        return new ArrayList<Object>();
    }

    public String getStyleClass(SAPResultDetailItem item) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStyleClass");
        }
        if (item instanceof SAPResultDetailTestRoleItem) {
            if (((SAPResultDetailTestRoleItem) item).getResultMetaTest() != null) {
                return getStyleClass(((SAPResultDetailTestRoleItem) item).getResultMetaTest());
            }
        }

        if ("O".equals(item.getOpt())) {
            return "rich-subtable-cell";
        }

        int verified = 0;
        int todo = 0;

        if (item.getInstancesVerified() != null) {
            verified = item.getInstancesVerified().size();
        }
        todo = Integer.parseInt(item.getCount());

        if (verified >= todo) {
            return "cat-cell-ok";
        } else {
            return "cat-cell-nok";
        }
    }

    public String createCookie() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createCookie");
        }
        CookiesPreset.instance().createCookie("V2connectathonCookie", getFilter(), null);
        return "/testing/test/cat.seam" + "?" + getFilter().getUrlParameters();
    }

    public void createCookieToLoadIt(String encodedUri) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createCookieToLoadIt");
        }
        if (encodedUri == null) {
            encodedUri = "None";
        }
        CookiesPreset.instance().createSimpleCookie("cookieToLoadCAT", encodedUri);
    }

    public String getPresetName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPresetName");
        }
        return CookiesPreset.instance().getPresetName("V2connectathonCookie", getFilter(), null);
    }

    public List<InfoCookies> getAllPresets() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllPresets");
        }
        return CookiesPreset.instance().getAllPresets("V2connectathonCookie");
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
        return CookiesPreset.instance().containCookie("V2connectathonCookie", getFilter(), null);
    }

    public String getCookieToLoadUri() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCookieToLoadUri");
        }
        return CookiesPreset.instance().getCookieUri("cookieToLoadCAT");
    }

    public String getUriWithParam(HttpServletRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUriWithParam");
        }
        return CookiesPreset.instance().getUriWithParam(request, getFilter(), null);
    }

    public boolean shortcutsIsFull() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("shortcutsIsFull");
        }
        return CookiesPreset.instance().shortcutsIsFull("V2connectathonCookie");
    }

    public String getSelectedPreset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedPreset");
        }
        return CookiesPreset.instance().getSelectedCatPreset();
    }

    public void setSelectedPreset(String selectedPreset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedPreset");
        }
        CookiesPreset.instance().setSelectedCatPreset(selectedPreset);
    }

    public String deleteSelectedCookie(String presetName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedCookie");
        }
        CookiesPreset.instance().deleteSelectedCookie(presetName);
        return "/testing/test/cat.seam";
    }

    public void saveNewMainPage(ValueChangeEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveNewMainPage");
        }
        CookiesPreset.instance().deleteCookieToLoad("cookieToLoadCAT");
        if (getSelectedPreset() != null) {
            CookiesPreset.instance().setMainPageCookie(true);
            createCookieToLoadIt((String) e.getNewValue());
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Main page is changed !");
        }
    }

    public void listResultSummary() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listResultSummary");
        }
        HSSFWorkbook workbook = new HSSFWorkbook();

        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());

        HSSFSheet worksheet = workbook.createSheet("Connectathon results summary");
        worksheet.createFreezePane(0, 1);

        int colIndex = 0;
        int rowIndex = 0;
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("Organisation");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("System");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("Profile");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("Actor");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("Option");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("Type");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("Results");

        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("R/O");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("V");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("W");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("P");
        getCell(worksheet, style, rowIndex, colIndex++).setCellValue("F");

        rowIndex++;

        List<SystemAIPOResultForATestingSession> values = (List<SystemAIPOResultForATestingSession>) getDatamodel()
                .getAllItems(FacesContext.getCurrentInstance());

        for (SystemAIPOResultForATestingSession sr : values) {
            colIndex = 0;
            SystemActorProfiles sap = sr.getSystemActorProfile();
            // Required by Lynn
            Set<InstitutionSystem> instSys = sap.getSystem().getInstitutionSystems();
            Institution orga = null;
            for (InstitutionSystem instSy : instSys) {
                orga = instSy.getInstitution();
            }

            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(orga.getName());

            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(sap.getSystem().getKeyword());
            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(
                    sap.getActorIntegrationProfileOption().getActorIntegrationProfile().getIntegrationProfile()
                            .getKeyword());
            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(
                    sap.getActorIntegrationProfileOption().getActorIntegrationProfile().getActor().getKeyword());
            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(
                    sap.getActorIntegrationProfileOption().getIntegrationProfileOption().getKeyword());
            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(sap.getTestingType().getKeyword());
            if (sr.getStatus() != null) {
                String label = ResourceBundle.instance().getString(sr.getStatus().getLabelToDisplay());
                getCell(worksheet, style, rowIndex, colIndex++).setCellValue(label);
            } else {
                getCell(worksheet, style, rowIndex, colIndex++).setCellValue("");
            }

            // Optionnal
            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(
                    sr.getTestRolesCountR() + "/" + sr.getTestRolesCountO());
            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(sr.getIndicatorVerified());

            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(sr.getIndicatorWaiting());

            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(sr.getIndicatorProgress());
            getCell(worksheet, style, rowIndex, colIndex++).setCellValue(sr.getIndicatorFailed());

            rowIndex++;
        }

        for (int columnIndex = 0; columnIndex < colIndex; columnIndex++) {
            worksheet.autoSizeColumn(columnIndex);
        }

        CellReference start = new CellReference(0, 0);
        String startString = start.formatAsString();
        CellReference end = new CellReference(rowIndex - 1, colIndex - 1);
        String endString = end.formatAsString();
        worksheet.setAutoFilter(CellRangeAddress.valueOf(startString + ":" + endString));

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        Pages.getCurrentBaseName();

        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        ServletOutputStream servletOutputStream = response.getOutputStream();
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=\"systemsSummary.xls\"");
        workbook.write(servletOutputStream);
        servletOutputStream.flush();
        servletOutputStream.close();
        facesContext.responseComplete();
    }

    private HSSFCell getCell(HSSFSheet worksheet, CellStyle style, int irow, int icol) {
        HSSFRow row = worksheet.getRow(irow);
        if (row == null) {
            row = worksheet.createRow(irow);
            row.setRowStyle(style);
        }
        HSSFCell cell = row.getCell(icol);
        if (cell == null) {
            cell = row.createCell(icol);
        }
        return cell;
    }

    public static class IntegerKey {
        private Integer key;

        public IntegerKey(Integer key) {
            super();
            this.key = key;
        }

        public Integer getKey() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getKey");
            }
            return key;
        }

        public void setKey(Integer key) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setKey");
            }
            this.key = key;
        }

        @Override
        public int hashCode() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("hashCode");
            }
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("equals");
            }
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            IntegerKey other = (IntegerKey) obj;
            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!key.equals(other.key)) {
                return false;
            }
            return true;
        }

    }
}
