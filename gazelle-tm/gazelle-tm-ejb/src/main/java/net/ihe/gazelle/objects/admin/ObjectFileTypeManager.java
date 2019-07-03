package net.ihe.gazelle.objects.admin;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.objects.model.ObjectFileType;
import net.ihe.gazelle.objects.model.ObjectFileTypeQuery;
import net.ihe.gazelle.objects.model.ObjectType;
import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author abderrazek boufahja
 */
public class ObjectFileTypeManager {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectFileTypeManager.class);

    protected ObjectFileType selectedObjectFileType;

    protected String messageForDeleteOFT;

    // getter and setter ///////////////////////////////////////
    private Filter listObjectFileTypeFilter;

    public String getMessageForDeleteOFT() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageForDeleteOFT");
        }
        return messageForDeleteOFT;
    }

    public void setMessageForDeleteOFT(String messageForDeleteOFT) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageForDeleteOFT");
        }
        this.messageForDeleteOFT = messageForDeleteOFT;
    }

    public ObjectFileType getSelectedObjectFileType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectFileType");
        }
        return selectedObjectFileType;
    }

    // methods /////////////////////////////////////////////////

    public void setSelectedObjectFileType(ObjectFileType selectedObjectFileType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectFileType");
        }
        this.selectedObjectFileType = selectedObjectFileType;
    }

    public FilterDataModel<ObjectFileType> getListObjectFileType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectFileType");
        }
        return new FilterDataModel<ObjectFileType>(getListObjectFileTypeFilter()) {
            @Override
            protected Object getId(ObjectFileType objectFileType) {
                return objectFileType.getId();
            }
        };
    }

    private Filter<ObjectFileType> getListObjectFileTypeFilter() {
        if (listObjectFileTypeFilter == null) {
            ObjectFileTypeQuery q = new ObjectFileTypeQuery();
            HQLCriterionsForFilter<ObjectFileType> result = q.getHQLCriterionsForFilter();
            this.listObjectFileTypeFilter = new Filter<ObjectFileType>(result);
        }
        return this.listObjectFileTypeFilter;
    }

    public void initializeSelectedObjectFileType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeSelectedObjectFileType");
        }
        this.selectedObjectFileType = new ObjectFileType();
        this.selectedObjectFileType.setWritable(false);
    }

    @Restrict("#{s:hasPermission('ObjectFileTypeManager', 'mergeSelectedObjectFileType', null)}")
    public void mergeSelectedObjectFileType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeSelectedObjectFileType");
        }
        this.persistObjectFileType(selectedObjectFileType);
    }

    @Restrict("#{s:hasPermission('ObjectFileTypeManager', 'generateMessageForDeleteObjectFileType', null)}")
    public void generateMessageForDeleteObjectFileType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateMessageForDeleteObjectFileType");
        }
        this.messageForDeleteOFT = null;
        if (this.selectedObjectFileType != null) {
            if (this.selectedObjectFileType.getId() != null) {
                List<ObjectType> lot = ObjectType.getObjectTypeFiltered(null, null, null, null, null);
                if (lot != null) {
                    if (lot.size() > 0) {
                        String label = org.jboss.seam.core.ResourceBundle.instance().getString(
                                "gazelle.testmanagement.object.LabelForPb1");
                        this.messageForDeleteOFT = " - " + label + " : ";
                        for (ObjectType objectType : lot) {
                            this.messageForDeleteOFT = this.messageForDeleteOFT
                                    + StringEscapeUtils.escapeHtml(objectType.getKeyword());
                            if (lot.indexOf(objectType) < (lot.size() - 1)) {
                                this.messageForDeleteOFT += ", ";
                            }
                        }
                    }
                }
            }
        }
    }

    @Restrict("#{s:hasPermission('ObjectFileTypeManager', 'deleteSelectedObjectFileType', null)}")
    public void deleteSelectedObjectFileType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedObjectFileType");
        }
        if (this.selectedObjectFileType != null) {
            if (this.selectedObjectFileType.getId() != null) {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                ObjectFileType objectFileTypeToRemove = entityManager.find(ObjectFileType.class,
                        selectedObjectFileType.getId());
                entityManager.remove(objectFileTypeToRemove);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.objFiletypeselecteddeleted']}");
            }
        }
    }

    @Restrict("#{s:hasPermission('ObjectFileTypeManager', 'persistObjectFileType', null)}")
    public void persistObjectFileType(ObjectFileType oft) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistObjectFileType");
        }
        if (oft != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(oft);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.OFTWasPersisted']}");
        }
    }

}
