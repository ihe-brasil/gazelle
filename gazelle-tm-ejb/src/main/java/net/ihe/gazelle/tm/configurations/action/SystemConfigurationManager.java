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

package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.dao.GazelleDAO;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.WSTransactionUsage;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.configurations.model.DICOM.AbstractDicomConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.*;
import net.ihe.gazelle.tm.configurations.model.OIDSConfigurationForSessionForHL7.OidsTypeForHL7;
import net.ihe.gazelle.tm.systems.action.SystemManager;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.users.UserService;
import net.ihe.gazelle.users.action.InstitutionManagerLocal;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.util.Pair;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Jean-Baptiste Meyer / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, sep
 * @class ConfigurationManager.java
 * @package net.ihe.gazelle.tm.systems.model
 * @see > jmeyer@irisa.fr - http://www.ihe-europe.org
 */
@Stateful
@Name("systemConfigurationManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("SystemConfigurationManagerLocal")
public class SystemConfigurationManager implements SystemConfigurationManagerLocal,
        Comparator<ActorIntegrationProfileOption>, Serializable {

    private static final String SELECTED_SYSTEM_IN_SESSION = "selectedSystemInSession";

    private static final String CHOOSEN_INSTITUTION_FOR_ADMIN = "choosenInstitutionForAdmin";

    private static final String CONFIGURATION_CONFIGURATIONS_XHTML = "/configuration/configurations.xhtml";

    private static final long serialVersionUID = 109870709841098014L;

    private static final Logger LOG = LoggerFactory.getLogger(SystemConfigurationManager.class);

    private List<Class<?>> listOfClassWithOIDS = null;
    private AbstractConfiguration currentConfiguration;

    private HashMap<ActorIntegrationProfileOption, List<AbstractConfiguration>> mapOfActorIntegrationProfileOption;

    /**
     * SelectedSystemInSession object managed my this manager bean and injected in JSF
     */

    private SystemInSession selectedSystemInSession;
    private Institution selectedInstitution;

    private SystemInSession previousSystemInSession;

    /**
     * ActivatedTestingSession object managed my this manager bean
     */
    private TestingSession activatedTestingSession;

    private Actor selectedActor;

    private SopClass selectedSopClass;

    private String selectedTable;

    private OIDSConfigurationForSessionForHL7 oidsConfigurationForSessionForHL7;

    private List<ConfigurationType> possibleConfigurationTypes;
    private List<Actor> possibleActors;
    private StringBuilder sb;

    private boolean displayHelpOnAssigningAuthority;

    public SystemConfigurationManager() {
    }

    public StringBuilder getSb() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSb");
        }
        return sb;
    }

    public void setSb(StringBuilder sb) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSb");
        }
        this.sb = sb;
    }

    @Override
    public String getLinkForTLSValidation(AbstractConfiguration configuration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkForTLSValidation");
        }
        String result = "#";
        if (configuration != null) {
            String tlsTestUrl = ApplicationPreferenceManager.getStringValue("tls_test_url");
            if (tlsTestUrl != null) {
                Configuration realConfiguration = configuration.getConfiguration();
                if (realConfiguration != null) {
                    Host host = realConfiguration.getHost();
                    if (host != null) {
                        String ip = host.getIp();
                        if (ip == null) {
                            ip = host.getHostname();
                        }
                        Integer port = null;
                        String type = "";
                        String additionalParameters = "";

                        if (configuration instanceof DicomSCPConfiguration) {
                            DicomSCPConfiguration tmConfig = (DicomSCPConfiguration) configuration;
                            port = tmConfig.getPortSecured();
                            type = "DICOM";
                            additionalParameters = "&aet=" + tmConfig.getAeTitle();
                        }
                        if (configuration instanceof HL7V2ResponderConfiguration) {
                            HL7V2ResponderConfiguration tmConfig = (HL7V2ResponderConfiguration) configuration;
                            port = tmConfig.getPortSecured();
                            type = "HL7";

                            additionalParameters = "&assigning_authority=" + tmConfig.getAssigningAuthority()
                                    + "&sending_receiving_application=" + tmConfig.getSendingReceivingApplication()
                                    + "&sending_receiving_facility=" + tmConfig.getSendingReceivingFacility();
                        }
                        if (configuration instanceof HL7V3ResponderConfiguration) {
                            HL7V3ResponderConfiguration tmConfig = (HL7V3ResponderConfiguration) configuration;
                            port = tmConfig.getPortSecured();
                            type = "HL7V3";
                            additionalParameters = "&assigning_authority=" + tmConfig.getAssigningAuthority() + "&url="
                                    + tmConfig.getUrl();
                        }
                        if (configuration instanceof WebServiceConfiguration) {
                            WebServiceConfiguration tmConfig = (WebServiceConfiguration) configuration;
                            port = tmConfig.getPortSecured();
                            type = "WS";
                            additionalParameters = "&url=" + tmConfig.getUrl();
                        }
                        if (configuration instanceof SyslogConfiguration) {
                            SyslogConfiguration tmConfig = (SyslogConfiguration) configuration;
                            if (tmConfig.getTransportLayer().equals(TransportLayer.getTCP_Protocol())) {
                                port = tmConfig.getPortSecured();
                                type = "SYSLOG";
                            }
                        }
                        if (configuration instanceof RawConfiguration) {
                            RawConfiguration tmConfig = (RawConfiguration) configuration;
                            port = tmConfig.getPortSecured();
                            type = "RAW";
                        }

                        if (port != null) {
                            result = tlsTestUrl + "ip=" + ip + "&port=" + port + "&type=" + type + additionalParameters;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public SopClass getSelectedSopClass() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSopClass");
        }
        return selectedSopClass;
    }

    @Override
    public void setSelectedSopClass(SopClass selectedSopClass) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSopClass");
        }
        this.selectedSopClass = selectedSopClass;
    }

    @Override
    public HashMap<ActorIntegrationProfileOption, List<AbstractConfiguration>> getMapOfActorIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMapOfActorIntegrationProfile");
        }
        return mapOfActorIntegrationProfileOption;
    }

    @Override
    public void setMapOfActorIntegrationProfile(
            HashMap<ActorIntegrationProfileOption, List<AbstractConfiguration>> mapOfActorIntegrationProfile) {
        this.mapOfActorIntegrationProfileOption = mapOfActorIntegrationProfile;
    }

    /**
     * Method used to instantiate the field assigning authority if it is null
     */
    @Override
    public void initHL7Object(AbstractHL7Configuration config) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initHL7Object");
        }
        if ((config.getAssigningAuthority() == null) || (config.getAssigningAuthority().trim().length() == 0)) {
            setDisplayHelpOnAssigningAuthority(true);
        } else {
            setDisplayHelpOnAssigningAuthority(false);
        }

    }

    @Override
    public String validateAssigningAuthority(String aa) {
        String message = "";
        if (aa == null || aa.trim().isEmpty()) {
            message = "Assigning-Authority is empty. Suggested value : " + AbstractConfiguration.getAssigningAuthority(selectedSystemInSession
                    .getSystem());
        }
        return message;
    }

    @Override
    public String getSelectedTable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTable");
        }
        return selectedTable;
    }

    @Override
    public void setSelectedTable(String selectedTable) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTable");
        }
        this.selectedTable = selectedTable;
    }

    @Override
    public void findConfigurationsForCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findConfigurationsForCompany");
        }
    }

    @Override
    public boolean isDisplayHelpOnAssigningAuthority() {
        return displayHelpOnAssigningAuthority;
    }

    @Override
    public void setDisplayHelpOnAssigningAuthority(boolean displayHelpOnAssigningAuthority) {
        this.displayHelpOnAssigningAuthority = displayHelpOnAssigningAuthority;
    }


    private void generateConfigurationsForAdmin(SystemInSession sIs, Institution is) throws Exception {
        if ((sIs == null) || (is == null)) {
            return;
        }
        Host host = null;
        if (Role.isLoggedUserAdmin()) {
            List<Host> listOfHost = Host.getListOfHostForASystemInSession(sIs);
            if (listOfHost.size() == 0) {
                host = Host.generateNewHostForSessionWithPersistance(is, sIs);
            } else {
                host = listOfHost.get(0);
            }
        }
        GenerateSystemConfiguration gsc = new GenerateSystemConfiguration();
        gsc.generateSystemConfigurationsOnly(host, sIs, TestingSession.getSelectedTestingSession());
    }

    @Override
    public void generateConfigurations(SystemInSession sIs) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateConfigurations");
        }
        Host host = null;

        if (sIs != null) {

            if (!Role.isLoggedUserAdmin()) {
                List<Host> listOfHost = Host.getListOfHostForASystemInSession(sIs);
                if (listOfHost.size() == 0) {
                    Institution loginUserInstitution = User.loggedInUser().getInstitution();
                    host = Host.generateNewHostForSessionWithPersistance(loginUserInstitution, sIs);

                } else {
                    host = listOfHost.get(0);

                }
                GenerateSystemConfiguration gsc = new GenerateSystemConfiguration();
                gsc.generateSystemConfigurationsOnly(host, sIs, TestingSession.getSelectedTestingSession());
            } else {
                Institution choosenInstitutionForAdmin = (Institution) Component
                        .getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);
                generateConfigurationsForAdmin(sIs, choosenInstitutionForAdmin);
            }

        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public void listConfigurationsByActorIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listConfigurationsByActorIntegrationProfile");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        Query q = entityManager.createQuery("Select aipo from ActorIntegrationProfileOption aipo");
        List<ActorIntegrationProfileOption> listOfActorIntegrationProfileOption = q.getResultList();

        mapOfActorIntegrationProfileOption = new HashMap<ActorIntegrationProfileOption, List<AbstractConfiguration>>();

        for (ActorIntegrationProfileOption aipo : listOfActorIntegrationProfileOption) {

            List<AbstractConfiguration> list = new ArrayList<AbstractConfiguration>();
            mapOfActorIntegrationProfileOption.put(aipo, list);

        }

    }

    @Override
    public String getListOfConfigs(List<AbstractConfiguration> inListOfConf) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfConfigs");
        }
        StringBuffer toReturn = new StringBuffer();
        if (inListOfConf == null) {
            return "";
        }
        for (AbstractConfiguration conf : inListOfConf) {
            toReturn.append(conf.getConfiguration().getConfigurationType().getTypeName());
            if (conf instanceof AbstractDicomConfiguration) {
                toReturn.append("(" + ((AbstractDicomConfiguration) conf).getSopClass() + ")");
            }
            toReturn.append("\n");
        }

        return toReturn.toString();
    }

    @Override
    public List<ActorIntegrationProfileOption> getOrderedActorIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOrderedActorIntegrationProfileOption");
        }
        List<ActorIntegrationProfileOption> listToReturn = new ArrayList<ActorIntegrationProfileOption>(
                mapOfActorIntegrationProfileOption.keySet());
        Collections.sort(listToReturn, this);

        return listToReturn;
    }

    private AbstractConfiguration copyAbstractConfiguration(AbstractConfiguration ac) {
        if (ac != null) {
            if (ac instanceof DicomSCUConfiguration) {
                return new DicomSCUConfiguration((DicomSCUConfiguration) ac);
            }
            if (ac instanceof DicomSCPConfiguration) {
                return new DicomSCPConfiguration((DicomSCPConfiguration) ac);
            }
            if (ac instanceof HL7V2InitiatorConfiguration) {
                return new HL7V2InitiatorConfiguration((HL7V2InitiatorConfiguration) ac);
            }
            if (ac instanceof HL7V3InitiatorConfiguration) {
                return new HL7V3InitiatorConfiguration((HL7V3InitiatorConfiguration) ac);
            }
            if (ac instanceof HL7V2ResponderConfiguration) {
                return new HL7V2ResponderConfiguration((HL7V2ResponderConfiguration) ac);
            }
            if (ac instanceof HL7V3ResponderConfiguration) {
                return new HL7V3ResponderConfiguration((HL7V3ResponderConfiguration) ac);
            }
            if (ac instanceof WebServiceConfiguration) {
                return new WebServiceConfiguration((WebServiceConfiguration) ac);
            }
            if (ac instanceof SyslogConfiguration) {
                return new SyslogConfiguration((SyslogConfiguration) ac);
            }
            if (ac instanceof RawConfiguration) {
                return new RawConfiguration((RawConfiguration) ac);
            }
        }
        return null;
    }

    @Override
    public List<Class<?>> getListOfClassWithOIDS() {
        if (listOfClassWithOIDS == null) {
            listOfClassWithOIDS = AbstractConfiguration.GetListOfClassWithOIDS();
        }
        return listOfClassWithOIDS;
    }

    private void generateOIDIfNeeded(AbstractConfiguration inConfiguration) {
        String oid = OidsAndIPParamForSessionManager.getExistingOIDForSystemAndConfigurationType(inConfiguration,
                inConfiguration.getConfiguration().getSystemInSession());

        if (oid != null) {
            try {
                if (getListOfClassWithOIDS().contains(
                        Class.forName(inConfiguration.getConfiguration().getConfigurationType().getClassName()))) {
                    if (inConfiguration instanceof AbstractHL7Configuration) {
                        ((AbstractHL7Configuration) inConfiguration).setAssigningAuthorityOID(oid);

                    } else if (inConfiguration instanceof WebServiceConfiguration) {
                        WebServiceConfiguration ws = ((WebServiceConfiguration) inConfiguration);

                        ws.setAssigningAuthorityOID(oid);

                    }
                }
            } catch (Exception e) {
            }

        } else {
            Map<Pair<Class<?>, IntegrationProfile>, String> possibleOIDForSystem = getPossibleOIDsForThisSystem();
            if (possibleOIDForSystem != null) {
                if (inConfiguration instanceof AbstractHL7Configuration) {
                    ((AbstractHL7Configuration) inConfiguration).setAssigningAuthorityOID(possibleOIDForSystem
                            .get(new Pair<Class<?>, IntegrationProfile>(inConfiguration.getClass(), null)));

                } else if (inConfiguration instanceof WebServiceConfiguration) {

                }

            } else {

            }
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemConfigurationManager', 'generateConfigurationsForCompany', null)}")
    public synchronized void generateConfigurationsForCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void generateConfigurationsForCompany");
        }
        Institution choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);
        activatedTestingSession = TestingSession.getSelectedTestingSession();
        EntityManager em = EntityManagerService.provideEntityManager();

        if ((choosenInstitutionForAdmin != null) && (choosenInstitutionForAdmin.getName() != null)) {

            List<SystemInSession> listOfSystemInSession = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                    choosenInstitutionForAdmin, activatedTestingSession);

            for (SystemInSession sIs : listOfSystemInSession) {
                try {
                    generateConfigurations(sIs);
                } catch (Exception e) {
                    net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
                }
            }

        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemConfigurationManager', 'generateConfigurationsForCompany', null)}")
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    public void generateConfigurationsForCompany(Institution institution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateConfigurationsForCompany");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        activatedTestingSession = TestingSession.getSelectedTestingSession();

        List<SystemInSession> listOfSystemInSession = SystemInSession.getSystemInSessionFiltered(em, null,
                activatedTestingSession, institution, null, null, null, null, null, null, null, null, null);

        for (SystemInSession sIs : listOfSystemInSession) {

            try {

                generateConfigurationsForAdmin(sIs, institution);

            } catch (Exception e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());

            }

        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemConfigurationManager', 'generateConfigurationsForSystem', null)}")
    public void generateConfigurationsForSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateConfigurationsForSystem");
        }
        try {
            if (Role.isLoggedUserAdmin() || Role.isLoggedUserMonitor()) {
                Institution choosenInstitutionForAdmin = (Institution) Component
                        .getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);
                generateConfigurationsForAdmin(selectedSystemInSession, choosenInstitutionForAdmin);
            } else {
                generateConfigurations(selectedSystemInSession);
            }
        } catch (Exception e) {
            net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }

    }

    @Override
    public void generateAllConfigurationsForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateAllConfigurationsForSession");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        List<Institution> listOfInstitution = TestingSession
                .getListOfInstitutionsParticipatingInSession(activatedTestingSession);

        for (Institution currentInstitution : listOfInstitution) {

            generateConfigurationsForCompany(currentInstitution);
        }

    }

    @Override
    public boolean isAValidCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAValidCompany");
        }
        Institution choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);
        if ((choosenInstitutionForAdmin == null) || (choosenInstitutionForAdmin.getName() == null)) {
            return false;
        } else {
            Institution tmp = Institution.findInstitutionWithName(choosenInstitutionForAdmin.getName());
            return tmp != null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemConfigurationManager', 'deleteConfiguration', null)}")
    public void deleteConfiguration(AbstractConfiguration inConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteConfiguration");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if ((inConfiguration == null) || (inConfiguration.getId() == null)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem, selected configuration is null ");
            return;
        }
        currentConfiguration = entityManager.find(inConfiguration.getClass(), inConfiguration.getId());
        entityManager.remove(currentConfiguration);
        entityManager.flush();

        Configuration c = entityManager.find(Configuration.class, inConfiguration.getConfiguration().getId());

        if (c != null) {
            entityManager.remove(c);
            entityManager.flush();
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemConfigurationManager', 'deleteConfiguration', null)}")
    public void deleteConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteConfiguration");
        }
        deleteConfiguration(currentConfiguration);

    }

    @Override
    public String addConfigButtonClick() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addConfigButtonClick");
        }
        if (selectedSystemInSession == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Please select a system.");
            return null;
        }

        // We choose an arbitrary configuration, will be changed later when the user will change the configuration type
        currentConfiguration = new HL7V2InitiatorConfiguration();
        currentConfiguration.getConfiguration().setConfigurationType(null);

        previousSystemInSession = selectedSystemInSession;

        return "/configuration/create/addConfiguration.xhtml";

    }

    @Override
    public List<SystemInSession> getPossibleSystems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleSystems");
        }
        SystemInSessionQuery systemInSessionQuery = new SystemInSessionQuery();
        systemInSessionQuery.testingSession().eq(TestingSession.getSelectedTestingSession());
        if (!Authorizations.ADMIN.isGranted()) {
            Institution institution = User.loggedInUser().getInstitution();
            systemInSessionQuery.system().institutionSystems().institution().eq(institution);
        }
        if (selectedInstitution != null) {
            systemInSessionQuery.system().institutionSystems().institution().eq(selectedInstitution);
        }
        systemInSessionQuery.system().keyword().order(true);
        systemInSessionQuery.addRestriction(HQLRestrictions.or(systemInSessionQuery.system().systemsInSession()
                .registrationStatus().neqRestriction(SystemInSessionRegistrationStatus.DROPPED), systemInSessionQuery
                .system().systemsInSession().registrationStatus().isNullRestriction()));
        return systemInSessionQuery.getList();
    }


    @Override
    public SystemInSession getPreviousSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPreviousSystemInSession");
        }
        return previousSystemInSession;
    }

    @Override
    public void setPreviousSystemInSession(SystemInSession previousSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPreviousSystemInSession");
        }
        this.previousSystemInSession = previousSystemInSession;
    }

    @Override
    public String editConfigButtonClick(AbstractConfiguration inConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editConfigButtonClick");
        }
        if ((selectedSystemInSession == null) && (inConfiguration.getConfiguration().getId() == null)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "System is null - Please select one ");
            return null;
        }
        currentConfiguration = inConfiguration;

        previousSystemInSession = selectedSystemInSession;
        selectedSystemInSession = currentConfiguration.getConfiguration().getSystemInSession();

        return "/configuration/create/addConfiguration.xhtml";
    }

    @Override
    @Restrict("#{s:hasPermission('SystemConfigurationManager', 'addEditConfiguration', null)}")
    public String addEditConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addEditConfiguration");
        }
        if ((currentConfiguration == null) || (currentConfiguration.getConfiguration() == null)) {
            return null;
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();

        if (currentConfiguration.getConfiguration().getId() == null) {
            currentConfiguration.getConfiguration().setSystemInSession(selectedSystemInSession);

            currentConfiguration.getProxyPortIfNeeded(null);
            Configuration conf = entityManager.merge(currentConfiguration.getConfiguration());
            currentConfiguration.setConfiguration(conf);

            currentConfiguration.setId(conf.getId());

            if (currentConfiguration instanceof WebServiceConfiguration) {

            }

            entityManager.merge(currentConfiguration.getConfiguration().getHost());
            currentConfiguration = entityManager.merge(currentConfiguration);
        } else {
            Configuration configurationInDatabase = entityManager.find(Configuration.class, currentConfiguration
                    .getConfiguration().getId());
            // If the configuration type changed we persist the new Configuration
            // and remove the previous one
            if (!configurationInDatabase.getConfigurationType().getClassName()
                    .equals(currentConfiguration.getConfiguration().getConfigurationType().getClassName())) {


                AbstractConfigurationQuery q = new AbstractConfigurationQuery();
                q.configuration().configurationType().eq(configurationInDatabase.getConfigurationType());
                q.configuration().id().eq(configurationInDatabase.getId());
                AbstractConfiguration oldConfiguration = q.getUniqueResult();
                if (oldConfiguration != null) {
                    entityManager.remove(oldConfiguration);
                    entityManager.flush();

                    currentConfiguration.getConfiguration().setSystemInSession(selectedSystemInSession);
                    currentConfiguration.getProxyPortIfNeeded(null);
                    Configuration conf = currentConfiguration.getConfiguration();

                    currentConfiguration.setId(conf.getId());
                    entityManager.merge(currentConfiguration.getConfiguration().getHost());
                    entityManager.merge(currentConfiguration);

                } else {
                    LOG.error("Unable to find this configuration in Database");
                }
            } else
            // same type of configuration
            {

                currentConfiguration.getProxyPortIfNeeded(null);
                entityManager.merge(currentConfiguration.getConfiguration().getHost());
                entityManager.merge(currentConfiguration.getConfiguration());

                if (currentConfiguration instanceof WebServiceConfiguration) {

                }
                currentConfiguration = entityManager.merge(currentConfiguration);
            }

        }
        selectedSystemInSession = previousSystemInSession;
        // reset selected sop class
        selectedSopClass = null;
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Configuration updated");

        if (Role.isLoggedUserAdmin()) {
            return CONFIGURATION_CONFIGURATIONS_XHTML + getSb();
        } else {
            return "/configuration/list/configurationMainWindow.xhtml" + getSb();
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemConfigurationManager', 'addEditConfiguration', null)}")
    public void addEditConfiguration(AbstractConfiguration currentConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addEditConfiguration");
        }
        setCurrentConfiguration(currentConfiguration);
        addEditConfiguration();
        if (currentConfiguration.getConfiguration().getHost().getSystemsInSession().size() > 1) {
            currentConfiguration.getConfiguration().getHost().setUsedByMoreOneSystemInSession(true);
        } else {
            currentConfiguration.getConfiguration().getHost().setUsedByMoreOneSystemInSession(false);
        }
    }

    @Override
    public String displaySystems(Host host) {
        StringBuilder sb = new StringBuilder();
        List<SystemInSession> sisList = host.getSystemsInSession();
        for (SystemInSession systemInSession : sisList) {
            sb.append(systemInSession.getSystem().getKeyword()).append(" ");
            sb.append("and").append(" ");
        }
        sb.delete(sb.length() - 4, sb.length());
        return sb.toString();
    }

    @Override
    public void getOIDForConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOIDForConfiguration");
        }
        generateOIDIfNeeded(currentConfiguration);
    }

    @Restrict("#{s:hasPermission('SystemConfigurationManager', 'deleteAllConfigurationsForSystem', null)}")
    private void deleteAllConfigurationsForSystem(SystemInSession inSys) {

        if (inSys != null) {
            Configuration.deleteConfigurationsForSystemInSession(inSys);

        }
    }

    @Override
    public String deleteAllConfigurationsForSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllConfigurationsForSystem");
        }
        deleteAllConfigurationsForSystem(selectedSystemInSession);
        return CONFIGURATION_CONFIGURATIONS_XHTML;
    }

    @Override
    public void deleteAllConfigurationsForCompany(Institution institution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllConfigurationsForCompany");
        }
        if (institution == null) {
            return;
        }
        List<SystemInSession> listOfSystemInSession = SystemInSession.getSystemsInSessionForCompany(institution
                .getName());
        for (SystemInSession sIs : listOfSystemInSession) {
            deleteAllConfigurationsForSystem(sIs);
        }
    }

    @Override
    public String deleteAllConfigurationsForCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllConfigurationsForCompany");
        }
        Institution choosenInstitutionForAdmin = (Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN);
        if (choosenInstitutionForAdmin == null) {
            LOG.error("choosenInstitutionForAdmin == null !!! leaving deleteAllConfigurationsForCompany() methods");
            return null;
        }
        deleteAllConfigurationsForCompany(choosenInstitutionForAdmin);
        return CONFIGURATION_CONFIGURATIONS_XHTML;
    }

    @Override
    public void deleteAllConfigurationsForSession(TestingSession ts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllConfigurationsForSession");
        }
        if (ts != null) {
            List<Institution> listOfInstitution = TestingSession.getListOfInstitutionsParticipatingInSession(ts);

            for (Institution is : listOfInstitution) {
                deleteAllConfigurationsForCompany(is);
            }
        }

    }

    @Override
    public String deleteAllConfigurationsForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllConfigurationsForSession");
        }
        activatedTestingSession = TestingSession.getSelectedTestingSession();
        deleteAllConfigurationsForSession(activatedTestingSession);
        return CONFIGURATION_CONFIGURATIONS_XHTML;
    }

    @Override
    public String displayFinalURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayFinalURL");
        }
        String res = this.processURL(this.currentConfiguration);
        return res;
    }

    @Override
    public String processURL(AbstractConfiguration wsConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("processURL");
        }
        if (wsConfiguration == null) {
            return null;
        }
        return wsConfiguration.getFinalURL();
    }

    @Override
    public void approveAndRefresh(AbstractConfiguration currentConf, String inTable) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("approveAndRefresh");
        }

        selectedTable = inTable;
        currentConf.getConfiguration().setIsApproved(true);
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.merge(currentConf.getConfiguration());

    }

    @Override
    public boolean isRendered(String formName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRendered");
        }
        if (currentConfiguration.getConfiguration().getConfigurationType() != null) {
            return currentConfiguration.getConfiguration().getConfigurationType().getTypeName().equals(formName);
        } else {
            if (formName.trim().equals("")) {
                return true;
            } else {
                return false;
            }
        }

    }

    @Override
    public String cancelAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelAction");
        }
        selectedSystemInSession = previousSystemInSession;
        // reset selected sop class
        selectedSopClass = null;

        if (Role.isLoggedUserAdmin()) {
            return CONFIGURATION_CONFIGURATIONS_XHTML + getSb();
        } else {
            return "/configuration/list/configurationMainWindow.xhtml" + getSb();
        }

    }

    public void getParamsUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParamsUrl");
        }
        setSelectedInstitution(null);
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String testSession = params.get("testSession");
        String institution = params.get("institution");
        String actor = params.get("actor");
        String wsTransactionUsage = params.get("wsTransactionUsage");
        String system = params.get("system");
        String type = params.get("class");

        sb = new StringBuilder();

        if (testSession != null && !testSession.isEmpty()) {
            sb.append("?testSession=").append(testSession);
        }
        if (institution != null && !institution.isEmpty()) {
            sb.append("&institution=").append(institution);
        }
        if (actor != null && !actor.isEmpty()) {
            sb.append("&actor=").append(actor);
        }
        if (wsTransactionUsage != null && !wsTransactionUsage.isEmpty()) {
            sb.append("&wsTransactionUsage=").append(wsTransactionUsage);
        }
        if (system != null && !system.isEmpty()) {
            sb.append("&system=").append(system);
        }
        if (type != null && !type.isEmpty()) {
            sb.append("&class=").append(type);
        }
    }

    @Override
    public void changeTypeOfConfig() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeTypeOfConfig");
        }
        if (currentConfiguration.getConfiguration().getConfigurationType() != null) {

            try {
                Class<?> c = Class.forName(currentConfiguration.getConfiguration().getConfigurationType()
                        .getClassName());

                currentConfiguration.getConfiguration().setConfigurationType(
                        currentConfiguration.getConfiguration().getConfigurationType());

                Constructor<?> constructor = c.getConstructor(currentConfiguration.getConfiguration().getClass());

                currentConfiguration = (AbstractConfiguration) constructor.newInstance(currentConfiguration
                        .getConfiguration());

                if (currentConfiguration.getClass() == DicomSCUConfiguration.class) {
                    ((DicomSCUConfiguration) currentConfiguration).setTransferRole("SCU");
                } else if (currentConfiguration.getClass() == DicomSCPConfiguration.class) {
                    ((DicomSCPConfiguration) currentConfiguration).setTransferRole("SCP");
                }

                // reset selected sop class
                selectedSopClass = null;

            } catch (InstantiationException e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "error");
            } catch (IllegalAccessException e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
            } catch (ClassNotFoundException e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
            } catch (SecurityException e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
            } catch (NoSuchMethodException e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
            } catch (IllegalArgumentException e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
            } catch (InvocationTargetException e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
            }
        }
    }

    @Override
    public String configClickFromMenu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("configClickFromMenu");
        }

        selectedActor = null;
        selectedSystemInSession = null;

        return "/configuration/list/configurationMainWindow.xhtml";
    }

    public Institution getSelectedInstitution() {
        return selectedInstitution;
    }

    public void setSelectedInstitution(Institution selectedInstitution) {
        this.selectedInstitution = selectedInstitution;
    }

    @Override
    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        if (selectedSystemInSession == null) {
            retrieveSelectedSystemVariable();
        }
        if (selectedSystemInSession != null && !selectedSystemInSession.isSystemInSessionRelatedToUser(selectedSystemInSession)) {
            selectedSystemInSession = null;
        }
        return selectedSystemInSession;
    }

    public String getInstitutionKeywordForASystem() {
        return System.getInstitutionsForASystem(selectedSystemInSession.getSystem()).get(0).getKeyword();
    }

    @Override
    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
        Contexts.getSessionContext().set(SELECTED_SYSTEM_IN_SESSION, selectedSystemInSession);
    }

    @Override
    public String prepareBean() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("prepareBean");
        }
        if (activatedTestingSession == null) {
            activatedTestingSession = TestingSession.getSelectedTestingSession();
        }
        selectedSystemInSession = null;
        return "";
    }

    @Override
    @Deprecated
    public String manageAllConfigurationsActions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("manageAllConfigurationsActions");
        }

        if (activatedTestingSession == null) {
            activatedTestingSession = TestingSession.getSelectedTestingSession();
        }

        selectedSystemInSession = null;

        return CONFIGURATION_CONFIGURATIONS_XHTML;
    }

    @Override
    public void setSystemInSessionForConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemInSessionForConfiguration");
        }
        if (currentConfiguration != null) {
            currentConfiguration.getConfiguration().setSystemInSession(selectedSystemInSession);
        }
    }

    @Override
    public AbstractConfiguration getCurrentConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentConfiguration");
        }
        return currentConfiguration;
    }

    @Override
    public void setCurrentConfiguration(AbstractConfiguration currentConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCurrentConfiguration");
        }
        this.currentConfiguration = currentConfiguration;
    }

    @Override
    public void reinitSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reinitSystem");
        }
        setSelectedSystemInSession(null);
    }

    @Override
    public void reinitInstitutionAndSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reinitInstitutionAndSystem");
        }
        InstitutionManagerLocal im = ((InstitutionManagerLocal) Component.getInstance("institutionManager"));
        im.setChoosenInstitutionForAdmin(null);

        reinitSystem();
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
    public void setSopClassToCurrentConf() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSopClassToCurrentConf");
        }
        if (selectedSopClass != null) {
            if (currentConfiguration instanceof AbstractDicomConfiguration) {
                ((AbstractDicomConfiguration) currentConfiguration).setSopClass(selectedSopClass);
            }

        }
    }

    @Override
    public String displayInstitutionForSystemInSession(SystemInSession inSystemSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayInstitutionForSystemInSession");
        }
        return SystemManager.displayInstitutionsForSystemInSession(inSystemSession);
    }

    @Override
    public void validateAssigningAuthorityOID(AbstractConfiguration configuration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateAssigningAuthorityOID");
        }
        if (activatedTestingSession == null) {
            activatedTestingSession = TestingSession.getSelectedTestingSession();
        }
        if (oidsConfigurationForSessionForHL7 == null) {
            oidsConfigurationForSessionForHL7 = OIDSConfigurationForSessionForHL7
                    .getConfigurationParametersForSession(activatedTestingSession);
        }
    }

    @Override
    public void setCurrentConfigurationAndTable(AbstractConfiguration inCurrentConfiguration, String inSelectedTable) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCurrentConfigurationAndTable");
        }
        currentConfiguration = inCurrentConfiguration;
        selectedTable = inSelectedTable;

    }

    @Override
    public void findPossibleConfigurationsForActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findPossibleConfigurationsForActor");
        }

        HashSet<ConfigurationType> set = new HashSet<ConfigurationType>();

        List<ConfigurationTypeMappedWithAIPO> confList;
        if (currentConfiguration.getConfiguration().getActor() == null) {

            possibleConfigurationTypes = null;
            currentConfiguration.getConfiguration().setConfigurationType(null);
            return;

        }
        if (possibleActors == null) {
            return;
        }

        for (Actor a : possibleActors) {
            confList = ConfigurationTypeMappedWithAIPO.getConfigurationTypeMappingWithAIPOFiltered(null, a, null);

            for (ConfigurationTypeMappedWithAIPO confTypeMapped : confList) {
                for (ConfigurationTypeWithPortsWSTypeAndSopClass confTypeWithPorts : confTypeMapped
                        .getListOfConfigurationTypes()) {
                    set.add(confTypeWithPorts.getConfigurationType());
                }
            }
        }

        possibleConfigurationTypes = new ArrayList<ConfigurationType>(set);

        Collections.sort(possibleConfigurationTypes);

        if ((possibleConfigurationTypes == null)
                || !possibleConfigurationTypes.contains(currentConfiguration.getConfiguration().getConfigurationType())) {
            currentConfiguration.getConfiguration().setConfigurationType(null);
        }
    }

    @Override
    public List<Actor> getPossibleActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActors");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        List<SystemActorProfiles> sysList = SystemActorProfiles.getSystemActorProfilesFiltered(em, null,
                selectedSystemInSession.getSystem(), null, null, null, null, null, null);

        Set<Actor> set = new HashSet<Actor>();

        Iterator<SystemActorProfiles> iterator = sysList.iterator();
        while (iterator.hasNext()) {
            ActorIntegrationProfileOption aipo = iterator.next().getActorIntegrationProfileOption();

            if ((aipo != null) && (aipo.getActorIntegrationProfile() != null)) {
                set.add(aipo.getActorIntegrationProfile().getActor());
            }
        }

        possibleActors = new LinkedList<Actor>(set);

        Collections.sort(possibleActors);

        return possibleActors;
    }

    @Override
    public void setPossibleActors(List<Actor> possibleActors) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPossibleActors");
        }
        this.possibleActors = possibleActors;
    }

    @Override
    public List<ConfigurationType> getPossibleConfigurationTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleConfigurationTypes");
        }

        if ((possibleConfigurationTypes == null) || (currentConfiguration.getConfiguration().getActor() == null)) {

            possibleConfigurationTypes = new ArrayList<ConfigurationType>();
            currentConfiguration.getConfiguration().setConfigurationType(null);
        }

        return possibleConfigurationTypes;
    }

    @Override
    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    @Override
    public int compare(ActorIntegrationProfileOption o1, ActorIntegrationProfileOption o2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compare");
        }
        if (o1.getActorIntegrationProfile().getActor().compareTo(o2.getActorIntegrationProfile().getActor()) == 0) {
            if (o1.getActorIntegrationProfile().getIntegrationProfile()
                    .compareTo(o2.getActorIntegrationProfile().getIntegrationProfile()) == 0) {
                if (o1.getIntegrationProfileOption() == null) {
                    return 1;
                }
                if (o2.getIntegrationProfileOption() == null) {
                    return 1;
                } else {
                    return o1.getIntegrationProfileOption().compareTo(o2.getIntegrationProfileOption());
                }

            } else {
                return o1.getActorIntegrationProfile().getIntegrationProfile()
                        .compareTo(o2.getActorIntegrationProfile().getIntegrationProfile());
            }
        } else {
            return o1.getActorIntegrationProfile().getActor().compareTo(o2.getActorIntegrationProfile().getActor());
        }

    }

    @Override
    public void retrieveOutjectedVariables() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveOutjectedVariables");
        }
        selectedSystemInSession = (SystemInSession) Component.getInstance(SELECTED_SYSTEM_IN_SESSION);
    }

    @Override
    public void retrieveCompanyVariable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveCompanyVariable");
        }
    }

    @Override
    public void retrieveSelectedSystemVariable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveSelectedSystemVariable");
        }
        selectedSystemInSession = (SystemInSession) Component.getInstance(SELECTED_SYSTEM_IN_SESSION);

        if (selectedSystemInSession != null && !UserService.hasRole(Role.ADMINISTRATOR_ROLE_STRING)
                && !selectedSystemInSession.isAuthorizedUser(selectedSystemInSession) && !UserService.hasRole(Role.VENDOR_ADMIN_ROLE_STRING)) {
            selectedSystemInSession = null;
        }
    }

    @Override
    public void initOneMenu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initOneMenu");
        }
        if (selectedSystemInSession == null || !selectedSystemInSession.isAuthorizedUser(selectedSystemInSession)) {
            GazelleDAO gazelleDAO = new GazelleDAO();
            List<SystemInSession> res = gazelleDAO.getNotDrppedSystemsInSessionForLoggedCompanyForSession();
            if (res.size() == 1) {
                setSelectedSystemInSession(res.get(0));
            }
        }
    }

    @Override
    public void checkTransportLayer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkTransportLayer");
        }
        if ((currentConfiguration != null) && (currentConfiguration instanceof SyslogConfiguration)) {
            if (((SyslogConfiguration) currentConfiguration).getTransportLayer() != null) {
                if (currentConfiguration.getConfiguration().getIsSecured() == null) {
                    currentConfiguration.getConfiguration().setIsSecured(false);
                }
                if (((SyslogConfiguration) currentConfiguration).getTransportLayer().equals(
                        TransportLayer.getUDP_Protocol())
                        && currentConfiguration.getConfiguration().getIsSecured()) {
                    currentConfiguration.getConfiguration().setIsSecured(false);
                }
            }
        }
    }

    private Map<Pair<Class<?>, IntegrationProfile>, String> getPossibleOIDsForThisSystem() {
        HashMap<Pair<Class<?>, IntegrationProfile>, String> map = new HashMap<Pair<Class<?>, IntegrationProfile>, String>();

        OIDSConfigurationForSessionForHL7 oidsConfigurationForHL7 = OIDSConfigurationForSessionForHL7
                .getConfigurationParametersForSession(activatedTestingSession);
        List<GenericOIDSConfigurationForSession> oidsConfigurationsForWS = GenericOIDSConfigurationForSession
                .getConfigurationParametersForSession(activatedTestingSession);

        if ((oidsConfigurationForHL7 != null) && (oidsConfigurationsForWS != null)) {

            String currentOIDForHL7V2 = oidsConfigurationForHL7.toString(OidsTypeForHL7.oidHL7V2);
            oidsConfigurationForHL7.incrementOID(OidsTypeForHL7.oidHL7V2);

            String currentOIDForHL7V3 = oidsConfigurationForHL7.toString(OidsTypeForHL7.oidHL7V3);
            oidsConfigurationForHL7.incrementOID(OidsTypeForHL7.oidHL7V3);

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(oidsConfigurationForHL7);

            Pair<Class<?>, IntegrationProfile> newPair = new Pair<Class<?>, IntegrationProfile>(
                    HL7V2InitiatorConfiguration.class, null);
            map.put(newPair, currentOIDForHL7V2);

            newPair = new Pair<Class<?>, IntegrationProfile>(HL7V2ResponderConfiguration.class, null);
            map.put(newPair, currentOIDForHL7V2);

            newPair = new Pair<Class<?>, IntegrationProfile>(HL7V3InitiatorConfiguration.class, null);
            map.put(newPair, currentOIDForHL7V3);

            newPair = new Pair<Class<?>, IntegrationProfile>(HL7V3ResponderConfiguration.class, null);
            map.put(newPair, currentOIDForHL7V3);

            for (GenericOIDSConfigurationForSession genericOID : oidsConfigurationsForWS) {
                newPair = new Pair<Class<?>, IntegrationProfile>(WebServiceConfiguration.class,
                        genericOID.getProfileFor());
                String currentOIDForWS = genericOID.toString();
                map.put(newPair, currentOIDForWS);

                genericOID.incrementOID();
                entityManager.merge(genericOID);
            }

            entityManager.flush();
        }
        return map;
    }

    /**
     * This method truncates a string if this string is too long (used in drop down list (criteria) during search)
     *
     * @param inString not truncated
     * @return truncated string if the given string is too long, else it returns the given string
     */
    @Override
    public String truncateToTwenty(String inString) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("truncateToTwenty");
        }
        if (inString.length() > 20) {

            inString = inString.substring(0, 20);
        }
        return inString;
    }

    /**
     * method called when user accesses the permanent link at /systems/systemInSession.seam?id=
     */
    @Override
    public void selectSystemInSessionById() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectSystemInSessionById");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String sisId = params.get("id");
        if ((sisId != null) && !sisId.isEmpty()) {
            try {
                Integer id = Integer.decode(sisId);
                EntityManager em = EntityManagerService.provideEntityManager();
                selectedSystemInSession = em.find(SystemInSession.class, id);
            } catch (NumberFormatException e) {
                selectedSystemInSession = null;
            }
        } else {
            selectedSystemInSession = null;
        }
    }

    @Override
    public String getWSTransactionUsageText(WebServiceConfiguration wsc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWSTransactionUsageText");
        }

        if (wsc != null) {
            if (wsc.getWsTransactionUsage() != null) {
                return wsc.getWsTransactionUsage().getTransaction().getKeyword() + ":"
                        + wsc.getWsTransactionUsage().getUsage();
            }
        }
        return null;
    }

    @Override
    public String getHL7V3TransactionUsageText(HL7V3ResponderConfiguration wsc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7V3TransactionUsageText");
        }
        if (wsc != null) {
            if (wsc.getWsTransactionUsage() != null) {
                return wsc.getWsTransactionUsage().getTransaction().getKeyword() + ":"
                        + wsc.getWsTransactionUsage().getUsage();
            }
        }
        return null;
    }

    @Override
    public List<WSTransactionUsage> allowedWSTransactionUsagesForCurrentConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("allowedWSTransactionUsagesForCurrentConfiguration");
        }
        if (this.currentConfiguration != null) {
            return WSTransactionUsage.getWSTransactionUsageFiltered(null, this.currentConfiguration.getConfiguration()
                    .getActor(), null, null);
        }
        return null;
    }

    @Override
    public void copyConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyConfiguration");
        }
        if (this.currentConfiguration != null) {
            AbstractConfiguration ac = this.copyAbstractConfiguration(currentConfiguration);
            EntityManager em = EntityManagerService.provideEntityManager();
            Configuration conf = em.merge(ac.getConfiguration());
            em.flush();
            ac.setConfiguration(conf);
            ac.setId(conf.getId());
            em.merge(ac);
            em.flush();
        }
    }

    @Override
    public boolean canGenerateConfigs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canGenerateConfigs");
        }
        if (UserService.hasRole(Role.VENDOR_ADMIN_ROLE_STRING) || UserService.hasRole(Role.VENDOR_ROLE_STRING)) {

            SystemInSessionQuery query = new SystemInSessionQuery();
            query.id().eq(selectedSystemInSession.getId());
            selectedSystemInSession = query.getUniqueResult();

            if ((selectedSystemInSession != null) && (selectedSystemInSession.getTestingSession() != null)) {
                Integer userInstitutionId = null;
                if (User.loggedInUser().getInstitution() != null) {
                    userInstitutionId = User.loggedInUser().getInstitution().getId();
                }
                if ((selectedSystemInSession.getSystem() != null) && (selectedSystemInSession.getSystem().getInstitutionSystems() != null)) {
                    Set<InstitutionSystem> institutionSystems = selectedSystemInSession.getSystem()
                            .getInstitutionSystems();
                    for (InstitutionSystem institutionSystem : institutionSystems) {
                        if ((institutionSystem != null) && (institutionSystem.getInstitution() != null)) {
                            if (institutionSystem.getInstitution().getId().equals(userInstitutionId)) {
                                return true;
                            }
                        }
                    }
                }

            }
        }
        return false;
    }

}