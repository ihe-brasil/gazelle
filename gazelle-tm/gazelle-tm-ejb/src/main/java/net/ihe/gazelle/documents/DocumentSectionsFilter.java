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
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.Document;
import net.ihe.gazelle.tf.model.DocumentSection;
import net.ihe.gazelle.tf.model.DocumentSectionQuery;
import net.ihe.gazelle.tf.model.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DocumentSectionsFilter extends Filter<DocumentSection> implements QueryModifier<DocumentSection> {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentSectionsFilter.class);
    private static final long serialVersionUID = -1356267683364128005L;
    List<Domain> selectedDomains;
    private Document selectedDocument;

    public DocumentSectionsFilter(Map<String, String> requestParameterMap) {
        super(getHQLCriterions(), requestParameterMap);
        queryModifiers.add(this);
    }

    private static HQLCriterionsForFilter<DocumentSection> getHQLCriterions() {

        DocumentSectionQuery query = new DocumentSectionQuery();
        HQLCriterionsForFilter<DocumentSection> result = query.getHQLCriterionsForFilter();

        result.addPath("document", query.document());
        result.addPath("domain", query.document().domain());
        result.addPath("type", query.type());
        result.addPath("section", query.section());
        result.addPath("document_type", query.document().type());

        return result;
    }

    public void setDomainRestriction(List<Domain> selectedDomains) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDomainRestriction");
        }
        this.selectedDomains = selectedDomains;
        modified();
    }

    public void setDocumentRestriction(Document document) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDocumentRestriction");
        }
        this.selectedDocument = document;
        modified();
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<DocumentSection> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        if (selectedDocument != null) {
            DocumentSectionQuery documentSectionQuery = new DocumentSectionQuery(queryBuilder);

            HQLRestriction documentRestriction = documentSectionQuery.document().id()
                    .eqRestriction(selectedDocument.getId());
            queryBuilder.addRestriction(documentRestriction);
        }
        if (selectedDomains != null) {
            if (!selectedDomains.isEmpty()) {
                DocumentSectionQuery query = new DocumentSectionQuery(queryBuilder);

                HQLRestriction domainRestriction;
                for (Domain domain : selectedDomains) {

                    domainRestriction = query.document().domain().id().eqRestriction(domain.getId());
                    queryBuilder.addRestriction(domainRestriction);
                }
            }
        }
    }
}
