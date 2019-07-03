package net.ihe.gazelle.pr.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.pr.bean.AdminStatus;
import net.ihe.gazelle.pr.bean.CrawlType;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.systems.model.IntegrationStatementStatus;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;

@Name("prAdmin")
@Scope(ScopeType.PAGE)
public class PRAdmin implements Serializable, QueryModifier<SystemInSession> {

    private static final long serialVersionUID = -8196621065574717481L;
    private static final Logger LOG = LoggerFactory.getLogger(PRAdmin.class);
    private Filter<SystemInSession> filter;
    private FilterDataModel<SystemInSession> systemsInSession;

    private AdminStatus status = AdminStatus.NEED_VERIFICATION;

    private IntegrationStatementStatus currentStatus = null;

    private String currentComment = null;

    private SystemInSession selectedSystemInSession;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

        String systemId = requestParameterMap.get("systemId");
        if (systemId != null) {
            selectedSystemInSession = EntityManagerService.provideEntityManager().find(SystemInSession.class,
                    Integer.parseInt(systemId));
        }
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
    }

    public void checkSystem(System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkSystem");
        }
        PRCrawlerManagerLocal prCrawlerManager = (PRCrawlerManagerLocal) Component.getInstance("prCrawlerManager");
        prCrawlerManager.crawlSystem(system, CrawlType.ADMIN);

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(system);
        entityManager.flush();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "System checked");
    }

    public void updateSystemStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSystemStatus");
        }
        PRStatusManagerLocal prStatusManager = (PRStatusManagerLocal) Component.getInstance("prStatusManager");
        prStatusManager.newAdminStatus(selectedSystemInSession.getSystem(), getCurrentStatus(), getCurrentComment());
        goNext();
    }

    public boolean hasNext() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasNext");
        }
        return (getNextSystemId() != null);
    }

    public void goNext() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("goNext");
        }
        Integer systemId = getNextSystemId();

        if (systemId != null) {
            Redirect redirect = new Redirect();
            redirect.setViewId("/pr/reviewSystem.xhtml");
            redirect.setParameter("systemId", systemId);
            redirect.execute();
        } else {
            // should not happen
            Redirect redirect = new Redirect();
            redirect.setViewId("/pr/systemsForAdmin.xhtml");
            redirect.execute();
        }
    }

    private Integer getNextSystemId() {
        Integer systemId = null;
        List<Integer> systemIdsToReview = (List<Integer>) Contexts.getSessionContext().get("systemIdsToReview");
        if ((systemIdsToReview != null) && (selectedSystemInSession != null)
                && (selectedSystemInSession.getId() != null)) {
            int indexOf = systemIdsToReview.indexOf(selectedSystemInSession.getId());
            if (indexOf >= 0) {
                indexOf = indexOf + 1;
                if (indexOf < systemIdsToReview.size()) {
                    systemId = systemIdsToReview.get(indexOf);
                }
            }
        }
        return systemId;
    }

    public void reviewSystem(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reviewSystem");
        }
        HQLQueryBuilder<SystemInSession> queryBuilder = getSystemsInSession().prepareQueryBuilder(
                FacesContext.getCurrentInstance(), true);
        List<Object[]> idsList = (List<Object[]>) queryBuilder.getMultiSelect("id");
        Integer list2[] = new Integer[idsList.size()];
        list2 = idsList.toArray(list2);
        List ids = new ArrayList<Integer>();
        for (Integer object : list2) {
            ids.add(object);
        }
        Contexts.getSessionContext().set("systemIdsToReview", ids);

        Redirect redirect = new Redirect();
        redirect.setViewId("/pr/reviewSystem.xhtml");
        redirect.setParameter("systemId", systemInSession.getId());
        redirect.execute();
    }

    public AdminStatus[] getStatuses() {
        return AdminStatus.values();
    }

    public IntegrationStatementStatus getCurrentStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentStatus");
        }
        if (currentStatus == null) {
            currentStatus = selectedSystemInSession.getSystem().getPrStatus();
        }
        return currentStatus;
    }

    public void setCurrentStatus(IntegrationStatementStatus currentStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCurrentStatus");
        }
        this.currentStatus = currentStatus;
    }

    public String getCurrentComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentComment");
        }
        if (currentComment == null) {
            currentComment = selectedSystemInSession.getSystem().getPrCommentValidatedByAdmin();
        }
        return currentComment;
    }

    public void setCurrentComment(String currentComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCurrentComment");
        }
        this.currentComment = currentComment;
    }

    public IntegrationStatementStatus[] getPossibleStatusesForSystem(System system) {
        Set<IntegrationStatementStatus> statuses = new TreeSet<IntegrationStatementStatus>(
                Arrays.asList(IntegrationStatementStatus.adminStatuses));
        statuses.add(getCurrentStatus());
        return statuses.toArray(new IntegrationStatementStatus[statuses.size()]);
    }

    public AdminStatus getStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus");
        }
        return status;
    }

    public void setStatus(AdminStatus status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setStatus");
        }
        this.status = status;
        getFilter().modified();
    }

    public Filter<SystemInSession> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            filter = new Filter<SystemInSession>(getHQLCriterions(), requestParameterMap);
        }
        return filter;
    }

    private HQLCriterionsForFilter<SystemInSession> getHQLCriterions() {
        SystemInSessionQuery query = new SystemInSessionQuery();
        HQLCriterionsForFilter<SystemInSession> result = query.getHQLCriterionsForFilter();

        result.addPath("institution", query.system().institutionSystems().institution());
        result.addPath("system", query.system().name());
        result.addPath("status", query.system().prStatus());

        TMCriterions.addAIPOCriterions(result, query.system().systemActorProfiles().actorIntegrationProfileOption());

        result.addQueryModifier(this);

        return result;
    }

    public FilterDataModel<SystemInSession> getSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsInSession");
        }
        if (systemsInSession == null) {
            systemsInSession = new FilterDataModel<SystemInSession>(getFilter()) {
                @Override
                protected Object getId(SystemInSession t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return systemsInSession;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<SystemInSession> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        if (status != null) {
            SystemInSessionQuery query = new SystemInSessionQuery(queryBuilder);
            switch (status) {
                case REFERENCED:
                    queryBuilder
                            .addRestriction(IntegrationStatementStatus.systemIntegrationStatementVisible(query.system()));
                    break;
                case NOT_REFERENCED:
                    queryBuilder.addRestriction(HQLRestrictions.not(IntegrationStatementStatus
                            .systemIntegrationStatementVisible(query.system())));
                    break;
                case WILL_BE_CRAWLED:
                    queryBuilder.addRestriction(IntegrationStatementStatus.systemIntegrationStatementNeedCrawling(query
                            .system()));
                    break;
                case NEED_VERIFICATION:
                    queryBuilder.addRestriction(IntegrationStatementStatus
                            .systemIntegrationStatementWaitingForValidation(query.system()));
                    break;
            }
        }
    }
}
