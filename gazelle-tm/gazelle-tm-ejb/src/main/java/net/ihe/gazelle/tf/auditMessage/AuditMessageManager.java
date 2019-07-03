package net.ihe.gazelle.tf.auditMessage;

import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.documents.DocumentSectionsFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.preferences.PreferenceService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessage;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessageQuery;
import net.ihe.gazelle.tf.model.auditMessage.TriggerEvent;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Name("auditMessageManager")
@Scope(ScopeType.PAGE)
public class AuditMessageManager implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(AuditMessageManager.class);
    private FilterDataModel<AuditMessage> auditMessages;
    private AuditMessageFilter filter;
    private AuditMessage currentAuditMessage = null;
    private DocumentSectionsFilter documentSectionFilter;
    private FilterDataModel<DocumentSection> documentSections;
    private String auditMessageSpecificationBaseUrl;
    ;

    public FilterDataModel<AuditMessage> getAuditMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessages");
        }
        if (auditMessages == null) {
            auditMessages = new FilterDataModel<AuditMessage>(getFilter()) {
                @Override
                protected Object getId(AuditMessage t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return auditMessages;
    }

    public String show(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("show");
        }
        return "/tf/auditMessage/show.xhtml?id=" + id;
    }

    public String edit(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("edit");
        }
        return "/tf/auditMessage/edit.xhtml?id=" + id;
    }

    public String cancel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancel");
        }
        if (getCurrentAuditMessage().getId() != null) {
            return show(getCurrentAuditMessage().getId());
        } else {
            return "index.xhtml";
        }
    }

    public String save() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("save");
        }
        if (getCurrentAuditMessage() != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            setCurrentAuditMessage(em.merge(getCurrentAuditMessage()));
        }
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Audit message has been saved");
        return show(getCurrentAuditMessage().getId());
    }

    public AuditMessage getCurrentAuditMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentAuditMessage");
        }
        if (currentAuditMessage == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            String id = fc.getExternalContext().getRequestParameterMap().get("id");
            if (id == null) {
                id = fc.getExternalContext().getRequestParameterMap().get("auditMessageId");
            }
            if (id != null) {
                findAuditMessage(id);
            } else {
                currentAuditMessage = new AuditMessage();
            }
        }
        return currentAuditMessage;
    }

    public void setCurrentAuditMessage(AuditMessage message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCurrentAuditMessage");
        }
        currentAuditMessage = message;
    }

    private void findAuditMessage(String id) {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        AuditMessageQuery query = new AuditMessageQuery(entityManager);
        query.id().eq(Integer.valueOf(id));
        setCurrentAuditMessage(query.getUniqueResult());
    }

    public List<Actor> getIssuingActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIssuingActors");
        }
        ActorQuery actorQuery = new ActorQuery();
        actorQuery.keyword().order(true);
        return actorQuery.getList();
    }

    public List<Transaction> getTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactions");
        }
        if (getCurrentAuditMessage() != null) {
            return ProfileLink.getTransactionForActor(getCurrentAuditMessage().getIssuingActor());
        } else {
            return ProfileLink.getTransactionForActor(null);
        }
    }

    @SuppressWarnings("unchecked")
    public AuditMessageFilter getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            final Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            filter = new AuditMessageFilter(requestParameterMap);
        }
        return filter;
    }

    public void deleteSection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSection");
        }
        getCurrentAuditMessage().setDocumentSection(null);
        try {
            EntityManager em = EntityManagerService.provideEntityManager();
            setCurrentAuditMessage(em.merge(getCurrentAuditMessage()));
            em.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "The document section has been removed");
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This section cannot be deleted");
        }

    }

    public void delete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete");
        }
        try {
            EntityManager em = EntityManagerService.provideEntityManager();
            setCurrentAuditMessage(em.find(AuditMessage.class, getCurrentAuditMessage().getId()));
            em.remove(getCurrentAuditMessage());
            em.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "The audit message has been deleted");
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This audit message  cannot be deleted");
        }
        getFilter().modified();
    }

    public String addSectionPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSectionPage");
        }
        return "/tf/auditMessage/addDocumentSection.xhtml?auditMessageId=" + getCurrentAuditMessage().getId();
    }

    public DocumentSectionsFilter getDocumentSectionFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentSectionFilter");
        }
        if (documentSectionFilter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            documentSectionFilter = new DocumentSectionsFilter(requestParameterMap);
        }
        return documentSectionFilter;
    }

    public FilterDataModel<DocumentSection> getDocumentSections() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentSections");
        }
        if (documentSections == null) {
            documentSections = new FilterDataModel<DocumentSection>(getDocumentSectionFilter()) {
                @Override
                protected Object getId(DocumentSection t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return documentSections;
    }

    public String addThisSection(AuditMessage auditMessage, DocumentSection documentSection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addThisSection");
        }
        auditMessage.setDocumentSection(documentSection);
        try {
            EntityManager em = EntityManagerService.provideEntityManager();
            auditMessage = em.merge(auditMessage);
            em.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Document section added");
        } catch (Throwable e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Cannot link this document section");
        }
        return edit(auditMessage.getId());
    }

    public SelectItem[] triggerEventList() {
        final List<TriggerEvent> events = Arrays.asList(TriggerEvent.values());
        final List<SelectItem> result = new ArrayList<SelectItem>();
        for (TriggerEvent event : events) {
            result.add(new SelectItem(event, event.getKeyword()));
        }
        return result.toArray(new SelectItem[result.size()]);
    }

    public String getAuditMessageSpecificationBaseUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessageSpecificationBaseUrl");
        }
        if (auditMessageSpecificationBaseUrl == null) {
            auditMessageSpecificationBaseUrl = PreferenceService.getString("atna_msg_specification_url");
            if (auditMessageSpecificationBaseUrl != null) {
                auditMessageSpecificationBaseUrl = auditMessageSpecificationBaseUrl + "/amview/auditMessage.seam?oid=";
            }
        }
        return auditMessageSpecificationBaseUrl;
    }

    public String getAuditMessageSpecUrl(AuditMessage msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessageSpecUrl");
        }
        if (msg != null && msg.getOid() != null && getAuditMessageSpecificationBaseUrl() != null) {
            return auditMessageSpecificationBaseUrl + msg.getOid();
        } else {
            return null;
        }
    }
}
