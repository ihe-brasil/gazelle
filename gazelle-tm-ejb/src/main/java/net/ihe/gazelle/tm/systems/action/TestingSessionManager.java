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
package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.menu.GazelleMenu;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.gazelletest.action.TestInstanceGenerator;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.skin.SkinBean;
import net.ihe.gazelle.tm.systems.model.TableSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingSessionQuery;
import net.ihe.gazelle.tm.users.action.UserManagerExtra;
import net.ihe.gazelle.users.model.*;
import net.ihe.gazelle.users.model.Role;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import java.io.Serializable;
import java.util.*;

/**
 * <b>Class Description : </b>TestingSessionManager<br>
 * <br>
 * This class manage the TestingSession object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add
 * a testingSession</li> <li>Delete a testingSession</li>
 * <li>Show a testingSession</li> <li>Edit a testingSession</li> <li>etc...</li>
 *
 * @class TestingSessionManager.java
 * @package net.ihe.gazelle.tf.action
 * @author Jean-Renan Chatel - / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @author jlabbe
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */

/**
 * @author jlabbe
 */


@Name("testingSessionManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("TestingSessionManagerLocal")
public class TestingSessionManager implements Serializable, TestingSessionManagerLocal, QueryModifier<TestingSession> {
    // ~ Statics variables and Class initializer ///////////////////////////////////////////////////////////////////////

    private static final long serialVersionUID = -1357012045588235656L;
    private static final int UP = 1;

    // ~ Attributes ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DOWN = -1;
    private static final Logger LOG = LoggerFactory.getLogger(TestingSessionManager.class);
    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;
    /**
     * List of all testingSession objects to be managed by this manager bean
     */
    private FilterDataModel<TestingSession> testingSessions;
    private Filter<TestingSession> filter;
    /**
     * Activated Testing Session object
     */
    private TestingSession activatedTestingSession;
    /**
     * Selected Testing Session object
     */
    private TestingSession selectedTestingSession;
    /**
     * Testing Session choosen by a user
     */
    private TestingSession testingSessionChoosen;
    /**
     * Testing Session choosen by a user for a copy
     */
    private TestingSession testingSessionChoosenForCopy;
    /**
     * List of all domains objects associated with a testing session - This object is managed by this manager bean
     */
    private List<Domain> selectedDomains;
    private List<Institution> selectedInstitutions;
    private Domain selectedDomain;
    private Address address;
    private boolean noTestingSessionChoosen = false;
    private boolean displayAll = false;
    private boolean internetTesting = false;

    @Override
    public boolean isInternetTesting() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isInternetTesting");
        }
        initializeTestingSessionChoosen();
        setInternetTesting(testingSessionChoosen.getInternetTesting());
        return internetTesting;
    }

    @Override
    public void setInternetTesting(boolean internetTesting) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInternetTesting");
        }
        this.internetTesting = internetTesting;
    }

    @Override
    public boolean isDisplayAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayAll");
        }
        return displayAll;
    }

    @Override
    public void setDisplayAll(boolean displayHidden) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAll");
        }
        this.displayAll = displayHidden;
    }

    @Override
    public boolean isNoTestingSessionChoosen() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isNoTestingSessionChoosen");
        }
        return noTestingSessionChoosen;
    }

    @Override
    public void setNoTestingSessionChoosen(boolean noTestingSessionChoosen) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNoTestingSessionChoosen");
        }
        this.noTestingSessionChoosen = noTestingSessionChoosen;
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

    public Filter<TestingSession> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter<TestingSession>(getHQLCriterion());
        }
        return filter;
    }

    private HQLCriterionsForFilter<TestingSession> getHQLCriterion() {
        TestingSessionQuery query = new TestingSessionQuery();
        HQLCriterionsForFilter criterion = query.getHQLCriterionsForFilter();
        criterion.addPath("zone", query.zone());
        criterion.addPath("type", query.type());
        criterion.addQueryModifier(this);
        return criterion;
    }

    /**
     * Set the variable "testingSessionChoosen" to the value of the activate testing session
     */
    @Override
    public void initializeTestingSessionChoosen() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeTestingSessionChoosen");
        }
        testingSessionChoosen = TestingSession.getSelectedTestingSession();
        Contexts.getSessionContext().set("testingSessionChoosen", testingSessionChoosen);

    }

    /**
     * Add a testingSession to the database This operation is allowed for some granted users (check the security.drl)
     *
     * @param d : TestingSession to add
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'addTestingSession', null)}")
    public String addTestingSession(TestingSession tSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestingSession");
        }

        if ((tSession != null) && (tSession.getRegistrationDeadlineDate() == null)) {
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tm.systems.setRegistrationDeadline");
            return null;
        }

        if (address != null) {
            address = entityManager.merge(address);
            tSession.setAddressSession(address);
        }

        if (tSession != null) {
            if (tSession.getId() == null) {
                tSession.setNextInvoiceNumber(1);
                entityManager.persist(tSession);
                entityManager.flush();

                selectedTestingSession = TestingSession.mergeTestingSession(tSession, entityManager);
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "TestingSession #{testingSession.description} created");
            } else {
                TestingSession existing = entityManager.find(TestingSession.class, tSession.getId());
                tSession.setId(existing.getId());

                selectedTestingSession = TestingSession.mergeTestingSession(tSession, entityManager);
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "TestingSession #{testingSession.description} updated");
            }
            if (selectedTestingSession.getTargetColor() == null) {
                setSessionColor(SkinBean.DEFAULT_COLOR_HTML);
                TestingSession.mergeTestingSession(selectedTestingSession, entityManager);
            }
        }

        LOG.warn(User.loggedInUser() + " has updated testing session " + selectedTestingSession.getDescription());

        // added to ensure that Gazelle Menu is rebuilt based on changes to
        // selected testing session for user - in relation to ITB efforts.
        GazelleMenu.refreshMenu();
        return "/administration/listSessions.seam";
    }

    /**
     * Update the testingSession's informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param d : TestingSession to be updated
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'updateTestingSession', null)}")
    public void updateTestingSession(final TestingSession d) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestingSession");
        }

        TestingSession.mergeTestingSession(d, entityManager);
    }

    /**
     * Save the testingSession's informations : Configuration This operation is allowed for some granted users (check the security.drl)
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'updateTestingSession', null)}")
    public void saveConfigurationOverview() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveConfigurationOverview");
        }

        TestingSession.mergeTestingSession(testingSessionChoosen, entityManager);
        FacesMessages.instance().add(StatusMessage.Severity.INFO,
                "Configuration for "
                        + testingSessionChoosen.getDescription()
                        + " has been successfully saved");

    }

    /**
     * Delete the selected testingSession This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedTestingSession : testingSession to delete
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'deleteTestingSession', null)}")
    public void deleteTestingSession(final TestingSession selectedTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestingSession");
        }
        TestingSession toDelete = TestingSession.mergeTestingSession(selectedTestingSession, entityManager);

        TestingSession.removeTestingSession(toDelete, entityManager);
    }

    /**
     * Delete the selected testingSession This operation is allowed for some granted users (check the security.drl)
     *
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'deleteTestingSession', null)}")
    public void deleteTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestingSession");
        }

        deleteTestingSession(selectedTestingSession);
    }

    /**
     * Edit the selected testingSession's informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedTestingSession : testingSession to edit
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'editTestingSession', null)}")
    public String editTestingSessionActionLink(final TestingSession tSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestingSessionActionLink");
        }

        initEditTestingSession(tSession);

        return "/administration/editSession.seam";
    }

    private void initEditTestingSession(TestingSession tSession) {
        selectedTestingSession = entityManager.find(TestingSession.class, tSession.getId());
        address = selectedTestingSession.getAddressSession();
    }

    public void createAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createAddress");
        }
        if (address == null) {
            address = new Address();
            address.setIso3166CountryCode(new Iso3166CountryCode(entityManager.find(Iso3166CountryCode.class, "-")));
        }
    }

    public void deleteAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAddress");
        }
        if (address.getId() != null) {
            selectedTestingSession.setAddressSession(null);
            selectedTestingSession = entityManager.merge(selectedTestingSession);
            address = entityManager.find(Address.class, address.getId());
            entityManager.remove(address);
        }
        address = null;
    }

    /**
     * Create a new testingSession. This method is used by a Java client This operation is allowed for some granted users (check the security.drl)
     *
     * @param d : testingSession to create
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'createTestingSession', null)}")
    public void createTestingSession(final TestingSession d) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createTestingSession");
        }

        entityManager.persist(d);
        entityManager.flush();
    }

    /**
     * Get all the testingSessions existing in the database
     *
     * @return List of all existing testingSessions
     */
    @Override
    public FilterDataModel<TestingSession> getTestingSessions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSessions");
        }
        return new FilterDataModel<TestingSession>(getFilter()) {
            @Override
            protected Object getId(TestingSession testingSession) {
                return testingSession.getId();
            }
        };
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    /**
     * When Add TestingSession button is clicked, the method called to redirect to editTestingSession page to add new testingSession
     *
     * @return String of web page to display
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'addNewTestingSessionButton', null)}")
    public String addNewTestingSessionButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewTestingSessionButton");
        }

        selectedTestingSession = new TestingSession();

        return "/administration/editSession.seam";
    }

    /**
     * Activate a testing session (pass to 'true' the activate attribute of that selected session, pass to 'false' all the other ones)
     *
     * @return String of web page to display
     */
    @Override
    @Restrict("#{s:hasPermission('TestingSessionManager', 'activateSession', null)}")
    public void activateSession(TestingSession tSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateSession");
        }
        // From the administration page, when the administrator clicks on the testing session to activate in the events table (eg. Connectathon
        // NA2009), we activate it and pass to true its 'activate'
        // attribute
        try {

            TestingSession newSessionToActivate = entityManager.find(TestingSession.class, tSession.getId());
            if (newSessionToActivate.getActiveSession()) {
                newSessionToActivate.setActiveSession(false);
            } else {
                newSessionToActivate.setActiveSession(true);
            }
            entityManager.persist(newSessionToActivate);
            entityManager.flush();

            activatedTestingSession = TestingSession.getSelectedTestingSession();
        } catch (javax.validation.ConstraintViolationException e) {
            Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
            for (ConstraintViolation<?> constraintViolation : constraintViolations) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, constraintViolation.getMessage());
            }
        }
    }

    @Override
    public List<Institution> getSelectedInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitutions");
        }
        return selectedInstitutions;
    }

    @Override
    public void setSelectedInstitutions(List<Institution> selectedInstitutions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitutions");
        }
        this.selectedInstitutions = selectedInstitutions;
    }

    @Override
    public TestingSession getActivatedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActivatedTestingSession");
        }
        return TestingSession.getSelectedTestingSession();

    }

    @Override
    public void setActivatedTestingSession(TestingSession activatedTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActivatedTestingSession");
        }
        this.activatedTestingSession = activatedTestingSession;
    }

    /**
     * This method checks the registration Deadline (for the registration in a testing session) If the registration deadline date is expired, it
     * returns true, that way it helps to hide a button in the
     * presentation layer, for instance, a vendor won't be able to edit a registered system after the registration deadline.
     *
     * @return boolean: True/False (True if deadline date is expired)
     */
    @Override
    public boolean isRegistrationDeadlineExpired() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRegistrationDeadlineExpired");
        }
        User usr = User.loggedInUser();
        boolean latereg = false;
        if (usr != null) {
            if (usr.getRoles() != null) {
                for (Role roll : usr.getRoles()) {
                    if (roll.getName().equals(Role.VENDOR_LATE_REGISTRATION_STRING)) {
                        latereg = true;
                    }
                }
            }
        }

        boolean res = (isRegistrationDeadlineExpired(TestingSession.getSelectedTestingSession()) && !latereg);
        return res;
    }

    @Override
    public boolean reuseSystemsForThisSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reuseSystemsForThisSession");
        }
        return false;
    }

    /**
     * This method checks the registration Deadline (for the registration in a testing session) If the registration deadline date is expired, it
     * returns true, that way it helps to hide a button in the
     * presentation layer, for instance, a vendor won't be able to edit a registered system after the registration deadline.
     *
     * @param inTestingSession
     * @return boolean: True/False (True if deadline date is expired)
     */

    @Override
    public boolean isRegistrationDeadlineExpired(TestingSession inTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRegistrationDeadlineExpired");
        }
        boolean expiredDeadline = false;

        if (inTestingSession == null) {
            return true;
        }

        Date currentTime = new Date();
        // a testing session with a null registration dead line will never be closed for registration
        if (inTestingSession.getRegistrationDeadlineDate() == null) {
            return false;
        }
        if (currentTime.after(inTestingSession.getRegistrationDeadlineDate())) {
            expiredDeadline = true;
        }

        return expiredDeadline;
    }

    @Override
    public List<Institution> getInstitutionsForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionsForSession");
        }
        return this.getInstitutionsForSession(testingSessionChoosen);
    }

    @Override
    public List<Institution> getInstitutionsForSession(TestingSession ts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionsForSession");
        }
        List<Institution> returnList = null;
        if (ts != null) {

            returnList = TestingSession.getListOfInstitutionsParticipatingInSession(ts);
        } else {
            returnList = getInstitutionsForActivatedSession();
        }
        Collections.sort(returnList);
        return returnList;
    }

    /**
     * returns the list of institutions owning systems for the selected testing session
     */
    @Override
    public List<Institution> getInstitutionsForActivatedSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionsForActivatedSession");
        }

        List<Institution> returnList = null;
        activatedTestingSession = TestingSession.getSelectedTestingSession();

        returnList = TestingSession.getListOfInstitutionsRegisterInSession(activatedTestingSession);

        return returnList;
    }

    @Override
    public List<Domain> getSelectedDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomains");
        }
        return selectedDomains;
    }

    @Override
    public void setSelectedDomains(List<Domain> selectedDomains) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomains");
        }
        this.selectedDomains = selectedDomains;
    }

    @Override
    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        if (selectedTestingSession == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            String id = fc.getExternalContext().getRequestParameterMap().get("editId");
            if (id != null) {
                initEditTestingSession(entityManager.find(TestingSession.class, Integer.valueOf(id)));
            }
        }
        return selectedTestingSession;
    }

    @Override
    public void setSelectedTestingSession(TestingSession selectedTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestingSession");
        }
        this.selectedTestingSession = selectedTestingSession;
    }

    @Override
    public TestingSession getTestingSessionChoosen() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSessionChoosen");
        }
        testingSessionChoosen = (TestingSession) Component.getInstance("testingSessionChoosen");
        return testingSessionChoosen;
    }

    @Override
    public void setTestingSessionChoosen(TestingSession testingSessionChoosen) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingSessionChoosen");
        }

        Contexts.getSessionContext().set("testingSessionChoosen", testingSessionChoosen);
        this.testingSessionChoosen = testingSessionChoosen;

    }

    @Override
    public TestingSession getTestingSessionChoosenForCopy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSessionChoosenForCopy");
        }
        return testingSessionChoosenForCopy;
    }

    @Override
    public void setTestingSessionChoosenForCopy(TestingSession testingSessionChoosenForCopy) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingSessionChoosenForCopy");
        }
        this.testingSessionChoosenForCopy = testingSessionChoosenForCopy;
    }

    @Override
    public void removeTestingSessionChoosenForCopy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeTestingSessionChoosenForCopy");
        }
        if (noTestingSessionChoosen) {
            this.testingSessionChoosenForCopy = null;
        }
    }

    @Override
    public void removenoTestingSessionChoosen() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removenoTestingSessionChoosen");
        }
        if (testingSessionChoosenForCopy != null) {
            this.noTestingSessionChoosen = false;
        }
    }

    @Override
    public List<TestingSession> getPossibleTestingSessionsForCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestingSessionsForCompany");
        }
        return TestingSession.getTestingSessionsWhereCompanyWasHere(Institution.getLoggedInInstitution().getName());
    }

    @Override
    public void enableCriticalStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("enableCriticalStatus");
        }
        selectedTestingSession = changeCriticalStatusForATestingSession(selectedTestingSession, true);
    }

    @Override
    public void disableCriticalStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("disableCriticalStatus");
        }
        selectedTestingSession = changeCriticalStatusForATestingSession(selectedTestingSession, false);
    }

    private TestingSession changeCriticalStatusForATestingSession(TestingSession inTestinSession,
                                                                  boolean isCriticalTestingSessionEnabled) {
        if (inTestinSession != null) {
            inTestinSession.setIsCriticalStatusEnabled(isCriticalTestingSessionEnabled);
            inTestinSession = TestingSession.mergeTestingSession(inTestinSession, entityManager);
            return inTestinSession;
        }
        return null;
    }

    @Override
    public List<TableSession> listTableSessionAvailable(TableSession inTable) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTableSessionAvailable");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Query q = em
                .createQuery("from TableSession ts where ts.id not IN ( SELECT distinct sis.tableSession.id from SystemInSession sis where sis" +
                        ".tableSession.id != null AND sis.testingSession =:inTestingSession )");
        q.setParameter("inTestingSession", TestingSession.getSelectedTestingSession());

        List<TableSession> listTableSession = q.getResultList();

        if (inTable != null) {
            listTableSession.add(0, inTable);
        }
        Collections.sort(listTableSession);

        return listTableSession;
    }

    @Override
    public void generateRandomlyTestInstanceForSelectedInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateRandomlyTestInstanceForSelectedInstitutions");
        }
        if (selectedInstitutions != null) {
            for (Institution ins : selectedInstitutions) {
                TestInstanceGenerator.generateRandomlyTestInstanceForSelectedInstitution(ins, selectedTestingSession,
                        entityManager);
            }
        }
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "test instances were automatically generated.");
    }

    @Override
    public List<TestingSession> getActiveSessions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActiveSessions");
        }
        return TestingSession.GetAllActiveSessions();
    }

    @Override
    public void addAllIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addAllIntegrationProfiles");
        }
        if ((selectedTestingSession != null) && (selectedDomain != null)) {
            List<IntegrationProfile> integrationProfilesForDP = selectedDomain.getIntegrationProfilesForDP();
            for (IntegrationProfile integrationProfile : integrationProfilesForDP) {
                if (integrationProfile.isAvailableForTestingSessions()) {
                    if (!selectedTestingSession.getIntegrationProfilesUnsorted().contains(integrationProfile)) {
                        selectedTestingSession.getIntegrationProfilesUnsorted().add(integrationProfile);
                    }
                }
            }
        }
    }

    @Override
    public void removeAllIntegrationProfilesFromDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAllIntegrationProfilesFromDomain");
        }
        if ((selectedTestingSession != null) && (selectedDomain != null)) {
            List<IntegrationProfile> integrationProfilesForDP = selectedDomain.getIntegrationProfilesForDP();
            for (IntegrationProfile integrationProfile : integrationProfilesForDP) {
                selectedTestingSession.getIntegrationProfilesUnsorted().remove(integrationProfile);
            }
        }
    }

    @Override
    public void removeAllIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAllIntegrationProfiles");
        }
        if (selectedTestingSession != null) {
            selectedTestingSession.getIntegrationProfilesUnsorted().clear();
            LOG.warn(User.loggedInUser() + " has remove all Integration Profiles from "
                    + selectedTestingSession.getDescription());
        }
    }

    @Override
    public List<TestType> getTestTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestTypes");
        }
        return TestType.getTestTypesWithoutMESA();
    }

    @Override
    public String getSessionColor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSessionColor");
        }
        String targetColor = SkinBean.DEFAULT_COLOR_HTML;
        if (selectedTestingSession != null) {
            targetColor = selectedTestingSession.getTargetColor();
        }
        return targetColor;
    }

    @Override
    public void setSessionColor(String sessionColor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSessionColor");
        }
        selectedTestingSession.setTargetColor(sessionColor);
    }

    public List<User> getPossibleTestSessionAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestSessionAdmin");
        }
        UserQuery query = new UserQuery();
        query.roles().name().eq(Role.TESTING_SESSION_ADMIN_STRING);
        query.username().order(true);
        List<User> users = query.getList();
        return users;
    }

    public List<TestingSession> getTestingSessionWhereUserIsAdmin(User user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSessionWhereUserIsAdmin");
        }
        return TestingSession.getTestingSessionWhereUserIsAdmin(user);
    }

    public void markAsDefaultTestingSession(TestingSession testingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("markAsDefaultTestingSession");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        unSetDefaultTestingSession(em);

        testingSession.setDefaultTestingSession(true);
        em.merge(testingSession);
        em.flush();
    }

    /**
     * Remove default testing session mark from previous default testing session
     *
     * @param em entity manager
     */
    private void unSetDefaultTestingSession(EntityManager em) {
        TestingSessionQuery query = new TestingSessionQuery();
        query.defaultTestingSession().eq(true);
        List<TestingSession> listDistinct = query.getListDistinct();
        for (TestingSession testingSession : listDistinct) {
            testingSession.setDefaultTestingSession(false);
            em.merge(testingSession);
        }
    }

    public Address getAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAddress");
        }
        return address;
    }

    public void setAddress(Address address) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAddress");
        }
        this.address = address;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<TestingSession> hqlQueryBuilder, Map<String, Object> map) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        TestingSessionQuery testingSessionQuery = new TestingSessionQuery();
        if (!isDisplayAll()) {
            hqlQueryBuilder.addRestriction(testingSessionQuery.hiddenSession().eqRestriction(false));
        }
    }

    public void moveOrderUp(int testingSessionId) {
        move(testingSessionId, DOWN);
    }

    public void moveOrderDown(int testingSessionId) {
        move(testingSessionId, UP);
    }

    public void move(int testingSessionId, int direction) {
        try {
            TestingSession t = entityManager.find(TestingSession.class, testingSessionId);
            Integer i = t.getOrderInGUI();

            if (!isCurrentGuiOrderIsOverloaded(i)) {
                moveFirstElementFromWantedPosition(direction, i);
            }

            t.setOrderInGUI(i + direction);
            entityManager.merge(t);
        } catch (javax.validation.ConstraintViolationException e) {
            Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
            for (ConstraintViolation<?> constraintViolation : constraintViolations) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, constraintViolation.getMessage());
            }

        }
    }

    /**
     * We move only the first one
     * <p>
     * The gui order is not unique, so multiple testing sessions can have the same number
     * We want to avoid moving all the testing session at the same time
     *
     * @param position  orderGui
     * @param direction +1: move down , -1 move up
     */
    private void moveFirstElementFromWantedPosition(int direction, Integer position) {
        TestingSessionQuery q = new TestingSessionQuery();
        q.orderInGUI().eq(position + direction);
        List<TestingSession> listDistinct = q.getListDistinct();

        TestingSession testingSession = listDistinct.get(0);
        testingSession.setOrderInGUI(position);
        entityManager.merge(testingSession);
    }

    /**
     * @param position orderGui
     * @return true if there is more than one testing session with the orderGui == to position
     */
    private boolean isCurrentGuiOrderIsOverloaded(int position) {
        TestingSessionQuery q = new TestingSessionQuery();
        q.orderInGUI().eq(position);
        List<TestingSession> TsAtCurrentPosition = q.getListDistinct();
        return TsAtCurrentPosition.size() != 1;
    }

    public boolean isUserAdminOfCurrentTestingSession(TestingSession currentTestingSession) {
        UserManagerExtra userManagerExtra = new UserManagerExtra();
        if (userManagerExtra.getSelectedTestingSession().getId().equals(currentTestingSession.getId())) {
            return Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION.isGranted();
        }
        return false;
    }
}
