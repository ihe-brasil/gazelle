package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLReloader;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.OIDSConfigurationForSessionForHL7.OidsTypeForHL7;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.util.Pair;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.*;

@Stateless
@Name("generateSystemConfiguration")
@GenerateInterface("GenerateSystemConfigurationLocal")
public class GenerateSystemConfiguration implements GenerateSystemConfigurationLocal {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateSystemConfiguration.class);
    private TestingSession activatedTestingSession;

    /**
     * Process a system configuration generation based on configuration drools. Be careful when adding a new type a filtering is processed to add
     * or not a configuration
     *
     * @param hostToUse
     * @param sIs
     */
    @Override
    public int generateSystemConfigurationsOnly(Host hostToUse, SystemInSession sIs,
                                                TestingSession testingSession) {

        activatedTestingSession = testingSession;
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        hostToUse = HQLReloader.reloadDetached(hostToUse);
        sIs = HQLReloader.reloadDetached(sIs);

        int numberOfConfigurationAdded = 0;
        List<ActorIntegrationProfileOption> listOfSystemActorProfile = SystemActorProfiles
                .getListOfActorIntegrationProfileOptions(sIs.getSystem(), entityManager);

        List<ConfigurationTypeMappedWithAIPO> confTypeMappedList = new ArrayList<ConfigurationTypeMappedWithAIPO>();
        for (ActorIntegrationProfileOption aipo : listOfSystemActorProfile) {
            List<ConfigurationTypeMappedWithAIPO> listOfConfTypeMapped = ConfigurationTypeMappedWithAIPO
                    .getConfigurationTypeMappingWithAIPOFiltered(aipo, null, null);

            if (listOfConfTypeMapped != null) {
                confTypeMappedList.addAll(listOfConfTypeMapped);
            }
        }

        String assigningAuthority = AbstractConfiguration.getAssigningAuthority(sIs.getSystem());
        Map<Pair<Class<?>, IntegrationProfile>, String> possibleOIDsForThisSystem = getPossibleOIDsForThisSystem();

        List<AbstractConfiguration> listOfconf = AbstractConfiguration
                .GetListOfAllConfigurationForASystemInSession(sIs);

        boolean isATNA = this.verifyIfSystemInSessionImplementATNA(sIs);

        Set<Integer> usedPorts = AbstractConfiguration.getUsedProxyPorts(sIs.getTestingSession());

        for (ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO : confTypeMappedList) {

            for (ConfigurationTypeWithPortsWSTypeAndSopClass confType : confTypeWithMappedWithAIPO
                    .getListOfConfigurationTypes()) {
                AbstractConfiguration configurationToAdd = null;

                AbstractConfiguration configurationToAddSecure = null;

                Class<?> clazz = DicomSCPConfiguration.class;
                try {
                    clazz = Class.forName(confType.getConfigurationType().getClassName());
                } catch (ClassNotFoundException e) {
                    LOG.error("Failed to load class", e);
                }

                if (clazz.equals(DicomSCPConfiguration.class)) {
                    configurationToAdd = appendDicomSCPConf(sIs, confTypeWithMappedWithAIPO, confType);
                } else if (clazz.equals(DicomSCUConfiguration.class)) {
                    configurationToAdd = appendDicomSCUConf(sIs, confTypeWithMappedWithAIPO, confType);
                } else if (clazz.equals(HL7V2InitiatorConfiguration.class)) {
                    configurationToAdd = appendHL7V2InititiatorConf(sIs, confTypeWithMappedWithAIPO, confType,
                            assigningAuthority, possibleOIDsForThisSystem.get(new Pair<Class<?>, IntegrationProfile>(
                                    HL7V2InitiatorConfiguration.class, null)));
                } else if (clazz.equals(HL7V3InitiatorConfiguration.class)) {
                    configurationToAdd = appendHL7V3InititiatorConf(sIs, confTypeWithMappedWithAIPO, confType,
                            assigningAuthority, possibleOIDsForThisSystem.get(new Pair<Class<?>, IntegrationProfile>(
                                    HL7V3InitiatorConfiguration.class, null)));
                } else if (clazz.equals(HL7V2ResponderConfiguration.class)) {
                    configurationToAdd = appendHL7V2ResponderConf(sIs, confTypeWithMappedWithAIPO, confType,
                            assigningAuthority, possibleOIDsForThisSystem.get(new Pair<Class<?>, IntegrationProfile>(
                                    HL7V2ResponderConfiguration.class, null)));
                } else if (clazz.equals(HL7V3ResponderConfiguration.class)) {
                    configurationToAdd = appendHL7V3ResponderConf(sIs, confTypeWithMappedWithAIPO, confType,
                            assigningAuthority);
                } else if (clazz.equals(WebServiceConfiguration.class)) {

                    configurationToAdd = appendWSConf(sIs, confTypeWithMappedWithAIPO, confType, assigningAuthority);
                } else if (clazz.equals(SyslogConfiguration.class)) {
                    configurationToAdd = appendSyslogConf(sIs, confTypeWithMappedWithAIPO, confType);
                } else if (clazz.equals(RawConfiguration.class)) {
                    configurationToAdd = appendRawConf(sIs, confTypeWithMappedWithAIPO, confType);
                } else {
                    LOG.error("no configuration type " + confType.getConfigurationType() + " known ");
                }

                Boolean addConfiguration = this
                        .verifyIfAConfigurationIsAlreadyGenerated(configurationToAdd, listOfconf);

                if (addConfiguration) {
                    configurationToAdd.getConfiguration().setHost(hostToUse);
                    configurationToAdd.getConfiguration().setSystemInSession(sIs);
                    configurationToAdd.getProxyPortIfNeeded(usedPorts);
                    Configuration conf = entityManager.merge(configurationToAdd.getConfiguration());
                    entityManager.flush();
                    configurationToAdd.setConfiguration(conf);
                    configurationToAdd.setId(configurationToAdd.getConfiguration().getId());
                    configurationToAdd = entityManager.merge(configurationToAdd);
                    entityManager.flush();
                    listOfconf.add(configurationToAdd);
                    numberOfConfigurationAdded++;
                }

                if (addConfiguration && isATNA && this.hasSecuredPort(configurationToAdd)) {
                    configurationToAddSecure = this.copyAbstractConfiguration(configurationToAdd);
                    Boolean addConfigurationSecure = true; // can be modifed if we want to check if a seccured configuration already exists
                    if (addConfigurationSecure) {
                        configurationToAddSecure.getConfiguration().setIsSecured(true);
                        Configuration conf2 = entityManager.merge(configurationToAddSecure.getConfiguration());
                        entityManager.flush();
                        configurationToAddSecure.setConfiguration(conf2);
                        configurationToAddSecure.setId(configurationToAddSecure.getConfiguration().getId());
                        configurationToAddSecure = entityManager.merge(configurationToAddSecure);
                        entityManager.flush();
                        listOfconf.add(configurationToAddSecure);
                        numberOfConfigurationAdded++;
                    }
                }
            }
        }

        entityManager.flush();

        return numberOfConfigurationAdded;
    }

    private boolean hasSecuredPort(AbstractConfiguration ac) {
        if ((ac instanceof DicomSCUConfiguration) && (((DicomSCUConfiguration) ac).getPortSecured() != null)) {
            return true;
        }
        if ((ac instanceof DicomSCPConfiguration) && (((DicomSCPConfiguration) ac).getPortSecured() != null)) {
            return true;
        }
        if ((ac instanceof HL7V2ResponderConfiguration)
                && (((HL7V2ResponderConfiguration) ac).getPortSecured() != null)) {
            return true;
        }
        if ((ac instanceof HL7V3ResponderConfiguration)
                && (((HL7V3ResponderConfiguration) ac).getPortSecured() != null)) {
            return true;
        }
        if ((ac instanceof WebServiceConfiguration) && (((WebServiceConfiguration) ac).getPortSecured() != null)) {
            return true;
        }
        if ((ac instanceof SyslogConfiguration) && (((SyslogConfiguration) ac).getPortSecured() != null)) {
            return true;
        }
        if ((ac instanceof RawConfiguration) && (((RawConfiguration) ac).getPortSecured() != null)) {
            return true;
        }
        return false;
    }

    private boolean verifyIfSystemInSessionImplementATNA(SystemInSession sis) {
        Set<SystemActorProfiles> lss = sis.getSystem().getSystemActorProfiles();
        for (SystemActorProfiles systemActorProfiles : lss) {
            if ((systemActorProfiles.getActorIntegrationProfileOption() != null)
                    && (systemActorProfiles.getActorIntegrationProfileOption().getActorIntegrationProfile() != null)
                    && (systemActorProfiles.getActorIntegrationProfileOption().getActorIntegrationProfile()
                    .getIntegrationProfile().getKeyword().equals("ATNA"))) {
                return true;
            }
        }
        return false;
    }

    private boolean verifyIfAConfigurationIsAlreadyGenerated(AbstractConfiguration configurationToAdd,
                                                             List<AbstractConfiguration> listOfconf) {
        Boolean addConfiguration = true;

        for (AbstractConfiguration existingConfigurationToTest : listOfconf) {
            if (configurationToAdd.getClass().equals(HL7V2InitiatorConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(HL7V2InitiatorConfiguration.class)) {
                if (configurationToAdd.getConfiguration().getActor()
                        .equals(existingConfigurationToTest.getConfiguration().getActor())) {
                    if (configurationToAdd.getConfiguration().getIsSecured() != null) {
                        addConfiguration = false;
                        break;
                    }
                }
            } else if (configurationToAdd.getClass().equals(HL7V2ResponderConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(HL7V2ResponderConfiguration.class)) {
                if (configurationToAdd.getConfiguration().getActor()
                        .equals(existingConfigurationToTest.getConfiguration().getActor())) {
                    addConfiguration = false;
                    break;
                }
            } else if (configurationToAdd.getClass().equals(HL7V3InitiatorConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(HL7V3InitiatorConfiguration.class)) {
                if (configurationToAdd.getConfiguration().getActor()
                        .equals(existingConfigurationToTest.getConfiguration().getActor())) {
                    addConfiguration = false;
                    break;
                }
            } else if (configurationToAdd.getClass().equals(HL7V3ResponderConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(HL7V3ResponderConfiguration.class)) {
                if (configurationToAdd.getConfiguration().getActor()
                        .equals(existingConfigurationToTest.getConfiguration().getActor())) {
                    addConfiguration = false;
                    break;
                }
            } else if (configurationToAdd.getClass().equals(DicomSCPConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(DicomSCPConfiguration.class)) {
                DicomSCPConfiguration dicomConfigurationToAdd = (DicomSCPConfiguration) configurationToAdd;
                DicomSCPConfiguration dicomConfigurationToTest = (DicomSCPConfiguration) existingConfigurationToTest;

                if (dicomConfigurationToAdd.getConfiguration().getActor()
                        .equals(dicomConfigurationToTest.getConfiguration().getActor())
                        && dicomConfigurationToAdd.getSopClass().equals(dicomConfigurationToTest.getSopClass())) {
                    addConfiguration = false;
                    break;
                }
            } else if (configurationToAdd.getClass().equals(DicomSCUConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(DicomSCUConfiguration.class)) {
                DicomSCUConfiguration dicomConfigurationToAdd = (DicomSCUConfiguration) configurationToAdd;
                DicomSCUConfiguration dicomConfigurationToTest = (DicomSCUConfiguration) existingConfigurationToTest;

                if (dicomConfigurationToAdd.getConfiguration().getActor()
                        .equals(dicomConfigurationToTest.getConfiguration().getActor())
                        && dicomConfigurationToAdd.getSopClass().equals(dicomConfigurationToTest.getSopClass()))

                {
                    addConfiguration = false;
                    break;
                }
            } else if (configurationToAdd.getClass().equals(WebServiceConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(WebServiceConfiguration.class)) {
                WebServiceConfiguration wsToAdd = (WebServiceConfiguration) configurationToAdd;
                WebServiceConfiguration wsToTest = (WebServiceConfiguration) existingConfigurationToTest;
                if ((wsToAdd.getConfiguration() != null)
                        && (wsToAdd.getConfiguration().getActor() != null)
                        && wsToAdd.getConfiguration().getActor().equals(wsToTest.getConfiguration().getActor())
                        && (wsToAdd.getWsTransactionUsage() != null)
                        && (wsToAdd.getWsTransactionUsage().equals(wsToTest.getWsTransactionUsage()))

                        ) {
                    addConfiguration = false;
                    break;
                }
            } else if (configurationToAdd.getClass().equals(SyslogConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(SyslogConfiguration.class)) {
                SyslogConfiguration syslogConfToAdd = (SyslogConfiguration) configurationToAdd;
                SyslogConfiguration syslogConfToTest = (SyslogConfiguration) existingConfigurationToTest;

                if ((syslogConfToAdd.getConfiguration() != null) && (syslogConfToAdd.getConfiguration().getActor() != null)
                        && (syslogConfToAdd.getPort() != null)) {
                    if (syslogConfToAdd.getConfiguration().getActor()
                            .equals(syslogConfToTest.getConfiguration().getActor())
                            && syslogConfToAdd.getPort().equals(syslogConfToTest.getPort()))

                    {
                        addConfiguration = false;
                        break;
                    }
                }
            } else if (configurationToAdd.getClass().equals(RawConfiguration.class)
                    && existingConfigurationToTest.getClass().equals(RawConfiguration.class)) {
                RawConfiguration rawlogConfToAdd = (RawConfiguration) configurationToAdd;
                RawConfiguration rawlogConfToTest = (RawConfiguration) existingConfigurationToTest;

                if ((rawlogConfToAdd.getConfiguration() != null) && (rawlogConfToAdd.getConfiguration().getActor() != null)
                        && (rawlogConfToAdd.getPort() != null)) {
                    if (rawlogConfToAdd.getConfiguration().getActor()
                            .equals(rawlogConfToTest.getConfiguration().getActor())
                            && rawlogConfToAdd.getPort().equals(rawlogConfToTest.getPort()))

                    {
                        addConfiguration = false;
                        break;
                    }
                }
            }

        }

        return addConfiguration;

    }

    private HL7V2InitiatorConfiguration appendHL7V2InititiatorConf(SystemInSession sis,
                                                                   ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                                   ConfigurationTypeWithPortsWSTypeAndSopClass confType, String
                                                                           assagningAuthorityForThisSystem,
                                                                   String assagningAuthorityOIDForThisSystem) {
        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();
        List<Institution> listInst = net.ihe.gazelle.tm.systems.model.System.getInstitutionsForASystem(sis.getSystem());
        String facility = "";
        StringBuffer buf = new StringBuffer();
        for (int currInst = 0; currInst < listInst.size(); currInst++) {
            buf.append(listInst.get(currInst).getKeyword());
            if (currInst < (listInst.size() - 1)) {
                buf.append("_");
            }
        }
        facility = this.truncateToTwenty(buf.toString());
        String usage = confType.getComment();

        return new HL7V2InitiatorConfiguration(new Configuration(actor, usage, false), this.truncateToTwenty(sis
                .getSystem().getKeyword()), facility, assagningAuthorityForThisSystem,
                assagningAuthorityOIDForThisSystem, usage);
    }

    private HL7V3InitiatorConfiguration appendHL7V3InititiatorConf(SystemInSession sis,
                                                                   ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                                   ConfigurationTypeWithPortsWSTypeAndSopClass confType, String
                                                                           assagningAuthorityForThisSystem,
                                                                   String assagningAuthorityOIDForThisSystem) {

        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();
        List<Institution> listInst = net.ihe.gazelle.tm.systems.model.System.getInstitutionsForASystem(sis.getSystem());
        String facility = "";
        StringBuffer buf = new StringBuffer();
        for (int currInst = 0; currInst < listInst.size(); currInst++) {
            buf.append(listInst.get(currInst).getKeyword());

            if (((currInst + 1) == listInst.size()) && (listInst.size() > 1)) {
                buf.append("&");
            }
        }
        facility = this.truncateToTwenty(buf.toString());
        String usage = confType.getComment();

        return new HL7V3InitiatorConfiguration(new Configuration(actor, usage, false), this.truncateToTwenty(sis
                .getSystem().getKeyword()), facility, assagningAuthorityForThisSystem,
                assagningAuthorityOIDForThisSystem, usage);

    }

    private HL7V2ResponderConfiguration appendHL7V2ResponderConf(SystemInSession sis,
                                                                 ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                                 ConfigurationTypeWithPortsWSTypeAndSopClass confType, String
                                                                         assagningAuthorityForThisSystem,
                                                                 String assagningAuthorityOIDForThisSystem) {

        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();

        List<Institution> listInst = net.ihe.gazelle.tm.systems.model.System.getInstitutionsForASystem(sis.getSystem());
        String facility = "";
        StringBuffer buf = new StringBuffer();
        for (int currInst = 0; currInst < listInst.size(); currInst++) {
            buf.append(listInst.get(currInst).getKeyword());

            if (((currInst + 1) == listInst.size()) && (listInst.size() > 1)) {
                buf.append("&");
            }
        }
        facility = this.truncateToTwenty(buf.toString());
        String usage = confType.getComment();

        return new HL7V2ResponderConfiguration(new Configuration(actor, usage, false), confType.getPortNonSecure(),
                confType.getPortSecure(), null, this.truncateToTwenty(sis.getSystem().getKeyword()), facility,
                assagningAuthorityForThisSystem, assagningAuthorityOIDForThisSystem, usage);

    }

    private HL7V3ResponderConfiguration appendHL7V3ResponderConf(SystemInSession sis,
                                                                 ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                                 ConfigurationTypeWithPortsWSTypeAndSopClass confType, String
                                                                         assagningAuthorityForThisSystem) {

        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();

        List<Institution> listInst = net.ihe.gazelle.tm.systems.model.System.getInstitutionsForASystem(sis.getSystem());
        String facility = "";
        StringBuffer buf = new StringBuffer();
        for (int currInst = 0; currInst < listInst.size(); currInst++) {
            buf.append(listInst.get(currInst).getKeyword());

            if (((currInst + 1) == listInst.size()) && (listInst.size() > 1)) {
                buf.append("&");
            }
        }
        facility = this.truncateToTwenty(buf.toString());

        String url = "your_url";
        /* TODO Add usage in the config type model if needed */
        String usage = confType.getComment();

        return new HL7V3ResponderConfiguration(new Configuration(actor, usage, false), url,
                confType.getPortNonSecure(), confType.getPortSecure(), null, usage, this.truncateToTwenty(sis
                .getSystem().getKeyword()), facility, assagningAuthorityForThisSystem, usage,
                confType.getWsTRansactionUsage());
    }

    private DicomSCUConfiguration appendDicomSCUConf(SystemInSession sis,
                                                     ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                     ConfigurationTypeWithPortsWSTypeAndSopClass confType) {
        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();

        SopClass sopClass = confType.getSopClass();
        String role = "SCU";
        String usage = confType.getComment();

        return new DicomSCUConfiguration(new Configuration(actor, usage, false), sis.getSystem().getKeyword(),
                confType.getPortNonSecure(), confType.getPortSecure(), null, sopClass, role);

    }

    private DicomSCPConfiguration appendDicomSCPConf(SystemInSession sis,
                                                     ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                     ConfigurationTypeWithPortsWSTypeAndSopClass confType) {

        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();

        SopClass sopClass = confType.getSopClass();
        String role = "SCP";
        String usage = confType.getComment();

        return new DicomSCPConfiguration(new Configuration(actor, usage, false), sis.getSystem().getKeyword(),
                confType.getPortNonSecure(), confType.getPortSecure(), null, sopClass, role);

    }

    private WebServiceConfiguration appendWSConf(SystemInSession sis,
                                                 ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                 ConfigurationTypeWithPortsWSTypeAndSopClass confType, String assagningAuthorityForThisSystem) {
        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();

        String url = "your_url";
        String usage = confType.getComment();
        return new WebServiceConfiguration(new Configuration(actor, usage, false), url, confType.getPortNonSecure(),
                confType.getPortSecure(), null, null, usage, assagningAuthorityForThisSystem,
                confType.getWsTRansactionUsage());

    }

    private SyslogConfiguration appendSyslogConf(SystemInSession sis,
                                                 ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                 ConfigurationTypeWithPortsWSTypeAndSopClass confType) {
        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();

        return new SyslogConfiguration(new Configuration(actor, "Syslog configuration Configuration for "
                + actor.getKeyword(), false), confType.getPortNonSecure(), null, confType.getPortSecure(),
                confType.getTransportLayer());

    }


    private RawConfiguration appendRawConf(SystemInSession sis,
                                                 ConfigurationTypeMappedWithAIPO confTypeWithMappedWithAIPO,
                                                 ConfigurationTypeWithPortsWSTypeAndSopClass confType) {
        Actor actor = confTypeWithMappedWithAIPO.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor();


        return new RawConfiguration(new Configuration(actor, "Raw configuration for "
                + actor.getKeyword(), false), confType.getPortNonSecure(), null, confType.getPortSecure(), confType.getTransactionDescription());
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

    // Duplicated from systemConfigurationManager
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

    // Duplicated from systemConfigurationManager
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
}
