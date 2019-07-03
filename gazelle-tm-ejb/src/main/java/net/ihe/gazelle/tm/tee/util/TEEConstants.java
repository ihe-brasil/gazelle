package net.ihe.gazelle.tm.tee.util;

/**
 * Holds various constants in Test Execution Engine
 *
 * @author tnabeel
 */
public abstract class TEEConstants {

    public static final String PROXY_JMS_PARAM_MESSAGE_ID = "msgId";
    public static final String PROXY_JMS_PARAM_CONNECTION_ID = "connId";
    public static final String PROXY_JMS_PARAM_PROXY_TYPE = "proxyType";
    public static final String PROXY_JMS_PARAM_PROXY_HOST_NAME = "proxyHostName";
    public static final String PROXY_JMS_PARAM_MESSAGE_DIRECTION = "messageDirection";
    public static final String PROXY_JMS_PARAM_DATE_RECEIVED = "dateReceived";
    public static final String PROXY_JMS_PARAM_SENDER_IP_ADDRESS = "senderIPAddress";
    public static final String PROXY_JMS_PARAM_SENDER_PORT = "senderPort";
    public static final String PROXY_JMS_PARAM_PROXY_PORT = "proxyPort";
    public static final String PROXY_JMS_PARAM_RECEIVER_IP_ADDRESS = "receiverIPAddress";
    public static final String PROXY_JMS_PARAM_RECEIVER_PORT = "receiverPort";
    public static final String PROXY_JMS_PARAM_MESSAGE_CONTENTS = "messageContents";

    public static final int DEFAULT_EXPECTED_STEP_INSTANCE_MESSAGE_COUNT = 2;

    public static final int HTTP_STATUS_CODE_OK = 200;

    public static final int MAX_RETRIES_FOR_REQUEST_RECORD_FIND = 8;
    public static final int SLEEP_INTERVAL_BETWEEN_REQUEST_RECORD_FINDS = 5000; // 5 seconds

}
