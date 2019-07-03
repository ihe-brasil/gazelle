package net.ihe.gazelle.dao;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.objects.model.ObjectFileType;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestOption;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstanceStatus;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.users.model.ConnectathonParticipantStatus;
import net.ihe.gazelle.users.model.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Name("gazelleDAO")
@Scope(ScopeType.PAGE)
public class GazelleDAO implements Serializable {

    private static final long serialVersionUID = -9156687277019988712L;

    private static final Logger LOG = LoggerFactory.getLogger(GazelleDAO.class);

    public GazelleDAO() {
        super();
    }

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        //
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
        //
    }

    public User FindUserWithUsername(String userName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("FindUserWithUsername");
        }
        return User.FindUserWithUsername(userName);
    }

    public List<String> getAllRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllRoles");
        }
        List<String> roles = new RoleQuery().name().getListDistinct();
        return roles;
    }

    public List<Currency> getCurrencyList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrencyList");
        }
        return new CurrencyQuery().getList();
    }

    public Institution getLoggedInInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLoggedInInstitution");
        }
        return Institution.getLoggedInInstitution();
    }

    public List<Iso3166CountryCode> getPossibleCountries() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleCountries");
        }
        Iso3166CountryCodeQuery query = new Iso3166CountryCodeQuery();
        query.printableName().order(true);
        return query.getList();
    }

    @SuppressWarnings("unchecked")
    public List<Institution> getPossibleInstitutionsForRegistration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleInstitutionsForRegistration");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();

        Query query = entityManager
                .createQuery("from Institution row where row.keyword <> :nullKeyword order by lower(row.keyword) asc");
        query.setParameter("nullKeyword", "NULL");
        List<Institution> possibleInstitutionsForRegistration = query.getResultList();
        Collections.sort(possibleInstitutionsForRegistration);
        try {
            Query secondQuery = entityManager.createQuery("from Institution row where row.keyword = :nullKeyword");
            secondQuery.setParameter("nullKeyword", "NULL");
            List<Institution> institutions = secondQuery.getResultList();

            if ((institutions != null) && (institutions.size() > 0)) {
                possibleInstitutionsForRegistration.add(0, institutions.get(0));
            } else {

            }
        } catch (Exception e) {
            LOG.error("", e);
            return possibleInstitutionsForRegistration;
        }
        return possibleInstitutionsForRegistration;
    }

    public List<Iso3166CountryCode> getPossibleVATCountries() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleVATCountries");
        }
        Iso3166CountryCodeQuery query = new Iso3166CountryCodeQuery();
        query.ec().isNotNull();
        query.iso().order(true);
        return query.getList();
    }

    public List<Institution> listAllInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listAllInstitutions");
        }
        return Institution.listAllInstitutions();
    }

    public List<Institution> listAllInstitutionsOrderByKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listAllInstitutionsOrderByKeyword");
        }
        return Institution.listAllInstitutionsOrderByKeyword();
    }

    public List<InstitutionType> listAllInstitutionType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listAllInstitutionType");
        }
        return new InstitutionTypeQuery().getList();
    }

    public List<User> listUsersForCompany(Institution inst) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listUsersForCompany");
        }
        return User.listUsersForCompany(inst);
    }

    public List<Actor> listAllActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listAllActors");
        }
        return Actor.listAllActors();
    }

    public List<IntegrationProfile> getIntegrationProfileByActor(Actor inActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileByActor");
        }
        return ActorIntegrationProfile.getIntegrationProfileByActor(inActor);
    }

    public List<Actor> getActorByIntegrationProfile(IntegrationProfile inIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorByIntegrationProfile");
        }
        return ActorIntegrationProfile.getActorByIntegrationProfile(inIntegrationProfile);
    }

    public List<ActorIntegrationProfileOption> getIntegrationProfilesOptionsForActor(Actor inActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesOptionsForActor");
        }
        return ActorIntegrationProfileOption.getIntegrationProfilesOptionsForActor(inActor);
    }

    public List<ActorIntegrationProfileOption> getActorsOptionsForIntegrationProfile(
            IntegrationProfile inIntegrationProfile) {
        return ActorIntegrationProfileOption.getActorsOptionsForIntegrationProfile(inIntegrationProfile);
    }

    public List<Domain> getPossibleDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleDomains");
        }
        return Domain.getPossibleDomains();
    }

    public List<IntegrationProfile> getAllIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllIntegrationProfile");
        }
        return IntegrationProfile.getAllIntegrationProfile();
    }

    public List<IntegrationProfile> getAllIntegrationProfileForTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllIntegrationProfileForTestingSession");
        }
        return IntegrationProfile.getAllIntegrationProfileForTestingSession();
    }

    public List<IntegrationProfile> getPossibleIntegrationProfiles(Domain inDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfiles");
        }
        return IntegrationProfile.getPossibleIntegrationProfiles(inDomain);
    }

    public List<IntegrationProfileStatusType> listAllIntegrationProfileStatusType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listAllIntegrationProfileStatusType");
        }
        return IntegrationProfileStatusType.listAllIntegrationProfileStatusType();
    }

    public List<ProfileLink> getProfileLinksForActor(Actor inActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfileLinksForActor");
        }
        return ProfileLink.getProfileLinksForActor(inActor);
    }

    public List<Transaction> getTransactionForActor(Actor inActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionForActor");
        }
        return ProfileLink.getTransactionForActor(inActor);
    }

    public List<ProfileLink> getProfileLinksForIntegrationProfile(IntegrationProfile inIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfileLinksForIntegrationProfile");
        }
        return ProfileLink.getProfileLinksForIntegrationProfile(inIntegrationProfile);
    }

    public List<Transaction> getAllTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllTransactions");
        }
        return Transaction.getAllTransactions();
    }

    public List<TransactionLink> getTransactionLinkForActor(Actor inActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionLinkForActor");
        }
        return TransactionLink.getTransactionLinkForActor(inActor);
    }

    public List<TransactionStatusType> listAllTransactionStatusType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listAllTransactionStatusType");
        }
        return TransactionStatusType.listAllTransactionStatusType();
    }

    public List<ObjectFileType> getListOfAllObjectFileType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllObjectFileType");
        }
        return ObjectFileType.getListOfAllObjectFileType();
    }

    public List<ConfigurationType> GetAllConfigurationType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("GetAllConfigurationType");
        }
        return ConfigurationType.GetAllConfigurationType();
    }

    public List<OIDRootDefinition> getAllOIDRootDefinition() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllOIDRootDefinition");
        }
        return OIDRootDefinition.getAllOIDRootDefinition();
    }

    public List<SopClass> listSopClasses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listSopClasses");
        }
        return SopClass.listSopClasses();
    }

    public List<TransportLayer> listTransportLayer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTransportLayer");
        }
        return TransportLayer.listTransportLayer();
    }

    public List<WebServiceType> listAllWebServices() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listAllWebServices");
        }
        return WebServiceType.listAllWebServices();
    }

    public List<ConnectathonParticipantStatus> getAllStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllStatus");
        }
        return ConnectathonParticipantStatus.getAllStatus();
    }

    public List<Host> getListOfHostForASystemInSession(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfHostForASystemInSession");
        }
        return Host.getListOfHostForASystemInSession(inSystemInSession);
    }

    public List<Host> getListOfHostForASystemInSessionAsAdmin(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfHostForASystemInSessionAsAdmin");
        }
        return Host.getListOfHostForASystemInSessionAsAdmin(inSystemInSession);
    }

    public List<OIDRequirement> getAllOIDRequirement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllOIDRequirement");
        }
        return OIDRequirement.getAllOIDRequirement();
    }

    public List<SystemInSessionStatus> getListSystemInSessionStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListSystemInSessionStatus");
        }
        return SystemInSessionStatus.getListStatus();
    }

    public List<SystemType> getPossibleSystemTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleSystemTypes");
        }
        return SystemType.getPossibleSystemTypes();
    }

    public List<TestOption> listTestOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTestOptions");
        }
        return TestOption.listTestOptions();
    }

    public List<TestRoles> getTestRolesListForATest(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRolesListForATest");
        }
        return TestRoles.getTestRolesListForATest(test);
    }

    public List<TestType> getTestTypeList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestTypeList");
        }
        return TestType.getTypeList();
    }

    public List<System> getSystemsForAnInstitution(Institution inst) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsForAnInstitution");
        }
        return System.getSystemsForAnInstitution(inst);
    }

    public List<Institution> getPossibleInstitutionsInActivatedSessionForSystemCreationByAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleInstitutionsInActivatedSessionForSystemCreationByAdmin");
        }
        return System.getPossibleInstitutionsInActivatedSessionForSystemCreationByAdmin();
    }

    public List<Institution> getInstitutionsForASystem(System inSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionsForASystem");
        }
        return System.getInstitutionsForASystem(inSystem);
    }

    public Integer getNumberOfSystemsForAnInstitutionForCurrentSession(Institution institutionUsed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfSystemsForAnInstitutionForCurrentSession");
        }
        SystemInSessionQuery query = new SystemInSessionQuery();

        query.testingSession().eqIfValueNotNull(TestingSession.getSelectedTestingSession());
        query.system().institutionSystems().institution().eqIfValueNotNull(institutionUsed);
        return query.getCount();
    }

    public List<SystemInSession> getSystemsInSessionForLoggedCompanyForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsInSessionForLoggedCompanyForSession");
        }
        return SystemInSession.getSystemsInSessionForLoggedCompanyForSession();
    }

    public List<SystemInSession> getNotDrppedSystemsInSessionForLoggedCompanyForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNotDrppedSystemsInSessionForLoggedCompanyForSession");
        }
        return SystemInSession.getNotDroppedSystemsInSessionForLoggedCompanyForSession();
    }

    public List<TestingSession> getPossibleTestingSessions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestingSessions");
        }
        return TestingSession.getPossibleTestingSessions();
    }

    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        return TestingSession.getSelectedTestingSession();
    }

    public List<Institution> getListOfInstitutionsParticipatingInSession(TestingSession ts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfInstitutionsParticipatingInSession");
        }
        return TestingSession.getListOfInstitutionsParticipatingInSession(ts);
    }

    public List<Institution> getListOfInstitutionsRegisterInSession(TestingSession ts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfInstitutionsRegisterInSession");
        }
        List<Institution> res = TestingSession.getListOfInstitutionsRegisterInSession(ts);
        if (res != null) {
            Collections.sort(res, new Comparator<Institution>() {
                @Override
                public int compare(Institution inst1, Institution inst2) {
                    if (inst1.getKeyword() == null) {
                        return -1;
                    }
                    if (inst2.getKeyword() == null) {
                        return 1;
                    }
                    return inst1.getKeyword().compareToIgnoreCase(inst2.getKeyword());
                }
            });
        }

        return res;
    }

    public List<TestingSession> getAllActiveSessions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllActiveSessions");
        }
        return TestingSession.GetAllActiveSessions();
    }

    public List<TestingSession> GetAllActiveSessionsWithoutHidden() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllActiveSessions");
        }
        return TestingSession.GetAllActiveSessionsWithoutHidden();
    }

    public TestStepsInstanceStatus getSTATUS_FAILED() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSTATUS_FAILED");
        }
        return TestStepsInstanceStatus.getStatusByKeyword(TestStepsInstanceStatus.STATUS_FAILED_STRING);
    }

    public TestStepsInstanceStatus getSTATUS_DONE() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSTATUS_DONE");
        }
        return TestStepsInstanceStatus.getStatusByKeyword(TestStepsInstanceStatus.STATUS_DONE_STRING);
    }

    public TestStepsInstanceStatus getSTATUS_ACTIVATED() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSTATUS_ACTIVATED");
        }
        return TestStepsInstanceStatus.getStatusByKeyword(TestStepsInstanceStatus.STATUS_ACTIVATED_STRING);
    }

    public TestStepsInstanceStatus getSTATUS_ACTIVATED_SIMU() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSTATUS_ACTIVATED_SIMU");
        }
        return TestStepsInstanceStatus.getStatusByKeyword(TestStepsInstanceStatus.STATUS_ACTIVATED_SIMU_STRING);
    }

    public TestStepsInstanceStatus getSTATUS_DISACTIVATED() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSTATUS_DISACTIVATED");
        }
        return TestStepsInstanceStatus.getStatusByKeyword(TestStepsInstanceStatus.STATUS_DISACTIVATED_STRING);
    }

    public TestStepsInstanceStatus getSTATUS_SKIPPED() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSTATUS_SKIPPED");
        }
        return TestStepsInstanceStatus.getStatusByKeyword(TestStepsInstanceStatus.STATUS_SKIPPED_STRING);
    }

    public TestStepsInstanceStatus getSTATUS_VERIFIED() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSTATUS_VERIFIED");
        }
        return TestStepsInstanceStatus.getStatusByKeyword(TestStepsInstanceStatus.STATUS_VERIFIED_STRING);
    }

    public Integer getTestInstanceListSizeForSystemInSessionByStatus(SystemInSession inSystemInSession, Status inStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceListSizeForSystemInSessionByStatus");
        }
        return TestInstanceParticipants.getTestInstanceListSizeForSystemInSessionByStatus(inSystemInSession, inStatus);
    }

    public List<TestInstanceParticipants> getTestInstanceParticipantsListForATest(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceParticipantsListForATest");
        }
        return TestInstanceParticipants.getTestInstanceParticipantsListForATest(testInstance);
    }

    public List<TestInstance> getTestInstanceListToBeVerifiedByTest(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceListToBeVerifiedByTest");
        }
        return TestInstance.getTestInstanceListToBeVerifiedByTest(inTest);
    }

}
