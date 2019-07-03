package net.ihe.gazelle.ws.tf.integrationProfiles;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileQuery;
import net.ihe.gazelle.tf.model.constraints.AipoRuleQuery;
import net.ihe.gazelle.tf.ws.data.IntegrationProfileNamesWrapper;
import net.ihe.gazelle.tf.ws.data.IntegrationProfileWrapper;
import net.ihe.gazelle.tf.ws.data.IntegrationProfilesWrapper;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestStatus;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("integrationProfileWs")
public class IntegrationProfileWs implements IntegrationProfileWsApi {
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationProfileWs.class);

    @Override
    public IntegrationProfileNamesWrapper getIntegrationProfilesNames(String domainKeyword, String actorkeyword)
            throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesNames");
        }
        IntegrationProfileQuery query = new IntegrationProfileQuery();
        if (domainKeyword != null && !"".equals(domainKeyword)) {
            query.domainsForDP().keyword().eq(domainKeyword);
        }
        if (actorkeyword != null && !"".equals(actorkeyword)) {
            query.actorIntegrationProfiles().actor().keyword().eq(actorkeyword);
        }

        return new IntegrationProfileNamesWrapper(query.keyword().getListDistinct());
    }

    @Override
    public IntegrationProfileNamesWrapper getIntegrationProfilesNamesWithRules(String domain) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesNamesWithRules");
        }
        AipoRuleQuery query = new AipoRuleQuery();
        if ((domain != null) && !"".equals(domain)) {
            query.aipoRules().integrationProfile().domainsForDP().keyword().eq(domain);
        }
        query.aipoRules().integrationProfile().keyword().isNotNull();
        return new IntegrationProfileNamesWrapper(query.aipoRules().integrationProfile().keyword().getListDistinct());
    }

    @Override
    public IntegrationProfileNamesWrapper getIntegrationProfilesNamesWithTests(String domain) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesNamesWithTests");
        }
        TestQuery query = new TestQuery();
        query.testStatus().keyword().eq(TestStatus.STATUS_READY_STRING);
        if ((domain != null) && !"".equals(domain)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().integrationProfile().domainsForDP().keyword().eq(domain);
        }
        List<String> integrationProfileKeywords = query.testRoles().roleInTest().testParticipantsList()
                .actorIntegrationProfileOption().actorIntegrationProfile().integrationProfile().keyword()
                .getListDistinct();

        return new IntegrationProfileNamesWrapper(integrationProfileKeywords);
    }

    @Override
    public IntegrationProfilesWrapper getIntegrationProfiles(String domainKeyword, String actorkeyword)
            throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfiles");
        }
        IntegrationProfileQuery query = new IntegrationProfileQuery();
        if (domainKeyword != null && !"".equals(domainKeyword)) {
            query.domainsForDP().keyword().eq(domainKeyword);
        }
        if (actorkeyword != null && !"".equals(actorkeyword)) {
            query.actorIntegrationProfiles().actor().keyword().eq(actorkeyword);
        }

        List<IntegrationProfile> integrationProfile = query.getListDistinct();

        List<IntegrationProfileWrapper> integrationProfileWrapperList = new ArrayList<IntegrationProfileWrapper>();
        for (IntegrationProfile ip : integrationProfile) {
            integrationProfileWrapperList.add(new IntegrationProfileWrapper(ip.getId(), ip.getKeyword(), ip.getName(),
                    null, ip.getIntegrationProfileStatusType().getKeyword()));
        }
        IntegrationProfilesWrapper integrationProfilesWrapper = new IntegrationProfilesWrapper(
                integrationProfileWrapperList);

        integrationProfilesWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());

        return integrationProfilesWrapper;
    }

    @Override
    public IntegrationProfileWrapper getIntegrationProfile(String id) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfile");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        IntegrationProfile ip = em.find(IntegrationProfile.class, Integer.valueOf(id));
        IntegrationProfileWrapper ipWrapper = new IntegrationProfileWrapper(ip.getId(), ip.getKeyword(), ip.getName(),
                ip.getDescription(), ip.getIntegrationProfileStatusType().getKeyword());
        ipWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());
        return ipWrapper;
    }
}
