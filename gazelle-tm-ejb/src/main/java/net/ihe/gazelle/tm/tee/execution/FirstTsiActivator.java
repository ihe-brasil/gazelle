package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.tee.dao.TestInstanceDAO;
import net.ihe.gazelle.tm.tee.dao.TestStepsInstanceDAO;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.status.dao.TestInstanceExecutionStatusDAO;
import net.ihe.gazelle.tm.tee.status.dao.TestStepsInstanceExecutionStatusDAO;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * This class is responsible for keeping track of all the needed updates to the DB when a Test Instance
 * is activated by user.
 *
 * @author tnabeel
 */
public class FirstTsiActivator {
    private static final Logger LOG = LoggerFactory.getLogger(FirstTsiActivator.class);

    private EntityManager entityManager;

    protected FirstTsiActivator(EntityManager entityManager) {
        Validate.notNull(entityManager);
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * This method activates the first step instance in the provided test instance for the provided initiator systemInSession
     *
     * @param testInstance
     * @param initiatorSystemInSession
     */
    protected void activateFirstTestStepInstance(TestInstance testInstance, SystemInSession initiatorSystemInSession) {
        Validate.notNull(testInstance);
        Validate.notNull(initiatorSystemInSession);

        // Find the first step instance to activate
        TestStepsInstanceDAO stepInstanceDAO = new TestStepsInstanceDAO(getEntityManager());
        TestStepsInstance testStepsInstance = stepInstanceDAO.findFirstTsiToActivate(testInstance, initiatorSystemInSession);
        TestStepsInstanceDAO tsiDAO = new TestStepsInstanceDAO(getEntityManager());
        TestInstanceDAO tiDAO = new TestInstanceDAO(getEntityManager());
        TestStepsInstanceExecutionStatusDAO tsiStatusDAO = new TestStepsInstanceExecutionStatusDAO();
        TestInstanceExecutionStatusDAO tiStatusDAO = new TestInstanceExecutionStatusDAO();

        // Activate the first step instance of the active Test Instance to WAITING
        if (testStepsInstance != null) {
            testStepsInstance.setLastChanged(new Date());
            testStepsInstance.setExecutionStatus(tsiStatusDAO.getTestStepExecutionStatusByEnum(TestStepInstanceExecutionStatusEnum.WAITING));
            tsiDAO.updateTestStepInstance(testStepsInstance);

        }

        // Deactivate all other Test Instances (and their step instances) for the same initiator system
        List<TestInstance> tisToDeactivate = tiDAO.findTisToDeactivate(testInstance, initiatorSystemInSession);
        for (TestInstance tiToDeactivate : tisToDeactivate) {
            // Deactivate test instance
            if (tiToDeactivate.getExecutionStatus() != null) {
                switch (tiToDeactivate.getExecutionStatus().getKey()) {
                    case ACTIVE:
                    case PAUSED:
                        tiToDeactivate.setLastChanged(new Date());
                        // ABORTED is user-initiated.  Yes... system is changing the status but it's being done because of user-initiated Test
                        // Instance activation.
                        tiToDeactivate.setExecutionStatus(tiStatusDAO.getTestInstanceExecutionStatusByEnum(TestInstanceExecutionStatusEnum.ABORTED));
                        tiDAO.updateTestInstance(tiToDeactivate);

                        break;
                }
            }
            // Deactivate step instances of the deactivated test instance
            if (tiToDeactivate.getTestStepsInstanceList() != null) {
                for (TestStepsInstance tsiToDeactivate : tiToDeactivate.getTestStepsInstanceList()) {
                    if (tsiToDeactivate.getExecutionStatus() != null) {
                        switch (tsiToDeactivate.getExecutionStatus().getKey()) {
                            case INITIATED:
                            case RESPONDED:
                            case WAITING:
                            case PAUSED:
                                tsiToDeactivate.setLastChanged(new Date());
                                // ABORTED is user-initiated.  Yes... system is changing the status but it's being done because of user-initiated
                                // Test Instance activation.
                                tsiToDeactivate.setExecutionStatus(tsiStatusDAO.getTestStepExecutionStatusByEnum
                                        (TestStepInstanceExecutionStatusEnum.ABORTED));
                                tsiDAO.updateTestStepInstance(testStepsInstance);

                                break;
                        }
                    }
                }
            }
        }
    }

}
