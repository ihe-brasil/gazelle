package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.tee.dao.*;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.MessageDirection;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import net.ihe.gazelle.tm.tee.parser.HL7Message;
import net.ihe.gazelle.tm.tee.parser.HL7MessageParser;
import net.ihe.gazelle.tm.tee.parser.HL7MsgParserFactory;
import net.ihe.gazelle.tm.tee.util.CollectionUtil;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for resolving tm_test_step_instance_id, tm_test_step_message_profile_id, sender_configuration_id, and
 * receiver_configuration_id,
 * sender_host_id, sender_institution_id, sender_system_id, receiver_host_id, receiver_institution_id, and receiver_system_id fields of
 * tm_step_instance_message
 * for the Request message intercepted by Proxy
 *
 * @author tnabeel
 */
public class RequestDataResolver {
    private static final Logger LOG = LoggerFactory.getLogger(RequestDataResolver.class);

    private static final String EXECUTION_DATA_FINDER = "select ti.id resolvedTestInstanceId,tsi.id resolvedStepInstanceId,senderTmConfig.id " +
            "senderTmConfigId,senderHost.id senderHostId,senderHost.hostname senderHostname,senderHost.ip senderIPAddress,senderSystem.id " +
            "senderSystemId,senderSystem.keyword senderSystemKeyword,senderSystem.name senderSystemName,senderInstitution.id senderInstitutionId," +
            "senderInstitution.keyword senderInstitutionKeyword,senderInstitution.name senderInstitutionName,s_stepMsgProfile.id " +
            "expectedRequestStepMsgProfileId,s_msgProfile.message_type expectedRequestMsgType,receiverTmConfig.id receiverTmConfigId,receiverHost" +
            ".id receiverHostId,receiverHost.hostname receiverHostname,receiverHost.ip receiverIPAddress,receiverSystem.id receiverSystemId," +
            "receiverSystem.keyword receiverSystemKeyword,receiverSystem.name receiverSystemName,    receiverInstitution.id receiverInstitutionId," +
            "receiverInstitution.keyword receiverInstitutionKeyword,receiverInstitution.name receiverInstitutionName,r_stepMsgProfile.id " +
            "expectedResponseStepMsgProfileId,r_msgProfile.message_type expectedResponseMsgType from tm_test_instance ti join " +
            "tm_test_instance_exec_status tiStatus on ti.execution_status_id=tiStatus.id join tm_test_instance_test_steps_instance titsi on ti" +
            ".id=titsi.test_instance_id join tm_test_steps_instance tsi on titsi.test_steps_instance_id=tsi.id join tm_test_steps step on tsi" +
            ".test_steps_id=step.id join tm_step_instance_exec_status tsiStatus on tsi.execution_status_id=tsiStatus.id join " +
            "tm_test_step_message_profile s_stepMsgProfile on s_stepMsgProfile.tm_test_steps_id=step.id join tf_hl7_message_profile s_msgProfile on" +
            " s_msgProfile.id=s_stepMsgProfile.tf_hl7_message_profile_id join tf_actor s_msgProfileActor on s_msgProfileActor.id=s_msgProfile" +
            ".actor_id join tm_test_step_message_profile r_stepMsgProfile on r_stepMsgProfile.tm_test_steps_id=step.id join tf_hl7_message_profile " +
            "r_msgProfile on r_msgProfile.id=r_stepMsgProfile.tf_hl7_message_profile_id join tf_actor r_msgProfileActor on r_msgProfileActor" +
            ".id=r_msgProfile.actor_id join tm_system_in_session senderSis on senderSis.id=tsi.system_in_session_initiator_id join tm_system " +
            "senderSystem on senderSis.system_id=senderSystem.id join tm_institution_system senderISystem on senderISystem.system_id=senderSystem" +
            ".id join usr_institution senderInstitution on senderInstitution.id=senderISystem.institution_id join tm_configuration senderTmConfig " +
            "on senderTmConfig.system_in_session_id=senderSis.id join tm_host senderHost on senderTmConfig.host_id=senderHost.id and senderSis" +
            ".id=senderHost.system_in_session_id join tf_actor senderActor on senderTmConfig.actor_id=senderActor.id join tm_configuration_type " +
            "senderConfigType on senderConfigType.id=senderTmConfig.configurationtype_id join tm_system_in_session receiverSis on receiverSis" +
            ".id=tsi.system_in_session_reponder_id join tm_system receiverSystem on receiverSis.system_id=receiverSystem.id join " +
            "tm_institution_system receiverISystem on receiverISystem.system_id=receiverSystem.id join usr_institution receiverInstitution on " +
            "receiverInstitution.id=receiverISystem.institution_id join tm_configuration receiverTmConfig on receiverTmConfig" +
            ".system_in_session_id=receiverSis.id join tm_host receiverHost on receiverTmConfig.host_id=receiverHost.id and receiverSis" +
            ".id=receiverHost.system_in_session_id join tf_actor receiverActor on receiverTmConfig.actor_id=receiverActor.id join " +
            "tm_configuration_type receiverConfigType on receiverConfigType.id=receiverTmConfig.configurationtype_id where tiStatus.key='ACTIVE' " +
            "and (senderActor.id=s_msgProfileActor.id and s_stepMsgProfile.direction=0) and (receiverActor.id=r_msgProfileActor.id and " +
            "r_stepMsgProfile.direction=1) and tsiStatus.key in ('WAITING') and (senderConfigType.type_name like '%initiator%') and " +
            "(receiverConfigType.type_name like '%responder%') and senderHost.ip=? and receiverHost.ip=? order by ti.id desc, tsi.id asc;";

    private static final String INITIATOR_DATA_FINDER = "select ti.id resolvedTestInstanceId,tsi.id resolvedStepInstanceId,senderTmConfig.id " +
            "senderTmConfigId,senderHost.id senderHostId,senderHost.hostname senderHostname,senderHost.ip senderIPAddress,senderSystem.id " +
            "senderSystemId,senderSystem.keyword senderSystemKeyword,senderSystem.name senderSystemName,senderInstitution.id senderInstitutionId," +
            "senderInstitution.keyword senderInstitutionKeyword,senderInstitution.name senderInstitutionName,s_stepMsgProfile.id " +
            "expectedRequestStepMsgProfileId,s_msgProfile.message_type expectedRequestMsgType from tm_test_instance ti join " +
            "tm_test_instance_exec_status tiStatus on ti.execution_status_id=tiStatus.id join tm_test_instance_test_steps_instance titsi on ti" +
            ".id=titsi.test_instance_id join tm_test_steps_instance tsi on titsi.test_steps_instance_id=tsi.id join tm_test_steps step on tsi" +
            ".test_steps_id=step.id join tm_step_instance_exec_status tsiStatus on tsi.execution_status_id=tsiStatus.id join " +
            "tm_test_step_message_profile s_stepMsgProfile on s_stepMsgProfile.tm_test_steps_id=step.id join tf_hl7_message_profile s_msgProfile on" +
            " s_msgProfile.id=s_stepMsgProfile.tf_hl7_message_profile_id join tf_actor s_msgProfileActor on s_msgProfileActor.id=s_msgProfile" +
            ".actor_id join tm_system_in_session senderSis on senderSis.id=tsi.system_in_session_initiator_id join tm_system senderSystem on " +
            "senderSis.system_id=senderSystem.id join tm_institution_system senderISystem on senderISystem.system_id=senderSystem.id join " +
            "usr_institution senderInstitution on senderInstitution.id=senderISystem.institution_id join tm_configuration senderTmConfig on " +
            "senderTmConfig.system_in_session_id=senderSis.id join tm_host senderHost on senderTmConfig.host_id=senderHost.id and senderSis" +
            ".id=senderHost.system_in_session_id join tf_actor senderActor on senderTmConfig.actor_id=senderActor.id join tm_configuration_type " +
            "senderConfigType on senderConfigType.id=senderTmConfig.configurationtype_id where tiStatus.key='ACTIVE' and (senderActor" +
            ".id=s_msgProfileActor.id and s_stepMsgProfile.direction=0) and tsiStatus.key in ('WAITING') and (senderConfigType.type_name like " +
            "'%initiator%') and senderHost.ip=? order by ti.id desc, tsi.id asc;";

    private EntityManager entityManager;

    public RequestDataResolver(EntityManager entityManager) {
        Validate.notNull(entityManager);
        this.entityManager = entityManager;
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * This method resolves the values for tmStepInstanceMessage.testStepInstance, tmStepInstanceMessage.senderTmConfiguration,
     * and tmStepInstanceMessage.receiverTmConfiguration
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    protected void resolveExecutionData(ProxyMsgData proxyMsgData, TmStepInstanceMessage tmStepInstanceMessage) {
        if (proxyMsgData.getMessageDirection() != MessageDirection.REQUEST) {
            throw new UnsupportedOperationException("This class is supposed to be used for request messages only.");
        }
        // Assume that user has set up everything correctly and try to resolve all the data at once (Test Step Instance, TM Configurations, and
        // Step Msg Profile)
        boolean resolutionSucceeded = resolveAllExecutionData(proxyMsgData, tmStepInstanceMessage);
        if (!resolutionSucceeded) {
            LOG.error("Failed to resolve test step instance and configurations for senderIPAddress '" + proxyMsgData.getSenderIPAddress() + "' " +
                    "receiverIPAddress='" + proxyMsgData.getReceiverIPAddress() + "'.");
            // We now need to resolve as much as we can so that UI can report as much as possible to user.
            handleExecutionDataResolutionFailure(proxyMsgData, tmStepInstanceMessage);
        }
    }

    /**
     * Attempts to resolve all the fields at once.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     * @return
     */
    private boolean resolveAllExecutionData(ProxyMsgData proxyMsgData, TmStepInstanceMessage tmStepInstanceMessage) {
        try {
            Query query = getEntityManager().createNativeQuery(EXECUTION_DATA_FINDER);
            query.setParameter(1, proxyMsgData.getSenderIPAddress());
            query.setParameter(2, proxyMsgData.getReceiverIPAddress());
            List<?> list = query.getResultList();

            if (!CollectionUtil.isEmpty(list)) {
                Object[] items = (Object[]) list.get(0);
//                Integer resolvedTestInstanceId = (Integer) items[0];
                Integer resolvedStepInstanceId = (Integer) items[1];
                Integer senderTmConfigId = (Integer) items[2];
                Integer senderHostId = (Integer) items[3];
//                String senderHostname = (String) items[4];
//                String senderIPAddress = (String) items[5];
                Integer senderSystemId = (Integer) items[6];
//                String senderSystemKeyword = (String) items[7];
//                String senderSystemName = (String) items[8];
                Integer senderInstitutionId = (Integer) items[9];
//                String senderInstitutionKeyword = (String) items[10];
//                String senderInstitutionName = (String) items[11];
                Integer expectedRequestStepMsgProfileId = (Integer) items[12];
//                String expectedRequestMsgType = (String) items[13];
                Integer receiverTmConfigId = (Integer) items[14];
                Integer receiverHostId = (Integer) items[15];
//                String receiverHostname = (String) items[16];
//                String receiverIPAddress = (String) items[17];
                Integer receiverSystemId = (Integer) items[18];
//                String receiverSystemKeyword = (String) items[19];
//                String receiverSystemName = (String) items[20];
                Integer receiverInstitutionId = (Integer) items[21];
//                String receiverInstitutionKeyword = (String) items[22];
//                String receiverInstitutionName = (String) items[23];
//                Integer expectedResponseStepMsgProfileId = (Integer) items[24];
//                String expectedResponseMsgType = (String) items[25];

                // If the finders throw error, then there's something really wrong with state of the database.
                tmStepInstanceMessage.setTmTestStepInstance(new TestStepsInstanceDAO(getEntityManager()).findTsiByTsiId(resolvedStepInstanceId,
                        true));
                tmStepInstanceMessage.setReceiverTmConfiguration(new TmConfigurationDAO(getEntityManager()).findTmConfiguration(receiverTmConfigId));
                tmStepInstanceMessage.setSenderTmConfiguration(new TmConfigurationDAO(getEntityManager()).findTmConfiguration(senderTmConfigId));
                tmStepInstanceMessage.setTmTestStepMessageProfile(new TestStepMsgProfileDAO(getEntityManager()).findTestStepMsgProfileById
                        (expectedRequestStepMsgProfileId));
                tmStepInstanceMessage.setSenderHost(new HostDAO(getEntityManager()).findHostById(senderHostId, true));
                tmStepInstanceMessage.setSenderSystem(new SystemDAO(getEntityManager()).findSystemById(senderSystemId, true));
                tmStepInstanceMessage.setSenderInstitution(new InstitutionDAO(getEntityManager()).findInstitutionById(senderInstitutionId, true));
                tmStepInstanceMessage.setReceiverHost(new HostDAO(getEntityManager()).findHostById(receiverHostId, true));
                tmStepInstanceMessage.setReceiverSystem(new SystemDAO(getEntityManager()).findSystemById(receiverSystemId, true));
                tmStepInstanceMessage.setReceiverInstitution(new InstitutionDAO(getEntityManager()).findInstitutionById(receiverInstitutionId, true));
                tmStepInstanceMessage.setReceivedMessageType(getMessageTypeFromProxyMsgData(proxyMsgData));
                // It's ok if validation throws TestExecutionException.  The previous data has been resolved and TEEManager will inform the user via
                // resolved test step instance.
                validateMsgType(proxyMsgData, tmStepInstanceMessage);
                return true;
            } else {
                return false;
            }
        } finally {

        }
    }

    /**
     * Attempts to resolve all initiator fields at once.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     * @return
     */
    private boolean resolveInitiatorExecutionData(ProxyMsgData proxyMsgData, TmStepInstanceMessage tmStepInstanceMessage) {
        Query query = getEntityManager().createNativeQuery(INITIATOR_DATA_FINDER);
        query.setParameter(1, proxyMsgData.getSenderIPAddress());
        List<?> list = query.getResultList();

        if (!CollectionUtil.isEmpty(list)) {
            Object[] items = (Object[]) list.get(0);
//            Integer resolvedTestInstanceId = (Integer) items[0];
            Integer resolvedStepInstanceId = (Integer) items[1];
            Integer senderTmConfigId = (Integer) items[2];
            Integer senderHostId = (Integer) items[3];
//            String senderHostname = (String) items[4];
//            String senderIPAddress = (String) items[5];
            Integer senderSystemId = (Integer) items[6];
//            String senderSystemKeyword = (String) items[7];
//            String senderSystemName = (String) items[8];
            Integer senderInstitutionId = (Integer) items[9];
//            String senderInstitutionKeyword = (String) items[10];
//            String senderInstitutionName = (String) items[11];
            Integer expectedRequestStepMsgProfileId = (Integer) items[12];
//            String expectedRequestMsgType = (String) items[13];

            // If the finders throw error, then there's something really wrong with state of the database.
            tmStepInstanceMessage.setTmTestStepInstance(new TestStepsInstanceDAO(getEntityManager()).findTsiByTsiId(resolvedStepInstanceId, true));
            tmStepInstanceMessage.setSenderTmConfiguration(new TmConfigurationDAO(getEntityManager()).findTmConfiguration(senderTmConfigId));
            tmStepInstanceMessage.setTmTestStepMessageProfile(new TestStepMsgProfileDAO(getEntityManager()).findTestStepMsgProfileById
                    (expectedRequestStepMsgProfileId));
            tmStepInstanceMessage.setSenderHost(new HostDAO(getEntityManager()).findHostById(senderHostId, true));
            tmStepInstanceMessage.setSenderSystem(new SystemDAO(getEntityManager()).findSystemById(senderSystemId, true));
            tmStepInstanceMessage.setSenderInstitution(new InstitutionDAO(getEntityManager()).findInstitutionById(senderInstitutionId, true));
            tmStepInstanceMessage.setReceivedMessageType(getMessageTypeFromProxyMsgData(proxyMsgData));

            // It's ok if validation throws TestExecutionException.  The previous data has been resolved and TEEManager will inform the user via
            // resolved test step instance.
            validateMsgType(proxyMsgData, tmStepInstanceMessage);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method gets called when all has not gone well.  It is used to report as much information as possible to end user.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    private void handleExecutionDataResolutionFailure(ProxyMsgData proxyMsgData, TmStepInstanceMessage tmStepInstanceMessage) {
        StringBuilder errorMessage = new StringBuilder();

        // Attempt to resolve all initiator-related data
        boolean initiatorDataResolutionSucceeded = resolveInitiatorExecutionData(proxyMsgData, tmStepInstanceMessage);
        if (!initiatorDataResolutionSucceeded) {
            // Resolve sender host, system, and institution
            Host senderHost = new HostDAO(getEntityManager()).findHostWithSystemAndInstitution(proxyMsgData.getSenderIPAddress());
            if (senderHost == null) {
                // Stop all processing
                throw new TestExecutionException("Sender Host IP Address '" + proxyMsgData.getSenderIPAddress() + "' is unknown to TM system. ");
            }
            tmStepInstanceMessage.setSenderHost(senderHost);

            if (senderHost.getSystemsInSession() == null) {
                errorMessage.append("Sender Host '").append(senderHost.getHostname()).append("' (IP:'").append(senderHost.getIp()).append("') does " +
                        "not have any systems in session. ");
            } else {
                System selectedSystem = null;
                for (SystemInSession sis : senderHost.getSystemsInSession()) {
                    selectedSystem = sis.getSystem();
                }

                tmStepInstanceMessage.setSenderSystem(selectedSystem);
                tmStepInstanceMessage.setSenderInstitution(selectedSystem.getInstitutionSystems().iterator().next()
                        .getInstitution());
                TestStepsInstance tsi = new TestStepsInstanceDAO(getEntityManager()).findActiveTsiForSis(senderHost.getSystemsInSession().get(0));
                if (tsi != null) {
                    tmStepInstanceMessage.setTmTestStepInstance(tsi);
                } else {
                    errorMessage.append(tmStepInstanceMessage.getSenderInstitution().getName()).append(" does not have any active test step " +
                            "instances. ");
                }
            }
            // See if sender host has any TM configurations
            if (CollectionUtil.isEmpty(senderHost.getConfigurations())) {
                errorMessage.append("Sender Host '").append(senderHost.getHostname()).append("' (IP:'").append(senderHost.getIp()).append("') does " +
                        "not have any TM configurations. ");
            }
        }

        // Resolve receiver host, system, and institution
        Host receiverHost = new HostDAO(getEntityManager()).findHostWithSystemAndInstitution(proxyMsgData.getReceiverIPAddress());
        if (receiverHost == null) {
            errorMessage.append("Receiver Host IP Address '").append(proxyMsgData.getReceiverIPAddress()).append("' is unknown to TM system. ");
        } else {
            tmStepInstanceMessage.setReceiverHost(receiverHost);
            if (receiverHost.getSystemsInSession() == null) {
                errorMessage.append("Receiver Host '").append(receiverHost.getHostname()).append("' (IP:'").append(receiverHost.getIp()).append("')" +
                        " does not have any systems in session. ");
            } else {
                System selectedSystem = null;
                for (SystemInSession sis : receiverHost.getSystemsInSession()) {
                    selectedSystem = sis.getSystem();
                }

                tmStepInstanceMessage.setReceiverSystem(selectedSystem);
                tmStepInstanceMessage.setReceiverInstitution(selectedSystem.getInstitutionSystems().iterator().next
                        ().getInstitution());
            }
        }

        // See if receiver host has any TM configurations
        if (receiverHost == null || CollectionUtil.isEmpty(receiverHost.getConfigurations())) {
            errorMessage.append("Receiver Host ").append(receiverHost != null ? "'" + receiverHost.getHostname() + "' (IP:'" + receiverHost.getIp()
                    + "') " : "").append("does not have any TM configurations. ");
        }
        throw new TestExecutionException(errorMessage.toString());
    }

    /**
     * Ensures that the actual message type encountered during test execution matches the one configured for the active test step.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    private void validateMsgType(ProxyMsgData proxyMsgData, TmStepInstanceMessage tmStepInstanceMessage) {
        Set<String> expectedMsgTypeSet = getExpectedMsgTypeSet(tmStepInstanceMessage.getTmTestStepInstance().getTestSteps().getMessageType());
        if (tmStepInstanceMessage.getReceivedMessageType() == null || !expectedMsgTypeSet.contains(tmStepInstanceMessage.getReceivedMessageType())) {
            if (expectedMsgTypeSet.size() > 1) {
                throw new TestExecutionException("Actual message type '" + tmStepInstanceMessage.getReceivedMessageType() + "' received from '" +
                        proxyMsgData.getSenderIPAddress() + "'" + (tmStepInstanceMessage.getTmTestStepInstance() != null && tmStepInstanceMessage
                        .getTmTestStepInstance().getTestInstance() != null ? " during Test Instance " + tmStepInstanceMessage.getTmTestStepInstance
                        ().getTestInstance().getId() + " execution" : "") + " did not match any of the expected message types " +
                        expectedMsgTypeSet + ".");
            } else {
                throw new TestExecutionException("Expected message type '" + tmStepInstanceMessage.getTmTestStepInstance().getTestSteps()
                        .getMessageType() + "' did not match actual message type '" + tmStepInstanceMessage.getReceivedMessageType() + "' received " +
                        "from '" + proxyMsgData.getSenderIPAddress() + "'" + (tmStepInstanceMessage.getTmTestStepInstance() != null &&
                        tmStepInstanceMessage.getTmTestStepInstance().getTestInstance() != null ? " during Test Instance " + tmStepInstanceMessage
                        .getTmTestStepInstance().getTestInstance().getId() + " execution" : "") + ".");
            }
        }
    }

    /**
     * Needed because message type column in tf_hl7_message_profile table can be short or long
     *
     * @param msgType
     * @return
     */
    private Set<String> getExpectedMsgTypeSet(String msgType) {
        Set<String> expectedMsgTypeSet = new LinkedHashSet<String>();
        String[] msgTypeTokens = msgType.split("\\^");
        if (msgTypeTokens.length > 2) {
            expectedMsgTypeSet.add(msgTypeTokens[0] + "_" + msgTypeTokens[1]);
            expectedMsgTypeSet.add(msgTypeTokens[0] + "^" + msgTypeTokens[1] + "^" + msgTypeTokens[2]);
        } else {
            expectedMsgTypeSet.add(msgTypeTokens[0]);
        }
        return expectedMsgTypeSet;
    }

    private String getMessageTypeFromProxyMsgData(ProxyMsgData proxyMsgData) {
        //we want to store the encountered message type
        HL7MessageParser hl7MsgParser = HL7MsgParserFactory.getInstance().createHL7MsgParser(proxyMsgData.getMessageContents());
        HL7Message message = hl7MsgParser.parseMessage();
        return message.getMessageType();
    }

}
