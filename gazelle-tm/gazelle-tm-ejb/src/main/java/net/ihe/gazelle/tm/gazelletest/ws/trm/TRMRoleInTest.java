package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TRMRoleInTest {
    private static final Logger LOG = LoggerFactory.getLogger(TRMRoleInTest.class);

    private Integer id;
    private String keyword;
    private List<TRMaipo> aipos;

    public void copyFromRoleInTest(RoleInTest roleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromRoleInTest");
        }
        this.id = roleInTest.getId();
        this.keyword = roleInTest.getKeyword();
        this.aipos = new ArrayList<TRMaipo>();
        List<TestParticipants> testParticipants = roleInTest.getTestParticipantsList();
        for (TestParticipants testParticipant : testParticipants) {
            ActorIntegrationProfileOption aipo = testParticipant.getActorIntegrationProfileOption();
            TRMaipo trMaipo = new TRMaipo();
            trMaipo.loadFromAipo(aipo);
            aipos.add(trMaipo);
        }
    }

    public List<TRMaipo> getAipos() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipos");
        }
        return aipos;
    }

    public void setAipos(List<TRMaipo> aipos) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAipos");
        }
        this.aipos = aipos;
    }

    public Integer getId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getId");
        }
        return id;
    }

    public void setId(Integer id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setId");
        }
        this.id = id;
    }

    public String getKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getKeyword");
        }
        return keyword;
    }

    public void setKeyword(String keyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setKeyword");
        }
        this.keyword = keyword;
    }

}
