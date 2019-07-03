package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.configurations.model.OIDRootDefinitionLabel;
import net.ihe.gazelle.tm.configurations.model.OIDRootDefinitionLabelQuery;
import net.ihe.gazelle.tm.configurations.model.OIDRootDefinitionQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * @author abderrazek boufahja
 */

@Scope(ScopeType.SESSION)
@Name("oidLabelManager")
@GenerateInterface("OIDLabelManagerLocal")
public class OIDLabelManager implements Serializable, OIDLabelManagerLocal {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(OIDLabelManager.class);

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    private Filter<OIDRootDefinitionLabel> filter;
    private OIDRootDefinitionLabel selectedOIDRootDefinitionLabel;

    @Override
    public List<IntegrationProfile> getPossibleIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfiles");
        }
        return IntegrationProfile.getAllIntegrationProfile();
    }


    @Override
    public OIDRootDefinitionLabel getSelectedOIDRootDefinitionLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedOIDRootDefinition");
        }
        return selectedOIDRootDefinitionLabel;
    }

    @Override
    public void setSelectedOIDRootDefinitionLabel(OIDRootDefinitionLabel selectedOIDRootDefinitionLabel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedOIDRootDefinition");
        }
        this.selectedOIDRootDefinitionLabel = selectedOIDRootDefinitionLabel;
    }

    @Override
    public void initializeOIDRootDefinitionLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeOIDRootDefinition");
        }
        this.selectedOIDRootDefinitionLabel = new OIDRootDefinitionLabel();
    }

    @Override
    public void saveOIDRootDefinitionLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveOIDRootDefinitionLabel");
        }
        if (this.selectedOIDRootDefinitionLabel != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            this.selectedOIDRootDefinitionLabel = em.merge(this.selectedOIDRootDefinitionLabel);
            em.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "The selected OID Root Definition label has been saved.");
        }
    }

    @Override
    public void deleteOIDRootDefinitionLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteOIDRootDefinitionLabel");
        }
        if (this.selectedOIDRootDefinitionLabel != null) {
            if (this.selectedOIDRootDefinitionLabel.getId() != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                this.selectedOIDRootDefinitionLabel = em.find(OIDRootDefinitionLabel.class, this.selectedOIDRootDefinitionLabel.getId());
                em.remove(this.selectedOIDRootDefinitionLabel);
                em.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "The selected OID Root Definition label has been deleted.");
            }
        }
    }

    @Override
    public boolean canDeleteSelectedOIDRootDefinitionLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canDeleteSelectedOIDRootDefinitionLabel");
        }
        if (this.selectedOIDRootDefinitionLabel != null) {
            if (this.selectedOIDRootDefinitionLabel.getId() != null) {
                OIDRootDefinitionQuery q = new OIDRootDefinitionQuery();
                q.label().id().eq(selectedOIDRootDefinitionLabel.getId());
                return q.getListNullIfEmpty() == null;
            }
        }
        return true;
    }

    @Override
    public Filter<OIDRootDefinitionLabel> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter<OIDRootDefinitionLabel>(getHQLCriterions());
        }
        return filter;
    }

    private HQLCriterionsForFilter<OIDRootDefinitionLabel> getHQLCriterions() {
        OIDRootDefinitionLabelQuery query = new OIDRootDefinitionLabelQuery();
        return query.getHQLCriterionsForFilter();
    }

    @Override
    public FilterDataModel<OIDRootDefinitionLabel> getOIDRootsLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOIDRootsLabel");
        }
        return new FilterDataModel<OIDRootDefinitionLabel>(getFilter()) {
            @Override
            protected Object getId(OIDRootDefinitionLabel oidRootDefinitionLabel) {
                return oidRootDefinitionLabel.getId();
            }
        };
    }

    @Override
    public List<OIDRootDefinitionLabel> getLabels() {
        OIDRootDefinitionLabelQuery q = new OIDRootDefinitionLabelQuery();
        q.label().order(true);
        return q.getListDistinctOrdered();
    }

    @Override
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
