package net.ihe.gazelle.pr.bean;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.pr.action.PRLogs;
import net.ihe.gazelle.pr.search.model.ISDownload;
import net.ihe.gazelle.pr.search.model.SearchLogReport;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemQuery;

import java.util.List;

public class PRLogsFilterDataModel extends FilterDataModel<System> {

    /**
     *
     */
    private static final long serialVersionUID = -2454528834938288515L;
    private PRLogs prLogs;

    public PRLogsFilterDataModel(Filter<System> filter, PRLogs prLogs) {
        super(filter);
        this.prLogs = prLogs;

    }

    @Override
    protected List<?> getRealCache(HQLQueryBuilder<System> queryBuilder) {

        if (prLogs.getType() == LogType.DOWNLOAD) {
            SystemQuery q = new SystemQuery(queryBuilder);
            q.downloads().id().order(false);
            List<Integer> idList = q.downloads().id().getListDistinctOrdered();
            return getSortedList(idList, ISDownload.class);
        } else {
            SystemQuery q = new SystemQuery(queryBuilder);
            q.searchLogReports().id().order(false);
            List<Integer> idList = q.searchLogReports().id().getListDistinctOrdered();
            return getSortedList(idList, SearchLogReport.class);
        }
    }

    @Override
    protected int getRealCount(HQLQueryBuilder<System> queryBuilder) {
        if (prLogs.getType() == LogType.DOWNLOAD) {
            return queryBuilder.getCountDistinctOnPath("this_.downloads.id");
        } else {
            return queryBuilder.getCountDistinctOnPath("this_.searchLogReports.id");
        }
    }

    private List<?> getSortedList(List<Integer> idList, Class<?> clazz) {
        HQLQueryBuilder queryBuilder = new HQLQueryBuilder(clazz);
        queryBuilder.addIn("id", idList);
        List list = queryBuilder.getList();
        return list;
    }

    @Override
    protected Object getId(System t) {
        return t.getId();
    }

    public Object getId(SearchLogReport t) {
        return t.getId();
    }

}
