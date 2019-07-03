/*
 * Copyright 2009 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.documents;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.util.DateDisplayUtil;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.paths.HQLSafePathBasicDate;
import net.ihe.gazelle.tf.model.Document;
import net.ihe.gazelle.tf.model.DocumentQuery;
import net.ihe.gazelle.tf.model.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class DocumentFilter extends Filter<Document> {

    private static final long serialVersionUID = 6630032466464757437L;
    private static final Logger LOG = LoggerFactory.getLogger(DocumentFilter.class);
    List<Domain> selectedDomains;

    public DocumentFilter(Map<String, String> requestParameterMap, QueryModifier<Document>... queryModifiers) {
        super(getHQLCriterions(queryModifiers), requestParameterMap);
    }

    private static HQLCriterionsForFilter<Document> getHQLCriterions(QueryModifier<Document>[] queryModifiers) {

        DocumentQuery query = new DocumentQuery();
        HQLCriterionsForFilter<Document> result = query.getHQLCriterionsForFilter();

        result.addPath("lifecycleStatus", query.lifecyclestatus());
        result.addPath("type", query.type());
        result.addPath("domain", query.domain());
        result.addPath("revision", query.revision());
        result.addPath("title", query.title());
        result.addPath("volume", query.volume());
        result.addPath("name", query.name());

        HQLSafePathBasicDate<Date> isDate = query.dateOfpublication();
        isDate.setCriterionWithoutTime(DateDisplayUtil.getTimeZone());
        result.addPath("dateOfPublication", isDate);

        for (QueryModifier<Document> queryModifier : queryModifiers) {
            result.addQueryModifier(queryModifier);
        }

        return result;
    }

    @Override
    public void appendHibernateFilters(HQLQueryBuilder<Document> queryBuilder, String excludedKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendHibernateFilters");
        }
        super.appendHibernateFilters(queryBuilder, excludedKeyword);
        if (selectedDomains != null) {
            if (!selectedDomains.isEmpty()) {
                DocumentQuery query = new DocumentQuery(queryBuilder);

                HQLRestriction documentRestriction;
                for (Domain domain : selectedDomains) {
                    documentRestriction = query.domain().id().eqRestriction(domain.getId());
                    queryBuilder.addRestriction(documentRestriction);
                }
            }
        }
    }

}
