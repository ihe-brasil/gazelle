package net.ihe.gazelle.tm.filter.modifier;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SystemNotTool implements QueryModifier {

    private static final long serialVersionUID = -8856490565543144558L;
    private static final Logger LOG = LoggerFactory.getLogger(SystemNotTool.class);
    private String systemEntityPath;

    public SystemNotTool(SystemEntity<System> systemEntity) {
        super();
        this.systemEntityPath = systemEntity.getPath();
    }

    @Override
    public void modifyQuery(HQLQueryBuilder queryBuilder, Map filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        SystemEntity<System> systemEntity = new SystemEntity<System>(systemEntityPath, queryBuilder);
        systemEntity.isTool().eq(false);
    }

}
