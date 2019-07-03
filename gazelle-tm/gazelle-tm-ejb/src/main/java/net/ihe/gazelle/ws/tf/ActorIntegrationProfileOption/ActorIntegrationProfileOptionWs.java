package net.ihe.gazelle.ws.tf.ActorIntegrationProfileOption;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOptionQuery;
import net.ihe.gazelle.tf.ws.data.AipoWrapper;
import net.ihe.gazelle.tf.ws.data.AiposWrapper;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("actorIntegrationProfileOptionWs")
public class ActorIntegrationProfileOptionWs implements ActorIntegrationProfileOptionWsApi {
    private static final Logger LOG = LoggerFactory.getLogger(ActorIntegrationProfileOptionWs.class);

    @Override
    public AiposWrapper getActorIntegrationProfileOptions(String domainKeyword, String actorkeyword, String integrationProfileKeyword, String
            integrationProfileOption) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfileOptions");
        }
        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
        if (domainKeyword != null && !"".equals(domainKeyword)) {
            query.actorIntegrationProfile().integrationProfile().domainsForDP().keyword().eq(domainKeyword);
        }
        if (actorkeyword != null && !"".equals(actorkeyword)) {
            query.actorIntegrationProfile().actor().keyword().eq(actorkeyword);
        }
        if (integrationProfileKeyword != null && !"".equals(integrationProfileKeyword)) {
            query.actorIntegrationProfile().integrationProfile().keyword().eq(integrationProfileKeyword);
        }
        if (integrationProfileOption != null && !"".equals(integrationProfileOption)) {
            query.integrationProfileOption().keyword().eq(integrationProfileOption);
        }

        List<ActorIntegrationProfileOption> aipos = query.getListDistinct();

        List<AipoWrapper> aipoWrapperList = new ArrayList<AipoWrapper>();
        for (ActorIntegrationProfileOption aipo : aipos) {
            aipoWrapperList.add(new AipoWrapper(aipo.getId(), aipo.getActorIntegrationProfile().getActor().getKeyword(), aipo
                    .getActorIntegrationProfile().getIntegrationProfile().getKeyword(), aipo
                    .getIntegrationProfileOption().getKeyword()));
        }
        AiposWrapper aiposWrapper = new AiposWrapper(aipoWrapperList);

        aiposWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());

        return aiposWrapper;
    }

    @Override
    public AipoWrapper getActorIntegrationProfileOption(String id) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfileOption");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        ActorIntegrationProfileOption aipo = em.find(ActorIntegrationProfileOption.class, Integer.valueOf(id));
        AipoWrapper aipoWrapper = new AipoWrapper(aipo.getId(), aipo.getActorIntegrationProfile().getActor().getKeyword(), aipo
                .getActorIntegrationProfile().getIntegrationProfile().getKeyword(), aipo
                .getIntegrationProfileOption().getKeyword());
        aipoWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());
        return aipoWrapper;
    }
}
