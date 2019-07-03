package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.dao.StandardDAO;
import net.ihe.gazelle.tf.model.NetworkCommunicationType;
import net.ihe.gazelle.tf.model.Standard;
import net.ihe.gazelle.tf.model.StandardQuery;
import net.ihe.gazelle.tf.model.Transaction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("standardManager")
@Scope(ScopeType.PAGE)
public class StandardManager implements Serializable {

    private static final long serialVersionUID = 3258279832612621115L;
    private static final List<SelectItem> communicationTypes;
    private static final Logger LOG = LoggerFactory.getLogger(StandardManager.class);

    static {
        communicationTypes = new ArrayList<SelectItem>();
        for (NetworkCommunicationType type : NetworkCommunicationType.values()) {
            communicationTypes.add(new SelectItem(type, type.getLabel()));
        }
    }

    private Filter<Standard> filter;
    private Standard selectedStandard;
    private Transaction selectedTransaction;

    public Filter<Standard> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestParameterMap();
            filter = new Filter<Standard>(getHQLCriterionsForFilter(), requestParameterMap);
        }
        return filter;
    }

    public FilterDataModel<Standard> getStandards() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStandards");
        }
        return new FilterDataModel<Standard>(getFilter()) {
            @Override
            protected Object getId(Standard t) {
                // TODO Auto-generated method stub
                return t.getId();
            }
        };
    }

    public void reset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reset");
        }
        getFilter().clear();
    }

    private HQLCriterionsForFilter<Standard> getHQLCriterionsForFilter() {
        StandardQuery query = new StandardQuery();
        HQLCriterionsForFilter<Standard> criteria = query.getHQLCriterionsForFilter();
        criteria.addPath("keyword", query.keyword());
        criteria.addPath("name", query.name());
        criteria.addPath("network", query.networkCommunicationType());
        return criteria;
    }

    public Standard getSelectedStandard() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedStandard");
        }
        if (this.selectedStandard == null) {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestParameterMap();
            String keyword = params.get("keyword");
            if (keyword != null) {
                selectedStandard = StandardDAO.getStandardByKeyword(keyword);
                if (selectedStandard == null) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, keyword + " does not reference a known standard");
                }
            } else {
                selectedStandard = new Standard();
            }
        }
        return selectedStandard;
    }

    public void setSelectedStandard(Standard selectedStandard) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedStandard");
        }
        this.selectedStandard = selectedStandard;
    }

    public void delete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete");
        }
        try {
            EntityManager em = EntityManagerService.provideEntityManager();
            selectedStandard = StandardDAO.getStandardByKeyword(selectedStandard.getKeyword());
            em.remove(selectedStandard);
            em.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "The standard has been deleted");
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This standard cannot be deleted, it might be referenced by at least one " +
                    "transaction");
        }
        getFilter().modified();
    }

    public void save() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("save");
        }
        selectedStandard = StandardDAO.save(selectedStandard);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Your changes have been saved");
    }

    public String show(String keyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("show");
        }
        return "/tf/standard/show.xhtml?keyword=" + keyword;
    }

    public String edit(String keyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("edit");
        }
        return "/tf/standard/edit.xhtml?keyword=" + keyword;
    }

    public List<SelectItem> getCommunicationTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCommunicationTypes");
        }
        return communicationTypes;
    }

    public Transaction getSelectedTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTransaction");
        }
        if (this.selectedTransaction == null) {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestParameterMap();
            String transactionId = params.get("transactionId");
            if (transactionId != null) {
                try {
                    Integer id = Integer.valueOf(transactionId);
                    selectedTransaction = EntityManagerService.provideEntityManager().find(Transaction.class, id);
                } catch (NumberFormatException e) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, transactionId + " is not a positive integer");
                }
            }
        }
        return selectedTransaction;
    }

    public void setSelectedTransaction(Transaction selectedTransaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTransaction");
        }
        this.selectedTransaction = selectedTransaction;
    }

    public void addStandardToTransaction(Standard inStandard) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addStandardToTransaction");
        }
        if (selectedTransaction != null && inStandard != null) {
            if (!selectedTransaction.getStandards().contains(inStandard)) {
                selectedTransaction.getStandards().add(inStandard);
                selectedTransaction.saveOrMerge(EntityManagerService.provideEntityManager());
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Standard " + inStandard.getKeyword() + " has been added to transaction "
                        + selectedTransaction.getKeyword());
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Standard " + inStandard.getKeyword() + " and transaction " +
                        selectedTransaction.getKeyword() + " are already linked");
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Either the transaction or the standard is not defined");
        }
    }

    public void deleteStandardFromTransaction(Standard inStandard) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteStandardFromTransaction");
        }
        if (selectedTransaction != null && inStandard != null) {
            selectedTransaction.getStandards().remove(inStandard);
            selectedTransaction.saveOrMerge(EntityManagerService.provideEntityManager());
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Either the transaction or the standard is not defined");
        }
    }
}
