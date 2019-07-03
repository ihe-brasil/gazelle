package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.simulator.ws.*;
import net.ihe.gazelle.simulator.ws.StartTestInstance;
import net.ihe.gazelle.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * @author abderrazek boufahja
 */
public class ClientCommonSimulator {
    private static final Logger LOG = LoggerFactory.getLogger(ClientCommonSimulator.class);

    public ClientCommonSimulator() {
    }

    public static boolean startTestInstance(String testInstanceId, String urlSimulatorWSDL) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean startTestInstance");
        }
        GazelleSimulatorManagerWSServiceStub client = new GazelleSimulatorManagerWSServiceStub(urlSimulatorWSDL);
        StartTestInstance startTestInstance = new StartTestInstance();
        startTestInstance.setTestInstanceId(testInstanceId);
        StartTestInstanceE startTestInstanceE = new StartTestInstanceE();
        startTestInstanceE.setStartTestInstance(startTestInstance);
        StartTestInstanceResponseE startTestInstanceResponseE = client.startTestInstance(startTestInstanceE);
        return startTestInstanceResponseE.getStartTestInstanceResponse().get_return();
    }

    public static boolean stopTestInstance(String testInstanceId, String urlSimulatorWSDL) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean stopTestInstance");
        }
        GazelleSimulatorManagerWSServiceStub client = new GazelleSimulatorManagerWSServiceStub(urlSimulatorWSDL);
        StopTestInstance stopTestInstance = new StopTestInstance();
        stopTestInstance.setTestInstanceId(testInstanceId);
        StopTestInstanceE stopTestInstanceE = new StopTestInstanceE();
        stopTestInstanceE.setStopTestInstance(stopTestInstance);
        StopTestInstanceResponseE stopTestInstanceResponseE = client.stopTestInstance(stopTestInstanceE);
        return stopTestInstanceResponseE.getStopTestInstanceResponse().get_return();
    }

    public static Pair<net.ihe.gazelle.simulator.ws.ContextualInformationInstance[], Boolean> sendMessageToSimulator(
            String urlSimulatorWSDL, String messageType, String testInstanceId, String testInstanceParticipantsId,
            net.ihe.gazelle.simulator.ws.Transaction trans, ConfigurationForWS confForWS,
            net.ihe.gazelle.simulator.ws.ContextualInformationInstance[] linputCI,
            net.ihe.gazelle.simulator.ws.ContextualInformationInstance[] loutputCI) throws RemoteException {
        GazelleSimulatorManagerWSServiceStub client = new GazelleSimulatorManagerWSServiceStub(urlSimulatorWSDL);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setMessageType(messageType);
        sendMessage.setTestInstanceId(testInstanceId);
        sendMessage.setTestInstanceParticipantsId(testInstanceParticipantsId);
        sendMessage.setTransaction(trans);

        sendMessage.setResponderConfiguration(confForWS);

        sendMessage.setListContextualInformationInstanceInput(linputCI);
        sendMessage.setListContextualInformationInstanceOutput(loutputCI);

        SendMessageE sendMessageE = new SendMessageE();
        sendMessageE.setSendMessage(sendMessage);
        SendMessageResponseE sendMessageResponseE = client.sendMessage(sendMessageE);
        ResultSendMessage rsm = sendMessageResponseE.getSendMessageResponse().get_return();
        if (rsm != null) {
            return new Pair<net.ihe.gazelle.simulator.ws.ContextualInformationInstance[], Boolean>(rsm.getListCII(),
                    rsm.getEvaluation());
        } else {
            return null;
        }
    }

}
