package net.ihe.gazelle.ws.tf.integrationProfileOptions;

import net.ihe.gazelle.tf.model.IntegrationProfileOptionQuery;
import net.ihe.gazelle.tf.ws.data.IntegrationProfileOptionNamesWrapper;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

@Stateless
@Name("integrationProfileOptionWs")
public class IntegrationProfileOptionWs implements IntegrationProfileOptionWsApi {
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationProfileOptionWs.class);

    @Override
    public IntegrationProfileOptionNamesWrapper getIntegrationProfileOptions(String domainKeyword, String actorkeyword, String
            integrationProfileKeyword) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileOptions");
        }

        IntegrationProfileOptionQuery query = new IntegrationProfileOptionQuery();
        if (domainKeyword != null && !"".equals(domainKeyword)) {
            query.listOfActorIntegrationProfileOption().actorIntegrationProfile().integrationProfile().domainsForDP().keyword().eq(domainKeyword);
        }
        if (actorkeyword != null && !"".equals(actorkeyword)) {
            query.listOfActorIntegrationProfileOption().actorIntegrationProfile().actor().keyword().eq(actorkeyword);
        }
        if (integrationProfileKeyword != null && !"".equals(integrationProfileKeyword)) {
            query.listOfActorIntegrationProfileOption().actorIntegrationProfile().integrationProfile().keyword().eq(integrationProfileKeyword);
        }
        return new IntegrationProfileOptionNamesWrapper(query.keyword().getListDistinct());
    }
}
