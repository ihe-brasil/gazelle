package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRMaipo {
    private static final Logger LOG = LoggerFactory.getLogger(TRMaipo.class);

    private Integer id;
    private String actor;
    private String integrationProfile;
    private String integrationProfileOption;

    public String getActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActor");
        }
        return actor;
    }

    public void setActor(String actor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActor");
        }
        this.actor = actor;
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

    public String getIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfile");
        }
        return integrationProfile;
    }

    public void setIntegrationProfile(String integrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIntegrationProfile");
        }
        this.integrationProfile = integrationProfile;
    }

    public String getIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileOption");
        }
        return integrationProfileOption;
    }

    public void setIntegrationProfileOption(String integrationProfileOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIntegrationProfileOption");
        }
        this.integrationProfileOption = integrationProfileOption;
    }

    public void loadFromAipo(ActorIntegrationProfileOption aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadFromAipo");
        }
        this.id = aipo.getId();
        ActorIntegrationProfile actorIntegrationProfile = aipo.getActorIntegrationProfile();
        if (actorIntegrationProfile != null) {
            Actor actor2 = actorIntegrationProfile.getActor();
            if (actor2 != null) {
                this.actor = actor2.getKeyword();
            }
            IntegrationProfile integrationProfile2 = actorIntegrationProfile.getIntegrationProfile();
            if (integrationProfile2 != null) {
                this.integrationProfile = integrationProfile2.getKeyword();
            }
        }
        IntegrationProfileOption integrationProfileOption2 = aipo.getIntegrationProfileOption();
        if (integrationProfileOption2 != null) {
            this.integrationProfileOption = integrationProfileOption2.getKeyword();
        }
    }

}
