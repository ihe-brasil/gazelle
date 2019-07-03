package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.MessageDirection;
import net.ihe.gazelle.tm.tee.model.ProxyTypeEnum;
import net.ihe.gazelle.tm.tee.util.TEEConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Arrays;
import java.util.Date;

/**
 * This is the JMS listener that listens for messages sent from Proxy application to queue/proxyMsgQueue.
 * It invokes the Test Execution Engine via TEEManager Stateless Session Bean.
 *
 * @author tnabeel
 */

//TODO update after jboss7 migration
//@MessageDriven(name = "ProxyMDB", activationConfig = {
//        @ActivationConfigProperty(
//                propertyName = "destinationType",
//                propertyValue = "javax.jms.Queue"),
//        @ActivationConfigProperty(
//        		propertyName = "destination",
//        		propertyValue = "queue/proxyMsgQueue")
//})
// Tareq:
// You can comment this out (along with import above) if you don't want to get tied to JBoss annotations. However, if you do that, you will
// see a warning/error during JBoss startup that's related to timing/ordering of JMS destination setup and MDB deployment. The warning/error
// is harmless though and test-execution will work as usual. I could not figure out a way to create the ordering dependency via descriptor in
// JBoss 5. The destinations-service.xml is already alphabetically preceding the name of the jar containing this MDB in the ear.
//@Depends("jboss.messaging.destination:service=Queue,name=proxyMsgQueue")
public class ProxyMDB implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyMDB.class);

    @EJB
    private TEEManagerLocal teeManager;

    //TODO fix after jboss7
    //@Resource
    private MessageDrivenContext messageDrivenContext;

    @Override
    public void onMessage(Message msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("onMessage");
        }
        if (msg == null) {
            LOG.error(getClass().getSimpleName() + " onMessage() received null message");
            return;
        }
        ProxyMsgData proxyMsgData = extractProxyMessage((MapMessage) msg);
        if (proxyMsgData == null) {
            return;
        }
        try {
            teeManager.processProxyMessage(proxyMsgData);
        } catch (Throwable e) {
            LOG.error(
                    getClass().getSimpleName() + " encountered error for proxyMsgId=" + proxyMsgData.getProxyMsgId()
                            + " proxyType=" + proxyMsgData.getProxyType() + " proxyHostName="
                            + proxyMsgData.getProxyHostName() + " messageDirection="
                            + proxyMsgData.getMessageDirection() + " connId=" + proxyMsgData.getConnectionId(), e);
            getMessageDrivenContext().setRollbackOnly();
        }
    }

    private ProxyMsgData extractProxyMessage(MapMessage jmsMessage) {
        ProxyMsgData proxyMsgData = new ProxyMsgData();
        try {
            proxyMsgData.setProxyMsgId(jmsMessage.getIntProperty(TEEConstants.PROXY_JMS_PARAM_MESSAGE_ID));
            proxyMsgData.setConnectionId(jmsMessage.getIntProperty(TEEConstants.PROXY_JMS_PARAM_CONNECTION_ID));
            proxyMsgData.setMessageDirection(resolveMessageDirection(jmsMessage
                    .getStringProperty(TEEConstants.PROXY_JMS_PARAM_MESSAGE_DIRECTION)));
            proxyMsgData.setProxyType(resolveProxyTypeEnum(jmsMessage
                    .getStringProperty(TEEConstants.PROXY_JMS_PARAM_PROXY_TYPE)));
            proxyMsgData.setProxyHostName(jmsMessage.getStringProperty(TEEConstants.PROXY_JMS_PARAM_PROXY_HOST_NAME));
            proxyMsgData.setDateReceived(new Date(jmsMessage
                    .getLongProperty(TEEConstants.PROXY_JMS_PARAM_DATE_RECEIVED)));
            proxyMsgData.setSenderIPAddress(jmsMessage
                    .getStringProperty(TEEConstants.PROXY_JMS_PARAM_SENDER_IP_ADDRESS));
            proxyMsgData.setSenderPort(getNullSafeIntValue(jmsMessage
                    .getIntProperty(TEEConstants.PROXY_JMS_PARAM_SENDER_PORT)));
            proxyMsgData.setProxyPort(getNullSafeIntValue(jmsMessage
                    .getIntProperty(TEEConstants.PROXY_JMS_PARAM_PROXY_PORT)));
            proxyMsgData.setReceiverIPAddress(jmsMessage
                    .getStringProperty(TEEConstants.PROXY_JMS_PARAM_RECEIVER_IP_ADDRESS));
            proxyMsgData.setReceiverPort(getNullSafeIntValue(jmsMessage
                    .getIntProperty(TEEConstants.PROXY_JMS_PARAM_RECEIVER_PORT)));
            proxyMsgData
                    .setMessageContents(jmsMessage.getStringProperty(TEEConstants.PROXY_JMS_PARAM_MESSAGE_CONTENTS));
        } catch (JMSException e) {
            LOG.error(getClass().getSimpleName() + " encountered error ", e);
        }
        return proxyMsgData;
    }

    private int getNullSafeIntValue(Integer port) {
        return port != null ? port : -1;
    }

    /**
     * Returns the MessageDirection for the provided strMsgDirection in JMS message.
     *
     * @param strProxyType
     * @return
     */
    private ProxyTypeEnum resolveProxyTypeEnum(String strProxyType) {
        try {
            return ProxyTypeEnum.valueOf(strProxyType);
        } catch (Exception e) {
            throw new TestExecutionException("Invalid proxyType passed in JMS message by JMS producer: '"
                    + strProxyType + "'.  Valid values include: " + Arrays.asList(ProxyTypeEnum.values()), e);
        }
    }

    /**
     * Returns the ProxyTypeEnum for the provided strProxyType in JMS message.
     *
     * @param strMsgDirection
     * @return
     */
    private MessageDirection resolveMessageDirection(String strMsgDirection) {
        try {
            return MessageDirection.valueOf(strMsgDirection);
        } catch (Exception e) {
            throw new TestExecutionException("Invalid messageDirection passed in JMS message by JMS producer: '"
                    + strMsgDirection + "'.  Valid values include: " + Arrays.asList(MessageDirection.values()), e);
        }
    }

    public MessageDrivenContext getMessageDrivenContext() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageDrivenContext");
        }
        return messageDrivenContext;
    }

    public void setMessageDrivenContext(MessageDrivenContext messageDrivenContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageDrivenContext");
        }
        this.messageDrivenContext = messageDrivenContext;
    }

}
