package net.ihe.gazelle.pr.bean;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.Suggester;
import net.ihe.gazelle.hql.criterion.HQLCriterion;

public class PRSuggester<E, F> extends Suggester<E, F> {

    private static final long serialVersionUID = -1841972222612109889L;

    public PRSuggester(Filter<E> filter, String keyword, HQLCriterion<E, F> criterion) {
        super(filter, keyword, criterion);
    }

}
