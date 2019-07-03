package net.ihe.gazelle.pr.action;

import net.ihe.gazelle.common.LinkDataProvider;
import net.ihe.gazelle.common.LinkDataProviderService;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.util.DateDisplayUtil;
import net.ihe.gazelle.hql.FilterLabelProvider;
import net.ihe.gazelle.hql.criterion.HQLCriterion;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.paths.HQLSafePathBasicDate;
import net.ihe.gazelle.pr.bean.*;
import net.ihe.gazelle.pr.search.model.SearchLogCriterion;
import net.ihe.gazelle.pr.search.model.SearchLogReport;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemQuery;
import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Name("prSearch")
@Scope(ScopeType.PAGE)
public class PRSearch implements Serializable {

    private static final long serialVersionUID = -5593980494945438669L;

    private static final Logger LOG = LoggerFactory.getLogger(PRSearch.class);

    private Filter<System> filter;
    private Filter<SystemInSession> filterLike;
    private FilterDataModel<System> systems;
    private List<System> systemList;

    private Map<ActorIntegrationProfileOption, Boolean> aipos;

    @In
    private EntityManager entityManager;

    private SearchLogReport currentSearchLogReport;
    private int index = -1;

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public Filter<System> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            filter = new PRSearchMainFilter(getCriterions(), requestParameterMap, this);
        }
        return filter;
    }

    public Filter<SystemInSession> getFilterLike() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterLike");
        }
        if (filterLike == null) {

            List<QueryModifier<SystemInSession>> queryModifiers = new ArrayList<QueryModifier<SystemInSession>>();
            queryModifiers.add(new PRSearchQueryModifierSystemInSession());

            SystemCriterion systemCriterion = new SystemCriterion("system", "system");
            List<HQLCriterion<SystemInSession, ?>> criterions = new ArrayList<HQLCriterion<SystemInSession, ?>>();
            criterions.add(systemCriterion);

            filterLike = new PRSearchFilterModifier<SystemInSession>(SystemInSession.class, queryModifiers, criterions,
                    this, false);

            filterLike.getSuggesters().put("system", new PRSuggester(filterLike, "system", systemCriterion));

        }
        return filterLike;
    }

    private HQLCriterionsForFilter<System> getCriterions() {
        SystemQuery query = new SystemQuery();

        HQLCriterionsForFilter<System> result = query.getHQLCriterionsForFilter();
        TMCriterions.addAIPOCriterions(result, query.systemsInSession().system().systemActorProfiles()
                .actorIntegrationProfileOption());

        result.addPath("institution", query.systemsInSession().system().institutionSystems().institution());
        result.addPath("systemType", query.systemsInSession().system().systemSubtypesPerSystemType().systemType());
        result.addPath("systemSubType", query.systemsInSession().system().systemSubtypesPerSystemType().systemSubType());
        result.addPath("systemName", query.systemsInSession().system().name());

        result.addQueryModifier(new PRSearchQueryModifierMain());

        HQLSafePathBasicDate<Date> isDate = query.systemsInSession().system().integrationStatementDate();
        isDate.setCriterionWithoutTime(DateDisplayUtil.getTimeZone());
        result.addPath("date", isDate);

        return result;
    }

    public FilterDataModel<System> getSystems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystems");
        }
        if (systems == null) {
            systems = getSystemsDataModel();
            if (isShowRatio()) {
                computeRatios(getLikeSystemSelected());
            }
        }
        return systems;
    }

    public void setSystems(List<System> systems) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystems");
        }
        this.systemList = systems;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean displayStartDiv() {
        return (getIndex() % 3 == 0);
    }

    public boolean displayEndDiv() {
        return (getIndex() % 3 == 2);
    }

    public List<System> getAllSystems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystems");
        }
        if (systemList == null) {
            systemList = (List<System>) getSystemsDataModel().getAllItems(FacesContext.getCurrentInstance());

            if (isShowRatio()) {
                computeRatios(getLikeSystemSelected());
            }
        }
        return systemList;
    }

    protected System getLikeSystemSelected() {
        return (System) getFilterLike().getFilterValues().get("system");
    }

    public FilterDataModel<System> getSystemsDataModel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsDataModel");
        }
        if (systems == null) {
            systems = new FilterDataModel<System>(getFilter()) {
                @Override
                protected Object getId(System t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return systems;
    }

    public boolean isShowRatio() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowRatio");
        }
        return getLikeSystemSelected() != null;
    }

    private void computeRatios(System refSystem) {
        SystemQuery query1 = new SystemQuery();
        query1.id().eq(refSystem.getId());
        System ref = query1.getUniqueResult();
        Set<SystemActorProfiles> saps = ref.getSystemActorProfiles();

        Set<Integer> refAipoIds = new HashSet<Integer>();
        for (SystemActorProfiles systemActorProfiles : saps) {
            if (systemActorProfiles.getActorIntegrationProfileOption() != null) {
                refAipoIds.add(systemActorProfiles.getActorIntegrationProfileOption().getId());
            }
        }

        if (refAipoIds.size() > 0) {
            for (System system : getAllSystems()) {
                saps = system.getSystemActorProfiles();

                Set<Integer> aipoIds = new HashSet<Integer>();
                for (SystemActorProfiles systemActorProfiles : saps) {
                    if ((systemActorProfiles != null)
                            && (systemActorProfiles.getActorIntegrationProfileOption() != null)) {
                        aipoIds.add(systemActorProfiles.getActorIntegrationProfileOption().getId());
                    }
                }

                int size = aipoIds.size();
                aipoIds.removeAll(refAipoIds);
                int sizeIntersect = size - aipoIds.size();

                double ratio = (1.0 * sizeIntersect) / (1.0 * refAipoIds.size());
                system.setRatio(ratio);
            }

            Collections.sort(getAllSystems(), new Comparator<System>() {
                @Override
                public int compare(System o1, System o2) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("compare");
                    }
                    int result = Double.compare(o2.getRatio(), o1.getRatio());
                    if (result == 0) {
                        return o1.getName().compareTo(o2.getName());
                    } else {
                        return result;
                    }
                }
            });

        }
    }

    public SearchLogReport getCurrentSearchLogReport() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentSearchLogReport");
        }
        return currentSearchLogReport;
    }

    public void logResearch() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("logResearch");
        }
        List<SearchLogCriterion> listOfSearchLogCriterion = new ArrayList<SearchLogCriterion>();

        Set<String> criterions = getFilter().getCriterions().keySet();
        for (String criterion : criterions) {
            Object value = getFilter().getRealFilterValue(criterion);
            Object selectedValue = getFilter().getFilterValues().get(criterion);
            if ((value != null) && (selectedValue != null)) {
                SearchLogCriterion searchLogCriterion = new SearchLogCriterion();
                searchLogCriterion.setCriterion(criterion);
                Object id = null;
                try {
                    id = PropertyUtils.getProperty(value, "id");
                } catch (Exception e) {
                    // Failed to get id...
                    id = null;
                }
                if ((id != null) && (id instanceof Integer)) {
                    searchLogCriterion.setValueId((Integer) id);
                }
                if (value instanceof Date) {
                    searchLogCriterion.setValueDate((Date) value);
                }
                if (value instanceof FilterLabelProvider) {
                    searchLogCriterion.setValueString(((FilterLabelProvider) value).getSelectableLabel());
                } else {
                    LinkDataProvider provider = LinkDataProviderService.getProviderForClass(value.getClass());
                    if (provider != null) {
                        searchLogCriterion.setValueString(provider.getLabel(value, false));
                    } else {
                        Converter converter = FacesContext.getCurrentInstance().getApplication()
                                .createConverter(value.getClass());
                        if (converter != null) {
                            searchLogCriterion.setValueString(converter.getAsString(FacesContext.getCurrentInstance(),
                                    null, value));
                        } else {
                            searchLogCriterion.setValueString(value.toString());
                        }
                    }
                }
                listOfSearchLogCriterion.add(searchLogCriterion);
            }
        }

        if (listOfSearchLogCriterion.size() > 1) {
            String user = Identity.instance().getCredentials().getUsername();

            List<System> foundIntegrationStatements = getAllSystems();

            String remoteIP = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                    .getRemoteAddr();
            String remoteHostname = "";
            try {
                InetAddress address = InetAddress.getByName(remoteIP);

                remoteHostname = address.getHostName();
            } catch (UnknownHostException e) {
                LOG.error("Unable to lookup " + remoteIP);
            }

            String browserInfo = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                    .getRequest()).getHeader("user-agent");
            // We truncate the String message if it is too long for column size
            // constraint (database)
            if (browserInfo.length() > 256) {
                browserInfo = browserInfo.substring(0, 256);
            }

            // We initialize the SearchLogReport object
            SearchLogReport searchLogReport = new SearchLogReport();
            searchLogReport.setPerformerUsername(user);
            searchLogReport.setPerformerIpAddress(remoteIP);
            searchLogReport.setPerformerHostname(remoteHostname);
            searchLogReport.setBrowserInformation(browserInfo);
            searchLogReport.setDate(new Date());
            searchLogReport.setSearchLogCriterion(listOfSearchLogCriterion);
            searchLogReport.setFoundSystemsForSearchLog(foundIntegrationStatements);

            entityManager.persist(searchLogReport);
            entityManager.flush();

            currentSearchLogReport = searchLogReport;
        }
    }

    public String getRatio(System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRatio");
        }
        return Long.toString(Math.round(system.getRatio() * 100));
    }

    public void clearFilters() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearFilters");
        }
        getFilter().clear();
        getFilterLike().clear();
    }

    public void showSimilar(int systemId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showSimilar");
        }
        clearFilters();

        SystemQuery query = new SystemQuery();
        query.id().eq(systemId);
        System selectedSystem = query.getUniqueResult();
        getFilterLike().getFilterValues().put("system", selectedSystem);
    }

    public void showAIPOs(int systemId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showAIPOs");
        }
        if (systemId == -1) {
            systemId = getLikeSystemSelected().getId();
        }
        SystemQuery query = new SystemQuery();
        query.id().eq(systemId);
        System selectedSystem = query.getUniqueResult();

        aipos = new TreeMap<ActorIntegrationProfileOption, Boolean>();

        query = new SystemQuery();
        query.id().eq(getLikeSystemSelected().getId());
        System system = query.getUniqueResult();

        Set<SystemActorProfiles> saps = system.getSystemActorProfiles();
        for (SystemActorProfiles systemActorProfiles : saps) {
            aipos.put(systemActorProfiles.getActorIntegrationProfileOption(), false);
        }

        saps = selectedSystem.getSystemActorProfiles();
        for (SystemActorProfiles systemActorProfiles : saps) {
            if (aipos.containsKey(systemActorProfiles.getActorIntegrationProfileOption())) {
                aipos.put(systemActorProfiles.getActorIntegrationProfileOption(), true);
            }
        }
    }

    public Map<ActorIntegrationProfileOption, Boolean> getAipos() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipos");
        }
        return aipos;
    }

    public List<ActorIntegrationProfileOption> getAipoList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoList");
        }
        if (aipos != null) {
            return new ArrayList<ActorIntegrationProfileOption>(aipos.keySet());
        } else {
            return null;
        }
    }

}
