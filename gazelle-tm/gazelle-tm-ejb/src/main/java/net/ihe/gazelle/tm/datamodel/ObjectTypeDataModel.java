package net.ihe.gazelle.tm.datamodel;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.objects.model.ObjectType;
import net.ihe.gazelle.objects.model.ObjectTypeQuery;
import net.ihe.gazelle.objects.model.ObjectTypeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTypeDataModel extends FilterDataModel<ObjectType> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectTypeDataModel.class);
    private static final long serialVersionUID = -941092980880923719L;
    private ObjectTypeStatus selectedObjectTypeStatusForSearch;

    public ObjectTypeDataModel() {
        this(null);
    }

    public ObjectTypeDataModel(ObjectTypeStatus objectTypeStatusForSearch) {
        super(new Filter<ObjectType>(getCriterions()));
        this.selectedObjectTypeStatusForSearch = objectTypeStatusForSearch;
    }

    private static HQLCriterionsForFilter<ObjectType> getCriterions() {
        ObjectTypeQuery query = new ObjectTypeQuery();
        HQLCriterionsForFilter<ObjectType> result = query.getHQLCriterionsForFilter();
        return result;
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<ObjectType> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        if (selectedObjectTypeStatusForSearch != null) {
            queryBuilder.addEq("objectTypeStatus", selectedObjectTypeStatusForSearch);
        }
    }

    public ObjectTypeStatus getSelectedObjectTypeStatusForSearch() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectTypeStatusForSearch");
        }
        return selectedObjectTypeStatusForSearch;
    }

    public void setSelectedObjectTypeStatusForSearch(ObjectTypeStatus selectedObjectTypeStatusForSearch) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectTypeStatusForSearch");
        }
        this.selectedObjectTypeStatusForSearch = selectedObjectTypeStatusForSearch;
        resetCache();
    }

    @Override
    protected Object getId(ObjectType t) {
        // TODO Auto-generated method stub
        return t.getId();
    }
}
