package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.Simulator;
import net.ihe.gazelle.tm.systems.model.SimulatorInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

/**
 * this class is created to be used instead of Active bpel engine
 *
 * @author abderrazek boufahja
 */
public class LocalGPClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalGPClient.class);

    public LocalGPClient() {
    }

    public static void startTestInstanceActivating(TestInstance ti, EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void startTestInstanceActivating");
        }
        List<TestStepsInstance> ltsi = ti.getTestStepsInstanceList();
        if ((ltsi != null) && (ltsi.size() > 0)) {
            Collections.sort(ltsi);
            LocalGPClient.activateTestStepsInstance(ltsi.get(0), entityManager);
        }
    }

    public static boolean continueProcess(TestStepsInstance inTestStepsInstance, EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean continueProcess");
        }
        TestInstance ti = TestInstance.getTestInstanceByTestStepsInstance(inTestStepsInstance);
        List<TestStepsInstance> ltsi = ti.getTestStepsInstanceList();
        Collections.sort(ltsi);
        int i = 0;
        boolean contin = true;
        boolean res = false;
        while ((i < ltsi.size()) && contin) {
            if (ltsi.get(i).equals(inTestStepsInstance)) {
                if (i < (ltsi.size() - 1)) {
                    TestStepsInstance tsi = ltsi.get(i + 1);
                    LocalGPClient.activateTestStepsInstance(tsi, entityManager);
                } else {
                    LocalGPClient.updateTestInstanceToCompleted(ti, entityManager);
                    res = true;
                }
                contin = false;
            }
            i++;
        }
        return res;
    }

    public static void activateTestStepsInstance(TestStepsInstance testStepsInstance, EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void activateTestStepsInstance");
        }
        if (testStepsInstance != null) {
            testStepsInstance.setTestStepsInstanceStatus(TestStepsInstanceStatus.getSTATUS_ACTIVATED());
            SystemInSession systemInSessionInitiator = testStepsInstance.getSystemInSessionInitiator();
            SystemInSession systemInSessionResponder = testStepsInstance.getSystemInSessionResponder();

            boolean autoResponse = systemInSessionInitiator.equals(systemInSessionResponder);

            if (SimulatorInSession.class.isInstance(systemInSessionInitiator)
                    && Simulator.class.isInstance(systemInSessionInitiator.getSystem()) && (!autoResponse)) {
                testStepsInstance.setTestStepsInstanceStatus(TestStepsInstanceStatus.getSTATUS_ACTIVATED_SIMU());
            }
            entityManager.merge(testStepsInstance);
            entityManager.flush();
        }
    }

    public static void updateTestInstanceToCompleted(TestInstance testInstance, EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void updateTestInstanceToCompleted");
        }
        if (testInstance != null) {
            testInstance.setLastStatus(Status.getSTATUS_COMPLETED());
            entityManager.merge(testInstance);
            entityManager.flush();
            LocalGPClient.updateSimulatorsOfTestInstanceToCompleted(testInstance);
        }
    }

    public static void updateSimulatorsOfTestInstanceToCompleted(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void updateSimulatorsOfTestInstanceToCompleted");
        }
        List<TestInstanceParticipants> ltip = testInstance.getTestInstanceParticipants();
        String testInstanceId = testInstance.getTestInstanceIdForSimulator();
        for (TestInstanceParticipants testInstanceParticipants : ltip) {
            if (testInstanceParticipants.getSystemInSessionUser().getSystemInSession() instanceof SimulatorInSession) {
                String url = ((Simulator) (testInstanceParticipants.getSystemInSessionUser().getSystemInSession()
                        .getSystem())).getUrl();
                try {
                    ClientCommonSimulator.stopTestInstance(testInstanceId, url);
                } catch (RemoteException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    public static void updateInputOfListTestSteps(TestStepsInstance tsi, ContextualInformationInstance ciigazTosave,
                                                  EntityManager em) {
        TestInstance ti = TestInstance.getTestInstanceByTestStepsInstance(tsi);
        for (TestStepsInstance tsi_temp : ti.getTestStepsInstanceList()) {
            if (tsi_temp.getTestSteps().getStepIndex().intValue() > tsi.getTestSteps().getStepIndex().intValue()) {
                for (net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance cii : tsi_temp
                        .getInputContextualInformationInstanceList()) {
                    if (cii.getContextualInformation().equals(ciigazTosave.getContextualInformation())) {
                        cii.setValue(ciigazTosave.getValue());
                        cii = em.merge(cii);
                        em.flush();
                    }
                }
            }
        }
    }

}
