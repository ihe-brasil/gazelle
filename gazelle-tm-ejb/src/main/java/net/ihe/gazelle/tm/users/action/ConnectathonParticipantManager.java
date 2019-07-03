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
package net.ihe.gazelle.tm.users.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.financial.action.FinancialCalc;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.users.model.ConnectathonParticipant;
import net.ihe.gazelle.tm.users.model.ConnectathonParticipantQuery;
import net.ihe.gazelle.tm.users.model.ConnectathonParticipantStatus;
import net.ihe.gazelle.users.action.InstitutionManagerLocal;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Person;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.util.Pair;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>Class Description : </b>ConnectathonParticipantManager<br>
 * <br>
 * This class manage the ConnectathonParticipant. It corresponds to the Business Layer. All operations to implement are done in this class :
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @class ConnectathonParticipantManager.java
 * @package net.ihe.gazelle.tm.users.action
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */

@Name("connectathonParticipantManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("ConnectathonParticipantManagerLocal")
public class ConnectathonParticipantManager implements Serializable, ConnectathonParticipantManagerLocal {

    static final int timeout = 10000;

    private static final String USER = "User ";

    private static final String IN_TESTING_SESSION = "inTestingSession";

    private static final String IN_INSTITUTION = "inInstitution";

    private static final String CHOOSEN_INSTITUTION_FOR_ADMIN = "choosenInstitutionForAdmin";

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = 1721723220971805773L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ConnectathonParticipantManager.class);
    /**
     * List of all Institutions which have none registered participants, variable used between business and presentation layer
     */
    List<Institution> companiesWithoutParticipants;
    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;
    /**
     * Testing Session choosen by a user
     */
    private TestingSession testingSessionChoosen;
    /**
     * Selected ConnectathonParticipant object managed by this bean, variable used between business and presentation layer
     */
    private ConnectathonParticipant selectedConnectathonParticipant;
    /**
     * List of ConnectathonParticipant objects managed by this bean, variable used between business and presentation layer
     */
    private List<ConnectathonParticipant> connectathonParticipants;
    /**
     * List of ConnectathonParticipant objects managed by this bean, variable used between business and presentation layer
     */
    private Institution selectedInstitution;
    /**
     * List of Users for importation. List of objects managed by this bean, variable used between business and presentation layer
     */
    private List<Pair<Boolean, User>> usersForImportation;
    /**
     * List of Contacts for importation. List of objects managed by this bean, variable used between business and presentation layer
     */
    private List<Pair<Boolean, Person>> contactsForImportation;
    private Integer nbOfParticipants = 0;
    /**
     * List of ConnectathonParticipant objects managed by this bean, variable used between business and presentation layer
     */
    private Boolean renderAddPanel;

    private Filter<ConnectathonParticipant> filter;
    private Boolean renderImportContactPanel;
    private Boolean renderImportUserPanel;
    /**
     * List of SystemInSession objects for this Connectathon (depending on logged in user : admin or vendor). This object is managed by this bean,
     * variable used between business and presentation layer
     */
    private List<SystemInSession> foundSystemsInSession;
    /**
     * Number of Men per day per System
     */
    private Integer menDayPerSystem;
    /**
     * Institution selected by Admin, variable used between business and presentation layer
     */
    private Institution choosenInstitutionForAdmin;
    private boolean monday = false;
    private boolean tuesday = false;
    private boolean wednesday = false;
    private boolean thursday = false;
    private boolean friday = false;
    private boolean vegetarian = false;
    private boolean socialEvent = false;

    public Integer getNbOfParticipants() {
        if (nbOfParticipants == 0) {
            connectathonParticipantsDataModel();
        }
        return nbOfParticipants;

    }

    public Boolean getRenderImportContactPanel() {
        return renderImportContactPanel;
    }

    public void setRenderImportContactPanel(Boolean renderImportContactPanel) {
        this.renderImportContactPanel = renderImportContactPanel;
    }

    public Boolean getRenderImportUserPanel() {
        return renderImportUserPanel;
    }

    public void setRenderImportUserPanel(Boolean renderImportUserPanel) {
        this.renderImportUserPanel = renderImportUserPanel;
    }

    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isSocialEvent() {
        return socialEvent;
    }

    public void setSocialEvent(boolean socialEvent) {
        this.socialEvent = socialEvent;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }


    /**
     * Get the list of Connectathon Participants, depending on the logged in user (admin or vendor)
     */
    @Override
    public void getAllConnectathonParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllConnectathonParticipants");
        }

        renderAddPanel = false;
        setRenderImportContactPanel(false);
        setRenderImportUserPanel(false);

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager()) {

            EntityManager em = EntityManagerService.provideEntityManager();
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            Query query;
            if (choosenInstitutionForAdmin != null) {
                query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                                ":inInstitution");
                query.setParameter(IN_INSTITUTION, choosenInstitutionForAdmin);
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());

            } else {
                query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());

            }
            connectathonParticipants = query.getResultList();

        } else {
            selectedInstitution = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution");
            query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
            query.setParameter(IN_INSTITUTION, selectedInstitution);
            connectathonParticipants = query.getResultList();

        }

        for (ConnectathonParticipant cp : connectathonParticipants) {
            if (cp.getInstitution() == null) {
                if (cp.getInstitutionOld() != null) {
                    cp.setInstitution(cp.getInstitutionOld());
                    entityManager.merge(cp);
                    entityManager.flush();
                    LOG.warn("Institution patched successfully !");
                }
            }
        }

    }

    @Override
    public boolean canViewFooter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canViewFooter");
        }
        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager()) {
            if (this.choosenInstitutionForAdmin != null) {
                return true;
            }
            return false;
        }
        if (this.selectedInstitution != null) {
            return true;
        }
        return false;
    }

    private void clearAddPanels() {
        setRenderImportContactPanel(false);
        setRenderAddPanel(false);
        setRenderImportUserPanel(false);
    }

    /**
     * Get the Contacts list for importation, depending on logged in user
     */
    @Override
    public void initializeContactsForImportation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeContactsForImportation");
        }

        clearAddPanels();
        setRenderImportContactPanel(true);

        List<Person> persons;

        contactsForImportation = new ArrayList<Pair<Boolean, Person>>();

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager()) {
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                persons = Person.listAllContactsForCompany(choosenInstitutionForAdmin);
            } else {
                persons = Person.listAllContacts();
            }
        } else {
            persons = Person.listAllContactsForCompany(Institution.getLoggedInInstitution());
        }
        boolean emailAlreadyExists;
        for (Person person : persons) {
            emailAlreadyExists = false;

            for (ConnectathonParticipant cp : connectathonParticipantsDataModel().getAllItems(FacesContext.getCurrentInstance())) {

                if (person.getEmail().equals(cp.getEmail())) {
                    emailAlreadyExists = true;
                }
            }

            if (!emailAlreadyExists) {
                contactsForImportation.add(new Pair<Boolean, Person>(false, person));
            }
        }

    }

    /**
     * Get the Contacts list for importation, depending on logged in user
     */
    @Override
    public void initializeUsersForImportation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeUsersForImportation");
        }
        clearAddPanels();
        setRenderImportUserPanel(true);

        List<User> users;

        usersForImportation = new ArrayList<Pair<Boolean, User>>();

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager()) {
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                users = User.listUsersForCompany(choosenInstitutionForAdmin);
            } else {
                users = User.listAllUsers();
            }
        } else {
            users = User.listUsersForCompany(Institution.getLoggedInInstitution());
        }
        boolean emailAlreadyExists;
        for (User user : users) {
            emailAlreadyExists = false;

            for (ConnectathonParticipant cp : connectathonParticipantsDataModel().getAllItems(FacesContext.getCurrentInstance())) {

                if (user.getEmail().equals(cp.getEmail())) {
                    emailAlreadyExists = true;
                }
            }

            if (!emailAlreadyExists) {
                usersForImportation.add(new Pair<Boolean, User>(false, user));
            }
        }
    }

    /**
     * Initialize variable before adding a participant, depending on logged in user
     */
    @Override
    public void addParticipantAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addParticipantAction");
        }
        clearAddPanels();

        renderAddPanel = true;
        selectedConnectathonParticipant = new ConnectathonParticipant();
        ConnectathonParticipantStatus cps = new ConnectathonParticipantStatus();
        selectedConnectathonParticipant.setConnectathonParticipantStatus(cps);

        if (!Role.isLoggedUserAdmin() && !Role.isLoggedUserProjectManager() && !Role.isLoggedUserMonitor()) {
            selectedConnectathonParticipant.setInstitution(Institution.getLoggedInInstitution());
        }
        selectedConnectathonParticipant.setTestingSession(TestingSession.getSelectedTestingSession());

    }

    /**
     * That method redirect to the Participant management page, depending on the selected company (used by admin)
     *
     * @param : selected institution
     * @return : JSF page to render
     */
    @Override
    public String manageCompany(Institution inInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("manageCompany");
        }

        renderAddPanel = false;
        choosenInstitutionForAdmin = entityManager.find(Institution.class, inInstitution.getId());
        Contexts.getSessionContext().set(CHOOSEN_INSTITUTION_FOR_ADMIN, selectedInstitution);

        return "/users/connectathon/listParticipants.seam";
    }

    /**
     * Add a participant for the Connectathon, depending on logged in user
     */
    @Override
    public void addParticipantForConnectathon() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addParticipantForConnectathon");
        }

        if (isEditExistingParticipant()) {
            if (isParticipantEmailChanged()) {
                saveParticipant();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Participant was successfully updated");
                renderAddPanel = false;
                getAllConnectathonParticipants();
                return;
            }
        }
        // check that the participant is not already registered for this testing session
        String email = selectedConnectathonParticipant.getEmail();
        TestingSession testingSession = selectedConnectathonParticipant.getTestingSession();

        List<ConnectathonParticipant> participants = getConnectathonParticipants(email, testingSession);

        if ((participants != null) && !participants.isEmpty()) {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.connectaton.participants.ParticipantExists");
        } else {
            saveParticipant();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Participant was successfully added");
        }
        renderAddPanel = false;
        getAllConnectathonParticipants();
    }

    private boolean isParticipantEmailChanged() {
        ConnectathonParticipant connectathonParticipantDb = entityManager.find(ConnectathonParticipant.class, selectedConnectathonParticipant.getId
                ());
        return connectathonParticipantDb.getEmail().equals(selectedConnectathonParticipant.getEmail());
    }

    private boolean isEditExistingParticipant() {
        return selectedConnectathonParticipant.getId() != null;
    }

    private void saveParticipant() {
        if ((selectedConnectathonParticipant.getInstitutionName() == null)
                || selectedConnectathonParticipant.getInstitutionName().isEmpty()) {
            if (selectedConnectathonParticipant.getInstitution() != null) {
                selectedConnectathonParticipant.setInstitutionName(selectedConnectathonParticipant.getInstitution()
                        .getName());
            }
        }
        try {
            entityManager.merge(selectedConnectathonParticipant);
            entityManager.flush();
        } catch (Exception e) {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "gazelle.users.connectaton.participants.CannotAddParticipant",
                    selectedConnectathonParticipant.getFirstname(), selectedConnectathonParticipant.getLastname(),
                    selectedConnectathonParticipant.getEmail());
        }
    }

    private List<ConnectathonParticipant> getConnectathonParticipants(String email, TestingSession testingSession) {
        Session session = (Session) entityManager.getDelegate();
        Criteria c = session.createCriteria(ConnectathonParticipant.class);
        c.add(Restrictions.ilike("email", email));
        c.add(Restrictions.eq("testingSession", testingSession));
        return c.list();
    }

    @Override
    public Filter<ConnectathonParticipant> getFilter() {
        if (filter == null) {
            filter = new Filter<ConnectathonParticipant>(getHQLCriterionsForFilter());
        }
        return filter;
    }

    private HQLCriterionsForFilter<ConnectathonParticipant> getHQLCriterionsForFilter() {
        ConnectathonParticipantQuery q = new ConnectathonParticipantQuery();
        HQLCriterionsForFilter<ConnectathonParticipant> criterionsForFilter = q.getHQLCriterionsForFilter();
        criterionsForFilter.addPath("testingSession", q.testingSession(), TestingSession.getSelectedTestingSession());
        criterionsForFilter.addPath("status", q.connectathonParticipantStatus());
        if (Identity.instance().isLoggedIn() && Identity.instance().hasRole("admin_role")) {
            InstitutionManagerLocal local = (InstitutionManagerLocal) Component.getInstance("institutionManager");
            criterionsForFilter.addPath("institution", q.institution(), local.getChoosenInstitutionForAdmin());
        } else {
            criterionsForFilter.addPath("institution", q.institution(), User.loggedInUser().getInstitution());
        }
        criterionsForFilter.addQueryModifier(new QueryModifier<ConnectathonParticipant>() {
            @Override
            public void modifyQuery(HQLQueryBuilder<ConnectathonParticipant> hqlQueryBuilder, Map<String, Object> map) {
                ConnectathonParticipantQuery q = new ConnectathonParticipantQuery();

                if (monday) {
                    hqlQueryBuilder.addRestriction(q.mondayMeal().eqRestriction(true));
                }
                if (tuesday) {
                    hqlQueryBuilder.addRestriction(q.tuesdayMeal().eqRestriction(true));
                }
                if (wednesday) {
                    hqlQueryBuilder.addRestriction(q.wednesdayMeal().eqRestriction(true));
                }
                if (thursday) {
                    hqlQueryBuilder.addRestriction(q.thursdayMeal().eqRestriction(true));
                }
                if (friday) {
                    hqlQueryBuilder.addRestriction(q.fridayMeal().eqRestriction(true));
                }
                if (vegetarian) {
                    hqlQueryBuilder.addRestriction(q.vegetarianMeal().eqRestriction(true));
                }
                if (socialEvent) {
                    hqlQueryBuilder.addRestriction(q.socialEvent().eqRestriction(true));
                }
            }
        });
        return criterionsForFilter;
    }

    public int countUsersByCategory(String category) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("countTests");
        }
        HQLQueryBuilder<ConnectathonParticipant> queryBuilder = new HQLQueryBuilder<ConnectathonParticipant>(ConnectathonParticipant.class);
        filter.appendHibernateFilters(queryBuilder);

        HQLRestriction restrictionCat = null;
        List<HQLRestriction> restrictions = queryBuilder.getRestrictions();

        restrictionCat = HQLRestrictions.eq(category, true);
        restrictions.add(restrictionCat);

        return queryBuilder.getCount();
    }

    public void resetFilter() {
        filter.clear();
        setMonday(false);
        setTuesday(false);
        setWednesday(false);
        setFriday(false);
        setVegetarian(false);
        setSocialEvent(false);
    }


    @Override
    public FilterDataModel<ConnectathonParticipant> connectathonParticipantsDataModel() {
        FilterDataModel<ConnectathonParticipant> data = new FilterDataModel<ConnectathonParticipant>(getFilter()) {
            @Override
            protected Object getId(ConnectathonParticipant connectathonParticipant) {
                return connectathonParticipant.getId();
            }
        };
        nbOfParticipants = data.getAllItems(FacesContext.getCurrentInstance()).size();
        return data;
    }

    /**
     * Delete a selected participant for the Connectathon
     */
    @Override
    public void deleteSelectedConnectathonParticipant() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedConnectathonParticipant");
        }

        selectedConnectathonParticipant = entityManager.find(ConnectathonParticipant.class,
                selectedConnectathonParticipant.getId());

        try {

            entityManager.remove(selectedConnectathonParticipant);
            entityManager.flush();

        } catch (Exception e) {
            LOG.warn(USER + selectedConnectathonParticipant.getEmail()
                    + " cannot be deleted - This case should not occur...");
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "gazelle.users.connectaton.participants.CannotDeleteParticipant",
                    selectedConnectathonParticipant.getFirstname(), selectedConnectathonParticipant.getLastname(),
                    selectedConnectathonParticipant.getEmail());
        }
        FinancialCalc.updateInvoiceIfPossible(selectedConnectathonParticipant.getInstitution(),
                selectedConnectathonParticipant.getTestingSession(), entityManager);
        getAllConnectathonParticipants();

    }

    /**
     * Delete all participants for the Connectathon, depending on the logged in user (admin or vendor)
     */
    @Override
    public void deleteAllConnectathonParticipantsForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllConnectathonParticipantsForSession");
        }

        for (ConnectathonParticipant cp : connectathonParticipantsDataModel().getAllItems(FacesContext.getCurrentInstance())) {
            ConnectathonParticipant cpToDelete = entityManager.find(ConnectathonParticipant.class, cp.getId());
            try {

                entityManager.remove(cpToDelete);
                entityManager.flush();

            } catch (Exception e) {

                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "gazelle.users.connectaton.participants.CannotDeleteParticipant", cp.getFirstname(),
                        cp.getLastname(), cp.getEmail());
            }
        }
        FinancialCalc.updateInvoiceIfPossible(choosenInstitutionForAdmin, TestingSession.getSelectedTestingSession(),
                entityManager);
    }

    /**
     * Get the list of SystemInSession objects for this Connectathon, depending on the logged in user (admin or vendor)
     */
    @Override
    public void getAllSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllSystemsInSession");
        }

        List<SystemInSession> listOfSiS = null;
        EntityManager em = EntityManagerService.provideEntityManager();
        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                listOfSiS = SystemInSession.getSystemsInSessionForCompanyForSession(em, choosenInstitutionForAdmin);
            } else {
                listOfSiS = SystemInSession.getSystemsInSessionForActivatedTestingSession();
            }
        } else {
            listOfSiS = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                    Institution.getLoggedInInstitution());
        }
        foundSystemsInSession = listOfSiS;
    }

    /**
     * Edit action : when a user clicks on Edit button, it initializes variables to edit a participant for the Connectathon
     */
    @Override
    public void editParticipant(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editParticipant");
        }

        renderAddPanel = true;
        selectedConnectathonParticipant = entityManager.find(ConnectathonParticipant.class, cp.getId());
    }

    /**
     * Initialize variable before adding a participant, depending on logged in user
     */
    @Override
    public void importUsers() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("importUsers");
        }

        for (int i = 0; i < usersForImportation.size(); i++) {

            if (usersForImportation.get(i).getObject1()) {
                ConnectathonParticipant cp = new ConnectathonParticipant();
                ConnectathonParticipantQuery query = new ConnectathonParticipantQuery();
                query.testingSession().eq(TestingSession.getSelectedTestingSession());
                query.email().eq(usersForImportation.get(i).getObject2().getEmail());

                if (query.getCount() == 0) {

                    try {

                        cp.setEmail(usersForImportation.get(i).getObject2().getEmail());
                        cp.setFirstname(usersForImportation.get(i).getObject2().getFirstname());
                        cp.setLastname(usersForImportation.get(i).getObject2().getLastname());
                        cp.setMondayMeal(true);
                        cp.setTuesdayMeal(true);
                        cp.setWednesdayMeal(true);
                        cp.setThursdayMeal(true);
                        cp.setFridayMeal(true);
                        cp.setVegetarianMeal(false);
                        cp.setSocialEvent(false);
                        cp.setTestingSession(TestingSession.getSelectedTestingSession());
                        cp.setInstitution(usersForImportation.get(i).getObject2().getInstitution());
                        cp.setInstitutionName(usersForImportation.get(i).getObject2().getInstitution().getName());

                        if (Role.isUserMonitor(usersForImportation.get(i).getObject2())) {
                            cp.setConnectathonParticipantStatus(ConnectathonParticipantStatus.getMonitorStatus());

                        } else if (Role.isUserVendorAdmin(usersForImportation.get(i).getObject2())
                                || Role.isUserVendorUser(usersForImportation.get(i).getObject2())) {
                            cp.setConnectathonParticipantStatus(ConnectathonParticipantStatus.getVendorStatus());

                        } else {
                            cp.setConnectathonParticipantStatus(ConnectathonParticipantStatus.getVisitorStatus());
                        }

                        entityManager.clear();
                        entityManager.persist(cp);
                        entityManager.flush();

                        FacesMessages.instance().add(StatusMessage.Severity.INFO, USER + usersForImportation.get(i).getObject2().getLastname() + " " +
                                "" + usersForImportation.get(i).getObject2().getEmail()
                                + " is imported");
                        renderAddPanel = false;
                        getAllConnectathonParticipants();

                    } catch (Exception e) {
                        LOG.warn(USER + usersForImportation.get(i).getObject2().getEmail()
                                + " is already added - This case case should not occur...");
                        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.users.connectaton.participants.CannotImportParticipant", cp.getFirstname(),
                                cp.getLastname(), cp.getEmail());

                    }
                    FinancialCalc.updateInvoiceIfPossible(cp.getInstitution(), cp.getTestingSession(), entityManager);
                } else {
                    LOG.warn(USER + usersForImportation.get(i).getObject2().getEmail()
                            + " is already added - This case case should not occur...");
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "gazelle.users.connectaton.participants.CannotImportParticipant",
                            usersForImportation.get(i).getObject2().getFirstname(),
                            usersForImportation.get(i).getObject2().getLastname(),
                            usersForImportation.get(i).getObject2().getEmail());
                }
            }

        }
    }

    /**
     * Initialize variable before adding a participant, depending on logged in user
     */
    @Override
    public void importContacts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("importContacts");
        }

        for (int i = 0; i < contactsForImportation.size(); i++) {

            if (contactsForImportation.get(i).getObject1()) {
                ConnectathonParticipant cp = new ConnectathonParticipant();
                ConnectathonParticipantQuery query = new ConnectathonParticipantQuery();
                query.testingSession().eq(TestingSession.getSelectedTestingSession());
                query.email().eq(contactsForImportation.get(i).getObject2().getEmail());

                if (query.getCount() == 0) {

                    try {

                        cp.setEmail(contactsForImportation.get(i).getObject2().getEmail());
                        cp.setFirstname(contactsForImportation.get(i).getObject2().getFirstname());
                        cp.setLastname(contactsForImportation.get(i).getObject2().getLastname());
                        cp.setMondayMeal(true);
                        cp.setTuesdayMeal(true);
                        cp.setWednesdayMeal(true);
                        cp.setThursdayMeal(true);
                        cp.setFridayMeal(true);
                        cp.setVegetarianMeal(false);
                        cp.setSocialEvent(false);
                        cp.setTestingSession(TestingSession.getSelectedTestingSession());
                        cp.setInstitution(contactsForImportation.get(i).getObject2().getInstitution());
                        cp.setInstitutionName(contactsForImportation.get(i).getObject2().getInstitution().getName());

                        cp.setConnectathonParticipantStatus(ConnectathonParticipantStatus.getVendorStatus());

                        entityManager.clear();
                        entityManager.persist(cp);
                        entityManager.flush();

                        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Conact " + contactsForImportation.get(i).getObject2()
                                .getLastname() + " " + contactsForImportation.get(i).getObject2().getEmail()
                                + " is imported");
                        renderAddPanel = false;
                        getAllConnectathonParticipants();

                    } catch (Exception e) {
                        LOG.warn(USER + contactsForImportation.get(i).getObject2().getEmail()
                                + " is already added - This case case should not occur...");
                        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.users.connectaton.participants.CannotImportParticipant", cp.getFirstname(),
                                cp.getLastname(), cp.getEmail());

                    }
                } else {
                    LOG.warn(USER + contactsForImportation.get(i).getObject2().getEmail()
                            + " is already added - This case case should not occur...");
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "gazelle.users.connectaton.participants.CannotImportParticipant",
                            contactsForImportation.get(i).getObject2().getFirstname(),
                            contactsForImportation.get(i).getObject2().getLastname(),
                            contactsForImportation.get(i).getObject2().getEmail());
                }
            }
        }

    }

    /**
     * Change and persist Status boolean value for : ConnectathonParticipantStatus (monitor, vendor, committee, visitor...)
     */
    @Override
    public void saveConnectathonParticipantStatus(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveConnectathonParticipantStatus");
        }
        if (cp == null) {
            LOG.warn("saveConnectathonParticipantStatus : ConnectathonParticipant is null ");
            return;
        }

        entityManager.merge(cp);
        entityManager.flush();

    }

    /**
     * Change and persist Day boolean value for : MONDAY
     */
    @Override
    public void saveMondayChange(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveMondayChange");
        }
        if (cp == null) {
            LOG.warn("saveMondayChange : ConnectathonParticipant is null ");
            return;
        }

        entityManager.merge(cp);
        entityManager.flush();

    }

    /**
     * Change and persist Day boolean value for : TUESDAY
     */
    @Override
    public void saveTuesdayChange(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveTuesdayChange");
        }
        if (cp == null) {
            LOG.warn("saveTuesdayChange : ConnectathonParticipant is null ");
            return;
        }

        entityManager.merge(cp);
        entityManager.flush();

    }

    /**
     * Change and persist Day boolean value for : WEDNESDAY
     */
    @Override
    public void saveWednesdayChange(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveWednesdayChange");
        }
        if (cp == null) {
            LOG.warn("saveWednesdayChange : ConnectathonParticipant is null ");
            return;
        }

        entityManager.merge(cp);
        entityManager.flush();

    }

    /**
     * Change and persist Day boolean value for : THURSDAY
     */
    @Override
    public void saveThursdayChange(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveThursdayChange");
        }
        if (cp == null) {
            LOG.warn("saveThursdayChange : ConnectathonParticipant is null ");
            return;
        }

        entityManager.merge(cp);
        entityManager.flush();

    }

    /**
     * Change and persist Day boolean value for : FRIDAY
     */
    @Override
    public void saveFridayChange(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveFridayChange");
        }
        if (cp == null) {
            LOG.warn("saveFridayChange : ConnectathonParticipant is null ");
            return;
        }

        entityManager.merge(cp);
        entityManager.flush();

    }

    /**
     * Change and persist Day boolean value for : VEGETARIAN MEAL
     */
    @Override
    public void saveVegetarianChange(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveVegetarianChange");
        }
        if (cp == null) {
            LOG.warn("saveVegetarianChange : ConnectathonParticipant is null ");
            return;
        }

        entityManager.merge(cp);
        entityManager.flush();

    }

    /**
     * Change and persist Day boolean value for : SOCIAL EVENT
     */
    @Override
    public void saveSocialEventChange(ConnectathonParticipant cp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveSocialEventChange");
        }
        if (cp == null) {
            LOG.warn("saveSocialEventChange : ConnectathonParticipant is null ");
            return;
        }

        entityManager.merge(cp);
        entityManager.flush();

    }

    /**
     * Get the list of companies which have none registered Participants
     */
    @Override
    public void initializeListOfCompaniesWithoutParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeListOfCompaniesWithoutParticipants");
        }

        EntityManager em = EntityManagerService.provideEntityManager();
        companiesWithoutParticipants = new ArrayList<Institution>();
        List<ConnectathonParticipant> foundParticipantsPerCompany;
        TestingSession ts = TestingSession.getSelectedTestingSession();
        List<Institution> companiesParticipating = TestingSession.getListOfInstitutionsParticipatingInSession(ts);

        for (Institution inst : companiesParticipating) {
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution");
            query.setParameter(IN_TESTING_SESSION, ts);
            query.setParameter(IN_INSTITUTION, inst);
            foundParticipantsPerCompany = query.getResultList();

            if (foundParticipantsPerCompany.size() == 0) {
                companiesWithoutParticipants.add(inst);
            }
        }
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

    /**
     * Get the list of Connectathon Participants STATUS attending on, depending on the logged in user (admin or vendor)
     */
    @Override
    public String getPeopleStatusAttending() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeopleStatusAttending");
        }

        List<ConnectathonParticipant> listOfCP = null;
        Integer iVendor = 0;
        Integer iMonitor = 0;
        Integer iCommittee = 0;
        Integer iVisitor = 0;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {

            EntityManager em = EntityManagerService.provideEntityManager();

            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                return "Vendor";
            } else {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                listOfCP = query.getResultList();
            }
        } else {
            return "Vendor";
        }

        for (ConnectathonParticipant cp : listOfCP) {
            if (cp.getConnectathonParticipantStatus().getId().compareTo(ConnectathonParticipantStatus.STATUS_VENDOR) == 0) {
                iVendor++;
            } else if (cp.getConnectathonParticipantStatus().getId()
                    .compareTo(ConnectathonParticipantStatus.STATUS_MONITOR) == 0) {
                iMonitor++;
            } else if (cp.getConnectathonParticipantStatus().getId()
                    .compareTo(ConnectathonParticipantStatus.STATUS_COMMITTEE) == 0) {
                iCommittee++;
            } else if (cp.getConnectathonParticipantStatus().getId()
                    .compareTo(ConnectathonParticipantStatus.STATUS_VISITOR) == 0) {
                iVisitor++;
            } else {
                LOG.error("getPeopleStatusAttending - Status not found !!!!"
                        + cp.getConnectathonParticipantStatus().getId());
            }
        }

        String returnedString = "<br/>" + iVendor + " Vendors<br/> " + iMonitor + " Monitors<br/> " + iCommittee + " Committees<br/> "
                + iVisitor + " Visitors";

        return returnedString;
    }

    /**
     * Get the list of Connectathon Participants attending on MONDAY, depending on the logged in user (admin or vendor)
     */
    @Override
    public List<ConnectathonParticipant> getPeopleAttendingOnMonday() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeopleAttendingOnMonday");
        }

        renderAddPanel = false;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {

            EntityManager em = EntityManagerService.provideEntityManager();
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                                ":inInstitution AND mondayMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                query.setParameter(IN_INSTITUTION, choosenInstitutionForAdmin);
                return query.getResultList();
            } else {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND mondayMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                return query.getResultList();
            }
        } else {
            selectedInstitution = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution AND mondayMeal IS true");
            query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
            query.setParameter(IN_INSTITUTION, selectedInstitution);
            return query.getResultList();
        }

    }

    /**
     * Get the list of Connectathon Participants attending on TUESDAY, depending on the logged in user (admin or vendor)
     */
    @Override
    public List<ConnectathonParticipant> getPeopleAttendingOnTuesday() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeopleAttendingOnTuesday");
        }

        renderAddPanel = false;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {

            EntityManager em = EntityManagerService.provideEntityManager();
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                                ":inInstitution AND tuesdayMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                query.setParameter(IN_INSTITUTION, choosenInstitutionForAdmin);
                return query.getResultList();
            } else {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND tuesdayMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                return query.getResultList();
            }
        } else {
            selectedInstitution = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution AND tuesdayMeal IS true");
            query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
            query.setParameter(IN_INSTITUTION, selectedInstitution);
            return query.getResultList();
        }
    }

    /**
     * Get the list of Connectathon Participants attending on WEDNESDAY, depending on the logged in user (admin or vendor)
     */
    @Override
    public List<ConnectathonParticipant> getPeopleAttendingOnWednesday() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeopleAttendingOnWednesday");
        }

        renderAddPanel = false;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {

            EntityManager em = EntityManagerService.provideEntityManager();
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                                ":inInstitution AND wednesdayMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                query.setParameter(IN_INSTITUTION, choosenInstitutionForAdmin);
                return query.getResultList();
            } else {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND wednesdayMeal IS " +
                                "true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                return query.getResultList();
            }
        } else {
            selectedInstitution = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution AND wednesdayMeal IS true");
            query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
            query.setParameter(IN_INSTITUTION, selectedInstitution);
            return query.getResultList();
        }
    }

    /**
     * Get the list of Connectathon Participants attending on THURSDAY, depending on the logged in user (admin or vendor)
     */
    @Override
    public List<ConnectathonParticipant> getPeopleAttendingOnThursday() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeopleAttendingOnThursday");
        }

        renderAddPanel = false;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {

            EntityManager em = EntityManagerService.provideEntityManager();
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                                ":inInstitution AND thursdayMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                query.setParameter(IN_INSTITUTION, choosenInstitutionForAdmin);
                return query.getResultList();
            } else {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND thursdayMeal IS " +
                                "true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                return query.getResultList();
            }
        } else {
            selectedInstitution = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution AND thursdayMeal IS true");
            query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
            query.setParameter(IN_INSTITUTION, selectedInstitution);
            return query.getResultList();
        }
    }

    /**
     * Get the list of Connectathon Participants attending on FRIDAY, depending on the logged in user (admin or vendor)
     */
    @Override
    public List<ConnectathonParticipant> getPeopleAttendingOnFriday() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeopleAttendingOnFriday");
        }

        renderAddPanel = false;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {

            EntityManager em = EntityManagerService.provideEntityManager();
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                                ":inInstitution AND fridayMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                query.setParameter(IN_INSTITUTION, choosenInstitutionForAdmin);
                return query.getResultList();
            } else {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND fridayMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                return query.getResultList();
            }
        } else {
            selectedInstitution = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution AND fridayMeal IS true");
            query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
            query.setParameter(IN_INSTITUTION, selectedInstitution);
            return query.getResultList();
        }
    }

    /**
     * Get the list of Connectathon Participants VEGETARIAN attending on, depending on the logged in user (admin or vendor)
     */
    @Override
    public List<ConnectathonParticipant> getVegetarianPeopleAttending() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getVegetarianPeopleAttending");
        }

        renderAddPanel = false;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {

            EntityManager em = EntityManagerService.provideEntityManager();
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                                ":inInstitution AND vegetarianMeal IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                query.setParameter(IN_INSTITUTION, choosenInstitutionForAdmin);
                return query.getResultList();
            } else {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND vegetarianMeal IS " +
                                "true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                return query.getResultList();
            }
        } else {
            selectedInstitution = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution AND vegetarianMeal IS true");
            query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
            query.setParameter(IN_INSTITUTION, selectedInstitution);
            return query.getResultList();
        }
    }

    /**
     * Get the list of Connectathon Participants attending on SOCIAL EVENT, depending on the logged in user (admin or vendor)
     */
    @Override
    public List<ConnectathonParticipant> getPeopleAttendingOnSocialEvent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeopleAttendingOnSocialEvent");
        }

        renderAddPanel = false;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserMonitor()) {

            EntityManager em = EntityManagerService.provideEntityManager();
            choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);

            if (choosenInstitutionForAdmin != null) {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                                ":inInstitution AND socialEvent IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                query.setParameter(IN_INSTITUTION, choosenInstitutionForAdmin);
                return query.getResultList();
            } else {
                Query query = em
                        .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND socialEvent IS true");
                query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
                return query.getResultList();
            }
        } else {
            selectedInstitution = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT cp FROM ConnectathonParticipant cp WHERE cp.testingSession = :inTestingSession AND cp.institution = " +
                            ":inInstitution AND socialEvent IS true");
            query.setParameter(IN_TESTING_SESSION, TestingSession.getSelectedTestingSession());
            query.setParameter(IN_INSTITUTION, selectedInstitution);
            return query.getResultList();
        }
    }

    @Override
    public ConnectathonParticipant getSelectedConnectathonParticipant() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedConnectathonParticipant");
        }
        return selectedConnectathonParticipant;
    }

    @Override
    public void setSelectedConnectathonParticipant(ConnectathonParticipant selectedConnectathonParticipant) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedConnectathonParticipant");
        }
        this.selectedConnectathonParticipant = selectedConnectathonParticipant;
    }

    @Override
    public List<ConnectathonParticipant> getConnectathonParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConnectathonParticipants");
        }

        return connectathonParticipants;
    }

    @Override
    public void setConnectathonParticipants(List<ConnectathonParticipant> connectathonParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setConnectathonParticipants");
        }
        this.connectathonParticipants = connectathonParticipants;
    }

    @Override
    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }
        return selectedInstitution;
    }

    @Override
    public void setSelectedInstitution(Institution selectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitution");
        }
        this.selectedInstitution = selectedInstitution;
    }

    @Override
    public List<Pair<Boolean, User>> getUsersForImportation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsersForImportation");
        }
        return usersForImportation;
    }

    @Override
    public GazelleListDataModel<Pair<Boolean, User>> getUsersForImportationDM() {
        return new GazelleListDataModel<Pair<Boolean, User>>(getUsersForImportation());
    }

    @Override
    public void setUsersForImportation(List<Pair<Boolean, User>> usersForImportation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setUsersForImportation");
        }
        this.usersForImportation = usersForImportation;
    }

    @Override
    public List<Pair<Boolean, Person>> getContactsForImportation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getContactsForImportation");
        }
        return contactsForImportation;
    }

    @Override
    public GazelleListDataModel<Pair<Boolean, Person>> getContactsForImportationDM() {
        return new GazelleListDataModel<Pair<Boolean, Person>>(getContactsForImportation());
    }

    @Override
    public void setContactsForImportation(List<Pair<Boolean, Person>> contactsForImportation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setContactsForImportation");
        }
        this.contactsForImportation = contactsForImportation;
    }

    @Override
    public Boolean getRenderAddPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRenderAddPanel");
        }
        return renderAddPanel;
    }

    @Override
    public void setRenderAddPanel(Boolean renderAddPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRenderAddPanel");
        }
        this.renderAddPanel = renderAddPanel;
    }

    @Override
    public List<SystemInSession> getFoundSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundSystemsInSession");
        }
        return foundSystemsInSession;
    }

    @Override
    public void setFoundSystemsInSession(List<SystemInSession> foundSystemsInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFoundSystemsInSession");
        }
        this.foundSystemsInSession = foundSystemsInSession;
    }

    @Override
    public Integer getTheoricMenDayPerSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTheoricMenDayPerSystem");
        }
        BigDecimal iPeople = new BigDecimal("2");
        BigDecimal iDays = new BigDecimal("5");
        BigDecimal iSubResult;
        BigDecimal iMenDayPerSystem;

        iSubResult = iPeople.multiply(iDays);
        iMenDayPerSystem = iSubResult.multiply(new BigDecimal(foundSystemsInSession.size()));

        return iMenDayPerSystem.intValue();
    }

    @Override
    public Integer getCurrentMenDayPerCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentMenDayPerCompany");
        }

        Integer iResult = getPeopleAttendingOnMonday().size() + getPeopleAttendingOnTuesday().size()
                + getPeopleAttendingOnWednesday().size() + getPeopleAttendingOnThursday().size()
                + getPeopleAttendingOnFriday().size();

        return iResult;
    }

    @Override
    public Integer getChargedMenDayPerSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChargedMenDayPerSystem");
        }
        return getCurrentMenDayPerCompany() - getTheoricMenDayPerSystem();
    }

    @Override
    public void setMenDayPerSystem(Integer menDayPerSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMenDayPerSystem");
        }
        this.menDayPerSystem = menDayPerSystem;
    }

    @Override
    public List<Institution> getCompaniesWithoutParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCompaniesWithoutParticipants");
        }
        return companiesWithoutParticipants;
    }

    @Override
    public void setCompaniesWithoutParticipants(List<Institution> companiesWithoutParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCompaniesWithoutParticipants");
        }
        this.companiesWithoutParticipants = companiesWithoutParticipants;
    }

    @Override
    public Integer getTheoricMenPerSystems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTheoricMenPerSystems");
        }
        if (this.foundSystemsInSession == null) {
            return 0;
        }
        int numSys = this.foundSystemsInSession.size();
        return numSys * TestingSession.getSelectedTestingSession().getNbParticipantsIncludedInSystemFees();
    }

    @Override
    public Integer getChargedMenPerSystems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChargedMenPerSystems");
        }
        return getNbOfParticipants() - getTheoricMenPerSystems();
    }

    public void importMonitorsInSession() {
        List<MonitorInSession> monitors = MonitorInSession.getAllActivatedMonitorsForATestingSession(TestingSession.getSelectedTestingSession());
        if (monitors == null || monitors.isEmpty()) {
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "No monitor available for this testing session");
        } else {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            int count = 0;
            for (MonitorInSession monitor : monitors) {
                User muser = monitor.getUser();
                // check monitor is not yet registered for this session
                ConnectathonParticipantQuery query = new ConnectathonParticipantQuery();
                query.email().eq(muser.getEmail());
                query.testingSession().eq(monitor.getTestingSession());
                ConnectathonParticipant participantFromDB = query.getUniqueResult();
                Institution minstitution = muser.getInstitution();
                if (participantFromDB == null) {
                    ConnectathonParticipant mparticipant = new ConnectathonParticipant(
                            muser.getFirstname(),
                            muser.getLastname(),
                            muser.getEmail(),
                            minstitution,
                            minstitution, true, true, true,
                            true, true, false, monitor.getTestingSession(), ConnectathonParticipantStatus.getMonitorStatus());
                    mparticipant.setSocialEvent(true);
                    mparticipant.setInstitutionName(minstitution.getName());
                    entityManager.merge(mparticipant);
                    entityManager.flush();
                    count++;
                }
            }
            FacesMessages.instance().add(StatusMessage.Severity.INFO, count + " monitors have been added to the list of participants");
        }
    }

    /**
     * Destroy the Manager bean when the session is over.
     */
    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
