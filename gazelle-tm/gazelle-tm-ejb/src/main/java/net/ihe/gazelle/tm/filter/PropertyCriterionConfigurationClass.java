package net.ihe.gazelle.tm.filter;

import net.ihe.gazelle.hql.criterion.PropertyCriterion;
import net.ihe.gazelle.tm.configurations.model.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PropertyCriterionConfigurationClass extends PropertyCriterion<AbstractConfiguration, Integer> {

    private static final long serialVersionUID = 7037325705078975371L;
    private static final Logger LOG = LoggerFactory.getLogger(PropertyCriterionConfigurationClass.class);
    private Map<String, String> friendlyNames;

    public PropertyCriterionConfigurationClass(Class<Integer> selectableClass, String keyword, String path,
                                               Map<String, String> friendlyNames) {
        super(selectableClass, keyword, path);
        this.friendlyNames = friendlyNames;
    }

    @Override
    public String getSelectableLabelNotNull(Integer instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectableLabelNotNull");
        }
        return friendlyNames.get(Integer.toString(instance));
    }

}