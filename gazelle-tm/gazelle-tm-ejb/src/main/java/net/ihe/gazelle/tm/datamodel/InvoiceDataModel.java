package net.ihe.gazelle.tm.datamodel;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.filter.valueprovider.InstitutionFixer;
import net.ihe.gazelle.tm.financial.model.Invoice;
import net.ihe.gazelle.tm.financial.model.InvoiceQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingSessionQuery;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InvoiceDataModel extends FilterDataModel<Invoice> {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceDataModel.class);

    private static final long serialVersionUID = -1033811539296564613L;

    private TestingSession selectedTestingSession;

    private Institution selectedInstitution;

    public InvoiceDataModel() {
        this(null);
    }

    public InvoiceDataModel(TestingSession selectedTestingSession) {
        super(new Filter<Invoice>(getCriterionList()));
        this.setSelectedTestingSession(selectedTestingSession);
    }

    public InvoiceDataModel(TestingSession selectedTestingSession, Filter<Invoice> filter) {
        super(filter);
        this.setSelectedTestingSession(selectedTestingSession);
    }

    private static HQLCriterionsForFilter<Invoice> getCriterionList() {
        InvoiceQuery query = new InvoiceQuery();

        HQLCriterionsForFilter<Invoice> result = query.getHQLCriterionsForFilter();

        TMCriterions.addTestingSession(result, "testingSession", query.testingSession());

        result.addPath("institution", query.institution(), null, InstitutionFixer.INSTANCE);

        return result;
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<Invoice> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        if (Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION.isGranted()) {
            InvoiceQuery query = new InvoiceQuery();
            User loggedInUser = User.loggedInUser();
            TestingSessionQuery query2 = new TestingSessionQuery();
            query2.testingSessionAdmin().id().eq(loggedInUser.getId());
            List<Integer> listDistinct = query2.id().getListDistinct();

            queryBuilder.addRestriction(query.testingSession().id().inRestriction(listDistinct));
        }

        if (selectedTestingSession != null) {
            queryBuilder.addRestriction(HQLRestrictions.eq("testingSession", selectedTestingSession));
        }
        if (selectedInstitution != null) {
            queryBuilder.addRestriction(HQLRestrictions.eq("institution", selectedInstitution));
        }
    }

    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        return selectedTestingSession;
    }

    public void setSelectedTestingSession(TestingSession selectedTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestingSession");
        }
        this.selectedTestingSession = selectedTestingSession;
    }

    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }
        return selectedInstitution;
    }

    public void setSelectedInstitution(Institution selectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitution");
        }
        this.selectedInstitution = selectedInstitution;
    }

    @Override
    protected Object getId(Invoice t) {
        // TODO Auto-generated method stub
        return t.getId();
    }

    public void clearFilters() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearFilters");
        }
        getFilter().clear();
    }
}
