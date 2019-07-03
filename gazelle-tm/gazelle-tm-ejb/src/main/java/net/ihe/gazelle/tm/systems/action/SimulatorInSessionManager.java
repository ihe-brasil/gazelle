package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemAIPOResultForATestingSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.utils.systems.IHEImplementationForSystem;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;


@Name("simulatorInSessionManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("SimulatorInSessionManagerLocal")
public class SimulatorInSessionManager implements SimulatorInSessionManagerLocal, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorInSessionManager.class);
    public static String noneKeywordString = "NONE";
    public static String noneDescriptionString = "None";
    Integer defaultTestingTypeId = 1;
    private SimulatorInSession selectedSimulatorInSession;
    private InstitutionSystem institutionSystem;
    private List<IHEImplementationForSystem> iheImplementations;

    // ///
    private ActorIntegrationProfileOption selectedActorIntegrationProfileOption;

    private List<Domain> possibleDomains;

    private Domain selectedDomain;

    // ///////////////////////////////////////////

    @Override
    public List<IHEImplementationForSystem> getIheImplementations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIheImplementations");
        }
        return iheImplementations;
    }

    @Override
    public void setIheImplementations(List<IHEImplementationForSystem> iheImplementations) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIheImplementations");
        }
        this.iheImplementations = iheImplementations;
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
    public ActorIntegrationProfileOption getSelectedActorIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActorIntegrationProfileOption");
        }
        return selectedActorIntegrationProfileOption;
    }

    @Override
    public void setSelectedActorIntegrationProfileOption(
            ActorIntegrationProfileOption selectedActorIntegrationProfileOption) {
        this.selectedActorIntegrationProfileOption = selectedActorIntegrationProfileOption;
    }

    @Override
    public List<Domain> getPossibleDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleDomains");
        }
        return possibleDomains;
    }

    @Override
    public void setPossibleDomains(List<Domain> possibleDomains) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPossibleDomains");
        }
        this.possibleDomains = possibleDomains;
    }

    // //

    @Override
    public InstitutionSystem getInstitutionSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionSystem");
        }
        return institutionSystem;
    }

    @Override
    public void setInstitutionSystem(InstitutionSystem institutionSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInstitutionSystem");
        }
        this.institutionSystem = institutionSystem;
    }

    @Override
    public SimulatorInSession getSelectedSimulatorInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSimulatorInSession");
        }
        return selectedSimulatorInSession;
    }

    @Override
    public void setSelectedSimulatorInSession(SimulatorInSession selectedSimulatorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSimulatorInSession");
        }
        this.selectedSimulatorInSession = selectedSimulatorInSession;
    }

    @Override
    public List<SimulatorInSession> getAvailableSimulatorInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAvailableSimulatorInSession");
        }
        TestingSession tss = TestingSession.getSelectedTestingSession();

        SimulatorInSessionQuery query = new SimulatorInSessionQuery();
        query.testingSession().eq(tss);
        return query.getList();
    }

    @Override
    public void deleteSelectedSimulatorInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedSimulatorInSession");
        }
        deleteSimulatorInSession(selectedSimulatorInSession);
    }

    @Override
    public void deleteSimulatorInSession(SimulatorInSession inSimulatorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSimulatorInSession");
        }
        if (inSimulatorInSession != null) {
            List<TestInstanceParticipants> testInstanceParticipantsList = TestInstanceParticipants
                    .getTestInstancesParticipantsFiltered(null, null, inSimulatorInSession.getSystem(), null, null,
                            null, null, null, null);
            if ((testInstanceParticipantsList != null) && (testInstanceParticipantsList.size() > 0)) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                        "The selected simulator " + inSimulatorInSession.getSystem().getKeyword()
                                + " is used in many test instances.\n it can not be deleted.");
            } else {
                SystemInSession.deleteSelectedSystemInSessionWithDependencies(inSimulatorInSession);
                try {
                    System.deleteSelectedSystem(inSimulatorInSession.getSystem());
                } catch (Exception e) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "The selected simulator " + inSimulatorInSession.getSystem().getKeyword()
                                    + " can not be deleted.");
                }
            }
        }
    }

    @Override
    public String editSimulatorInSession(SimulatorInSession inSimulatorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSimulatorInSession");
        }
        selectedSimulatorInSession = inSimulatorInSession;
        return "/systems/simulators/editSimulator.xhtml";
    }

    @Override
    public void cancelSimulatorInSessionEdition() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelSimulatorInSessionEdition");
        }
        selectedSimulatorInSession = null;
    }

    @Override
    public List<User> getAllowedUsersToManageSimulators() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllowedUsersToManageSimulators");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        Query query = entityManager
                .createQuery("SELECT DISTINCT usr FROM User usr JOIN usr.roles role WHERE role=:inRole1 or role=:inRole2");
        query.setParameter("inRole1", Role.getADMINISTRATOR_ROLE());
        query.setParameter("inRole2", Role.getTESTS_EDITOR_ROLE());
        return query.getResultList();
    }

    @Override
    public Simulator persistSimulator(Simulator inSimulator) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSimulator");
        }
        String newkeyword = inSimulator.computeKeyword();
        inSimulator.setKeyword(newkeyword);
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        inSimulator = entityManager.merge(inSimulator);
        entityManager.flush();
        return inSimulator;
    }

    @Override
    public void persistSelectedSimulatorInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedSimulatorInSession");
        }
        Simulator system = persistSimulator(Simulator.class.cast(selectedSimulatorInSession.getSystem()));
        if (selectedSimulatorInSession.getId() == null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            institutionSystem.setSystem(system);
            institutionSystem = entityManager.merge(institutionSystem);
            entityManager.flush();
        }
        system = persistSimulator(system);
        selectedSimulatorInSession.setSystem(system);
        selectedSimulatorInSession.setRegistrationStatus(SystemInSessionRegistrationStatus.COMPLETED);
        selectedSimulatorInSession = persistSimulatorInSession(selectedSimulatorInSession);
    }

    @Override
    public SimulatorInSession persistSimulatorInSession(SimulatorInSession inSimulatorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSimulatorInSession");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        inSimulatorInSession = entityManager.merge(inSimulatorInSession);
        entityManager.flush();
        FacesMessages.instance().addToControl("SimulatorsglobalMessage",
                "The simulator " + inSimulatorInSession.getSystem().getKeyword() + " was updated sucessfully.");
        return inSimulatorInSession;
    }

    @Override
    public String createNewSimulatorInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createNewSimulatorInSession");
        }
        Simulator simulator = new Simulator();
        User connectedUser = User.loggedInUser();
        simulator.setOwnerUser(connectedUser);
        List<User> userList = User.getUsersFiltered(null, Role.getADMINISTRATOR_ROLE());
        institutionSystem = new InstitutionSystem();
        institutionSystem.setInstitution(userList.get(0).getInstitution());
        institutionSystem.setSystem(simulator);
        selectedSimulatorInSession = new SimulatorInSession(simulator, TestingSession.getSelectedTestingSession());
        return "/systems/simulators/editSimulator.xhtml";
    }

    // -----------------------------------

    @Override
    public String getUrlOfSimuInSession(SimulatorInSession sis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUrlOfSimuInSession");
        }
        String res = "";
        if (sis != null) {
            if (sis.getSystem() != null) {
                res = Simulator.class.cast(sis.getSystem()).getUrl();
            }
        }

        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SystemType> getPossibleSystemTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleSystemTypes");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        List<SystemType> lst = entityManager.createQuery("from SystemType row order by systemTypeKeyword asc ").getResultList();
        return lst;
    }

    @Override
    public void initializeActorIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeActorIntegrationProfileOption");
        }
        if (selectedActorIntegrationProfileOption == null) {
            selectedActorIntegrationProfileOption = new ActorIntegrationProfileOption();
            selectedActorIntegrationProfileOption.setActorIntegrationProfile(new ActorIntegrationProfile());
        }
        possibleDomains = getPossibleDomainsDependingOnTestingSession(TestingSession.getSelectedTestingSession());
    }

    private List<Domain> getPossibleDomainsDependingOnTestingSession(TestingSession ts) {
        return ts.getDomains();
    }

    @Override
    public void resetSelectedOtherItems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetSelectedOtherItems");
        }
        if (selectedActorIntegrationProfileOption == null) {
            selectedActorIntegrationProfileOption = new ActorIntegrationProfileOption();
        }
        if (selectedActorIntegrationProfileOption.getActorIntegrationProfile() == null) {
            selectedActorIntegrationProfileOption.setActorIntegrationProfile(new ActorIntegrationProfile());
        }

        selectedActorIntegrationProfileOption.getActorIntegrationProfile().setIntegrationProfile(null);
        selectedActorIntegrationProfileOption.getActorIntegrationProfile().setActor(null);
        selectedActorIntegrationProfileOption.setIntegrationProfileOption(null);
    }

    @Override
    public void resetSelectedItems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetSelectedItems");
        }
        selectedDomain = null;
        resetSelectedOtherItems();
        if (possibleDomains == null) {
            possibleDomains = getPossibleDomainsDependingOnTestingSession(TestingSession.getSelectedTestingSession());
        }
    }

    @Override
    public List<IntegrationProfileOption> getPossibleIPOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIPOption");
        }
        List<IntegrationProfileOption> listOfIPO = IntegrationProfileOption.getListOfIntegrationProfileOptions(
                selectedActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                selectedActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile());
        if (listOfIPO.size() == 1) {
            selectedActorIntegrationProfileOption.setIntegrationProfileOption(listOfIPO.get(0));
        } else {
            selectedActorIntegrationProfileOption.setIntegrationProfileOption(null);
        }
        return listOfIPO;
    }

    /**
     * Add a combination Actor/Profile/OPtion to a system, if this combination does not exist. Also calculate the news fees amount.
     *
     * @param d   : Domain
     * @param ip  : Integration Profile
     * @param a   : Actor
     * @param ipo : Integration Profile Option
     * @return JSF page to render
     */

    // *
    @Override
    public void addIHEImplementationForSimulator(Domain d, ActorIntegrationProfileOption inActorIntegrationProfileOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addIHEImplementationForSimulator");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        try {

            TestingType defaultTestingType;
            inActorIntegrationProfileOption = ActorIntegrationProfileOption.getActorIntegrationProfileOption(
                    inActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                    inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile(),
                    inActorIntegrationProfileOption.getIntegrationProfileOption());

            inActorIntegrationProfileOption = entityManager.find(ActorIntegrationProfileOption.class,
                    inActorIntegrationProfileOption.getId());

            if (d == null) {
                LOG.warn("addIHEImplementationForSystem : Domain d is null...");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tf.faces.DomainNull");

            }
            if (inActorIntegrationProfileOption == null) {
                LOG.warn("addIHEImplementationForSystem : ActorIntegrationProfileOption is null...");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tf.faces.AIPONull");

            } else if (inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile() == null) {
                LOG.warn("addIHEImplementationForSystem : IntegrationProfile a is null...");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tf.faces.IPNull");

            } else if (inActorIntegrationProfileOption.getActorIntegrationProfile().getActor() == null) {
                LOG.warn("addIHEImplementationForSystem : Actor a is null...");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tf.faces.ActorNull");

            } else if (inActorIntegrationProfileOption.getIntegrationProfileOption() == null) {
                inActorIntegrationProfileOption.setIntegrationProfileOption(new IntegrationProfileOption(
                        noneKeywordString, noneDescriptionString));

            }

            IHEImplementationForSystem iheImplementationForSystem = null;
            if (inActorIntegrationProfileOption != null) {
                iheImplementationForSystem = new IHEImplementationForSystem(d,
                        inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile(),
                        inActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                        inActorIntegrationProfileOption.getIntegrationProfileOption());
            }

            // We create the list containing all IHE Implementations for the Integration Statement, if it does not exist
            if (iheImplementations == null) {
                iheImplementations = new Vector<IHEImplementationForSystem>();
            }

            // We check that the asked IHEImplementationForSystem does not exist in the IHE Implementations list
            if (isExistingIHEImplementationForSystem(iheImplementationForSystem)) {
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("facesM",
                        StatusMessage.Severity.WARN, "gazelle.systems.system.faces.ThisIHEImplementationAlreadyExists",
                        "gazelle.systems.system.faces.ThisIHEImplementationAlreadyExists");
            } else {
                // We keep the selected system in the session
                iheImplementations.add(iheImplementationForSystem);

                // We retrieve the default TestingType from the entity, this TestingType will be associated to this new SystemActorProfile
                defaultTestingType = entityManager.find(TestingType.class, defaultTestingTypeId);

                // Add SystemActorProfile entity into Database
                SystemActorProfiles systemActorProfile;

                if (inActorIntegrationProfileOption.getIntegrationProfileOption().getKeyword().equals(noneKeywordString)) {
                    IntegrationProfileOption ipo1 = entityManager.find(IntegrationProfileOption.class, 1);

                    // We finally create sap with null option using with the option id = 1 (corresponds to the null option)
                    systemActorProfile = new SystemActorProfiles(selectedSimulatorInSession.getSystem(),
                            inActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                            inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile(), ipo1,
                            defaultTestingType);
                } else {
                    systemActorProfile = new SystemActorProfiles(selectedSimulatorInSession.getSystem(),
                            inActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                            inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile(),
                            inActorIntegrationProfileOption.getIntegrationProfileOption(), defaultTestingType);
                }

                entityManager.merge(systemActorProfile);
                entityManager.flush();

                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.systems.system.faces.IHEImplementationAdded",
                        d.getName(),
                        inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile().getName(),
                        inActorIntegrationProfileOption.getActorIntegrationProfile().getActor().getName(),
                        inActorIntegrationProfileOption.getIntegrationProfileOption().getDescription(),
                        selectedSimulatorInSession.getSystem().getName());

                // Add the Null Option for this system/Actor/Profile if it does not exist
                IntegrationProfileOption ipo1 = entityManager.find(IntegrationProfileOption.class, 1);

                List<SystemActorProfiles> sapList = SystemActorProfiles
                        .getSystemActorProfilesFiltered(entityManager, null, selectedSimulatorInSession.getSystem(),
                                null, null, null, iheImplementationForSystem.getIntegrationProfile(), ipo1,
                                iheImplementationForSystem.getActor());

                Integer resultOfOption = sapList.size();

                // If it does not exist, we create it
                if (resultOfOption == 0) {

                    // We finally create sap with null option using with the option id = 1 (corresponds to the null option)
                    SystemActorProfiles noneOptionSystemActorProfile = new SystemActorProfiles(
                            selectedSimulatorInSession.getSystem(), inActorIntegrationProfileOption
                            .getActorIntegrationProfile().getActor(), inActorIntegrationProfileOption
                            .getActorIntegrationProfile().getIntegrationProfile(), ipo1, defaultTestingType);

                    entityManager.merge(noneOptionSystemActorProfile);
                    entityManager.flush();

                }
            }

        } catch (ConstraintViolationException exc) {

            ExceptionLogging.logException(exc, LOG);
            LOG.error("Constraint Exception on : " + exc.getConstraintName());
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                    "gazelle.systems.system.faces.ThisIHEImplementationAlreadyExists");

        } catch (Exception exc2) {
            ExceptionLogging.logException(exc2, LOG);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.systems.system.faces.Error" + exc2.getMessage());

        }
    }

    private Boolean isExistingIHEImplementationForSystem(IHEImplementationForSystem iheImplementationForSystem) {
        Boolean isExisting = false;
        Integer i;

        // We cannot compare IHE implementation using : isExisting = iheImplementations.contains(iheImplementationForSystem); because objects are
        // different.

        for (i = 0; i < iheImplementations.size(); i++) {
            if (iheImplementations.get(i).getActor().getId().equals(iheImplementationForSystem.getActor().getId())) {

                if (iheImplementations.get(i).getIntegrationProfile().getId()
                        .equals(iheImplementationForSystem.getIntegrationProfile().getId())) {

                    if (iheImplementationForSystem.getIntegrationProfileOption().getKeyword().equals(noneKeywordString)) // if there is no
                    // integration profile option for this IHE implementation
                    {

                        if (iheImplementations.get(i).getIntegrationProfileOption().getKeyword()
                                .equals(noneKeywordString)) {

                            isExisting = true;
                        }
                    } else { // if there is an integration profile option for this IHE implementation

                        if (iheImplementations.get(i).getIntegrationProfileOption().getId()
                                .equals(iheImplementationForSystem.getIntegrationProfileOption().getId())) {

                            isExisting = true;
                        }
                    }
                }
            }
        }

        return isExisting;
    }

    @Override
    public void deleteIHEImplementationForSystem(IHEImplementationForSystem iheImplementationForSystem, Boolean isReset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteIHEImplementationForSystem");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        try {
            synchronized (this) {
                SystemActorProfiles currentSystemActorProfile;
                if (!isReset) {
                    iheImplementations.remove(iheImplementationForSystem);
                }
                if (iheImplementationForSystem.getIntegrationProfileOption() == null) {
                    currentSystemActorProfile = (SystemActorProfiles) entityManager
                            .createQuery(
                                    "select distinct sap from SystemActorProfiles sap where sap.actorIntegrationProfileOption" +
                                            ".actorIntegrationProfile.actor.id = "
                                            + iheImplementationForSystem.getActor().getId()
                                            + " and sap.system.id = "
                                            + selectedSimulatorInSession.getSystem().getId()
                                            + " and sap.actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile.id = "
                                            + iheImplementationForSystem.getIntegrationProfile().getId())
                            .getSingleResult();

                } else {
                    currentSystemActorProfile = (SystemActorProfiles) entityManager
                            .createQuery(
                                    "select distinct sap from SystemActorProfiles sap where sap.actorIntegrationProfileOption" +
                                            ".actorIntegrationProfile.actor.id = "
                                            + iheImplementationForSystem.getActor().getId()
                                            + " and sap.system.id = "
                                            + selectedSimulatorInSession.getSystem().getId()
                                            + " and sap.actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile.id = "
                                            + iheImplementationForSystem.getIntegrationProfile().getId()
                                            + " and sap.actorIntegrationProfileOption.integrationProfileOption.id = "
                                            + iheImplementationForSystem.getIntegrationProfileOption().getId())
                            .getSingleResult();
                }

                SystemAIPOResultForATestingSession.deleteSystemAIPOResult(currentSystemActorProfile);
                entityManager.remove(currentSystemActorProfile);
                entityManager.flush();
            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            StatusMessages.instance().add("Err : " + e.getMessage());

        }

    }

    @Override
    public String displayInstitutionForSystem(System inSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayInstitutionForSystem");
        }
        List<String> listInstKey = System.getInstitutionsKeywordsForASystem(inSystem);
        StringBuffer s = new StringBuffer();

        int size = listInstKey.size();
        for (int i = 0; i < size; i++) {
            if ((size > 1) && (i == (size - 1))) {
                s.append(" / ");
            }
            if (listInstKey.get(i) != null) {
                s.append(listInstKey.get(i));
            }
        }
        return s.toString();
    }

    @Override
    public String getKeywordWithoutSuffixForSelectedSimu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getKeywordWithoutSuffixForSelectedSimu");
        }
        String key = null;
        if (this.selectedSimulatorInSession != null) {
            if (this.selectedSimulatorInSession.getSystem() != null) {
                if (this.selectedSimulatorInSession.getSystem().getId() != null) {
                    key = this.selectedSimulatorInSession.getSystem().getKeywordWithoutSuffix();
                }
            }
        }

        if (key != null) {
            if (!key.equals("")) {
                return key;
            }
        }
        key = this.selectedSimulatorInSession.getSystem().getSystemType().getSystemTypeKeyword() + "_"
                + this.institutionSystem.getInstitution().getKeyword();
        return key;

    }

    @Override
    public List<IntegrationProfileOption> getListOfIntegrationProfileOptions(Actor inActor, IntegrationProfile inIP) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfIntegrationProfileOptions");
        }
        return IntegrationProfileOption.getListOfIntegrationProfileOptions(inActor, inIP);
    }

    // -----------------------------------

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }
}
