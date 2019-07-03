package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.MessageDirection;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for determining whether or not all messages have been received for a test step instance.
 *
 * @author tnabeel
 */
public class ResponseTracker {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseTracker.class);

    private EntityManager entityManager;

    protected ResponseTracker(EntityManager entityManager) {
        Validate.notNull(entityManager);
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Returns true if every request TmStepInstanceMessage for the resolved testStepInstance has received a response; false otherwise.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     * @param allStepInstanceMessagesForTsi
     * @return
     */
    protected boolean receivedResponseForEveryRequest(ProxyMsgData proxyMsgData,
                                                      TmStepInstanceMessage tmStepInstanceMessage, List<TmStepInstanceMessage>
                                                              allStepInstanceMessagesForTsi) {

        Map<Integer, TmStepInstanceMessage> noResponseStepInstanceMessageMap = getNoResponseStepInstanceMessageMap(proxyMsgData,
                tmStepInstanceMessage, allStepInstanceMessagesForTsi);

        // If all responses are accounted for then noResponseStepInstanceMessageMap should be empty
        boolean receivedResponseForEveryRequest = noResponseStepInstanceMessageMap.isEmpty();
        if (!receivedResponseForEveryRequest) {

        }
        return receivedResponseForEveryRequest;

    }

    /**
     * Returns all the request TmStepInstanceMessages for the resolved testStepInstance that have not received a response yet.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     * @param allStepInstanceMessagesForTsi
     * @return
     */
    private Map<Integer, TmStepInstanceMessage> getNoResponseStepInstanceMessageMap(ProxyMsgData proxyMsgData,
                                                                                    TmStepInstanceMessage tmStepInstanceMessage,
                                                                                    List<TmStepInstanceMessage> allStepInstanceMessagesForTsi) {

        Map<Integer, TmStepInstanceMessage> requestStepInstanceMessageMap = getRequestStepInstanceMessageMap(allStepInstanceMessagesForTsi);

        Map<Integer, TmStepInstanceMessage> noResponseStepInstanceMessageMap = new HashMap<Integer, TmStepInstanceMessage>();
        noResponseStepInstanceMessageMap.putAll(requestStepInstanceMessageMap);

        // Go through and remove noResponseMessageMap entries that we got responses for
        for (TmStepInstanceMessage stepInstanceMessage : allStepInstanceMessagesForTsi) {
            if (stepInstanceMessage.getDirection() == MessageDirection.RESPONSE) {
                if (stepInstanceMessage.getRelatesToStepInstanceMessage() != null) {
                    TmStepInstanceMessage requestMsgMappingRemoved = noResponseStepInstanceMessageMap.remove(stepInstanceMessage
                            .getRelatesToStepInstanceMessage().getId());
                    if (requestMsgMappingRemoved == null) {
                        throw new TestExecutionException("StepInstanceMessage is misplaced. Could not find " + stepInstanceMessage
                                .getRelatesToStepInstanceMessage().getId() + " in " + noResponseStepInstanceMessageMap.keySet());
                    }
                }
            }
        }

        return noResponseStepInstanceMessageMap;

    }

    /**
     * Returns all the request TmStepInstanceMessages for the testStepInstance that corresponds to the tmStepInstanceMessage being processed
     *
     * @param allStepInstanceMessagesForTsi
     * @return
     */
    private Map<Integer, TmStepInstanceMessage> getRequestStepInstanceMessageMap(List<TmStepInstanceMessage> allStepInstanceMessagesForTsi) {
        Map<Integer, TmStepInstanceMessage> requestStepInstanceMessageMap = new HashMap<Integer, TmStepInstanceMessage>();
        for (TmStepInstanceMessage item : allStepInstanceMessagesForTsi) {
            if (item.getDirection() == MessageDirection.REQUEST) {
                if (item.getTmTestStepInstance() != null) {
                    requestStepInstanceMessageMap.put(item.getId(), item);
                }
            }
        }
        return requestStepInstanceMessageMap;
    }

}
