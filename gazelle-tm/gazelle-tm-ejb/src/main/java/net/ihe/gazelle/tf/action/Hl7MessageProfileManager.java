package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.preferences.PreferenceService;
import net.ihe.gazelle.tf.model.AffinityDomain;
import net.ihe.gazelle.tf.model.Hl7MessageProfile;
import net.ihe.gazelle.tf.model.Hl7MessageProfileQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Name("tfHl7MessageProfileManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("Hl7MessageProfileManagerLocal")
public class Hl7MessageProfileManager implements Serializable, Hl7MessageProfileManagerLocal {

    private static final long serialVersionUID = 450911179351283760L;
    private static final Logger LOG = LoggerFactory.getLogger(Hl7MessageProfileManager.class);

    private Hl7MessageProfile selectedProfile;
    private boolean displayProfile;
    private boolean displayAffinityDomainList;
    private AffinityDomain newAffinityDomain;
    private Filter<Hl7MessageProfile> filter;

    private String gazelleHL7ValidatorURL = null;

    @Override
    public FilterDataModel<Hl7MessageProfile> getAllProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllProfiles");
        }
        return new FilterDataModel<Hl7MessageProfile>(getFilter()) {
            @Override
            protected Object getId(Hl7MessageProfile hl7MessageProfile) {
                return hl7MessageProfile.getId();
            }
        };
    }

    public Filter<Hl7MessageProfile> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter<Hl7MessageProfile>(getHQLCriterionsForFilter());
        }
        return filter;
    }

    private HQLCriterionsForFilter<Hl7MessageProfile> getHQLCriterionsForFilter() {
        Hl7MessageProfileQuery query = new Hl7MessageProfileQuery();
        HQLCriterionsForFilter<Hl7MessageProfile> criterionsForFilter = query.getHQLCriterionsForFilter();
        criterionsForFilter.addPath("actor", query.actor());
        criterionsForFilter.addPath("transaction", query.transaction());
        criterionsForFilter.addPath("domain", query.domain());
        criterionsForFilter.addPath("afDomain", query.affinityDomains().labelToDisplay());
        criterionsForFilter.addPath("version", query.hl7Version());
        criterionsForFilter.addPath("type", query.messageType());
        return criterionsForFilter;
    }

    @Override
    public boolean getDisplayProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayProfile");
        }
        return displayProfile;
    }

    @Override
    public void setDisplayProfile(boolean displayProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayProfile");
        }
        this.displayProfile = displayProfile;
    }

    @Override
    public Hl7MessageProfile getSelectedProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedProfile");
        }
        return selectedProfile;
    }

    @Override
    public void setSelectedProfile(Hl7MessageProfile selectedProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedProfile");
        }
        this.selectedProfile = selectedProfile;
    }

    @Override
    public boolean isDisplayAffinityDomainList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayAffinityDomainList");
        }
        return displayAffinityDomainList;
    }

    @Override
    public void setDisplayAffinityDomainList(boolean displayAffinityDomainList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAffinityDomainList");
        }
        this.displayAffinityDomainList = displayAffinityDomainList;
    }

    @Override
    public AffinityDomain getNewAffinityDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNewAffinityDomain");
        }
        return newAffinityDomain;
    }

    @Override
    public void setNewAffinityDomain(AffinityDomain newAffinityDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNewAffinityDomain");
        }
        this.newAffinityDomain = newAffinityDomain;
    }

    /***********************************************************************/

    @Override
    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public void editProfile(Hl7MessageProfile profile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editProfile");
        }
        selectedProfile = profile;
        displayProfile = true;
        displayAffinityDomainList = false;
    }

    @Override
    public void saveSelectedProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveSelectedProfile");
        }
        Hl7MessageProfile.addMessageProfileReference(selectedProfile);
        displayProfile = false;
        selectedProfile = null;
    }

    @Override
    public void cancelDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelDisplay");
        }
        displayProfile = false;
        selectedProfile = null;
    }

    @Override
    public List<SelectItem> getAffinityDomainList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAffinityDomainList");
        }
        List<AffinityDomain> affinityDomains = AffinityDomain.getAllAffinityDomains();
        List<SelectItem> items = null;
        if (affinityDomains != null) {
            items = new ArrayList<SelectItem>();
            items.add(new SelectItem(null, ResourceBundle.instance().getString("gazelle.common.PleaseSelect")));
            for (AffinityDomain ad : affinityDomains) {
                if (!selectedProfile.getAffinityDomains().contains(ad)) {
                    items.add(new SelectItem(ad, ad.getLabelToDisplay()));
                }
            }
        }
        newAffinityDomain = null;
        return items;
    }

    @Override
    public void removeAffinityDomain(AffinityDomain inAffinityDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAffinityDomain");
        }
        if ((selectedProfile.getAffinityDomains() != null)
                && selectedProfile.getAffinityDomains().contains(inAffinityDomain)) {
            selectedProfile.getAffinityDomains().remove(inAffinityDomain);
        }
    }

    @Override
    public void addAffinityDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addAffinityDomain");
        }
        if (selectedProfile.getAffinityDomains() != null) {
            selectedProfile.getAffinityDomains().add(newAffinityDomain);
        } else {
            List<AffinityDomain> aDomains = new ArrayList<AffinityDomain>();
            aDomains.add(newAffinityDomain);
            selectedProfile.setAffinityDomains(aDomains);
        }
        displayAffinityDomainList = false;
    }

    public String getGazelleHL7ValidatorURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGazelleHL7ValidatorURL");
        }
        if (gazelleHL7ValidatorURL == null) {
            gazelleHL7ValidatorURL = PreferenceService.getString("gazelle_hl7_validator_url");
        }
        return gazelleHL7ValidatorURL;
    }

}