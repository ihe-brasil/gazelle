package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.constraints.AipoRule;
import net.ihe.gazelle.tf.model.constraints.AipoRuleQuery;
import net.ihe.gazelle.tf.ws.data.RuleWrapper;
import net.ihe.gazelle.tf.ws.data.RulesWrapper;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("rulesWs")
public class RulesWs implements RulesWsApi {

    private static final Logger LOG = LoggerFactory.getLogger(RulesWs.class);

    @Override
    public RulesWrapper getRules() throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRules");
        }
        AipoRuleQuery query = new AipoRuleQuery();
        List<AipoRule> rules = query.getListDistinct();

        List<RuleWrapper> ruleWrapperList = new ArrayList<RuleWrapper>();

        for (AipoRule rule : rules) {
            ruleWrapperList.add(new RuleWrapper(rule.getName(), rule.getId()));
        }

        return new RulesWrapper(ruleWrapperList);
    }

    @Override
    public RulesWrapper getRules(String integrationProfileKeyword) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRules");
        }
        AipoRuleQuery query = new AipoRuleQuery();
        if (integrationProfileKeyword != null) {
            query.aipoRules().integrationProfile()
                    .eq(IntegrationProfile.findIntegrationProfileWithKeyword(integrationProfileKeyword));
        }
        List<AipoRule> rules = query.getListDistinct();

        List<RuleWrapper> ruleWrapperList = new ArrayList<RuleWrapper>();

        for (AipoRule rule : rules) {
            ruleWrapperList.add(new RuleWrapper(rule.getName(), rule.getId()));
        }

        RulesWrapper rulesWrapper = new RulesWrapper(ruleWrapperList);

        rulesWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());

        return rulesWrapper;
    }

    @Override
    public RuleWrapper getRule(String id) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRule");
        }

        RuleWrapper result;
        if (id != null) {
            AipoRuleQuery query = new AipoRuleQuery();
            query.id().eq(Integer.valueOf(id));
            AipoRule uniqueResult = query.getUniqueResult();
            result = new RuleWrapper(uniqueResult.getName(), uniqueResult.getId());
            result.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());
        } else {
            result = new RuleWrapper();
        }

        return result;
    }

    @Override
    public RuleWrapper getRulesWithConsequence(String integrationProfile, String actor) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRulesWithConsequence");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RuleWrapper getRulesWithCause(String integrationProfile, String actor) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRulesWithCause");
        }
        // TODO Auto-generated method stub
        return null;
    }
}
