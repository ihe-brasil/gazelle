package net.ihe.gazelle.tm.application.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.application.action.PreferencesModifier;
import net.ihe.gazelle.common.preference.PreferenceType;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingSessionQuery;
import net.ihe.gazelle.tm.users.model.UserPreferences;
import net.ihe.gazelle.tm.users.model.UserPreferencesQuery;
import net.ihe.gazelle.users.model.*;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Name("installationManager")
@Scope(ScopeType.PAGE)
public class InstallationManager implements Serializable {

    private static final long serialVersionUID = -7034574532590515371L;

    private static final Logger LOG = LoggerFactory.getLogger(InstallationManager.class);

    private static final int CREATE_ORGANIZATION = 1;
    private static final int CREATE_ACCOUNT = 2;
    private static final int CREATE_TESTING_SESSION = 3;
    private static final int CONFIGURE = 4;
    private static final int END = 5;
    private static final int FINISH = 6;

    private Integer step;
    private Institution newOrganization;
    private User newUser;
    private TestingSession newTestingSession;
    private List<Institution> availableInstitutions;

    public Integer getStep() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStep");
        }
        return step;
    }

    public void setStep(Integer step) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setStep");
        }
        this.step = step;
    }

    public Institution getNewOrganization() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNewOrganization");
        }
        return newOrganization;
    }

    public void setNewOrganization(Institution newInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNewOrganization");
        }
        this.newOrganization = newInstitution;
    }

    public User getNewUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNewUser");
        }
        return newUser;
    }

    public void setNewUser(User newUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNewUser");
        }
        this.newUser = newUser;
    }

    public TestingSession getNewTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNewTestingSession");
        }
        return newTestingSession;
    }

    public void setNewTestingSession(TestingSession newTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNewTestingSession");
        }
        this.newTestingSession = newTestingSession;
    }

    public List<Institution> getAvailableInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAvailableInstitutions");
        }
        return availableInstitutions;
    }

    public void setAvailableInstitutions(List<Institution> availableInstitutions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAvailableInstitutions");
        }
        this.availableInstitutions = availableInstitutions;
    }

    public void initializeInstallation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeInstallation");
        }
        if (ApplicationPreferenceManager.instance().getInstallationDone()) {
            step = FINISH;
        } else {
            // count the number of registered institutions
            InstitutionQuery q = new InstitutionQuery();
            Integer nbOfInstitution = q.getCount();
            if (nbOfInstitution == 0) {
                step = CREATE_ORGANIZATION;
                newOrganization = new Institution();
                availableInstitutions = null;
            } else {
                // count the number of registered users
                UserQuery q1 = new UserQuery();
                Integer nbOfUsers = q1.getCount();
                if (nbOfUsers == 0) {
                    step = CREATE_ACCOUNT;
                    newUser = new User();
                    q.keyword().neqRestriction(null);
                    availableInstitutions = q.getList();
                    Collections.sort(availableInstitutions);
                    if (availableInstitutions.size() == 1) {
                        newUser.setInstitution(availableInstitutions.get(0));
                    }
                } else {
                    // count the number of created testing sessions
                    TestingSessionQuery q2 = new TestingSessionQuery();
                    Integer nbOfTestingSessions = q2.getCount();
                    if (nbOfTestingSessions == 0) {
                        step = CREATE_TESTING_SESSION;
                        newTestingSession = new TestingSession();
                    } else {
                        step = CONFIGURE;
                    }
                }
            }
        }
    }

    public void realizeStep() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("realizeStep");
        }

        switch (step) {
            case CREATE_ORGANIZATION:
                createOrganization();
                break;
            case CREATE_ACCOUNT:
                createAccount();
                break;
            case CREATE_TESTING_SESSION:
                createTestingSession();
                break;
            case CONFIGURE:
                configureApplication();
                break;
            case END:
                break;
            default:
                break;
        }
    }

    private void createOrganization() {
        EntityManager em = EntityManagerService.provideEntityManager();
        newOrganization.setActivated(true);
        newOrganization = em.merge(newOrganization);
        em.flush();
        // organization used to create a new organization
        Institution institution = new Institution();
        institution.setKeyword("NULL");
        institution.setName("New company - Create your company");
        institution.setActivated(true);
        institution.setUrl("http://ihe.net");
        institution.setInstitutionType(new InstitutionTypeQuery().getUniqueResult());
        em.merge(institution);
        em.flush();
        newUser = new User();
        newUser.setInstitution(newOrganization);
        step++;
    }

    private void createAccount() {

        if ((newUser.getPassword() != null) && (newUser.getPasswordConfirmation() != null)
                && newUser.getPassword().equals(newUser.getPasswordConfirmation())) {
            newUser.setActivated(true);
            newUser.setBlocked(false);
            newUser.setLoginsCounter(0);
            newUser.setFailedLoginAttemptsCounter(0);
            newUser.setCreationDate(new Date());
            List<Role> roles = new ArrayList<Role>();
            roles.add(Role.getADMINISTRATOR_ROLE());
            newUser.setRoles(roles);
            EntityManager em = EntityManagerService.provideEntityManager();
            newUser = em.merge(newUser);
            em.flush();
            UserPreferences userPreferences = new UserPreferences();
            userPreferences.setShowSequenceDiagram(true);
            userPreferences.setUser(newUser);
            userPreferences.setDisplayEmail(false);
            userPreferences.setNumberOfResultsPerPage(UserPreferences.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
            em.merge(userPreferences);
            em.flush();
            newTestingSession = new TestingSession();
            step++;
        } else {
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tm.install.passwordsDoNotMatch");
        }
    }

    private void createTestingSession() {
        if (newTestingSession.getRegistrationDeadlineDate() == null) {
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tm.systems.setRegistrationDeadline");
            return;
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        newTestingSession.setActiveSession(true);
        newTestingSession.setHiddenSession(false);
        newTestingSession.setOrderInGUI(1);
        newTestingSession = em.merge(newTestingSession);
        em.flush();
        step++;

        setFirstTestingSession(em);
    }

    private void setFirstTestingSession(EntityManager em) {
        UserPreferencesQuery q = new UserPreferencesQuery();
        q.user().id().eq(newUser.getId());
        UserPreferences userPreferences = q.getUniqueResult();
        userPreferences.setSelectedTestingSession(newTestingSession);
        em.merge(userPreferences);
        em.flush();
    }

    private void configureApplication() {
        PreferencesModifier preferencesModifierBean = (PreferencesModifier) Component
                .getInstance("preferencesModifier");
        preferencesModifierBean.saveValues();
        String basepath = ApplicationPreferenceManager.getStringValue("gazelle_home_path");
        if (basepath != null) {
            File newDir = new File(basepath);
            if (!newDir.exists()) {
                if (newDir.mkdirs()) {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.tm.install.directoryCreated", basepath);
                    List<String> dirToCreate = new ArrayList<String>();
                    dirToCreate.add("data_path");
                    dirToCreate.add("reports_path");
                    dirToCreate.add("bin_path");
                    dirToCreate.add("integration_statements_path");
                    dirToCreate.add("invoices_path");
                    dirToCreate.add("contracts_path");
                    dirToCreate.add("certificates_path");
                    dirToCreate.add("log_return_path");
                    dirToCreate.add("hl7_conformance_statements_path");
                    dirToCreate.add("dicom_conformance_statements_path");
                    dirToCreate.add("objects_path");
                    dirToCreate.add("file_steps_path");
                    for (String path : dirToCreate) {
                        path = ApplicationPreferenceManager.getStringValue(path);
                        if (path != null) {
                            String datapath = ApplicationPreferenceManager.getStringValue("data_path");
                            File file = new File(basepath.concat("/").concat(datapath).concat("/").concat(path));
                            if (file.mkdirs()) {
                                LOG.info("file directory created");
                            } else {
                                LOG.error("Failed to create file");
                            }
                        }
                    }
                } else {
                    FacesMessages.instance()
                            .addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tm.install.cannotCreateDirectory", basepath);
                }
            }
        }
        step++;
    }

    public String completeInstallation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("completeInstallation");
        }
        ApplicationPreferenceManager preferencesBean = (ApplicationPreferenceManager) Component
                .getInstance("applicationPreferenceManager");
        preferencesBean.setValue(PreferenceType.BOOLEAN, "installation_done", true);
        return "/home.seam";
    }

}
