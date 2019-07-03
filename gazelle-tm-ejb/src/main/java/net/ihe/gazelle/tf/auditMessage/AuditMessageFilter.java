package net.ihe.gazelle.tf.auditMessage;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessage;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessageQuery;

import java.util.Map;

public class AuditMessageFilter extends Filter<AuditMessage> {

    /**
     *
     */
    private static final long serialVersionUID = 8759324442059827568L;

    public AuditMessageFilter(Map<String, String> requestParameterMap, QueryModifier<AuditMessage>... queryModifiers) {
        super(getHQLCriterions(queryModifiers), requestParameterMap);
    }

    private static HQLCriterionsForFilter<AuditMessage> getHQLCriterions(QueryModifier<AuditMessage>[] queryModifiers) {

        AuditMessageQuery query = new AuditMessageQuery();
        HQLCriterionsForFilter<AuditMessage> result = query.getHQLCriterionsForFilter();

        result.addPath("event", query.auditedEvent());
        result.addPath("transaction", query.auditedTransaction());
        result.addPath("actor", query.issuingActor());
        result.addPath("docSection", query.documentSection());
        result.addPath("oid", query.oid());

        for (QueryModifier<AuditMessage> queryModifier : queryModifiers) {
            result.addQueryModifier(queryModifier);
        }

        return result;
    }
}
