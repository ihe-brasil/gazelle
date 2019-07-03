package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.tm.gazelletest.bean.AIPOSystemPartners;
import net.ihe.gazelle.tm.gazelletest.bean.Partner;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Name("partnersManager")
@Scope(ScopeType.PAGE)
public class PartnersManager implements Serializable {
    private static final long serialVersionUID = -3781439240744642052L;
    private static final Logger LOG = LoggerFactory.getLogger(PartnersManager.class);
    private List<Partner> possiblePartners;

    private TestingSession testingSession;

    private boolean displayModal = false;

    private RoleInTest selectedRit;

    public RoleInTest getSelectedRit() {
        return selectedRit;
    }

    public void setSelectedRit(RoleInTest selectedRit) {
        this.selectedRit = selectedRit;
    }

    public List<Partner> getPossiblePartners() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossiblePartners");
        }
        return possiblePartners;
    }

    public GazelleListDataModel<Partner> getPartnersList() {
        List<Partner> res = getPossiblePartners();
        GazelleListDataModel<Partner> dm = new GazelleListDataModel<Partner>(res);
        return dm;
    }

    public void showPartnersFor(AIPOSystemPartners partners, RoleInTest rit) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showPartnersFor");
        }
        setSelectedRit(rit);
        possiblePartners = partners.showPartnersFor(rit);
        Collections.sort(possiblePartners);
        setDisplayModal(true);
    }

    public boolean isInternetTesting() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isInternetTesting");
        }
        if (testingSession != null) {
            return testingSession.getInternetTesting();
        } else {
            return false;
        }
    }

    public boolean isDisplayModal() {
        return displayModal;
    }

    public void setDisplayModal(boolean displayModal) {
        this.displayModal = displayModal;
    }
}
