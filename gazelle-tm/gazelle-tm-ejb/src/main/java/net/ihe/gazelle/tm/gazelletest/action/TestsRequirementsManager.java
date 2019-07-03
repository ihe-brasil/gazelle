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

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.util.Pair;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;


@Name("testsRequirementsManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("TestsRequirementsManagerLocal")
public class TestsRequirementsManager implements TestsRequirementsManagerLocal, Serializable {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = 123933585424530L;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TestsRequirementsManager.class);

    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;

    private List<Actor> listOfActorsForCurrentIntegrationProfile;
    private List<Test> listOfTestsForCurrentIntegrationProfile;

    private TestType selectedTestType = TestType.getTYPE_CONNECTATHON();

    private List<Test> listOfTestsAvailableAfterSearch;
    private String selectedTestString;
    private String selectedActorString;

    private Domain domainSelected;
    private IntegrationProfile integrationProfileSelected;

    private List<TestPeerType> possibleTestPeerTypes;
    /**
     * Variable used to filter tests by Peer type
     */
    private TestPeerType selectedTestPeerType;

    private List<String> editTests = new ArrayList<String>();
    private Actor selectedActor;
    private Test selectedTest;
    private TestOption selectedOption;
    private IntegrationProfileOption selectedIPOption;
    private String messageToDisplayForConfirmation;
    private Integer cardMin;
    private Integer cardMax;

    private TestRoles currentTestRole;

    @Override
    public List<Actor> getListOfActorsForCurrentIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfActorsForCurrentIntegrationProfile");
        }
        return listOfActorsForCurrentIntegrationProfile;
    }

    @Override
    public void setListOfActorsForCurrentIntegrationProfile(List<Actor> listOfActorsForCurrentIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfActorsForCurrentIntegrationProfile");
        }
        this.listOfActorsForCurrentIntegrationProfile = listOfActorsForCurrentIntegrationProfile;
    }

    @Override
    public void findTestPeerTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestPeerTypes");
        }
        possibleTestPeerTypes = TestPeerType.getPeerTypeList();
    }

    @Override
    public List<TestPeerType> getPossibleTestPeerTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestPeerTypes");
        }
        if (possibleTestPeerTypes == null) {
            findTestPeerTypes();
        }
        return possibleTestPeerTypes;
    }

    @Override
    public TestType getSelectedTestType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestType");
        }
        return selectedTestType;
    }

    @Override
    public void setSelectedTestType(TestType selectedTestType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestType");
        }
        this.selectedTestType = selectedTestType;
    }

    @Override
    public String getSelectedActorString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActorString");
        }
        return selectedActorString;
    }

    @Override
    public void setSelectedActorString(String selectedActorString) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActorString");
        }
        this.selectedActorString = selectedActorString;
    }

    @Override
    public TestPeerType getSelectedTestPeerType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestPeerType");
        }
        return selectedTestPeerType;
    }

    @Override
    public void setSelectedTestPeerType(TestPeerType selectedTestPeerType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestPeerType");
        }
        this.selectedTestPeerType = selectedTestPeerType;
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public void initTestRequirements() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTestRequirements");
        }
        if (integrationProfileSelected == null) {
            listOfActorsForCurrentIntegrationProfile = null;
            listOfTestsForCurrentIntegrationProfile = null;
        } else {
            listOfTestsForCurrentIntegrationProfile = TestRoles.getTestFiltered(
                    Collections.singletonList(selectedTestType), TestStatus.getSTATUS_READY(), null, null, null, null,
                    integrationProfileSelected, null, null, true, selectedTestPeerType);

            if (listOfTestsForCurrentIntegrationProfile != null) {
                Collections.sort(listOfTestsForCurrentIntegrationProfile);
            }

            listOfActorsForCurrentIntegrationProfile = TestRoles.getDistinctActors(TestRoles.getTestsFiltered(null,
                    integrationProfileSelected, null, null, null, Collections.singletonList(selectedTestType), null,
                    null, null, true));

            if (listOfActorsForCurrentIntegrationProfile != null) {
                Collections.sort(listOfActorsForCurrentIntegrationProfile);
            }

        }

    }

    @Override
    public List<Test> getListOfTestsForCurrentIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestsForCurrentIntegrationProfile");
        }
        return listOfTestsForCurrentIntegrationProfile;
    }

    @Override
    public void setListOfTestsForCurrentIntegrationProfile(List<Test> listOfTestsForCurrentIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfTestsForCurrentIntegrationProfile");
        }
        this.listOfTestsForCurrentIntegrationProfile = listOfTestsForCurrentIntegrationProfile;
    }

    @Override
    public IntegrationProfile getIntegrationProfileSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileSelected");
        }
        return integrationProfileSelected;
    }

    @Override
    public void setIntegrationProfileSelected(IntegrationProfile integrationProfileSelected) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIntegrationProfileSelected");
        }
        this.integrationProfileSelected = integrationProfileSelected;
    }

    @Override
    public List<String> getEditTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditTests");
        }
        return editTests;
    }

    @Override
    public void setEditTests(List<String> editTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditTests");
        }
        this.editTests = editTests;
    }

    @Override
    public void addTestToMatrix() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestToMatrix");
        }
        if (selectedTestString == null) {
            return;
        }

        listOfTestsAvailableAfterSearch = testAutoComplete();
        if ((listOfTestsAvailableAfterSearch != null) && (listOfTestsAvailableAfterSearch.size() == 1)) {

            selectedTest = listOfTestsAvailableAfterSearch.get(0);
            selectedTestString = selectedTest.getKeyword();
            addTestToMatrixOnly();
        }
    }

    @Override
    public void addTestToMatrix(Test inCurrentTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestToMatrix");
        }
        selectedTest = inCurrentTest;
        addTestToMatrixOnly();
    }

    private void addTestToMatrixOnly() {
        if (!listOfTestsForCurrentIntegrationProfile.contains(selectedTest)) {
            listOfTestsForCurrentIntegrationProfile.add(selectedTest);
        }
    }

    @Override
    public String getMessageToDisplayForConfirmation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageToDisplayForConfirmation");
        }

        return messageToDisplayForConfirmation;
    }

    @Override
    public void setMessageToDisplayForConfirmation(String messageToDisplayForConfirmation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageToDisplayForConfirmation");
        }

        this.messageToDisplayForConfirmation = messageToDisplayForConfirmation;
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
    public List<Test> getListOfTestsAvailableAfterSearch() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestsAvailableAfterSearch");
        }
        if (listOfTestsAvailableAfterSearch != null) {

        } else {

        }
        return listOfTestsAvailableAfterSearch;
    }

    @Override
    public void setListOfTestsAvailableAfterSearch(List<Test> listOfTestsAvailableAfterSearch) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfTestsAvailableAfterSearch");
        }
        this.listOfTestsAvailableAfterSearch = listOfTestsAvailableAfterSearch;
    }

    @Override
    public List<Test> testAutoComplete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testAutoComplete");
        }

        List<Test> returnList = null;
        Session s = (Session) entityManager.getDelegate();
        Criteria c = s.createCriteria(Test.class);

        String searchedString = selectedTestString;

        if (searchedString.trim().equals("*")) {
            c.addOrder(Order.asc("keyword"));
            returnList = c.list();
        } else {
            c.add(Restrictions.or(Restrictions.ilike("name", "%" + searchedString + "%"),
                    Restrictions.ilike("keyword", searchedString + "%")));
            c.addOrder(Order.asc("keyword"));
            returnList = c.list();

        }

        return returnList;
    }

    @Override
    public List<Test> actorAutoComplete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("actorAutoComplete");
        }

        List<Test> returnList = null;
        Session s = (Session) entityManager.getDelegate();
        Criteria c = s.createCriteria(Test.class);

        String searchedString = selectedActorString;

        if (searchedString.trim().equals("*")) {
            c.addOrder(Order.asc("keyword"));
            returnList = c.list();
        } else {
            c.add(Restrictions.or(Restrictions.ilike("name", "%" + searchedString + "%"),
                    Restrictions.ilike("keyword", searchedString + "%")));
            c.addOrder(Order.asc("keyword"));
            returnList = c.list();

        }

        return returnList;
    }

    @Override
    public String getSelectedTestString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestString");
        }
        return selectedTestString;
    }

    @Override
    public void setSelectedTestString(String selectedTestString) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestString");
        }
        this.selectedTestString = selectedTestString;
    }

    @Override
    public boolean displaySelectTestModalPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displaySelectTestModalPanel");
        }
        return ((listOfTestsAvailableAfterSearch != null) && (listOfTestsAvailableAfterSearch.size() > 1));
    }

    @Override
    public List<TestRoles> findTestsRolesForSelectedTestActorIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestsRolesForSelectedTestActorIP");
        }
        List<TestRoles> tests = TestRoles.getTestsFiltered(null, integrationProfileSelected, null, selectedActor,
                selectedTest, null, null, null, null, null);

        return tests;
    }

    @Override
    public void setTestActor(Test inTest, Actor inActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestActor");
        }
        selectedTest = inTest;
        selectedActor = inActor;

        cancelEditTestRole();
    }

    @Override
    public String findTestURLByTestByIPByActorByAIP(Test inTest, IntegrationProfile inIP, Actor inActor,
                                                    IntegrationProfileOption inIntegrationProfileOption) {
        List<TestRoles> tests = TestRoles.getTestsFiltered(null, inIP, inIntegrationProfileOption, inActor, inTest,
                null, null, null, null, true);
        if ((tests != null) && (tests.size() > 0)) {
            return tests.get(0).getUrl();
        }
        return null;
    }

    @Override
    public String findTestURLDocumentationByTestByIPByActorByAIP(Test inTest, IntegrationProfile inIP, Actor inActor,
                                                                 IntegrationProfileOption inIntegrationProfileOption) {
        List<TestRoles> tests = TestRoles.getTestsFiltered(null, inIP, inIntegrationProfileOption, inActor, inTest,
                null, null, null, null, true);
        if ((tests != null) && (tests.size() > 0)) {
            return tests.get(0).getUrlDocumentation();
        }
        return null;
    }

    @Override
    public boolean displayToolTips(Test inTest, IntegrationProfile inIP, Actor inActor,
                                   IntegrationProfileOption inIntegrationProfileOption) {
        String res = null;
        res = findTestURLByTestByIPByActorByAIP(inTest, inIP, inActor, inIntegrationProfileOption);
        if (res != null && !res.isEmpty()) {
            return true;
        }
        res = findTestURLDocumentationByTestByIPByActorByAIP(inTest, inIP, inActor, inIntegrationProfileOption);
        if (res != null && !res.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public List<Pair<TestOption, List<IntegrationProfileOption>>> findTestsRolesForTestActorIP(Test inTest,
                                                                                               Actor inActor, IntegrationProfile inIP) {

        LOG.debug("findTestsRolesForTestActorIP for " + inTest.getKeyword() + " Actor :" + inActor.getKeyword()
                + " IP : " + inIP.getKeyword());
        List<TestRoles> tests = TestRoles.getTestsFiltered(null, inIP, null, inActor, inTest, null, null, null, null,
                true);

        HashMap<TestOption, HashSet<IntegrationProfileOption>> myMap = new HashMap<TestOption, HashSet<IntegrationProfileOption>>();

        for (TestRoles t : tests) {

            if (!myMap.containsKey(t.getTestOption())) {
                myMap.put(t.getTestOption(), new HashSet<IntegrationProfileOption>());
            }

            List<TestParticipants> testParticipantsList = t.getRoleInTest().getTestParticipantsList();

            for (TestParticipants tp : testParticipantsList) {
                if (tp.getActorIntegrationProfileOption().getActorIntegrationProfile().getActor().equals(inActor)
                        && tp.getActorIntegrationProfileOption().getActorIntegrationProfile().getIntegrationProfile()
                        .equals(inIP) && ((tp.getTested() != null) && (tp.getTested().booleanValue() == true))) {
                    myMap.get(t.getTestOption()).add(
                            tp.getActorIntegrationProfileOption().getIntegrationProfileOption());
                }
            }

        }

        List<Pair<TestOption, List<IntegrationProfileOption>>> resultList = new ArrayList<Pair<TestOption, List<IntegrationProfileOption>>>();

        for (Map.Entry<TestOption, HashSet<IntegrationProfileOption>> to : myMap.entrySet()) {
            Pair<TestOption, List<IntegrationProfileOption>> myPair = new Pair<TestOption, List<IntegrationProfileOption>>(
                    to.getKey(), new ArrayList<IntegrationProfileOption>(to.getValue()));

            resultList.add(myPair);
        }

        return resultList;
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
    public TestOption getSelectedOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedOption");
        }
        return selectedOption;
    }

    @Override
    public void setSelectedOption(TestOption inSelectedOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedOption");
        }
        this.selectedOption = inSelectedOption;
    }

    @Override
    public IntegrationProfileOption getSelectedIPOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIPOption");
        }
        return selectedIPOption;
    }

    @Override
    public void setSelectedIPOption(IntegrationProfileOption selectedIPOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIPOption");
        }
        this.selectedIPOption = selectedIPOption;
    }

    // Deprecated by JRC - Sept 3rd, 2009 -
    // TestParticipants was deprecated. It is now deleted because it is replace by ActorIntegrationProfileOption
    // This method was adding (merging) a TestParticipant. As it is now not possible to adding (merging) in the TF from the TM, it is deprecated.
    @Override
    @Deprecated
    public void addTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestRole");
        }
        if (currentTestRole == null) {
            currentTestRole = new TestRoles();
        }
        if (selectedActor == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Selected actor is null");
            return;
        }

        try {

            if ((cardMin != null) && (cardMax != null) && (cardMin <= cardMax)) {

                currentTestRole.setCardMax(cardMax);
                currentTestRole.setCardMin(cardMin);

                currentTestRole.setTestOption(selectedOption);

                currentTestRole = entityManager.merge(currentTestRole);
                entityManager.flush();

                cancelEditTestRole();
            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            cancelEditTestRole();
        }

    }

    @Override
    public void editTestRole(TestRoles inTestRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestRole");
        }
        if (inTestRole != null) {
            currentTestRole = inTestRole;
            cardMax = inTestRole.getCardMax();
            cardMin = inTestRole.getCardMin();

            try {
                selectedOption = inTestRole.getTestOption();
            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
            }

        }
    }

    @Override
    public void deleteTestRole(TestRoles inTestRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestRole");
        }
        if (inTestRole != null) {
            entityManager.remove(inTestRole);
            entityManager.flush();
        }
    }

    @Override
    public TestRoles getCurrentTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentTestRole");
        }
        return currentTestRole;
    }

    @Override
    public void setCurrentTestRole(TestRoles inTestRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCurrentTestRole");
        }
        this.currentTestRole = inTestRole;
    }

    @Override
    public void cancelEditTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelEditTestRole");
        }
        cardMax = 1;
        cardMin = 1;

        currentTestRole = null;
        selectedOption = null;
        selectedIPOption = null;
    }

    @Override
    public Domain getDomainSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainSelected");
        }
        return domainSelected;
    }

    @Override
    public void setDomainSelected(Domain domainSelected) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDomainSelected");
        }
        this.domainSelected = domainSelected;
    }

    @Override
    public void reInitValues(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reInitValues");
        }
        if (o == null) {
            return;
        }
        if (o instanceof Domain) {
            selectedActor = null;
            selectedIPOption = null;

        } else if (o instanceof IntegrationProfile) {
            selectedIPOption = null;
        }

    }

    @Override
    public String findTestURLDocumentationByTestInstanceParticipants(TestInstanceParticipants inTestInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestURLDocumentationByTestInstanceParticipants");
        }

        TestRoles testRoles = TestRoles.getTestRolesByRoleInTestByTest(inTestInstanceParticipants.getTestInstance()
                .getTest(), inTestInstanceParticipants.getRoleInTest());
        if (testRoles != null) {
            return testRoles.getUrlDocumentation();
        }
        return null;
    }

    @Override
    public List<TestOption> getTestOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestOptions");
        }
        return TestOption.listTestOptions();
    }
}
