/*
 * Copyright 2008 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

/**
 * This manager manages both the list and creation of a role in test and list and creation of test roles
 *
 * @author jbmeyer
 */

@Name("roleInTestManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("RoleInTestManagerLocal")
public class RoleInTestManager implements RoleInTestManagerLocal, Serializable, QueryModifier<RoleInTest> {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = 123933585461830L;
    private static final Logger LOG = LoggerFactory.getLogger(RoleInTestManager.class);
    private static int MAX_PARTICIPANTS = 99;
    private static String SEARCH_BY_DOMAIN_AIPO = "Search By Domain/AIPO";
    private static String SEARCH_BY_TRANSACTION = "Search By Transaction";
    private static String SEARCH_BY_TRANSACTION_INITIATOR = "Search By Transaction Initiator";
    private static String SEARCH_BY_TRANSACTION_RESPONDER = "Search By Transaction Responder";
    public List<TestParticipants> testParticipantsForTestRole;
    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;
    private FilterDataModel<RoleInTest> foundRoleInTests;
    private boolean showRoleInTestFound = false;
    private RoleInTest selectedRoleInTest;
    private Domain selectedDomain;
    private TestParticipants selectedTestParticipants;
    private String findRoleInTestField;
    private boolean displayRoleInTestList = true;
    private boolean displayAddParticipantPanel = false;
    private boolean displayAddRoleInTest = false;
    private Test selectedTest;
    private Actor selectedActor;
    private IntegrationProfile selectedIntegrationProfile;
    private IntegrationProfileOption selectedIntegrationProfileOption;
    private List<TestRoles> listOfTestRoles;
    private TestRoles selectedTestRoles;
    private String roleInTestKeywordTyped;
    private String selectedCriterion;
    private String selectedInitiatorResponderCriterion;
    private Transaction selectedTransaction;
    private List<TestParticipants> selectedTestParticipantsList;
    private Boolean isWideSearch;
    private Boolean isPlayedByAToolSearch;
    private boolean roleInTestKeywordTypedUsed = false;

    public RoleInTestManager() {

    }

    @Override
    public List<TestParticipants> getTestParticipantsForTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestParticipantsForTestRole");
        }
        return testParticipantsForTestRole;
    }

    @Override
    public void setTestParticipantsForTestRole(List<TestParticipants> testParticipantsForTestRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestParticipantsForTestRole");
        }
        this.testParticipantsForTestRole = testParticipantsForTestRole;
    }

    @Override
    public String getSelectedInitiatorResponderCriterion() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInitiatorResponderCriterion");
        }
        return selectedInitiatorResponderCriterion;
    }

    @Override
    public void setSelectedInitiatorResponderCriterion(String selectedInitiatorResponderCriterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInitiatorResponderCriterion");
        }
        this.selectedInitiatorResponderCriterion = selectedInitiatorResponderCriterion;
    }

    @Override
    public boolean isShowRoleInTestFound() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowRoleInTestFound");
        }
        return showRoleInTestFound;
    }

    @Override
    public void setShowRoleInTestFound(boolean showRoleInTestFound) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowRoleInTestFound");
        }
        this.showRoleInTestFound = showRoleInTestFound;
    }

    @Override
    public FilterDataModel<RoleInTest> getFoundRoleInTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundRoleInTests");
        }
        if (foundRoleInTests == null) {
            RoleInTestQuery query = new RoleInTestQuery();
            HQLCriterionsForFilter<RoleInTest> hqlCriterions = query.getHQLCriterionsForFilter();
            TMCriterions.addAIPOCriterions(hqlCriterions, query.testParticipantsList().actorIntegrationProfileOption());
            hqlCriterions.addQueryModifier(this);
            Filter<RoleInTest> filter = new Filter<RoleInTest>(hqlCriterions);
            foundRoleInTests = new FilterDataModel<RoleInTest>(filter) {
                @Override
                protected Object getId(RoleInTest t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        foundRoleInTests.getFilter().modified();
        return foundRoleInTests;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<RoleInTest> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        if ((isPlayedByAToolSearch != null) && isPlayedByAToolSearch) {
            queryBuilder
                    .addRestriction(HQLRestrictions.eq("isRolePlayedByATool", true));
        }

        String searchField;
        if (roleInTestKeywordTypedUsed) {
            searchField = StringUtils.trimToNull(roleInTestKeywordTyped);
        } else {
            searchField = StringUtils.trimToNull(findRoleInTestField);
        }


        if (searchField != null) {
            searchField = "%" + searchField + "%";

            if ((isWideSearch != null) && isWideSearch) {
                queryBuilder
                        .addRestriction(HQLRestrictions.or(
                                HQLRestrictions.like("keyword", searchField),
                                HQLRestrictions
                                        .like("testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile.keyword",
                                                searchField),
                                HQLRestrictions
                                        .like("testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile.name",
                                                searchField),
                                HQLRestrictions
                                        .like("testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile.actor.keyword",
                                                searchField),
                                HQLRestrictions
                                        .like("testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile.actor.name",
                                                searchField),
                                HQLRestrictions
                                        .like("testParticipantsList.actorIntegrationProfileOption.integrationProfileOption.keyword",
                                                searchField),
                                HQLRestrictions
                                        .like("testParticipantsList.actorIntegrationProfileOption.integrationProfileOption.name",
                                                searchField)));
            } else {
                queryBuilder.addRestriction(HQLRestrictions.like("keyword", searchField));
            }
        }
        queryBuilder.addOrder("keyword", true);
    }

    @Override
    public List<TestParticipants> getSelectedTestParticipantsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestParticipantsList");
        }
        return selectedTestParticipantsList;
    }

    @Override
    public void setSelectedTestParticipantsList(List<TestParticipants> selectedTestParticipantsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestParticipantsList");
        }
        this.selectedTestParticipantsList = selectedTestParticipantsList;
    }

    @Override
    public String getSEARCH_BY_DOMAIN_AIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSEARCH_BY_DOMAIN_AIPO");
        }
        return SEARCH_BY_DOMAIN_AIPO;
    }

    @Override
    public String getSEARCH_BY_TRANSACTION() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSEARCH_BY_TRANSACTION");
        }
        return SEARCH_BY_TRANSACTION;
    }

    @Override
    public Transaction getSelectedTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTransaction");
        }
        return selectedTransaction;
    }

    @Override
    public void setSelectedTransaction(Transaction selectedTransaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTransaction");
        }
        this.selectedTransaction = selectedTransaction;
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
    public RoleInTest getSelectedRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedRoleInTest");
        }

        return selectedRoleInTest;
    }

    @Override
    public void setSelectedRoleInTest(RoleInTest selectedRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedRoleInTest");
        }
        this.selectedRoleInTest = selectedRoleInTest;
    }

    @Override
    public void selectRoleInTestAndUpdateKeyword(RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectRoleInTestAndUpdateKeyword");
        }

        this.selectedTestRoles.setRoleInTest(inRoleInTest);
        this.roleInTestKeywordTyped = this.selectedTestRoles.getRoleInTest().getKeyword();
    }

    @Override
    public boolean isDisplayRoleInTestList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayRoleInTestList");
        }
        return displayRoleInTestList;
    }

    @Override
    public void setDisplayRoleInTestList(boolean displayRoleInTestList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayRoleInTestList");
        }
        this.displayRoleInTestList = displayRoleInTestList;
    }

    @Override
    public boolean isDisplayAddParticipantPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayAddParticipantPanel");
        }
        return displayAddParticipantPanel;
    }

    @Override
    public void setDisplayAddParticipantPanel(boolean displayAddParticipantPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAddParticipantPanel");
        }
        this.displayAddParticipantPanel = displayAddParticipantPanel;
    }

    @Override
    public boolean isDisplayAddRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayAddRoleInTest");
        }
        return displayAddRoleInTest;
    }

    @Override
    public void setDisplayAddRoleInTest(boolean displayAddRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAddRoleInTest");
        }
        this.displayAddRoleInTest = displayAddRoleInTest;
    }

    @Override
    public String getFindRoleInTestField() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFindRoleInTestField");
        }
        return findRoleInTestField;
    }

    @Override
    public void setFindRoleInTestField(String findRoleInTestField) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFindRoleInTestField");
        }
        this.findRoleInTestField = findRoleInTestField;
        this.selectedRoleInTest = null;
        roleInTestKeywordTypedUsed = false;
        getFoundRoleInTests().getFilter().modified();
    }

    @Override
    public String setFindRoleInTestFieldToNullAndsetRoleInTestKeywordSearch(RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFindRoleInTestFieldToNullAndsetRoleInTestKeywordSearch");
        }

        roleInTestKeywordTyped = null;
        setFindRoleInTestField(inRoleInTest.getKeyword());
        selectedRoleInTest = RoleInTest.getRoleInTestWithAllParameters(inRoleInTest);
        return "/testing/roleInTest/listRoleInTest.seam";//DONE
    }

    @Override
    public TestRoles getSelectedTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestRoles");
        }
        return selectedTestRoles;
    }

    @Override
    public void setSelectedTestRoles(TestRoles selectedTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestRoles");
        }
        this.selectedTestRoles = selectedTestRoles;
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
    public IntegrationProfileOption getSelectedIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfileOption");
        }
        return selectedIntegrationProfileOption;
    }

    @Override
    public void setSelectedIntegrationProfileOption(IntegrationProfileOption selectedIntegrationProfileOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfileOption");
        }
        this.selectedIntegrationProfileOption = selectedIntegrationProfileOption;
    }

    @Override
    public void selectTestRolesAndUpdateKeyword(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectTestRolesAndUpdateKeyword");
        }
        if (inTestRoles == null) {
            LOG.warn("in test role is null !");
            return;
        }

        int roleInTestId = inTestRoles.getRoleInTest().getId();

        this.selectedTestRoles = new TestRoles(inTestRoles);
        this.selectedTestRoles.setId(inTestRoles.getId());
        this.selectedTestRoles.getRoleInTest().setId(roleInTestId);

        if (selectedTestRoles.getRoleInTest() != null) {
            this.roleInTestKeywordTyped = selectedTestRoles.getRoleInTest().getKeyword();
        }

    }

    @Override
    public String getRoleInTestKeywordTyped() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTestKeywordTyped");
        }
        return roleInTestKeywordTyped;
    }

    @Override
    public void setRoleInTestKeywordTyped(String roleInTestKeywordTyped) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTestKeywordTyped");
        }
        this.roleInTestKeywordTyped = roleInTestKeywordTyped;
        roleInTestKeywordTypedUsed = true;
    }

    @Override
    public List<TestRoles> getListOfTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestRoles");
        }
        return listOfTestRoles;
    }

    @Override
    public void setListOfTestRoles(List<TestRoles> listOfTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfTestRoles");
        }
        this.listOfTestRoles = listOfTestRoles;
    }

    @Override
    public void findRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findRoleInTest");
        }
        closeAddAParticipantPanel();
        closeAddARoleInTestPanel();
        this.foundRoleInTests.resetCache();
    }

    @Override
    public void findAllRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findAllRoleInTest");
        }
        this.foundRoleInTests.resetCache();
        this.setFindRoleInTestField(null);

        selectedRoleInTest = null;

        closeAddAParticipantPanel();
        closeAddARoleInTestPanel();
    }

    @Override
    public void initTestParticipant() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTestParticipant");
        }

        selectedTestParticipants = new TestParticipants();
        ActorIntegrationProfileOption aipo = new ActorIntegrationProfileOption();
        ActorIntegrationProfile aip = new ActorIntegrationProfile();
        IntegrationProfileOption ipo = new IntegrationProfileOption();

        aipo.setActorIntegrationProfile(aip);
        aipo.setIntegrationProfileOption(ipo);

        selectedTestParticipants.setActorIntegrationProfileOption(aipo);
        selectedTestParticipants.setTested(true);

        closeAddAParticipantPanel();
        closeAddARoleInTestPanel();
        getFoundRoleInTests().resetCache();
    }

    @Override
    public void listTestRolesForSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTestRolesForSelectedTest");
        }
        if (selectedTest == null) {
            return;
        }

        listOfTestRoles = TestRoles
                .getTestsFiltered(null, null, null, null, selectedTest, null, null, null, null, null);

        if (selectedTestRoles == null) {
            selectedTestRoles = new TestRoles();
            selectedTestRoles.setCardMin(0);
            selectedTestRoles.setCardMax(1);
            selectedTestRoles.setTestOption(TestOption.getTEST_OPTION_REQUIRED());
        }

    }

    @Override
    public String displayTestRolesPageForSelectedTestIntegrationProfileActor(Test inTest, IntegrationProfile inIP,
                                                                             Actor inActor) {

        selectedTest = inTest;
        selectedIntegrationProfile = inIP;
        selectedActor = inActor;

        return "/testing/roleInTest/testRolesTableForTestRequirements.seam";//DONE

    }

    @Override
    public void listTestRolesForSelectedTestIntegrationProfileActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTestRolesForSelectedTestIntegrationProfileActor");
        }
        if ((selectedTest == null) || (selectedIntegrationProfile == null) || (selectedActor == null)) {
            return;
        }

        listOfTestRoles = TestRoles.getTestsFiltered(null, selectedIntegrationProfile, null, selectedActor,
                selectedTest, null, null, null, null, null);

        if (selectedTestRoles == null) {
            selectedTestRoles = new TestRoles();
            selectedTestRoles.setCardMin(0);
            selectedTestRoles.setCardMax(1);
            selectedTestRoles.setTestOption(TestOption.getTEST_OPTION_REQUIRED());
        }

    }

    @Override
    public Test getSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTest");
        }
        return selectedTest;
    }

    @Override
    public void setSelectedTest(Test selectedTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTest");
        }
        this.selectedTest = selectedTest;
    }

    @Override
    public void updateAllTestParticipants(RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateAllTestParticipants");
        }
        if ((inRoleInTest != null) && (inRoleInTest.getTestParticipantsList() != null)) {
            List<TestParticipants> tpList = new ArrayList<TestParticipants>(inRoleInTest.getTestParticipantsList());
            for (TestParticipants tp : inRoleInTest.getTestParticipantsList()) {

                List<TestParticipants> tpL = TestParticipants.getTestParticipantsFiltered(
                        tp.getActorIntegrationProfileOption(), null, null, null, null,
                        inRoleInTest.getIsTestedForAllParticipants());
                TestParticipants tpNew = null;
                if ((tpL != null) && (tpL.size() > 0)) {
                    tpNew = tpL.get(0);

                } else {
                    tpNew = new TestParticipants();
                    tpNew.setActorIntegrationProfileOption(tp.getActorIntegrationProfileOption());
                    tpNew.setTested(inRoleInTest.getIsTestedForAllParticipants());

                    tpNew = entityManager.merge(tpNew);
                }

                tpList.remove(tp);
                tpList.add(tpNew);
            }

            inRoleInTest.setTestParticipantsList(tpList);
            entityManager.merge(inRoleInTest);
            entityManager.flush();
            this.getFoundRoleInTests().getFilter().modified();
        }
    }

    private void updateTestParticipant(RoleInTest inRoleInTest, TestParticipants inSelectedTestParticipants,
                                       boolean inValue) {
        if (inSelectedTestParticipants != null) {
            entityManager.clear();
            List<TestParticipants> tpList = TestParticipants.getTestParticipantsFiltered(
                    inSelectedTestParticipants.getActorIntegrationProfileOption(), null, null, null, null, inValue);
            TestParticipants tp = null;
            if ((tpList != null) && (tpList.size() > 0)) {
                tp = tpList.get(0);

            } else {
                tp = new TestParticipants();
                tp.setActorIntegrationProfileOption(inSelectedTestParticipants.getActorIntegrationProfileOption());
                tp.setTested(inSelectedTestParticipants.getTested());

                tp = entityManager.merge(tp);
                entityManager.flush();
            }

            inRoleInTest.getTestParticipantsList().remove(inSelectedTestParticipants);
            inRoleInTest.getTestParticipantsList().add(tp);

            entityManager.merge(inRoleInTest);

            entityManager.flush();

            Collections.sort(new ArrayList(inRoleInTest.getTestParticipantsList()));
            this.getFoundRoleInTests().getFilter().modified();
        }
    }

    @Override
    public void updateTestParticipant(RoleInTest inRoleInTest, TestParticipants inSelectedTestParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestParticipant");
        }
        Boolean isTested = inSelectedTestParticipants.getTested();
        this.selectedTestParticipants = inSelectedTestParticipants;
        updateTestParticipant(inRoleInTest, this.selectedTestParticipants, isTested);
    }

    @Override
    public void updateTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestRoles");
        }
        if ((roleInTestKeywordTyped == null) || (roleInTestKeywordTyped.trim().length() == 0)) {
            this.showRoleInTestFound = false;
            return;
        }
        if (selectedTest == null) {
            this.showRoleInTestFound = false;
            return;
        }

        if (selectedTestRoles.getRoleInTest() == null) {
            getFoundRoleInTests().getFilter().modified();
            if (this.getFoundRoleInTests().size() == 1) {
                this.showRoleInTestFound = false;
                selectedTestRoles.setRoleInTest((RoleInTest) this.getFoundRoleInTests()
                        .getAllItems(FacesContext.getCurrentInstance()).get(0));
                roleInTestKeywordTyped = selectedTestRoles.getRoleInTest().getKeyword();
            } else {
                this.showRoleInTestFound = true;
                return;
            }
        }
        this.showRoleInTestFound = false;

        List<TestRoles> listOfTestRoles = TestRoles.getTestsFiltered(null, null, null, null, selectedTest, null, null,
                null, null, null);
        if (!TestRoles.testRolesContainsKeyword(roleInTestKeywordTyped, listOfTestRoles)
                || (selectedTestRoles.getId() != null)) {
            if (selectedTestRoles.getCardMin() == null) {
                selectedTestRoles.setCardMin(1);
            }
            if (selectedTestRoles.getCardMax() == null) {
                selectedTestRoles.setCardMax(1);
            }

            if (selectedTestRoles.getCardMax() < selectedTestRoles.getCardMin()) {
                int tmp = selectedTestRoles.getCardMax();
                selectedTestRoles.setCardMax(selectedTestRoles.getCardMin());
                selectedTestRoles.setCardMin(tmp);
            }

            if (selectedTestRoles.getCardMax() > RoleInTestManager.MAX_PARTICIPANTS) {
                selectedTestRoles.setCardMax(RoleInTestManager.MAX_PARTICIPANTS);
            }
            selectedTestRoles.setTest(selectedTest);

            try {
                selectedTestRoles = entityManager.merge(selectedTestRoles);

                entityManager.flush();

                selectedTestRoles = null;

            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Errors : " + e.getMessage());
            }
            cancelEditingTestRoles();
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This test already contains the role " + roleInTestKeywordTyped);
        }

    }

    @Override
    public void cancelEditingTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelEditingTestRoles");
        }
        selectedTestRoles = new TestRoles();
        selectedTestRoles.setCardMin(0);
        selectedTestRoles.setCardMax(1);
        selectedTestRoles.setTestOption(TestOption.getTEST_OPTION_REQUIRED());
        roleInTestKeywordTyped = null;
    }

    @Override
    public void deleteTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestRole");
        }
        if (selectedTestRoles == null) {
            return;
        }

        try {
            selectedTestRoles = entityManager.find(TestRoles.class, selectedTestRoles.getId());
            entityManager.remove(selectedTestRoles);
            entityManager.flush();
            cancelEditingTestRoles();
        } catch (PersistenceException pex) {
            Throwable throwable = pex.getCause();
            if (throwable instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) throwable;
                SQLException sqlException = cve.getSQLException().getNextException();
                if (sqlException != null) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Problem deleting role, there is a constraint exception : " + sqlException.getMessage());
                }

            }
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem deleting role " + e.getMessage());
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public List<IntegrationProfile> getPossibleIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfiles");
        }

        if ((selectedDomain == null) || (selectedDomain.getId() == null)) {
            return null;
        }

        List<IntegrationProfile> listOfIPs = entityManager.createQuery(
                "select distinct ip from IntegrationProfile ip join ip.domainsForDP  d where d.id = "
                        + selectedDomain.getId() + " order by ip.keyword").getResultList();

        Collections.sort(listOfIPs);

        return listOfIPs;
    }

    @Override
    public List<Actor> getPossibleActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActors");
        }
        List<Actor> actorL = null;
        if ((selectedIntegrationProfile != null) && (selectedIntegrationProfile.getId() != null)) {
            List<ActorIntegrationProfileOption> listOfTestParticipants = ActorIntegrationProfileOption
                    .getActorIntegrationProfileOptionFiltered(null, selectedIntegrationProfile, null, null);

            HashSet<Actor> setOfActor = new HashSet<Actor>();
            if (listOfTestParticipants == null) {
                return null;
            }
            for (ActorIntegrationProfileOption tp : listOfTestParticipants) {
                setOfActor.add(tp.getActorIntegrationProfile().getActor());
            }

            actorL = new ArrayList<Actor>(setOfActor);
            Collections.sort(actorL);

        }

        return actorL;
    }

    @Override
    public List<IntegrationProfileOption> getPossibleOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleOptions");
        }
        List<IntegrationProfileOption> ipOL = null;
        if ((selectedIntegrationProfile != null) && (selectedIntegrationProfile.getId() != null)
                && (selectedActor != null) && (selectedActor.getId() != null)) {
            List<ActorIntegrationProfileOption> listOfTestParticipants = ActorIntegrationProfileOption
                    .getActorIntegrationProfileOptionFiltered(null, selectedIntegrationProfile, null, selectedActor);

            HashSet<IntegrationProfileOption> setOfOption = new HashSet<IntegrationProfileOption>();
            if (listOfTestParticipants == null) {
                return null;
            }
            for (ActorIntegrationProfileOption tp : listOfTestParticipants) {
                setOfOption.add(tp.getIntegrationProfileOption());
            }

            ipOL = new ArrayList<IntegrationProfileOption>(setOfOption);
            Collections.sort(ipOL);
            if (ipOL.size() == 1) {
                selectedTestParticipants.getActorIntegrationProfileOption().setIntegrationProfileOption(ipOL.get(0));
            }
        }

        return ipOL;
    }

    @Override
    public void reinit(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reinit");
        }

        if (o instanceof Domain) {

            selectedIntegrationProfile = null;
            selectedActor = null;
            selectedIntegrationProfileOption = null;

        } else if (o instanceof IntegrationProfile) {
            selectedActor = null;
            selectedIntegrationProfileOption = null;

        } else if (o instanceof Actor) {
            selectedIntegrationProfileOption = null;

        }
    }

    @Override
    public List<Domain> getListOfDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfDomain");
        }
        DomainQuery query = new DomainQuery();
        query.keyword().order(true);
        return query.getList();
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
    public TestParticipants getSelectedTestParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestParticipants");
        }
        return selectedTestParticipants;
    }

    @Override
    public void setSelectedTestParticipants(TestParticipants selectedTestParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestParticipants");
        }
        this.selectedTestParticipants = selectedTestParticipants;
    }

    @Override
    public boolean verifyRoleInTestKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("verifyRoleInTestKeyword");
        }

        RoleInTestQuery roleInTestQuery = new RoleInTestQuery();
        roleInTestQuery.keyword().eq(selectedRoleInTest.getKeyword());
        List<RoleInTest> listOfRit = roleInTestQuery.getList();
        boolean toReturn;

        if (selectedRoleInTest.getKeyword().trim().length() == 0) {
            toReturn = false;
        } else if (listOfRit.size() == 0) {
            toReturn = true;
        } else {
            if (listOfRit.size() > 1) {
                toReturn = false;
            } else {
                if (listOfRit.get(0).getId().equals(selectedRoleInTest.getId())) {
                    toReturn = true;
                } else {
                    toReturn = false;
                }
            }
        }

        return toReturn;
    }

    @Override
    public void addOrUpdateRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addOrUpdateRole");
        }

        if (selectedRoleInTest == null) {
            return;
        }

        if ((selectedRoleInTest.getKeyword() != null) && (selectedRoleInTest.getKeyword().trim().length() > 0)) {
            if (verifyRoleInTestKeyword()) {
                selectedRoleInTest = entityManager.merge(selectedRoleInTest);
                entityManager.flush();
                this.getFoundRoleInTests().getFilter().modified();
                if (displayAddRoleInTest) {
                    closeAddARoleInTestPanel();
                }
                findAllRoleInTest();
                clearFilter();
            } else {
                FacesMessages.instance().addToControl("keyword", "Keyword already exists");
            }
        } else {
            FacesMessages.instance().addToControl("keyword", "Keyword is null");
        }
    }

    public void addParticipantToRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addParticipantToRoleInTest");
        }

        RoleInTestTestParticipantsManager roleInTestTestParticipantsManager = new RoleInTestTestParticipantsManager(
                selectedRoleInTest, selectedTestParticipants, selectedActor, selectedIntegrationProfile,
                selectedIntegrationProfileOption);
        roleInTestTestParticipantsManager.addParticipantToRoleInTest();
        this.getFoundRoleInTests().getFilter().modified();
        foundRoleInTests.resetCache();
        closeAddAParticipantPanel();
    }

    @Override
    public void deleteSelectedRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedRoleInTest");
        }

        if (selectedRoleInTest == null) {
            return;
        }

        try {
            selectedRoleInTest = entityManager.find(RoleInTest.class, selectedRoleInTest.getId());
            deleteAllTestParticipants();
            entityManager.remove(selectedRoleInTest);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Role deleted");
            findRoleInTestField = null;
            selectedRoleInTest = null;
            this.getFoundRoleInTests().getFilter().modified();
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to delete this role");
            LOG.error("" + e.getMessage());
        }
    }

    public boolean isRoleInTestContainsTestParticipants(RoleInTest roleInTest) {
        return roleInTest.getTestParticipantsList() != null && !roleInTest.getTestParticipantsList().isEmpty();
    }

    public boolean isRoleInTestContainsTestParticipants() {
        return selectedRoleInTest != null && isRoleInTestContainsTestParticipants(selectedRoleInTest);
    }

    public boolean isRoleInTestContainsTestRoles(RoleInTest roleInTest) {
        return roleInTest.getTestRoles() != null && !roleInTest.getTestRoles().isEmpty();
    }

    public boolean isRoleInTestContainsTestRoles() {
        return selectedRoleInTest != null && isRoleInTestContainsTestRoles(selectedRoleInTest);
    }

    public List<Test> getTestsUsingSelectedRoleInTest() {
        ArrayList<Test> testsUsingSelectedRoleInTest = new ArrayList<>();
        if (selectedRoleInTest != null) {
            for (TestRoles testRoles : selectedRoleInTest.getTestRoles()) {
                testsUsingSelectedRoleInTest.add(testRoles.getTest());
            }
        }
        return testsUsingSelectedRoleInTest;
    }

    @Override
    public void setRoleInTestAndSelectedTestParticipant(RoleInTest inCurrentRoleInTest,
                                                        TestParticipants inTestParticipant) {

        selectedRoleInTest = inCurrentRoleInTest;
        selectedTestParticipants = inTestParticipant;
    }

    @Override
    public void deleteSelectedTestParticipant() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedTestParticipant");
        }

        if ((selectedRoleInTest == null) || (selectedRoleInTest.getId() == null) || (selectedTestParticipants == null)
                || (selectedTestParticipants.getId() == null)) {
            return;
        }

        selectedTestParticipants = (TestParticipants) entityManager.createQuery(
                "FROM TestParticipants fetch all properties where id=" + selectedTestParticipants.getId())
                .getSingleResult();
        selectedRoleInTest.getTestParticipantsList().remove(selectedTestParticipants);
        selectedRoleInTest = entityManager.merge(selectedRoleInTest);
        entityManager.flush();
        this.getFoundRoleInTests().getFilter().modified();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test participant removed.");
    }

    @Override
    public void deleteAllTestParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllTestParticipants");
        }

        if (selectedRoleInTest == null) {
            return;
        }

        if (selectedRoleInTest.getTestParticipantsList() != null) {
            selectedRoleInTest.getTestParticipantsList().clear();
            selectedRoleInTest = entityManager.merge(selectedRoleInTest);
            entityManager.flush();
            this.getFoundRoleInTests().getFilter().modified();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "All test participants are deleted.");
        }

    }

    @Override
    public void openAddATestParticipantPanel(RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openAddATestParticipantPanel");
        }
        selectedRoleInTest = inRoleInTest;
        displayRoleInTestList = true;
        displayAddParticipantPanel = true;
        selectedCriterion = SEARCH_BY_DOMAIN_AIPO;
        selectedInitiatorResponderCriterion = SEARCH_BY_TRANSACTION_INITIATOR;
        selectedTestParticipantsList = new ArrayList<TestParticipants>();
        selectedTransaction = null;
    }

    @Override
    public void openAddRoleInTestPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openAddRoleInTestPanel");
        }
        selectedRoleInTest = new RoleInTest();
        displayRoleInTestList = false;
        displayAddParticipantPanel = false;
        displayAddRoleInTest = true;
    }

    @Override
    public void openAddRoleInTestPanel(RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openAddRoleInTestPanel");
        }
        selectedRoleInTest = inRoleInTest;
        displayRoleInTestList = false;
        displayAddRoleInTest = true;
    }

    @Override
    public void closeAddAParticipantPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeAddAParticipantPanel");
        }
        displayRoleInTestList = true;
        displayAddParticipantPanel = false;

    }

    @Override
    public void closeAddARoleInTestPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeAddARoleInTestPanel");
        }
        displayRoleInTestList = true;
        displayAddRoleInTest = false;
    }

    @Override
    public void closeAndCancelAddARoleInTestPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeAndCancelAddARoleInTestPanel");
        }
        closeAddARoleInTestPanel();
        selectedRoleInTest = null;
    }

    @Override
    public String backToTestsMatrix() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("backToTestsMatrix");
        }
        return "/testing/testsDefinition/listAllTestsRequirements.xhtml";//DONE
    }

    @Override
    public List<String> getCriteriaSelectionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriteriaSelectionList");
        }
        List<String> criteriaList = new ArrayList<String>();
        criteriaList.add(SEARCH_BY_DOMAIN_AIPO);
        criteriaList.add(SEARCH_BY_TRANSACTION);
        return criteriaList;
    }

    @Override
    public List<String> getInitiatorResponderList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInitiatorResponderList");
        }
        List<String> initiatorResponderList = new ArrayList<String>();
        initiatorResponderList.add(SEARCH_BY_TRANSACTION_INITIATOR);
        initiatorResponderList.add(SEARCH_BY_TRANSACTION_RESPONDER);
        return initiatorResponderList;
    }

    @Override
    public List<TestParticipants> getActorIntegrationProfileOptionListForSelectedTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfileOptionListForSelectedTransaction");
        }
        List<ActorIntegrationProfileOption> aipoList = null;
        if (selectedTransaction != null) {

            if (selectedInitiatorResponderCriterion != null) {
                if (selectedInitiatorResponderCriterion.equals(SEARCH_BY_TRANSACTION_INITIATOR)) {
                    aipoList = ProfileLink.getAIPOByTransactionForActorActingAsFromActor(selectedTransaction);
                }

                if (selectedInitiatorResponderCriterion.equals(SEARCH_BY_TRANSACTION_RESPONDER)) {
                    aipoList = ProfileLink.getAIPOByTransactionForActorActingAsToActor(selectedTransaction);
                }
            }
        }
        List<TestParticipants> testParticipants = new ArrayList<TestParticipants>();
        if (aipoList != null) {
            for (ActorIntegrationProfileOption aipo : aipoList) {
                List<TestParticipants> tmp = TestParticipants.getTestParticipantsFiltered(aipo, null, null, null, null,
                        null);
                testParticipants.addAll(tmp);

            }

            return new ArrayList<TestParticipants>(testParticipants);
        } else {
            return new ArrayList<TestParticipants>();
        }
    }

    @Override
    public void addSelectedAIPOToRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSelectedAIPOToRoleInTest");
        }

        if (selectedRoleInTest != null) {
            selectedTestParticipantsList = getActorIntegrationProfileOptionListForSelectedTransaction();
            if (selectedTestParticipantsList != null) {
                if (selectedRoleInTest.getTestParticipantsList() == null) {
                    selectedRoleInTest.setTestParticipantsList(new ArrayList<TestParticipants>());
                }
                for (TestParticipants tp : selectedTestParticipantsList) {
                    if (!selectedRoleInTest.getTestParticipantsList().contains(tp)) {
                        updateTestParticipant(selectedRoleInTest, tp, true);
                    }
                }
            }
        }
        closeAddAParticipantPanel();
    }

    @Override
    public RoleInTest updateRoleInTest(RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateRoleInTest");
        }
        if (inRoleInTest != null) {
            inRoleInTest = entityManager.merge(inRoleInTest);
            entityManager.flush();
            this.getFoundRoleInTests().getFilter().modified();
        }
        return inRoleInTest;
    }

    @Override
    public Boolean getIsWideSearch() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIsWideSearch");
        }
        return isWideSearch;
    }

    @Override
    public void setIsWideSearch(Boolean isWideSearch) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIsWideSearch");
        }
        this.isWideSearch = isWideSearch;
    }

    @Override
    public void clearFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearFilter");
        }
        this.foundRoleInTests.resetCache();
        this.foundRoleInTests.getFilter().clear();
        this.findRoleInTestField = null;
    }

    @Override
    public List<TestParticipants> getTestParticipantsForTestRole(TestRoles role) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestParticipantsForTestRole");
        }
        TestParticipantsQuery query = new TestParticipantsQuery();
        query.roleInTest().testRoles().id().eq(role.getId());
        setTestParticipantsForTestRole(query.getListDistinct());
        return getTestParticipantsForTestRole();
    }

    public Boolean getIsPlayedByAToolSearch() {
        return isPlayedByAToolSearch;
    }

    public void setIsPlayedByAToolSearch(Boolean playedByAToolSearch) {
        isPlayedByAToolSearch = playedByAToolSearch;
    }
}
