package net.ihe.gazelle.pr.action;

import eu.bitwalker.useragentutils.UserAgent;
import net.ihe.gazelle.common.action.DateDisplay;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.filter.action.DatatableStateHolderBean;
import net.ihe.gazelle.common.filter.criterion.DateValueConvertor;
import net.ihe.gazelle.common.util.DateDisplayUtil;
import net.ihe.gazelle.hql.HQLInterval;
import net.ihe.gazelle.hql.HQLIntervalCount;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterion;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.criterion.date.DateValue;
import net.ihe.gazelle.hql.paths.HQLSafePathBasicDate;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.pr.bean.LogMode;
import net.ihe.gazelle.pr.bean.LogType;
import net.ihe.gazelle.pr.bean.PRLogsFilter;
import net.ihe.gazelle.pr.bean.PRLogsFilterDataModel;
import net.ihe.gazelle.pr.search.model.*;
import net.ihe.gazelle.tm.filter.valueprovider.InstitutionFixer;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemQuery;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Name("prLogs")
@Scope(ScopeType.PAGE)
public class PRLogs implements Serializable, QueryModifier<System> {

    private static final DateValueConvertor DATE_VALUE_CONVERTOR = new DateValueConvertor();

    private static final long serialVersionUID = 2126957362101666607L;

    private static final Date NEW_VERSION_DATE;
    private static final Logger LOG = LoggerFactory.getLogger(PRLogs.class);

    static {
        Calendar cal = Calendar.getInstance();
        cal.set(2013, 1, 7, 13, 30, 0);
        NEW_VERSION_DATE = cal.getTime();
    }

    private Filter<System> filter;
    private FilterDataModel<System> events;

    private HQLInterval interval = HQLInterval.MONTH;
    private LogType type = LogType.SEARCH;
    private LogMode mode = LogMode.ONLY_FILTERED;

    private SearchLogReport searchLogReport;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    public HQLInterval[] getIntervals() {
        return HQLInterval.values();
    }

    public LogType[] getTypes() {
        return LogType.values();
    }

    public LogMode[] getModes() {
        return LogMode.values();
    }

    public Filter<System> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            Map<String, String> requestParameterMap = null;
            FacesContext fc = FacesContext.getCurrentInstance();
            if ((fc != null) && (fc.getExternalContext() != null)
                    && (fc.getExternalContext().getRequestParameterMap() != null)) {
                requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            }
            filter = new PRLogsFilter(getCriterions(), requestParameterMap, this);

            addDateCriterionToFilter(getDateValue(requestParameterMap));
        }
        return filter;
    }

    private DateValue getDateValue(Map<String, String> requestParameterMap) {
        if (requestParameterMap == null) {
            return null;
        }
        String dateValueString = requestParameterMap.get("date");
        if (dateValueString == null) {
            return null;
        }
        return (DateValue) DATE_VALUE_CONVERTOR.getAsObject(null, null, dateValueString);
    }

    private void addDateCriterionToFilter(DateValue dateValue) {
        SystemQuery systemQuery = new SystemQuery();
        HQLSafePathBasicDate<Date> searchDate = null;
        if (type == LogType.DOWNLOAD) {
            searchDate = systemQuery.downloads().date();
        } else {
            searchDate = systemQuery.searchLogReports().date();
        }
        searchDate.setCriterionWithoutTime(DateDisplayUtil.getTimeZone());

        HQLCriterion criterion = searchDate.getCriterion("date");
        filter.addCriterion(criterion, null);
        if (dateValue == null) {
            filter.getFilterValues().put("date", criterion.getValueInitiator().getValue());
        } else {
            filter.getFilterValues().put("date", dateValue);
        }
    }

    public String getDateFormat() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDateFormat");
        }
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, LocaleSelector
                .instance().getLocale());
        if (dateFormat instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) dateFormat).toLocalizedPattern();
        } else {
            return new SimpleDateFormat().toLocalizedPattern();
        }
    }

    public String getDateField() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDateField");
        }
        if (type == LogType.DOWNLOAD) {
            return "downloads.date";
        } else {
            return "searchLogReports.date";
        }
    }

    private HQLCriterionsForFilter<System> getCriterions() {
        SystemQuery systemQuery = new SystemQuery();
        HQLCriterionsForFilter<System> result = systemQuery.getHQLCriterionsForFilter();

        result.addPath("institution", systemQuery.institutionSystems().institution(), InstitutionFixer.INSTANCE,
                InstitutionFixer.INSTANCE);
        result.addPath("systemType", systemQuery.systemSubtypesPerSystemType().systemType());
        result.addPath("systemSubType", systemQuery.systemSubtypesPerSystemType().systemSubType());
        result.addPath("systemName", systemQuery.name());

        result.addQueryModifier(this);

        return result;
    }

    public LogType getType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getType");
        }
        return type;
    }

    public void setType(LogType type) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setType");
        }
        this.type = type;
        events = null;

        DateValue dateValue = (DateValue) getFilter().getFilterValues().get("date");
        addDateCriterionToFilter(dateValue);

        DatatableStateHolderBean dataTableStateHolder = (DatatableStateHolderBean) Component
                .getInstance("dataTableStateHolder");
        dataTableStateHolder.getSortOrders().clear();
        dataTableStateHolder.setDescendingOn(getDateField());
    }

    public LogMode getMode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMode");
        }
        return mode;
    }

    public void setMode(LogMode mode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMode");
        }
        this.mode = mode;
    }

    public boolean isDownloadType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDownloadType");
        }
        return type == LogType.DOWNLOAD;
    }

    public boolean isSearchType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isSearchType");
        }
        return type == LogType.SEARCH;
    }

    public FilterDataModel<System> getEvents() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEvents");
        }
        if (events == null) {
            events = new PRLogsFilterDataModel(getFilter(), this);
        }
        return events;
    }

    public SearchLogReport getSearch(PREvent prLog) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSearch");
        }
        if (prLog instanceof SearchLogReport) {
            return (SearchLogReport) prLog;
        } else if (prLog instanceof ISDownload) {
            return ((ISDownload) prLog).getSearchLogReport();
        } else {
            return null;
        }
    }

    public boolean showDownloads(SearchLogReport searchLogReport) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showDownloads");
        }
        boolean res = false;

        if (searchLogReport != null && searchLogReport.getDate().after(NEW_VERSION_DATE) && (searchLogReport.getIsDownloads().size() > 0)) {
            res = true;
        }
        return res;
    }

    public HQLInterval getInterval() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInterval");
        }
        return interval;
    }

    public void setInterval(HQLInterval interval) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInterval");
        }
        this.interval = interval;
    }

    public SearchLogReport getSearchLogReport() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSearchLogReport");
        }
        return searchLogReport;
    }

    public void setSearchLogReport(SearchLogReport searchLogReport) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSearchLogReport");
        }
        SearchLogReportQuery query = new SearchLogReportQuery();
        query.id().eq(searchLogReport.getId());
        searchLogReport = query.getUniqueResult();
        searchLogReport.getFoundSystemsForSearchLog().size();
        searchLogReport.getSearchLogCriterion().size();
        this.searchLogReport = searchLogReport;
    }

    public int getNumberOfCriteria(SearchLogReport prEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfCriteria");
        }
        if (prEvent == null) {
            return -1;
        } else {
            SearchLogReportQuery query = new SearchLogReportQuery();
            query.id().eq(prEvent.getId());
            return query.searchLogCriterion().id().getCountDistinctOnPath();
        }
    }

    public int getNumberOfFoundSystemsForSearchLog(SearchLogReport prEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfFoundSystemsForSearchLog");
        }
        if (prEvent == null) {
            return -1;
        } else {
            SearchLogReportQuery query = new SearchLogReportQuery();
            query.id().eq(prEvent.getId());
            return query.foundSystemsForSearchLog().id().getCountDistinctOnPath();
        }
    }

    public String getBrowserInfo(String browserInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBrowserInfo");
        }
        UserAgent userAgent = UserAgent.parseUserAgentString(browserInformation);
        return userAgent.getOperatingSystem().getName() + " - " + userAgent.getBrowser().getName();
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<System> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        if (type == LogType.SEARCH) {
            queryBuilder.addRestriction(HQLRestrictions.gt("this_.searchLogReports.numberOfCriteria", 1));
            queryBuilder.addRestriction(HQLRestrictions.isNotNull("this_.searchLogReports.id"));
        } else {
            queryBuilder.addRestriction(HQLRestrictions.isNotNull("this_.downloads.id"));
        }
    }

    public List<ISDownload> getDownloadsFiltered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDownloadsFiltered");
        }
        if (searchLogReport != null) {
            return getDownloadsFiltered(searchLogReport);
        } else {
            return new ArrayList<ISDownload>();
        }
    }

    public List<ISDownload> getDownloadsFiltered(SearchLogReport logReport) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDownloadsFiltered");
        }
        ISDownloadQuery query = new ISDownloadQuery();
        query.searchLogReport().id().eq(logReport.getId());
        Object oInstitution = InstitutionFixer.INSTANCE.getValue();
        Institution institution = null;
        if (oInstitution != null) {
            institution = (Institution) oInstitution;
        } else {
            if (getFilter().getFilterValues().get("institution") != null) {
                institution = (Institution) getFilter().getFilterValues().get("institution");
            }
        }
        if (institution != null) {
            query.system().institutionSystems().institution().id().eq(institution.getId());
        }
        return query.getList();
    }

    public String getCriterionType(SearchLogCriterion criterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriterionType");
        }
        String result = null;
        if ("java.util.Date".equals(criterion.getCriterion())) {
            result = "gazelle.productregistry.search.criteria.PublicationDate";
        }
        if ("net.ihe.gazelle.tf.model.Domain".equals(criterion.getCriterion())) {
            result = "gazelle.tf.domain";
        }
        if ("net.ihe.gazelle.tf.model.Actor".equals(criterion.getCriterion())) {
            result = "gazelle.testmanagement.system.label.Actor";
        }
        if ("net.ihe.gazelle.tf.model.IntegrationProfile".equals(criterion.getCriterion())) {
            result = "gazelle.tf.table.IntegrationProfile";
        }
        if ("net.ihe.gazelle.tf.model.IntegrationProfileOption".equals(criterion.getCriterion())) {
            result = "gazelle.productregistry.system.label.IPO";
        }
        if ("net.ihe.gazelle.users.model.Institution".equals(criterion.getCriterion())) {
            result = "net.ihe.gazelle.tm.Company";
        }
        if ("net.ihe.gazelle.pr.systems.model.System".equals(criterion.getCriterion())) {
            result = "gazelle.configuration.system.system";
        }
        if ("net.ihe.gazelle.systems.model.SystemType".equals(criterion.getCriterion())) {
            result = "net.ihe.gazelle.tm.SystemType";
        }
        if ("net.ihe.gazelle.systems.model.SystemSubType".equals(criterion.getCriterion())) {
            result = "net.ihe.gazelle.tm.SystemSubtype";
        }
        if (result == null) {
            result = criterion.getCriterion();
        }
        return StatusMessage.getBundleMessage(result, result);

    }

    public String getCriterionName(SearchLogCriterion criterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriterionName");
        }
        if ("date".equals(criterion.getCriterion())) {
            Converter converter = FacesContext.getCurrentInstance().getApplication().createConverter(DateValue.class);
            DateValue dateValue = (DateValue) converter.getAsObject(FacesContext.getCurrentInstance(), null,
                    criterion.getValueString());
            switch (dateValue.getIntervalType()) {
                case BEFORE:
                    return "Before " + DateDisplay.instance().displayDateTime(dateValue.getValue1());
                case AFTER:
                    return "After " + DateDisplay.instance().displayDateTime(dateValue.getValue1());
                case BETWEEN:
                    return "Between " + DateDisplay.instance().displayDateTime(dateValue.getValue1()) + " and "
                            + DateDisplay.instance().displayDateTime(dateValue.getValue2());
                default:
                    return "Any";
            }
        }
        if (criterion.getValueDate() != null) {
            String key = null;
            if (criterion.getIsBeforeDate()) {
                key = "net.ihe.gazelle.tm.Before";
            } else {
                key = "net.ihe.gazelle.tm.After";
            }
            return StatusMessage.getBundleMessage(key, key) + " "
                    + DateDisplay.instance().displayDateTime(criterion.getValueDate());
        }
        return criterion.getValueString();
    }

    public String javascriptSeries() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("javascriptSeries");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

        interval = HQLInterval.valueOf(requestParameterMap.get("interval"));
        type = LogType.valueOf(requestParameterMap.get("type"));
        mode = LogMode.valueOf(requestParameterMap.get("mode"));

        List<HQLIntervalCount>[] intervals;
        if (mode == LogMode.WITH_UNFILTERED) {
            intervals = new List[2];
        } else {
            intervals = new List[1];
        }

        Date startDate = null;
        Date endDate = null;

        Object dateValueObject = getFilter().getFilterValues().get("date");
        if ((dateValueObject != null) && (dateValueObject instanceof DateValue)) {
            DateValue dateValue = (DateValue) dateValueObject;
            switch (dateValue.getIntervalType()) {
                case BETWEEN:
                    startDate = dateValue.getValue1();
                    endDate = dateValue.getValue2();
                    break;
                case BEFORE:
                    endDate = dateValue.getValue1();
                    break;
                case AFTER:
                    startDate = dateValue.getValue1();
                    break;
            }
        }

        SystemQuery systemQuery = new SystemQuery();
        HQLQueryBuilder<System> queryBuilder = (HQLQueryBuilder<System>) systemQuery.getQueryBuilder();
        modifyQuery(queryBuilder, null);
        getFilter().appendHibernateFilters(queryBuilder);
        String searchDate = null;
        String idPath;
        if (type == LogType.DOWNLOAD) {
            searchDate = systemQuery.downloads().date().toString();
            idPath = systemQuery.downloads().id().toString();
        } else {
            searchDate = systemQuery.searchLogReports().date().toString();
            idPath = systemQuery.searchLogReports().id().toString();
        }
        intervals[0] = queryBuilder.getCountPerInterval(interval, searchDate.toString(), startDate, endDate,
                idPath.toString());

        if (mode == LogMode.WITH_UNFILTERED) {
            systemQuery = new SystemQuery();
            queryBuilder = (HQLQueryBuilder<System>) systemQuery.getQueryBuilder();
            modifyQuery(queryBuilder, null);
            intervals[1] = queryBuilder.getCountPerInterval(interval, searchDate.toString(), startDate, endDate,
                    idPath.toString());
            interval.addMissingIntervals(intervals);
        }

        StringBuilder result = new StringBuilder("var seriesData = [");
        appendInterval(intervals[0], result);
        result.append(",\r\n");
        if (mode == LogMode.WITH_UNFILTERED) {
            appendInterval(intervals[1], result);
        }
        result.append("];");

        return result.toString();
    }

    private void appendInterval(List<HQLIntervalCount> intervals, StringBuilder series) {
        series.append("[");
        boolean first = true;
        for (HQLIntervalCount hqlIntervalCount : intervals) {
            if (first) {
                first = false;
            } else {
                series.append(",\r\n");
            }
            series.append("{ x: ");
            series.append(hqlIntervalCount.getIntervalStartDate().getTime() / 1000);
            series.append(", y: ");
            series.append(hqlIntervalCount.getCount());
            series.append(" }");
        }
        series.append("]");
    }

    public String getTypeFromFrame() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTypeFromFrame");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        LogType mode = LogType.valueOf(requestParameterMap.get("type"));
        return mode.getLabel();
    }

    public String getIntervalFromFrame() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntervalFromFrame");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        HQLInterval interval = HQLInterval.valueOf(requestParameterMap.get("interval"));
        return interval.name();
    }

    public String getModeFromFrame() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getModeFromFrame");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        LogMode mode = LogMode.valueOf(requestParameterMap.get("mode"));
        if (mode == LogMode.ONLY_FILTERED) {
            return "false";
        } else {
            return "true";
        }
    }

}
