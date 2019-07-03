package net.ihe.gazelle.tm.filter.entities;

import net.ihe.gazelle.hql.HQLQueryBuilder;

public interface SearchEntity<T> {

    /**
     * Add restrictions to the provided query builder. Query may not be performed (ie no result but no restriction), and method returns false
     *
     * @param queryBuilder
     * @param query
     * @return restriction add
     */
    boolean addRestrictionsForQuery(HQLQueryBuilder<T> queryBuilder, String query);

    Class<T> getEntityClass();

}
