package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.util.File;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.users.model.Institution;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author abderrazek boufahja
 */
public abstract class ValidationNistReturn {

    public static int FILE_AUTOMATICALLY_VERIFIED = 2;

    public static int FILE_AUTOMATICALLY_FAILED = 0;

    public static int FILE_NOT_AUTOMATICALLY_VERIFIED = 1;

    public static int XDS_RANGE_LOW1 = 11701;
    public static int XDS_RANGE_HIGH1 = 12099;

    public static int XDS_RANGE_LOW2 = 12300;
    public static int XDS_RANGE_HIGH2 = 12499;

    public static Status validateNistTestReturns(List<File> files,
                                                 TestInstanceParticipants inCurrentTestInstanceParticipants, Institution inInstitution) {
        StringBuffer logBuffer = new StringBuffer();
        logBuffer.append("<Test>" + inCurrentTestInstanceParticipants.getTestInstance().getTest().getKeyword()
                + "</Test><Vendor>" + inInstitution.getKeyword() + "</Vendor>");
        EntityManager em = EntityManagerService.provideEntityManager();

        TestInstance testInstanceToUse = null;
        if (inCurrentTestInstanceParticipants.getTestInstance().getId() != null) {
            testInstanceToUse = em
                    .find(TestInstance.class, inCurrentTestInstanceParticipants.getTestInstance().getId());
        } else {
            testInstanceToUse = inCurrentTestInstanceParticipants.getTestInstance();
        }

        if (testInstanceToUse.getDescription() == null) {
            testInstanceToUse.setDescription("");
        }
        em.merge(testInstanceToUse);
        em.flush();
        return null;

    }
}
