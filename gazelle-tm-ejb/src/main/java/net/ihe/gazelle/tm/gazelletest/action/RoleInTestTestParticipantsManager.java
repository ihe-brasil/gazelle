package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RoleInTestTestParticipantsManager {

    private static final Logger LOG = LoggerFactory.getLogger(RoleInTestTestParticipantsManager.class);
    private RoleInTest selectedRoleInTest;
    private TestParticipants selectedTestParticipants;
    private Actor selectedActor;
    private IntegrationProfile selectedIntegrationProfile;
    private IntegrationProfileOption selectedIntegrationProfileOption;
    private EntityManager entityManager;

    public RoleInTestTestParticipantsManager(RoleInTest selectedRoleInTest, TestParticipants selectedTestParticipants,
                                             Actor selectedActor, IntegrationProfile selectedIntegrationProfile,
                                             IntegrationProfileOption selectedIntegrationProfileOption) {
        super();
        this.selectedRoleInTest = selectedRoleInTest;
        this.selectedTestParticipants = selectedTestParticipants;
        this.selectedActor = selectedActor;
        this.selectedIntegrationProfile = selectedIntegrationProfile;
        this.selectedIntegrationProfileOption = selectedIntegrationProfileOption;
        this.entityManager = EntityManagerService.provideEntityManager();
    }

    public void addParticipantToRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addParticipantToRoleInTest");
        }
        if ((selectedRoleInTest == null) || (selectedTestParticipants == null)) {
            return;
        }

        ActorIntegrationProfileOption aipo = ActorIntegrationProfileOption.getActorIntegrationProfileOption(
                selectedActor, selectedIntegrationProfile, selectedIntegrationProfileOption);
        saveTestParticipants(aipo, selectedTestParticipants, selectedRoleInTest);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test participant added for " + selectedRoleInTest.getKeyword());
    }

    private void saveTestParticipants(ActorIntegrationProfileOption aipo, TestParticipants testParticipant,
                                      RoleInTest roleInTest) {

        roleInTest = entityManager.find(RoleInTest.class, roleInTest.getId());
        List<TestParticipants> testParticipantsList = new ArrayList<TestParticipants>(
                roleInTest.getTestParticipantsList());

        LOG.info("roleInTest " + roleInTest.getId() + ", roleInTest.getTestParticipantsList().size() "
                + testParticipantsList.size());
        testParticipant.setActorIntegrationProfileOption(aipo);

        List<TestParticipants> testParticipants = TestParticipants.getTestParticipantsFiltered(aipo, null, null,
                null, null, null);

        if (testParticipants != null) {
            LOG.info("testParticipants = " + (testParticipants.size()));
            for (TestParticipants testParticipantTmp : testParticipants) {
                LOG.info("check tests participants");
                if (testParticipantTmp.getTested().equals(testParticipant.getTested())) {
                    testParticipant = testParticipantTmp;

                } else {
                    LOG.info("remove test participants");
                    testParticipantsList.remove(testParticipantTmp);
                }
            }
            testParticipant = entityManager.merge(testParticipant);

            LOG.info("testParticipant id = " + testParticipant.getId());
            LOG.info("roleInTest " + roleInTest.getId() + ", roleInTest.getTestParticipantsList().size() "
                    + roleInTest.getTestParticipantsList().size());
            if (!testParticipantsList.contains(testParticipant)) {

                testParticipantsList.add(testParticipant);
                roleInTest.setTestParticipantsList(testParticipantsList);

                // Trick to allow the change of date and modifier Id to called in the pre persist method of auditModule
                roleInTest.setLastChanged(new Date());
                entityManager.merge(roleInTest);
                entityManager.flush();

                TestParticipants oldtestParticipant = testParticipant;

                boolean value = oldtestParticipant.getTested();

                testParticipant = new TestParticipants();
                testParticipant.setTested(value);

                selectedIntegrationProfileOption = null;

            } else {

                for (TestParticipants participant : roleInTest.getTestParticipantsList()) {
                    LOG.info("  "
                            + participant.getActorIntegrationProfileOption().getActorIntegrationProfile()
                            .toString() + " WITH " + aipo.getIntegrationProfileOption().toString() + " "
                            + participant.getTested());
                }
            }
        }
    }
}
