package net.ihe.gazelle.pr.bean;

import net.ihe.gazelle.hql.criterion.AssociationCriterion;
import net.ihe.gazelle.tm.systems.model.InstitutionSystem;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SystemCriterion extends AssociationCriterion<SystemInSession, System> {

    private static final Logger LOG = LoggerFactory.getLogger(SystemCriterion.class);

    private static final long serialVersionUID = 3507213777297010120L;

    public SystemCriterion(String keyword, String associationPath) {
        super(System.class, keyword, associationPath);
    }

    @Override
    public String getSelectableLabelNotNull(System instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectableLabelNotNull");
        }
        StringBuilder result = new StringBuilder();
        Set<InstitutionSystem> institutionSystems = instance.getInstitutionSystems();
        for (InstitutionSystem institutionSystem : institutionSystems) {
            if (result.length() > 0) {
                result.append(" - ");
            }
            result.append(institutionSystem.getInstitution().getKeyword());
        }
        if (result.length() > 0) {
            result.append(" - ");
        }
        result.append(instance.getKeyword());
        if (StringUtils.trimToNull(instance.getVersion()) != null) {
            result.append(" (");
            result.append(instance.getVersion());
            result.append(")");
        }
        return result.toString();
    }

}
