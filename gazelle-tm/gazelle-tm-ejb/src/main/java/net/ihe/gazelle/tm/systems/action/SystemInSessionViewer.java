package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.configurations.model.HostQuery;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemQuery;
import net.ihe.gazelle.users.model.Institution;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.jboss.seam.ScopeType.PAGE;

@Name("systemInSessionViewer")
@Scope(PAGE)
@GenerateInterface("SystemInSessionViewerLocal")
public class SystemInSessionViewer extends AbstractSystemInSessionEditor implements Serializable, SystemInSessionViewerLocal {
    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionViewer.class);
    private static final String SEPARATOR = " - ";

    @In
    private EntityManager entityManager;
    private Filter<Host> filter;

    /**
     * Search the institution corresponding to the system. Used by JSF to render.
     *
     * @return String = Name of the institution(s) to display
     */
    public static String getInstitutionsForSystemStatic(System selectedSystem) {
        LOG.debug("String getInstitutionsForSystemStatic");
        SystemQuery query = new SystemQuery();
        query.id().eq(selectedSystem.getId());
        List<String> institutions = query.institutionSystems().institution().keyword().getListDistinct();
        return StringUtils.join(institutions, SEPARATOR);
    }

    public System getSystem() {
        return systemInSession.getSystem();
    }

    public FilterDataModel<Host> getHosts() {
        return new FilterDataModel<Host>(getFilter()) {
            @Override
            protected Object getId(Host host) {
                return host.getId();
            }
        };
    }

    private Filter<Host> getFilter() {
        if (filter == null) {
            HostQuery query = new HostQuery();
            HQLCriterionsForFilter<Host> result = query.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<Host>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<Host> hqlQueryBuilder, Map<String, Object> map) {
                    if (systemInSession != null) {
                        HostQuery q = new HostQuery();
//                        hqlQueryBuilder.addRestriction(q.configurations().systemInSession().id().eqRestriction(systemInSession.getId()));
                        List<Institution> institutions = System.getInstitutionsForASystem(systemInSession.getSystem());
                        hqlQueryBuilder.addRestriction(q.institution().inRestriction(institutions));
                        hqlQueryBuilder.addRestriction(q.testingSession().eqRestriction(systemInSession.getTestingSession()));
                    }
                }
            });
            this.filter = new Filter<Host>(result);
        }
        return this.filter;
    }

    public void setFilter(Filter<Host> filter) {
        this.filter = filter;
    }

    @Override
    public String getSystemInSessionKeywordBase() {
        return null;
    }

    @Override
    @Create
    public void init() {
        final String systemInSessionId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        String selectedTab = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedTab");
        if (systemInSessionId != null) {
            try {
                Integer id = Integer.parseInt(systemInSessionId);
                systemInSession = entityManager.find(SystemInSession.class, id);
                initializeDicomDocuments();
                initializeHL7Documents();

                if (selectedTab == null || selectedTab.equals("editSystemSummaryTab")) {
                    selectedTab = "showSystemSummaryTab";
                }
                setDefaultTab(selectedTab);
            } catch (NumberFormatException e) {
                LOG.error("failed to find system in session with id = " + systemInSessionId);
            }
        }
    }

    /**
     * Search the institution corresponding to the system. Used by JSF to render.
     *
     * @return String = Name of the institution(s) to display
     */
    @Override
    public String getInstitutionsForSystem(System selectedSystem) {
        LOG.debug("getInstitutionsForSystem");
        return getInstitutionsForSystemStatic(selectedSystem);
    }
}
