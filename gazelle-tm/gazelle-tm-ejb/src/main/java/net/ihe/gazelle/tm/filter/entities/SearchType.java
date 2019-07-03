package net.ihe.gazelle.tm.filter.entities;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.objects.model.ObjectInstance;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.User;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.core.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public enum SearchType implements
        SearchEntity {

    SAMPLE("gazelle.tm.search.sample", "spl", ObjectInstance.class),

    TEST_INSTANCE("gazelle.tm.search.testInstance", "ti", TestInstance.class),

    TEST("gazelle.tm.test.Test", "t", Test.class),

    MONITOR("gazelle.tm.search.monitor", "m", MonitorInSession.class),

    SYSTEM("gazelle.tm.search.system", "sys", SystemInSession.class),

    USER("gazelle.tm.search.user", "u", User.class);
    private static final Logger LOG = LoggerFactory.getLogger(SearchType.class);

    private String label;

    private Class<?> entityClass;

    private String shortId;

    private SearchType(String label, String shortId, Class<?> entityClass) {
        this.label = label;
        this.shortId = shortId;
        this.entityClass = entityClass;
    }

    public static void addListRestrictions(HQLQueryBuilder<?> queryBuilder, List<HQLRestriction> restrictions) {
        if (restrictions.size() == 1) {
            queryBuilder.addRestriction(restrictions.get(0));
        } else if (restrictions.size() > 0) {
            HQLRestriction[] array = restrictions.toArray(new HQLRestriction[restrictions.size()]);
            queryBuilder.addRestriction(HQLRestrictions.or(array));
        }
    }

    public static void tryAddId(List<HQLRestriction> restrictions, String query) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("tryAddId");
        }
        try {
            Integer id = Integer.parseInt(query);
            restrictions.add(HQLRestrictions.eq("id", id));
        } catch (NumberFormatException e) {
            // no restriction...
        }
    }

    @Override
    public boolean addRestrictionsForQuery(HQLQueryBuilder queryBuilder, String query) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addRestrictionsForQuery");
        }
        List<HQLRestriction> restrictions;
        switch (this) {
            case MONITOR:
                restrictions = new ArrayList<HQLRestriction>();
                SearchType.tryAddId(restrictions, query);
                restrictions.add(HQLRestrictions.like("user.username", query));
                restrictions.add(HQLRestrictions.like("user.firstname", query));
                restrictions.add(HQLRestrictions.like("user.lastname", query));
                addListRestrictions(queryBuilder, restrictions);
                queryBuilder.addEq("testingSession", TestingSession.getSelectedTestingSession());
                queryBuilder.addEq("isActivated", true);
                return true;
            case SAMPLE:
                restrictions = new ArrayList<HQLRestriction>();
                SearchType.tryAddId(restrictions, query);
                restrictions.add(HQLRestrictions.like("name", query));
                restrictions.add(HQLRestrictions.like("description", query));
                restrictions.add(HQLRestrictions.like("object.description", query));
                SearchType.addListRestrictions(queryBuilder, restrictions);
                queryBuilder.addEq("system.testingSession", TestingSession.getSelectedTestingSession());
                return true;
            case SYSTEM:
                restrictions = new ArrayList<HQLRestriction>();
                restrictions.add(HQLRestrictions.like("system.keyword", query));
                restrictions.add(HQLRestrictions.like("system.name", query));
                SearchType.addListRestrictions(queryBuilder, restrictions);
                queryBuilder.addEq("testingSession", TestingSession.getSelectedTestingSession());
                return true;
            case TEST:
                restrictions = new ArrayList<HQLRestriction>();
                SearchType.tryAddId(restrictions, query);
                restrictions.add(HQLRestrictions.like("name", query));
                restrictions.add(HQLRestrictions.like("keyword", query));
                restrictions.add(HQLRestrictions.like("testDescription.description", query));
                SearchType.addListRestrictions(queryBuilder, restrictions);
                return true;
            case TEST_INSTANCE:
                try {
                    Integer id = Integer.parseInt(query);
                    queryBuilder.addEq("id", id);
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            case USER:
                restrictions = new ArrayList<HQLRestriction>();
                SearchType.tryAddId(restrictions, query);
                restrictions.add(HQLRestrictions.like("username", query));
                restrictions.add(HQLRestrictions.like("firstname", query));
                restrictions.add(HQLRestrictions.like("lastname", query));
                SearchType.addListRestrictions(queryBuilder, restrictions);
                return true;
            default:
                return false;
        }
    }

    @Override
    public Class getEntityClass() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntityClass");
        }
        return entityClass;
    }

    public List<?> search(String query, EntityManager entityManager, int maxCount) {
        HQLQueryBuilder queryBuilder = new HQLQueryBuilder(getEntityClass());
        boolean process = addRestrictionsForQuery(queryBuilder, query);
        if (process) {
            queryBuilder.setMaxResults(maxCount);
            return queryBuilder.getList();
        } else {
            return new ArrayList(0);
        }
    }

    public String getLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        return ResourceBundle.instance().getString(label);
    }

    public String getShortId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getShortId");
        }
        return shortId;
    }

    private HQLRestriction addPatientIdRestriction(String query) {
        // different parts of the patient ID :
        // identifier^^^domainKeyword&domainRoot&typeCode
        String[] split1 = StringUtils.split(query, '&');
        if (split1.length == 3) {
            String[] split2 = StringUtils.splitByWholeSeparator(split1[0], "^^^");
            if (split2.length == 2) {
                String identifier = split2[0];
                String domainKeyword = split2[1];
                String domainRoot = split1[1];
                String typeCode = split1[2];
                return HQLRestrictions.and(addEq("identifiers.identifier", identifier),
                        addEq("identifiers.authority.keyword", domainKeyword),
                        addEq("identifiers.authority.rootOID", domainRoot),
                        addEq("identifiers.typeCode.keyword", typeCode));
            }
        }

        return null;
    }

    private HQLRestriction addEq(String path, String value) {
        if (value.equals("null")) {
            return HQLRestrictions.isNull(path);
        } else {
            return HQLRestrictions.eq(path, value);
        }
    }

}
