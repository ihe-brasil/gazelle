package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.tee.dao.StepInstanceMsgDAO;
import net.ihe.gazelle.tm.tee.dao.TestStepMsgProfileDAO;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.MessageDirection;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import net.ihe.gazelle.tm.tee.model.TmTestStepMessageProfile;
import net.ihe.gazelle.tm.tee.parser.HL7Message;
import net.ihe.gazelle.tm.tee.parser.HL7MessageParser;
import net.ihe.gazelle.tm.tee.parser.HL7MsgParserFactory;
import net.ihe.gazelle.tm.tee.util.TEEConstants;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 * This class is responsible for resolving tm_test_step_instance_id, tm_test_step_message_profile_id, sender_configuration_id, and
 * receiver_configuration_id,
 * sender_host_id, sender_institution_id, sender_system_id, receiver_host_id, receiver_institution_id, and receiver_system_id fields of
 * tm_step_instance_message
 * for the Response message intercepted by Proxy
 *
 * @author tnabeel
 */
public class ResponseDataResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseDataResolver.class);

    private EntityManager entityManager;

    public ResponseDataResolver(EntityManager entityManager) {
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
        if (proxyMsgData.getMessageDirection() != MessageDirection.RESPONSE) {
            throw new UnsupportedOperationException("This class is supposed to be used for response messages only.");
        }
        TmStepInstanceMessage reqTmStepInstanceMessage = resolveTmStepInstanceMessageForResponseMessage(proxyMsgData);
        // Resolve receiverTmConfiguration, senderTmConfiguration, and testStepInstance from resolved REQUEST TmStepInstanceMessage
        TestStepsInstance testStepInstance = reqTmStepInstanceMessage.getTmTestStepInstance();
        tmStepInstanceMessage.setReceiverTmConfiguration(reqTmStepInstanceMessage.getSenderTmConfiguration());
        tmStepInstanceMessage.setSenderTmConfiguration(reqTmStepInstanceMessage.getReceiverTmConfiguration());
        tmStepInstanceMessage.setSenderHost(reqTmStepInstanceMessage.getReceiverHost());
        tmStepInstanceMessage.setSenderSystem(reqTmStepInstanceMessage.getReceiverSystem());
        tmStepInstanceMessage.setSenderInstitution(reqTmStepInstanceMessage.getReceiverInstitution());
        tmStepInstanceMessage.setReceiverHost(reqTmStepInstanceMessage.getSenderHost());
        tmStepInstanceMessage.setReceiverSystem(reqTmStepInstanceMessage.getSenderSystem());
        tmStepInstanceMessage.setReceiverInstitution(reqTmStepInstanceMessage.getSenderInstitution());
        tmStepInstanceMessage.setRelatesToStepInstanceMessage(reqTmStepInstanceMessage);
        tmStepInstanceMessage.setTmTestStepInstance(testStepInstance);

        //we need to set the received message type
        HL7MessageParser hl7MsgParser = HL7MsgParserFactory.getInstance().createHL7MsgParser(proxyMsgData.getMessageContents());
        HL7Message message = hl7MsgParser.parseMessage();
        tmStepInstanceMessage.setReceivedMessageType(message.getMessageType());
        resolveStepMessageProfile(proxyMsgData, testStepInstance, tmStepInstanceMessage);
    }

    private void resolveStepMessageProfile(ProxyMsgData proxyMsgData, TestStepsInstance testStepInstance, TmStepInstanceMessage
            tmStepInstanceMessage) {

        TmTestStepMessageProfile tmTestStepMessageProfile = new TestStepMsgProfileDAO(entityManager)
                .findTestStepMsgProfileByTestStepInstanceAndMsgTypeAndDirection(testStepInstance, tmStepInstanceMessage.getReceivedMessageType(),
                        proxyMsgData.getMessageDirection());
        if (tmTestStepMessageProfile == null) {
            if (testStepInstance != null) {
                throw new TestExecutionException("Incorrect messageType '" + tmStepInstanceMessage.getReceivedMessageType() + "' received from '" +
                        proxyMsgData.getSenderIPAddress() + "'" + " during Test Instance " + testStepInstance.getId() + " execution" + ".  " +
                        "Initiator must submit message whose type matches the message type of the active test step.",
                        "This error can also take place if the tm_test_step_message_profile record has not been defined via DML or the UI" + " for " +
                                "tm_test_steps id=" + testStepInstance.getTestSteps().getId() + " tf_hl7_message_profile messageType='" +
                                testStepInstance.getTestSteps().getResponderMessageType() + "' direction=" + proxyMsgData.getMessageDirection()
                                .ordinal());
            }
        }

        tmStepInstanceMessage.setTmTestStepMessageProfile(tmTestStepMessageProfile);
    }

    /**
     * Returns the REQUEST TmStepInstanceMessage that corresponds to the provided RESPONSE TmStepInstanceMessage
     * and that has successfully resolved its testStepInstance.  Since the messages can be processed out of order,
     * it waits until the REQUEST TmStepInstanceMessage's testStepInstance has been resolved.
     *
     * @param proxyMsgData
     * @return
     */
    private TmStepInstanceMessage resolveTmStepInstanceMessageForResponseMessage(ProxyMsgData proxyMsgData) {
        TmStepInstanceMessage tmStepInstanceMessage = null;
        // Giving 1 minute for request to be processed (without validations)
        int retryCount = 0;
        String msgProxyInfo = " proxyType=" + proxyMsgData.getProxyType() + " proxyHostName='" + proxyMsgData.getProxyHostName() + "' direction=" +
                MessageDirection.REQUEST.ordinal() + " connectionId=" + proxyMsgData.getConnectionId() + " proxyMsgId=" + proxyMsgData
                .getProxyMsgId() + " messageDirection=" + proxyMsgData.getMessageDirection();
        while (retryCount < TEEConstants.MAX_RETRIES_FOR_REQUEST_RECORD_FIND) {
            tmStepInstanceMessage = new StepInstanceMsgDAO(getEntityManager()).findTmStepInstanceMsgByProxyTypeAndHostAndDirectionAndConnId
                    (proxyMsgData.getProxyType()
                    , proxyMsgData.getProxyHostName(), MessageDirection.REQUEST, proxyMsgData.getConnectionId());
            if (tmStepInstanceMessage == null || tmStepInstanceMessage.getTmTestStepInstance() == null) {
                retryCount++;
                String msgSuffix = "for " + (tmStepInstanceMessage != null ? " tmStepInstanceMessage " + tmStepInstanceMessage.getId() : "") +
                        msgProxyInfo + ". Trying again... retryCount=" + retryCount + ", waited " + (retryCount * TEEConstants
                        .SLEEP_INTERVAL_BETWEEN_REQUEST_RECORD_FINDS / 1000) + " seconds.";
                if (tmStepInstanceMessage == null) {
                    LOG.warn("resolveTmStepInstanceMessageForResponseMessage()  Could not find tmStepInstanceMessage " + msgSuffix);
                } else {

                }
                try {
                    Thread.sleep(TEEConstants.SLEEP_INTERVAL_BETWEEN_REQUEST_RECORD_FINDS);
                } catch (InterruptedException e) {
                    LOG.error("resolveTmStepInstanceMessageForResponseMessage() failed for proxyMsgId=" + proxyMsgData, e);
                }
            } else {
                break;
            }
        }
        if (tmStepInstanceMessage == null || tmStepInstanceMessage.getTmTestStepInstance() == null) {
            throw new TestExecutionException("Failed to find a tmStepInstanceMessage with resolved TmTestStepInstance for" + msgProxyInfo);
        } else {

        }
        return tmStepInstanceMessage;
    }

}
