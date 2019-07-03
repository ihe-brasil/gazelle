package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 * @author abderrazek boufahja
 */
public class ValidationForTIP {
    private static final Logger LOG = LoggerFactory.getLogger(ValidationForTIP.class);
    public static int XDS_RANGE_LOW1 = 11701;
    public static int XDS_RANGE_HIGH1 = 12099;

    public static int XDS_RANGE_LOW2 = 12300;
    public static int XDS_RANGE_HIGH2 = 12499;

    public TestInstanceParticipants setTestToFailedCore(TestInstanceParticipants inCurrentInstanceParticipant) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestToFailedCore");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (inCurrentInstanceParticipant == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to set the test to verified : test instance used is null");
            return null;
        }
        TestInstance currentTestInstance = entityManager.find(TestInstance.class, inCurrentInstanceParticipant
                .getTestInstance().getId());

        Status lastStatus = Status.getSTATUS_FAILED();
        currentTestInstance.setLastStatus(lastStatus);

        currentTestInstance = entityManager.merge(currentTestInstance);
        inCurrentInstanceParticipant.setTestInstance(currentTestInstance);
        inCurrentInstanceParticipant = entityManager.merge(inCurrentInstanceParticipant);
        entityManager.flush();
        return inCurrentInstanceParticipant;
    }

    public void setTestToVerified(TestInstanceParticipants inCurrentInstanceParticipant) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestToVerified");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (inCurrentInstanceParticipant == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to set the test to verified : test instance used is null");
            return;
        }
        TestInstance currentTestInstance = entityManager.find(TestInstance.class, inCurrentInstanceParticipant
                .getTestInstance().getId());

        Status lastStatus = Status.getSTATUS_VERIFIED();
        currentTestInstance.setLastStatus(lastStatus);

        currentTestInstance = entityManager.merge(currentTestInstance);
        inCurrentInstanceParticipant.setTestInstance(currentTestInstance);
        entityManager.merge(inCurrentInstanceParticipant);
        entityManager.flush();

    }

    protected void sendMailToNotifyFailing(Status newStatus, Status lastStatus, int i) {
        if (newStatus != null) {
            if (newStatus.equals(Status.getSTATUS_FAILED())) {
                if (lastStatus != null) {
                    if (!newStatus.equals(lastStatus)) {
                        Boolean cansend = ApplicationPreferenceManager.getBooleanValue("send_mail_mesa_notification");
                        if (cansend != null) {
                            if (cansend) {
                                Renderer.instance().render("/testing/test/mesa/modificationStatusEmail" + i + ".xhtml"); //DONE
                            }
                        }
                    }
                }
            }
        }
    }

}
