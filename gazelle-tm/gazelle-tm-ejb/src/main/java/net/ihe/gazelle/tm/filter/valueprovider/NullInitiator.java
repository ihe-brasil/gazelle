package net.ihe.gazelle.tm.filter.valueprovider;

import net.ihe.gazelle.hql.NullValue;
import net.ihe.gazelle.hql.criterion.ValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullInitiator implements ValueProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NullInitiator.class);

    private static final long serialVersionUID = 6491672574734114191L;

    @Override
    public Object getValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValue");
        }
        return NullValue.NULL_VALUE;
    }

}
