package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.NetworkCommunicationType;
import net.ihe.gazelle.tf.model.Standard;
import net.ihe.gazelle.tf.model.StandardQuery;
import net.ihe.gazelle.tf.ws.data.StandardWrapper;
import net.ihe.gazelle.tf.ws.data.StandardsWrapper;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("standardWs")
public class StandardWs implements StandardWsApi {
    private static final Logger LOG = LoggerFactory.getLogger(StandardWs.class);

    @Override
    public StandardsWrapper getStandards(String domain, String transaction, String communicationType) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStandards");
        }
        StandardQuery query = new StandardQuery();
        if (domain != null) {
            query.transactions().profileLinks().actorIntegrationProfile().integrationProfile().domainsForDP().keyword().eq(domain);
        }
        if (transaction != null) {
            query.transactions().keyword().eq(transaction);
        }
        if (communicationType != null) {
            query.networkCommunicationType().eq(NetworkCommunicationType.getEnumWithLabel(communicationType));
        }
        List<Standard> standards = query.getListDistinct();
        List<StandardWrapper> standardWrapperList = new ArrayList<StandardWrapper>();
        for (Standard standard : standards) {
            standardWrapperList.add(new StandardWrapper(standard.getId(), standard.getKeyword(), standard.getName(), standard
                    .getNetworkCommunicationType().getLabel()));
        }
        StandardsWrapper standardWrapper = new StandardsWrapper(standardWrapperList);
        standardWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());

        return standardWrapper;
    }

    @Override
    public StandardWrapper getStandard(String id) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStandard");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Standard standard = em.find(Standard.class, Integer.valueOf(id));
        StandardWrapper standardWrapper = new StandardWrapper(standard.getId(), standard.getKeyword(), standard.getName(), standard
                .getNetworkCommunicationType().getLabel());
        standardWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());
        return standardWrapper;
    }

}
