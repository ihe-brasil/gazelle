package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.messaging.MessagePropertyChanged;
import net.ihe.gazelle.messaging.MessagingProvider;
import net.ihe.gazelle.proxy.ws.*;
import net.ihe.gazelle.proxy.ws.Configuration;
import net.ihe.gazelle.proxy.ws.StartTestInstance;
import net.ihe.gazelle.proxy.ws.client.ProxyForTMService;
import net.ihe.gazelle.proxy.ws.client.ProxyForTMServiceStub;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;

@MetaInfServices(MessagingProvider.class)
public class ProxyManager implements MessagingProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyManager.class);

    public ProxyManager() {
        super();
    }

    public static String getMinProxyPort() throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getMinProxyPort");
        }
        ProxyForTMServiceStub proxyForTM = (ProxyForTMServiceStub) getService();
        if (proxyForTM != null) {
            try {
                proxyForTM._getServiceClient().getOptions().setTimeOutInMilliSeconds(10000);
                GetMinProxyPort minProxyPort = new GetMinProxyPort();
                GetMinProxyPortE minProxyPortE = new GetMinProxyPortE();
                minProxyPortE.setGetMinProxyPort(minProxyPort);
                GetMinProxyPortResponseE portMinResponse = proxyForTM.getMinProxyPort(minProxyPortE);
                return portMinResponse.getGetMinProxyPortResponse().get_return();
            } catch (Throwable e) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Cannot reach proxy, check your configuration in Administration");
                throw new RemoteException("failure", e);
            }
        }
        return null;
    }

    public static String getMaxProxyPort() throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getMaxProxyPort");
        }
        ProxyForTMServiceStub proxyForTM = (ProxyForTMServiceStub) getService();
        if (proxyForTM != null) {
            try {
                proxyForTM._getServiceClient().getOptions().setTimeOutInMilliSeconds(10000);
                GetMaxProxyPort maxProxyPort = new GetMaxProxyPort();
                GetMaxProxyPortE maxProxyPortE = new GetMaxProxyPortE();
                maxProxyPortE.setGetMaxProxyPort(maxProxyPort);
                GetMaxProxyPortResponseE portMaxResponse = proxyForTM.getMaxProxyPort(maxProxyPortE);
                return portMaxResponse.getGetMaxProxyPortResponse().get_return();
            } catch (Throwable e) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Cannot reach proxy, check your configuration in Administration");
                throw new RemoteException("failure", e);
            }
        }
        return null;
    }

    public static void startAllChannels() throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void startAllChannels");
        }
        ProxyForTMService proxyForTM = getService();
        if (proxyForTM != null) {
            try {
                Collection<Configuration> confList = getConfigurationsForCurrentSession();
                if (confList != null && !confList.isEmpty()) {
                    StartAllChannels startAllChannels = new StartAllChannels();
                    startAllChannels.setConfigurationsList(confList.toArray(new Configuration[confList.size()]));

                    StartAllChannelsE startAllChannelsE = new StartAllChannelsE();
                    startAllChannelsE.setStartAllChannels(startAllChannels);
                    proxyForTM.startAllChannels(startAllChannelsE);
                } else {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No configurations found");
                }
            } catch (Throwable e) {
                throw new RemoteException("failure", e);
            }
        } else {
            throw new RemoteException("null service");
        }
    }

    public static boolean startTestInstance(net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance testInstance)
            throws RemoteException {
        ProxyForTMService proxyForTM = getService();
        if (proxyForTM != null) {
            try {
                TestInstance proxyTestInstance = new TestInstance();
                proxyTestInstance.setId(testInstance.getId());

                Map<Integer, Configuration> configurationMap = new HashMap<Integer, Configuration>();
                List<Step> steps = new ArrayList<Step>();
                getStepsAndConfigurations(testInstance, steps, configurationMap);

                ArrayList<Configuration> confList = new ArrayList<Configuration>(configurationMap.values());
                proxyTestInstance.setConfigurations(confList.toArray(new Configuration[confList.size()]));
                proxyTestInstance.setSteps(steps.toArray(new Step[steps.size()]));
                StartTestInstanceE startTestInstanceE = new StartTestInstanceE();
                StartTestInstance startTestInstance = new StartTestInstance();
                startTestInstance.setTestInstance(proxyTestInstance);
                startTestInstanceE.setStartTestInstance(startTestInstance);
                proxyForTM.startTestInstance(startTestInstanceE);
                return true;
            } catch (Throwable e) {
                LOG.error("Error : " + e);
                return false;
            }
        }
        return false;
    }

    private static void getStepsAndConfigurations(
            net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance testInstance, List<Step> steps,
            Map<Integer, Configuration> configurations) {

        List<TestStepsInstance> testStepsInstanceList = testInstance.getTestStepsInstanceList();
        for (TestStepsInstance testStepsInstance : testStepsInstanceList) {

            RoleInTest roleInit = testStepsInstance.getTestSteps().getTestRolesInitiator().getRoleInTest();
            SystemInSession sysInit = testStepsInstance.getSystemInSessionInitiator();

            RoleInTest roleResp = testStepsInstance.getTestSteps().getTestRolesResponder().getRoleInTest();
            SystemInSession sysResp = testStepsInstance.getSystemInSessionResponder();

            List<AbstractConfiguration> confsInit = getConfigurations(testInstance, roleInit, sysInit);
            List<AbstractConfiguration> confsResp = getConfigurations(testInstance, roleResp, sysResp);

            Step step = new Step();
            step.setId(testStepsInstance.getId());
            step.setStepIndex(testStepsInstance.getTestSteps().getStepIndex());
            steps.add(step);

            List<Integer> confInitIds = new ArrayList<Integer>();
            for (AbstractConfiguration configuration : confsInit) {
                confInitIds.add(configuration.getId());
                addAndConvertConfiguration(configurations, configuration);
            }
            List<Integer> confRespIds = new ArrayList<Integer>();
            for (AbstractConfiguration configuration : confsResp) {
                confRespIds.add(configuration.getId());
                addAndConvertConfiguration(configurations, configuration);
            }

            int[] senderIdsArray = ArrayUtils.toPrimitive(confInitIds.toArray(new Integer[confInitIds.size()]));
            step.setSenderIds(senderIdsArray);
            int[] receiverIdsArray = ArrayUtils.toPrimitive(confRespIds.toArray(new Integer[confRespIds.size()]));
            step.setReceiverIds(receiverIdsArray);

        }

    }

    public static Collection<Configuration> getConfigurationsForCurrentSession() {
        Map<Integer, Configuration> configurations = new HashMap<Integer, Configuration>();
        List<AbstractConfiguration> confs = AbstractConfiguration
                .listAllConfigurationsForATestingSession(TestingSession.getSelectedTestingSession());
        for (AbstractConfiguration configuration : confs) {
            addAndConvertConfiguration(configurations, configuration);
        }
        return configurations.values();
    }

    private static void addAndConvertConfiguration(Map<Integer, Configuration> configurations,
                                                   AbstractConfiguration configuration) {
        if (configurations.get(configuration.getId()) == null) {
            Configuration conf = convertConfiguration(configuration);
            if (conf != null && conf.getHost() != null) {
                configurations.put(configuration.getId(), conf);
            }
        }
    }

    private static List<AbstractConfiguration> getConfigurations(
            net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance testInstance, RoleInTest roleInTest,
            SystemInSession systemInSession) {
        List<AbstractConfiguration> confs = new ArrayList<AbstractConfiguration>();

        if (roleInTest != null) {
            TestInstanceParticipants tip = TestInstanceParticipants
                    .getTestInstanceParticipantsByTestInstanceBySiSByRoleInTest(testInstance, systemInSession,
                            roleInTest);
            if (tip != null) {
                Actor actor = tip.getActorIntegrationProfileOption().getActorIntegrationProfile().getActor();
                confs = AbstractConfiguration.listAllConfigurationsForAnActorAndASystem(actor, systemInSession, null);
            }
        }
        return confs;
    }

    private static Configuration convertConfiguration(AbstractConfiguration abstractConfiguration) {
        Configuration configuration = null;

        Integer port = -1;
        Integer proxyPort = -1;
        ChannelType type = null;

        if (abstractConfiguration instanceof DicomSCPConfiguration) {
            DicomSCPConfiguration tmConfig = (DicomSCPConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            proxyPort = tmConfig.getPortProxy();
            type = ChannelType.DICOM;
        }
        if (abstractConfiguration instanceof DicomSCUConfiguration) {
            DicomSCUConfiguration tmConfig = (DicomSCUConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            proxyPort = tmConfig.getPortProxy();
            type = ChannelType.DICOM;
        }
        if (abstractConfiguration instanceof HL7V2InitiatorConfiguration) {
            port = -1;
            proxyPort = -1;
            type = ChannelType.HL7;
        }
        if (abstractConfiguration instanceof HL7V2ResponderConfiguration) {
            HL7V2ResponderConfiguration tmConfig = (HL7V2ResponderConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            proxyPort = tmConfig.getPortProxy();
            type = ChannelType.HL7;
        }
        if (abstractConfiguration instanceof HL7V3InitiatorConfiguration) {
            port = -1;
            proxyPort = -1;
            type = ChannelType.HTTP;
        }
        if (abstractConfiguration instanceof HL7V3ResponderConfiguration) {
            HL7V3ResponderConfiguration tmConfig = (HL7V3ResponderConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            proxyPort = tmConfig.getPortProxy();
            type = ChannelType.HTTP;
        }
        if (abstractConfiguration instanceof WebServiceConfiguration) {
            WebServiceConfiguration tmConfig = (WebServiceConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            proxyPort = tmConfig.getPortProxy();
            type = ChannelType.HTTP;
        }
        if (abstractConfiguration instanceof SyslogConfiguration) {
            SyslogConfiguration tmConfig = (SyslogConfiguration) abstractConfiguration;
            if (tmConfig.getTransportLayer().equals(TransportLayer.getTCP_Protocol())) {
                port = tmConfig.getPort();
                proxyPort = tmConfig.getPortProxy();
                type = ChannelType.SYSLOG;
            }
        }
        if (abstractConfiguration instanceof RawConfiguration) {
            RawConfiguration tmConfig = (RawConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            proxyPort = tmConfig.getPortProxy();
            type = ChannelType.RAW;

        }

        if (type != null) {
            configuration = new Configuration();
            configuration.setId(abstractConfiguration.getId());
            net.ihe.gazelle.tm.configurations.model.Configuration origConfiguration = abstractConfiguration
                    .getConfiguration();
            if (origConfiguration == null) {
                return null;
            }
            Host host = origConfiguration.getHost();
            if (host == null) {
                return null;
            }
            configuration.setHost(host.getIp());
            Actor actor = origConfiguration.getActor();
            if (actor == null) {
                configuration.setName("?");
            } else {
                configuration.setName(actor.getKeyword());
            }

            if (port == null) {
                port = -1;
            }
            if (proxyPort == null) {
                proxyPort = -1;
            }

            configuration.setPort(port);
            configuration.setProxyPort(proxyPort);
            configuration.setType(type);
        }
        return configuration;
    }

    public static void markTestStep(Integer id) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void markTestStep");
        }
        ProxyForTMService proxyForTM = getService();
        if (proxyForTM != null) {
            try {
                MarkTestStepE markTestStepE = new MarkTestStepE();
                MarkTestStep markTestStep = new MarkTestStep();
                markTestStep.setTestStepId(id);
                markTestStepE.setMarkTestStep(markTestStep);
                proxyForTM.markTestStep(markTestStepE);
            } catch (Throwable e) {
                // FIXME LOG error
            }
        }
    }

    private static String getPreferenceString(String key) {
        return ApplicationPreferenceManager.getStringValue(key);
    }

    private static ProxyForTMService getService() throws RemoteException {
        String proxyWebserviceEndpoint = getPreferenceString("gazelle_proxy_webservice_endpoint");
        if ((proxyWebserviceEndpoint == null) || proxyWebserviceEndpoint.equals("")) {
            return null;
        } else {
            ProxyForTMService proxyForTM = new ProxyForTMServiceStub(proxyWebserviceEndpoint);
            ProxyForTMServiceStub proxyForTM2 = (ProxyForTMServiceStub) proxyForTM;
            proxyForTM2._getServiceClient().getOptions()
                    .setProperty(org.apache.axis2.Constants.Configuration.DISABLE_SOAP_ACTION, true);

            return proxyForTM2;
        }
    }

    public static String getGazelleProxyURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getGazelleProxyURL");
        }
        return getPreferenceString("gazelle_proxy_url");
    }

    public static String getGazelleProxyStep() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getGazelleProxyStep");
        }
        return getPreferenceString("gazelle_proxy_step");
    }

    public static String getGazelleProxyMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getGazelleProxyMessage");
        }
        return getPreferenceString("gazelle_proxy_message");
    }

    /**
     * Returns all the active Proxy channels
     *
     * @return
     */
    public static List<Configuration> retrieveAllActiveChannels() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveAllActiveChannels");
        }
        try {
            ProxyForTMServiceStub proxyForTM = (ProxyForTMServiceStub) getService();
            if (proxyForTM != null) {
                proxyForTM._getServiceClient().getOptions().setTimeOutInMilliSeconds(10000);
                GetAllActiveChannels allChannels = new GetAllActiveChannels();
                GetAllActiveChannelsE allChannelsE = new GetAllActiveChannelsE();
                allChannelsE.setGetAllActiveChannels(allChannels);
                GetAllActiveChannelsResponseE portMaxResponse = proxyForTM.getAllActiveChannels(allChannelsE);
                if (portMaxResponse != null) {
                    Configuration[] channels = portMaxResponse.getGetAllActiveChannelsResponse().getChannels();
                    if (channels != null) {
                        return Arrays.asList(channels);
                    }
                }
            }
            return new ArrayList<Configuration>();
        } catch (Throwable e) {
            if (LOG.isInfoEnabled()) {
                throw new RuntimeException("Get Proxy Channels failed", e);
            }
            LOG.error("Get Proxy Channels failed");
            throw new RuntimeException("Get Proxy Channels failed", e);
        }
    }

    /**
     * Returns true if the provided proxy port is being listened on; false otherwise
     *
     * @param proxyPort
     * @return
     */
    public static boolean channelStarted(int proxyPort) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean channelStarted");
        }
        try {
            ProxyForTMServiceStub proxyForTM = (ProxyForTMServiceStub) getService();
            if (proxyForTM != null) {
                proxyForTM._getServiceClient().getOptions().setTimeOutInMilliSeconds(10000);
                ChannelStarted channelStarted = new ChannelStarted();
                channelStarted.setProxyPort(proxyPort);
                ChannelStartedE channelStartedE = new ChannelStartedE();
                channelStartedE.setChannelStarted(channelStarted);
                ChannelStartedResponseE channelStartedResponse = proxyForTM.channelStarted(channelStartedE);
                if (channelStartedResponse != null) {
                    return channelStartedResponse.getChannelStartedResponse().get_return();
                }
            }
            return false;
        } catch (Throwable e) {
            throw new RuntimeException("ProxyForTMServiceStub channelStarted failed", e);
        }
    }

    @Override
    public void receiveMessage(Object message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("receiveMessage");
        }
        if (message instanceof MessagePropertyChanged) {
            MessagePropertyChanged messagePropertyChanged = (MessagePropertyChanged) message;
            if (TestStepsInstance.class.isAssignableFrom(messagePropertyChanged.getObject().getClass())) {
                if (messagePropertyChanged.getPropertyName().equals("testStepsInstanceStatus")) {
                    TestStepsInstance testStepsInstance = (TestStepsInstance) messagePropertyChanged.getObject();
                    // sends a query to the proxy
                    if (testStepsInstance.getId() != null) {
                        try {
                            ProxyManager.markTestStep(testStepsInstance.getId());
                        } catch (RemoteException e) {
                            LOG.error("", e);
                        }
                    }
                }
            }
        }
    }

}
