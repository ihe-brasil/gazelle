/*
 * Copyright 2008 IHE International (http://www.ihe.net)
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
package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.tf.model.ProfileLink;
import net.ihe.gazelle.tf.model.TransactionOptionType;
import net.ihe.gazelle.tf.model.TransactionOptionTypeQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

@Name("transactionOptionTypeManager")
@Scope(ScopeType.PAGE)
public class TransactionOptionTypeManager implements Serializable {


    private static final Logger LOG = LoggerFactory.getLogger(TransactionOptionTypeManager.class);

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1357012043456235100L;

    @In
    private EntityManager entityManager;

    private TransactionOptionType selectedTransactionOptionType;
    private Filter<TransactionOptionType> filter;


    public Filter<TransactionOptionType> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            TransactionOptionTypeQuery query = new TransactionOptionTypeQuery();
            filter = new Filter<TransactionOptionType>(query.getHQLCriterionsForFilter());
        }
        return filter;
    }

    public FilterDataModel<TransactionOptionType> getTransactionOptionTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionOptionTypes");
        }
        return new FilterDataModel<TransactionOptionType>(getFilter()) {
            @Override
            protected Object getId(TransactionOptionType transactionOptionType) {
                return transactionOptionType.getId();
            }
        };
    }


    public List<TransactionOptionType> getTransactionOptionTypesList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionOptionTypesList");
        }
        return getTransactionOptionTypes().getAllItems(FacesContext.getCurrentInstance());
    }

    public TransactionOptionType getSelectedTransactionOptionType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTransactionOptionType");
        }
        return selectedTransactionOptionType;
    }

    public void setSelectedTransactionOptionType(TransactionOptionType selectedTransactionOptionType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTransactionOptionType");
        }
        this.selectedTransactionOptionType = selectedTransactionOptionType;
    }

    public String listTransactionOptionTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTransactionOptionTypes");
        }
        return "/tf/transactionOptionType/listTransactionOptionTypes.seam";
    }

    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void saveModifications() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveModifications");
        }
        if (this.selectedTransactionOptionType != null) {
            TransactionOptionTypeQuery query = new TransactionOptionTypeQuery();
            query.keyword().eq(selectedTransactionOptionType.getKeyword());
            if (selectedTransactionOptionType.getId() != null) {
                query.id().neq(selectedTransactionOptionType.getId());
            }
            if (query.getCount() > 0) {
                FacesMessages
                        .instance()
                        .add(StatusMessage.Severity.ERROR, "The specified keyword is used by an other Transaction Option Type. Choose an other one.");
                return;
            } else {
                this.selectedTransactionOptionType = entityManager.merge(this.selectedTransactionOptionType);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "The transactionOptionType has been saved.");
            }
        }
    }

    public void initializeSelectedTransactionOptionType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeSelectedTransactionOptionType");
        }
        this.selectedTransactionOptionType = new TransactionOptionType();
    }

    public void deleteSelectedTransactionOptionType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedTransactionOptionType");
        }
        if (this.selectedTransactionOptionType != null) {
            int numbpl = this.getNumberlistProfileLinkRelatedToSelectedTransactionOptionType();
            if (numbpl > 0) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                        "This Transaction Option Type is used on a profile link. Delete these profiles link first.");
                return;
            }
            selectedTransactionOptionType = entityManager.find(TransactionOptionType.class,
                    selectedTransactionOptionType.getId());
            entityManager.remove(selectedTransactionOptionType);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO,
                    "This Transaction Option Type has been removed !");
        }
    }

    public int getNumberlistProfileLinkRelatedToSelectedTransactionOptionType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberlistProfileLinkRelatedToSelectedTransactionOptionType");
        }
        if (this.selectedTransactionOptionType != null) {
            return ProfileLink.getNumberProfileLinksForTransactionOptionType(selectedTransactionOptionType);
        }
        return 0;
    }

}
