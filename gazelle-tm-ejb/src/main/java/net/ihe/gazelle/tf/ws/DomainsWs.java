package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.model.DomainQuery;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessageQuery;
import net.ihe.gazelle.tf.model.constraints.AipoRuleQuery;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestStatus;
import net.ihe.gazelle.tm.ws.data.domain.DomainsWrapper;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

@Stateless
@Name("domainsWs")
public class DomainsWs implements DomainsWsApi {
    private static final Logger LOG = LoggerFactory.getLogger(DomainsWs.class);

    @Override
    public DomainsWrapper getDomains() throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomains");
        }
        DomainQuery query = new DomainQuery();
        query.keyword().order(true);
        return new DomainsWrapper(query.keyword().getListDistinct());
    }

    @Override
    public DomainsWrapper getDomainsWithTests() throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainsWithTests");
        }
        TestQuery query = new TestQuery();
        query.testStatus().keyword().eq(TestStatus.STATUS_READY_STRING);
        return new DomainsWrapper(query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                .actorIntegrationProfile().integrationProfile().domainsForDP().keyword().getListDistinct());
    }

    @Override
    public DomainsWrapper getDomainsWithAuditMessage() throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainsWithAuditMessage");
        }
        AuditMessageQuery query = new AuditMessageQuery();
        return new DomainsWrapper(
                query.issuingActor().actorIntegrationProfiles().integrationProfile().domainsForDP().keyword()
                        .getListDistinct());
    }

    @Override
    public DomainsWrapper getDomainsWithRules() throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainsWithRules");
        }
        AipoRuleQuery query = new AipoRuleQuery();
        return new DomainsWrapper(query.aipoRules().integrationProfile().domainsForDP().keyword().getListDistinct());
    }

}
