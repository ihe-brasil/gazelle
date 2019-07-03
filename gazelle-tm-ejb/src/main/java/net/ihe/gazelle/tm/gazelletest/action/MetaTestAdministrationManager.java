package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Name("metaTestAdministrationManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("MetaTestAdministrationManagerLocal")
public class MetaTestAdministrationManager implements MetaTestAdministrationManagerLocal, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(MetaTestAdministrationManager.class);

    private MetaTest selectedMetaTest;

    private Domain selectedDomain;

    private IntegrationProfile selectedIntegrationProfile;

    private Actor selectedActor;

    private boolean showMetaTestPanel = false;

    private boolean showEditTestAssignmentPanel = false;

    private String selectedCriterion;

    private List<TestRoles> selectedTestRoles;

    private List<TestRoles> availableTestRoles;

    private List<MetaTest> foundMetaTestList;

    private String metaTestKeyword;

    private String metaTestDescription;

    private TestRoles selectedTestRole;

    @Override
    public TestRoles getSelectedTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestRole");
        }
        return selectedTestRole;
    }

    @Override
    public void setSelectedTestRole(TestRoles selectedTestRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestRole");
        }
        this.selectedTestRole = selectedTestRole;
    }

    @Override
    public String getMetaTestKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaTestKeyword");
        }
        return metaTestKeyword;
    }

    @Override
    public void setMetaTestKeyword(String metaTestKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMetaTestKeyword");
        }
        this.metaTestKeyword = metaTestKeyword;
    }

    @Override
    public String getMetaTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaTestDescription");
        }
        return metaTestDescription;
    }

    @Override
    public void setMetaTestDescription(String metaTestDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMetaTestDescription");
        }
        this.metaTestDescription = metaTestDescription;
    }

    @Override
    public boolean isShowEditTestAssignmentPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowEditTestAssignmentPanel");
        }
        return showEditTestAssignmentPanel;
    }

    @Override
    public void setShowEditTestAssignmentPanel(boolean showEditTestAssignmentPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowEditTestAssignmentPanel");
        }
        this.showEditTestAssignmentPanel = showEditTestAssignmentPanel;
    }

    @Override
    public boolean isShowMetaTestPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowMetaTestPanel");
        }
        return showMetaTestPanel;
    }

    @Override
    public void setShowMetaTestPanel(boolean showMetaTestPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowMetaTestPanel");
        }
        this.showMetaTestPanel = showMetaTestPanel;
    }

    @Override
    public Domain getSelectedDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomain");
        }
        return selectedDomain;
    }

    @Override
    public void setSelectedDomain(Domain selectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomain");
        }
        this.selectedDomain = selectedDomain;
    }

    @Override
    public IntegrationProfile getSelectedIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfile");
        }
        return selectedIntegrationProfile;
    }

    @Override
    public void setSelectedIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfile");
        }
        this.selectedIntegrationProfile = selectedIntegrationProfile;
    }

    @Override
    public Actor getSelectedActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActor");
        }
        return selectedActor;
    }

    @Override
    public void setSelectedActor(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        this.selectedActor = selectedActor;
    }

    @Override
    public MetaTest getSelectedMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedMetaTest");
        }
        return selectedMetaTest;
    }

    @Override
    public void setSelectedMetaTest(MetaTest selectedMetaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedMetaTest");
        }
        this.selectedMetaTest = selectedMetaTest;
    }

    @Override
    public String getSelectedCriterion() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedCriterion");
        }
        return selectedCriterion;
    }

    @Override
    public void setSelectedCriterion(String selectedCriterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedCriterion");
        }
        this.selectedCriterion = selectedCriterion;
    }

    @Override
    public List<TestRoles> getSelectedTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestRoles");
        }
        return selectedTestRoles;
    }

    @Override
    public void setSelectedTestRoles(List<TestRoles> selectedTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestRoles");
        }
        this.selectedTestRoles = selectedTestRoles;
    }

    @Override
    public List<TestRoles> getAvailableTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAvailableTestRoles");
        }
        return availableTestRoles;
    }

    @Override
    public void setAvailableTestRoles(List<TestRoles> availableTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAvailableTestRoles");
        }
        this.availableTestRoles = availableTestRoles;
    }

    @Override
    public List<MetaTest> getFoundMetaTestList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundMetaTestList");
        }
        return foundMetaTestList;
    }

    @Override
    public void setFoundMetaTestList(List<MetaTest> foundMetaTestList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFoundMetaTestList");
        }
        this.foundMetaTestList = foundMetaTestList;
    }

    @Override
    public void searchMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchMetaTest");
        }
        foundMetaTestList = MetaTest.getAllMetaTest();
    }

    @Override
    public boolean validateMetaTestKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateMetaTestKeyword");
        }
        if ((selectedMetaTest.getId() != null) && selectedMetaTest.getKeyword().equals(metaTestKeyword)) {
            return true;
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Query query = em.createQuery("SELECT mt FROM MetaTest mt where mt.keyword='" + metaTestKeyword + "'");
        List<MetaTest> list = query.getResultList();
        if (list.size() > 0) {
            return false;
        }
        return true;
    }

    @Override
    public void editMetaTest(MetaTest inMetaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editMetaTest");
        }
        selectedMetaTest = inMetaTest;
        showMetaTestPanel = true;
        selectedDomain = null;
        selectedActor = null;
        selectedIntegrationProfile = null;
        showEditTestAssignmentPanel = false;
        selectedTestRoles = new ArrayList<TestRoles>();
        availableTestRoles = new ArrayList<TestRoles>();
        metaTestKeyword = inMetaTest.getKeyword();
        metaTestDescription = inMetaTest.getDescription();
    }

    @Override
    public void addNewMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewMetaTest");
        }
        selectedMetaTest = new MetaTest();
        metaTestKeyword = null;
        metaTestDescription = null;
        showMetaTestPanel = true;
        selectedDomain = null;
        selectedActor = null;
        selectedIntegrationProfile = null;
    }

    @Override
    public void removeTestFromSelectedMetaTest(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeTestFromSelectedMetaTest");
        }
        if ((selectedMetaTest != null) && (inTestRoles != null)) {
            List<TestRoles> testRolesList = selectedMetaTest.getTestRolesList();
            testRolesList.remove(inTestRoles);
            selectedMetaTest.setTestRolesList(testRolesList);
            EntityManager em = EntityManagerService.provideEntityManager();
            selectedMetaTest = em.merge(selectedMetaTest);
        }
    }

    @Override
    public void findTestsForSelectedCriteria() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestsForSelectedCriteria");
        }
        availableTestRoles = null;

        if (selectedCriterion.equals(MonitorInSessionAdministrationManager.SEARCH_BY_DOMAIN_LABEL_TO_DISPLAY)
                && (selectedDomain != null)) {
            availableTestRoles = TestRoles.getTestsFiltered(null, selectedDomain, null, null, null, null,
                    TestType.getTestTypesWithoutMESA(), null, null, TestStatus.getSTATUS_READY(), null);

        } else {
            if (selectedCriterion.equals(MonitorInSessionAdministrationManager.SEARCH_BY_PROFILE_LABEL_TO_DISPLAY)
                    && (selectedIntegrationProfile != null)) {
                availableTestRoles = TestRoles.getTestsFiltered(null, null, selectedIntegrationProfile, null, null,
                        null, TestType.getTestTypesWithoutMESA(), null, null, TestStatus.getSTATUS_READY(), null);
            } else {
                if (selectedCriterion.equals(MonitorInSessionAdministrationManager.SEARCH_BY_ACTOR_LABEL_TO_DISPLAY)
                        && (selectedActor != null)) {
                    availableTestRoles = TestRoles.getTestsFiltered(null, null, null, null, selectedActor, null,
                            TestType.getTestTypesWithoutMESA(), null, null, TestStatus.getSTATUS_READY(), null);
                }
            }
        }
        if (availableTestRoles != null) {
            Collections.sort(availableTestRoles);
        }
        if ((selectedMetaTest.getId() != null) && (availableTestRoles != null)) {
            selectedTestRoles = getTestRolesListByCriteria();
        } else {
            availableTestRoles = new ArrayList<TestRoles>();
            selectedTestRoles = new ArrayList<TestRoles>();
        }
    }

    private List<TestRoles> getTestRolesListByCriteria() {
        List<TestRoles> result = new ArrayList<TestRoles>();
        if (selectedCriterion.equals(MonitorInSessionAdministrationManager.SEARCH_BY_DOMAIN_LABEL_TO_DISPLAY)
                && (selectedDomain != null)) {
            result = MetaTest.getTestRolesFiltered(selectedMetaTest, null, selectedDomain, null, null, null, null,
                    TestType.getTestTypesWithoutMESA(), null, null, TestStatus.getSTATUS_READY(), null);
        } else {
            if (selectedCriterion.equals(MonitorInSessionAdministrationManager.SEARCH_BY_PROFILE_LABEL_TO_DISPLAY)
                    && (selectedIntegrationProfile != null)) {
                result = MetaTest.getTestRolesFiltered(selectedMetaTest, null, null, selectedIntegrationProfile, null,
                        null, null, TestType.getTestTypesWithoutMESA(), null, null, TestStatus.getSTATUS_READY(), null);
            } else {
                if (selectedCriterion.equals(MonitorInSessionAdministrationManager.SEARCH_BY_ACTOR_LABEL_TO_DISPLAY)
                        && (selectedActor != null)) {
                    result = MetaTest.getTestRolesFiltered(selectedMetaTest, null, null, null, null, selectedActor,
                            null, TestType.getTestTypesWithoutMESA(), null, null, TestStatus.getSTATUS_READY(), null);
                }
            }
        }
        return result;
    }

    @Deprecated
    @Override
    public void addSelectedTestRolesToMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSelectedTestRolesToMetaTest");
        }
        List<TestRoles> testRoles = selectedMetaTest.getTestRolesList();
        if (testRoles == null) {
            testRoles = new ArrayList<TestRoles>();
        } else {
            // testRoles already in the metaTest corresponding to the criteria
            List<TestRoles> list = getTestRolesListByCriteria();
            testRoles.removeAll(list);
        }
        testRoles.addAll(selectedTestRoles);
        selectedMetaTest.setTestRolesList(testRoles);
        EntityManager em = EntityManagerService.provideEntityManager();
        selectedMetaTest = em.merge(selectedMetaTest);
        showEditTestAssignmentPanel = false;
    }

    @Override
    public void addSelectedTestRolesToMetaTest2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSelectedTestRolesToMetaTest");
        }
        if (selectedTestRoles != null && !selectedTestRoles.isEmpty()) {
            for (TestRoles testRole : selectedTestRoles) {
                if (selectedMetaTest.getTestRolesList() == null) {
                    List<TestRoles> testRoles = new ArrayList<TestRoles>();
                    testRoles.add(testRole);
                    selectedMetaTest.setTestRolesList(testRoles);
                } else if (selectedMetaTest.getTestRolesList().isEmpty() || !selectedMetaTest.getTestRolesList().contains(testRole)) {
                    selectedMetaTest.getTestRolesList().add(testRole);
                }
            }
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        selectedMetaTest = em.merge(selectedMetaTest);
        showEditTestAssignmentPanel = false;
    }

    @Override
    public void persistSelectedMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedMetaTest");
        }
        if (validateMetaTestKeyword()) {
            EntityManager em = EntityManagerService.provideEntityManager();
            selectedMetaTest.setKeyword(metaTestKeyword);
            selectedMetaTest.setDescription(metaTestDescription);
            selectedMetaTest = em.merge(selectedMetaTest);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "The meta Test " + selectedMetaTest.getKeyword() + " was successfully saved");
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "An other meta test with same keyword already exits");
        }
    }

    @Override
    public void initMetaTestManagement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initMetaTestManagement");
        }
        selectedTestRoles = new ArrayList<TestRoles>();
        availableTestRoles = new ArrayList<TestRoles>();
        showMetaTestPanel = false;
    }

    @Override
    public void deleteMetaTest(MetaTest inMetaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteMetaTest");
        }
        if (inMetaTest != null) {
            EntityManager em = EntityManagerService.provideEntityManager();

            try {
                MetaTest metaTest = em.find(MetaTest.class, inMetaTest.getId());
                em.remove(metaTest);
                FacesMessages.instance().add(StatusMessage.Severity.INFO, " MetaTest " + inMetaTest.getKeyword() + " was successfully deleted ");

                MetaTestsOverview metaTestsOverview = (MetaTestsOverview) Component.getInstance("metaTestsOverview");
                metaTestsOverview.getFoundMetaTests().resetCache();
            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to delete MetaTest : " + inMetaTest.getKeyword());
            }
        }
    }

    @Override
    public List<MonitorInSession> getMonitorsListByMetaTest(MetaTest inMetaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMonitorsListByMetaTest");
        }
        if (inMetaTest != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT DISTINCT mIs FROM MonitorInSession mIs JOIN mIs.testList test, MetaTest mt JOIN mt.testRolesList testRoles" +
                            " WHERE testRoles.test=test AND mt=:inMetaTest AND mis.testingSession=:inTestingSession");
            query.setParameter("inMetaTest", inMetaTest);
            query.setParameter("inTestingSession", TestingSession.getSelectedTestingSession());
            List<MonitorInSession> list = query.getResultList();
            return list;
        }
        return new ArrayList<MonitorInSession>();
    }

    @Override
    public List<SystemInSession> getSystemInSessionListByMetaTest(MetaTest inMetaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemInSessionListByMetaTest");
        }
        if (inMetaTest != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em.createQuery("SELECT DISTINCT sIs "
                    + "FROM TestRoles tr JOIN tr.roleInTest.testParticipantsList participants, "
                    + "SystemInSession sIs, " + "SystemActorProfiles sap, "
                    + "MetaTest mt JOIN mt.testRolesList testRoles "
                    + "WHERE participants.actorIntegrationProfileOption = sap.actorIntegrationProfileOption "
                    + "AND sap.system=sIs.system " + "AND participants.tested='true' "
                    + "AND tr.testOption=:inTestOption " + "AND testRoles=tr " + "AND mt=:inMetaTest "
                    + "AND sIs.testingSession=:inTestingSession");
            query.setParameter("inMetaTest", inMetaTest);
            query.setParameter("inTestOption", TestOption.getTEST_OPTION_REQUIRED());
            query.setParameter("inTestingSession", TestingSession.getSelectedTestingSession());
            List<SystemInSession> list = query.getResultList();
            return list;
        }
        return new ArrayList<SystemInSession>();
    }

    @Override
    public Integer getSystemInSessionListSizeByMetaTest(MetaTest inMetaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemInSessionListSizeByMetaTest");
        }
        if (inMetaTest != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em.createQuery("SELECT count(DISTINCT sIs) " + "FROM SystemInSession as sIs, "
                    + "TestRoles tr JOIN tr.roleInTest.testParticipantsList participants, "
                    + "SystemActorProfiles sap, " + "MetaTest mt JOIN mt.testRolesList testRoles "
                    + "WHERE participants.actorIntegrationProfileOption = sap.actorIntegrationProfileOption "
                    + "AND sap.system=sIs.system " + "AND participants.tested='true' "
                    + "AND tr.testOption=:inTestOption " + "AND testRoles=tr " + "AND mt=:inMetaTest "
                    + "AND sIs.testingSession=:inTestingSession");
            query.setParameter("inMetaTest", inMetaTest);
            query.setParameter("inTestingSession", TestingSession.getSelectedTestingSession());
            query.setParameter("inTestOption", TestOption.getTEST_OPTION_REQUIRED());
            return ((Long) query.getSingleResult()).intValue();
        }
        return 0;
    }

    @Override
    public void editTestAssignment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestAssignment");
        }
        showEditTestAssignmentPanel = true;
        availableTestRoles = new ArrayList<TestRoles>();
        selectedTestRoles = new ArrayList<TestRoles>();
        selectedDomain = null;
        selectedActor = null;
        selectedIntegrationProfile = null;
        selectedCriterion = null;
    }

    @Override
    public void deleteSelectedMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedMetaTest");
        }
        deleteMetaTest(selectedMetaTest);
    }

    @Override
    public void initSelectedMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initSelectedMetaTest");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String pageid = params.get("id");
        this.selectedMetaTest = null;
        if (pageid != null) {
            if (!pageid.isEmpty()) {
                try {
                    int id = Integer.valueOf(pageid);
                    this.selectedMetaTest = em.find(MetaTest.class, id);
                } catch (NumberFormatException e) {

                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.ProblemIdFormat']}");
                }
            }
        }
    }

    @Override
    public String getPermalinkToMetaTest(MetaTest metaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPermalinkToMetaTest");
        }
        if ((metaTest != null) && (metaTest.getId() != null)) {
            return ApplicationPreferenceManager.instance().getApplicationUrl() + "testing/metatest/metaTest.seam?id=" //DONE
                    + metaTest.getId();
        }
        return null;
    }

    @Override
    public String editMetaTestAndGoToMetaTestList(MetaTest mt) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editMetaTestAndGoToMetaTestList");
        }
        this.editMetaTest(mt);
        return "/testing/testsDefinition/metaTestList.xhtml"; //DONE
    }

    @Override
    public void removeSelectedTestRoleFromSelectedMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeSelectedTestRoleFromSelectedMetaTest");
        }
        if (this.selectedTestRole != null) {
            this.removeTestFromSelectedMetaTest(this.selectedTestRole);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Selected TestRole is deleted from the current MetaTest.");
        }
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }
}
