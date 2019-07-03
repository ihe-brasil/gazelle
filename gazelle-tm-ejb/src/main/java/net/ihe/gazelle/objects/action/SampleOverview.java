package net.ihe.gazelle.objects.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.objects.model.ObjectInstance;
import net.ihe.gazelle.objects.model.ObjectInstanceQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Role;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.security.Identity;
import org.richfaces.component.UIColumn;
import org.richfaces.component.UIDataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Map;

@Name("sampleOverview")
@Scope(ScopeType.PAGE)
@Synchronized(timeout = 10000)
public class SampleOverview implements Serializable, QueryModifier<ObjectInstance> {
    private static final long serialVersionUID = 6474253271362471546L;
    private static final Logger LOG = LoggerFactory.getLogger(SampleOverview.class);
    private FilterDataModel<ObjectInstance> objectInstances;
    private Filter<ObjectInstance> filter;

    public SampleOverview() {
        super();
    }

    public String getPermanentLinkForCreate(ObjectInstance inObjectInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPermanentLinkForCreate");
        }
        String result = "";
        if (inObjectInstance != null) {
            if (inObjectInstance.getId() != null) {
                result = ApplicationPreferenceManager.instance().getApplicationUrl() + "objects/sample.seam?id="
                        + inObjectInstance.getId().toString();
            }
        }
        return result;
    }

    public FilterDataModel<ObjectInstance> getObjectInstances() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getObjectInstances");
        }
        if (objectInstances == null) {
            objectInstances = new FilterDataModel<ObjectInstance>(getFilter()) {
                @Override
                protected Object getId(ObjectInstance t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return objectInstances;
    }

    public Filter<ObjectInstance> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            ObjectInstanceQuery query = new ObjectInstanceQuery();
            HQLCriterionsForFilter<ObjectInstance> hqlCriterionsForFilter = query.getHQLCriterionsForFilter();

            hqlCriterionsForFilter.addPath("testing_session", query.system().testingSession(),
                    TestingSession.getSelectedTestingSession());
            hqlCriterionsForFilter.addPath("objectType", query.object());

            hqlCriterionsForFilter.addPath("objectFileType", query.objectInstanceFiles().file().type());

            hqlCriterionsForFilter.addPath("institution", query.system().system().institutionSystems().institution());
            hqlCriterionsForFilter.addPath("system", query.system().system());
            hqlCriterionsForFilter.addPath("objectInstanceValidation", query.validation());
            hqlCriterionsForFilter.addPath("objectInstanceType", query.objectUsageType());

            hqlCriterionsForFilter.addQueryModifier(this);

            filter = new Filter<ObjectInstance>(hqlCriterionsForFilter);
        }
        return filter;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<ObjectInstance> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        ObjectInstanceQuery query = new ObjectInstanceQuery(queryBuilder);
        if (!(Identity.instance().hasRole(Role.ADMINISTRATOR_ROLE_STRING))
                && !(Identity.instance().hasRole(Role.MONITOR_ROLE_STRING))) {
            query.addRestriction(query.object().objectReaders().isNotEmptyRestriction());
        }
    }

    public void resetFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetFilter");
        }
        this.objectInstances = null;
        this.filter = null;
        this.getFilter().clear();
        this.resetDatatableFiltering();
    }

    public void resetDatatableFiltering() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetDatatableFiltering");
        }
        UIDataTable table = (UIDataTable) FacesContext.getCurrentInstance().getViewRoot()
                .findComponent("globalform:resultsForSearchDecorateSearch:tableOfSample");
        for (UIComponent column : table.getChildren()) {
            ((UIColumn) column).setFilterValue("");
        }
    }

}
