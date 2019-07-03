package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.tm.tee.model.MessageDirection;
import net.ihe.gazelle.tm.tee.model.ProxyTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

/**
 * Encapsulates the data retrieved from the Proxy database
 *
 * @author tnabeel
 */
public class ProxyMsgData implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ProxyMsgData.class);
    private int proxyMsgId;

    private MessageDirection messageDirection;

    private int connectionId;

    private String messageContents;
    private byte[] httpHeaders;

    private String proxyMsgType; // just a string in Proxy DB

    private Date dateReceived;

    private String senderIPAddress;
    private int senderPort;
    private String receiverIPAddress;
    private int receiverPort;

    private int proxyPort;

    private String receiverTmIPAddress;
    private String receiverTmActorKeyword;
    private int receiverTmConfigId;
    private int senderTmConfigId;
    private int tmTestInstanceId;

    private ProxyTypeEnum proxyType;
    private String proxyHostName;

    public int getProxyMsgId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyMsgId");
        }
        return proxyMsgId;
    }

    public void setProxyMsgId(int proxyMsgId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxyMsgId");
        }
        this.proxyMsgId = proxyMsgId;
    }

    public MessageDirection getMessageDirection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageDirection");
        }
        return messageDirection;
    }

    public void setMessageDirection(MessageDirection messageDirection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageDirection");
        }
        this.messageDirection = messageDirection;
    }

    public int getConnectionId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConnectionId");
        }
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setConnectionId");
        }
        this.connectionId = connectionId;
    }

    public String getMessageContents() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageContents");
        }
        return messageContents;
    }

    public void setMessageContents(String messageContents) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageContents");
        }
        this.messageContents = messageContents;
    }

    public byte[] getHttpHeaders() {
        return httpHeaders.clone();
    }

    public void setHttpHeaders(byte[] httpHeaders) {
        this.httpHeaders = httpHeaders.clone();
    }

    public String getProxyMsgType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyMsgType");
        }
        return proxyMsgType;
    }

    public void setProxyMsgType(String proxyMsgType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxyMsgType");
        }
        this.proxyMsgType = proxyMsgType;
    }

    public Date getDateReceived() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDateReceived");
        }
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDateReceived");
        }
        this.dateReceived = dateReceived;
    }

    public int getProxyPort() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyPort");
        }
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxyPort");
        }
        this.proxyPort = proxyPort;
    }

    public String getSenderIPAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSenderIPAddress");
        }
        return senderIPAddress;
    }

    public void setSenderIPAddress(String senderIPAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSenderIPAddress");
        }
        this.senderIPAddress = senderIPAddress;
    }

    public int getSenderPort() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSenderPort");
        }
        return senderPort;
    }

    public void setSenderPort(int senderPort) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSenderPort");
        }
        this.senderPort = senderPort;
    }

    public String getReceiverIPAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReceiverIPAddress");
        }
        return receiverIPAddress;
    }

    public void setReceiverIPAddress(String receiverIPAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReceiverIPAddress");
        }
        this.receiverIPAddress = receiverIPAddress;
    }

    public int getReceiverPort() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReceiverPort");
        }
        return receiverPort;
    }

    public void setReceiverPort(int receiverPort) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReceiverPort");
        }
        this.receiverPort = receiverPort;
    }

    public String getReceiverTmIPAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReceiverTmIPAddress");
        }
        return receiverTmIPAddress;
    }

    public void setReceiverTmIPAddress(String receiverTmIPAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReceiverTmIPAddress");
        }
        this.receiverTmIPAddress = receiverTmIPAddress;
    }

    public String getReceiverTmActorKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReceiverTmActorKeyword");
        }
        return receiverTmActorKeyword;
    }

    public void setReceiverTmActorKeyword(String receiverTmActorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReceiverTmActorKeyword");
        }
        this.receiverTmActorKeyword = receiverTmActorKeyword;
    }

    public int getReceiverTmConfigId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReceiverTmConfigId");
        }
        return receiverTmConfigId;
    }

    public void setReceiverTmConfigId(int receiverTmConfigId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReceiverTmConfigId");
        }
        this.receiverTmConfigId = receiverTmConfigId;
    }

    public int getSenderTmConfigId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSenderTmConfigId");
        }
        return senderTmConfigId;
    }

    public void setSenderTmConfigId(int senderTmConfigId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSenderTmConfigId");
        }
        this.senderTmConfigId = senderTmConfigId;
    }

    public int getTmTestInstanceId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTmTestInstanceId");
        }
        return tmTestInstanceId;
    }

    public void setTmTestInstanceId(int tmTestInstanceId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTmTestInstanceId");
        }
        this.tmTestInstanceId = tmTestInstanceId;
    }

    public ProxyTypeEnum getProxyType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyType");
        }
        return proxyType;
    }

    public void setProxyType(ProxyTypeEnum proxyType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxyType");
        }
        this.proxyType = proxyType;
    }

    public String getProxyHostName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyHostName");
        }
        return proxyHostName;
    }

    public void setProxyHostName(String proxyHostName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProxyHostName");
        }
        this.proxyHostName = proxyHostName;
    }

    @Override
    public int hashCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode");
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + proxyMsgId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("equals");
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProxyMsgData other = (ProxyMsgData) obj;
        if (proxyMsgId != other.proxyMsgId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("toString");
        }
        return longString();
    }

    public String shortString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("shortString");
        }
        return "proxyMsgId=" + proxyMsgId + ", messageDirection=" + messageDirection + ", proxyTypeEnum=" + proxyType
                + ", proxyHostName='" + proxyHostName + "', connectionId="
                + connectionId + ", proxyMsgType=" + proxyMsgType + ", dateReceived=" + dateReceived
                + ", senderIPAddress=" + senderIPAddress + ", senderPort=" + senderPort + ", receiverIPAddress="
                + receiverIPAddress + ", receiverPort=" + receiverPort + ", proxyPort=" + proxyPort + ", receiverTmIPAddress="
                + receiverTmIPAddress + ", receiverTmActorKeyword=" + receiverTmActorKeyword + ", senderTmConfigId=" + senderTmConfigId
                + ", receiverTmConfigId=" + receiverTmConfigId + ", tmTestInstanceId=" + tmTestInstanceId;
    }

    public String longString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("longString");
        }
        return shortString() + ", messageContents=" + (messageContents != null ? messageContents : null);
    }

}
