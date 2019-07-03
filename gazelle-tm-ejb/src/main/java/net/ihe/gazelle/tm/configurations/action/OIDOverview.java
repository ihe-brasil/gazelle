package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tm.configurations.model.OIDSystemAssignment;
import net.ihe.gazelle.tm.configurations.model.OIDSystemAssignmentQuery;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Name("oidOverview")
@Scope(ScopeType.PAGE)
public class OIDOverview implements Serializable {

    private static final long serialVersionUID = 8265234618185584650L;
    private static final Logger LOG = LoggerFactory.getLogger(OIDOverview.class);
    private Filter<OIDSystemAssignment> filter = null;

    private FilterDataModel<OIDSystemAssignment> listOSA;

    private OIDSystemAssignment selectedOIDSystemAssignement;

    public OIDSystemAssignment getSelectedOIDSystemAssignement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedOIDSystemAssignement");
        }
        return selectedOIDSystemAssignement;
    }

    public void setSelectedOIDSystemAssignement(OIDSystemAssignment selectedOIDSystemAssignement) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedOIDSystemAssignement");
        }
        this.selectedOIDSystemAssignement = selectedOIDSystemAssignement;
    }

    public void saveSelectedOIDSystemAssignement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveSelectedOIDSystemAssignement");
        }
        if (this.selectedOIDSystemAssignement != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            this.selectedOIDSystemAssignement = em.merge(this.selectedOIDSystemAssignement);
            em.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "The selected OID was saved.");
        }
    }

    public void setSelectedSystemInSession(SystemInSession systemInSessionToUse) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        getFilter().getFilterValues().put("system", systemInSessionToUse);
    }

    public void setSelectedActor(Actor actor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        getFilter().getFilterValues().put("actor", actor);
    }

    public Filter<OIDSystemAssignment> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

            filter = new Filter<OIDSystemAssignment>(getCriterions(), requestParameterMap);
        }
        return filter;
    }

    private HQLCriterionsForFilter<OIDSystemAssignment> getCriterions() {
        OIDSystemAssignmentQuery query = new OIDSystemAssignmentQuery();
        HQLCriterionsForFilter<OIDSystemAssignment> criterions = query.getHQLCriterionsForFilter();

        TMCriterions.addTestingSession(criterions, "testingSession", query.systemInSession().testingSession());

        criterions.addPath("institution", query.systemInSession().system().institutionSystems().institution());
        criterions.addPath("system", query.systemInSession());

        TMCriterions.addAIPOCriterions(criterions, query.oidRequirement().actorIntegrationProfileOptionList());

        criterions.addPath("label", query.oidRequirement().label());

        criterions.addPath("oid", query.oid());

        return criterions;
    }

    public FilterDataModel<OIDSystemAssignment> getListOSA() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOSA");
        }
        if (listOSA == null) {
            listOSA = new FilterDataModel<OIDSystemAssignment>(getFilter()) {
                @Override
                protected Object getId(OIDSystemAssignment t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return listOSA;
    }

    public boolean currentUserCanEditOID(OIDSystemAssignment currentOSA) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("currentUserCanEditOID");
        }
        if (currentOSA != null) {
            User usr = User.loggedInUser();

            if (currentOSA.getSystemInSession().getTestingSession().testingSessionClosedForUser()) {
                return false;
            }

            Institution ins = usr.getInstitution();

            OIDSystemAssignmentQuery oidSystemAssignmentQuery = new OIDSystemAssignmentQuery();
            oidSystemAssignmentQuery.id().eq(currentOSA.getId());
            List<Institution> institutions = oidSystemAssignmentQuery.systemInSession().system().institutionSystems()
                    .institution().getListDistinct();
            if ((institutions.size() > 0) && (ins != null)) {
                for (Institution institution : institutions) {
                    if (institution.getKeyword().equals(ins.getKeyword())) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
