/*
 * Copyright 2008 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.*;

import static org.jboss.seam.ScopeType.SESSION;

/**
 * <b>Class Description : </b>DomainManager<br>
 * <br>
 * This class manage the Domain object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add a
 * domain</li> <li>Delete a domain</li> <li>Show a domain</li>
 * <li>Edit a domain</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel / Jean-Baptiste Meyer INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @class DomainManager.java
 * @package net.ihe.gazelle.tf.action
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */


@Name("domainManager")
@Scope(SESSION)
@GenerateInterface("DomainManagerLocal")
public class DomainManager implements Serializable, DomainManagerLocal {
    // ~ Statics variables and Class initializer ///////////////////////////////////////////////////////////////////////

    private static final long serialVersionUID = -1357012043456235656L;

    private static final Logger LOG = LoggerFactory.getLogger(DomainManager.class);

    @In
    private EntityManager entityManager;

    /**
     * selected domain
     */
    private Domain selectedDomain;

    // --------------------------------- for linking profiles to domains

    private List<Hl7MessageProfile> hl7MessageProfilesForDP;
    private Filter<Domain> filter;
    /*
         * Parameters for display driving
         */
    private boolean viewing;
    private String showProfiles = "true";
    private List<IntegrationProfile> profilesTarget = new ArrayList<IntegrationProfile>();
    // ************************************* Variable screen messages and labels
    @In
    private Map<String, String> messages;
    /*
     * listDomains.xhtml title, either "Domain Browsing" or "Domain Management" depending on whether user has MasterModel edit privileges
     */
    private String lstTitle;
    /* editDomain title and subtitle, which are Add or Edit depending on context */
    private String editTitle;
    private String editSubTitle;
    private Filter<IntegrationProfile> integrationProfilesFilter;
    private Filter<Document> documentsFilter;

    public Filter<Domain> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            DomainQuery query = new DomainQuery();
            filter = new Filter<Domain>(query.getHQLCriterionsForFilter());
        }
        return filter;
    }

    @Override
    public String getShowProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getShowProfiles");
        }
        return showProfiles;
    }

    @Override
    public void setShowProfiles(String s) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowProfiles");
        }
        showProfiles = s;
    }

    @Override
    public Domain getSelectedDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomain");
        }
        return selectedDomain;
    }

    @Override
    public void setSelectedDomain(Domain selectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomain");
        }
        this.selectedDomain = selectedDomain;
    }

    @Override
    public List<IntegrationProfile> getProfilesSource() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfilesSource");
        }
        List<IntegrationProfile> profilesSource = IntegrationProfile.listAllIntegrationProfiles();
        return profilesSource;
    }

    @Override
    public List<IntegrationProfile> getProfilesTarget() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfilesTarget");
        }
        return profilesTarget;
    }

    @Override
    public void setProfilesTarget(List<IntegrationProfile> l) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProfilesTarget");
        }
        profilesTarget = l;
    }

    @Override
    public FilterDataModel<Domain> getDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomains");
        }
        return new FilterDataModel<Domain>(getFilter()) {
            @Override
            protected Object getId(Domain domain) {
                return domain.getId();
            }
        };
    }

    @Override
    public List<Domain> getAllDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllDomains");
        }
        return Domain.getPossibleDomains();
    }

    @Override
    public List<Hl7MessageProfile> getHl7MessageProfilesForDP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHl7MessageProfilesForDP");
        }
        return hl7MessageProfilesForDP;
    }

    @Override
    public String getEditTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditTitle");
        }
        return messages.get(editTitle);
    }

    @Override
    public String getEditSubTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditSubTitle");
        }
        return messages.get(editSubTitle);
    }

    /**
     * Add or update a domain
     *
     * @param d : Domain to add
     * @return String : JSF page to render
     */

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String updateDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateDomain");
        }

        if (!isKeywordAvailable()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "A domain with this keyword already exists");
            return null;
        }
        if (!isNameAvailable()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "A domain with this name already exists");
            return null;
        }

        if (selectedDomain != null) {
            selectedDomain = entityManager.merge(selectedDomain);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Domain #{domain.name} updated");
            entityManager.flush();
        }

        return "/tf/domain/listDomains.seam";
    }

    /**
     * remove domain. displays side effects screen and asks are you sure. If they are, deleteDomain is called
     *
     * @return - delete effects screen
     * @selectedDomain - Domain to be removed.
     */

    @Override
    @SuppressWarnings("unchecked")
    public String removeDomain(Domain inSelectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeDomain");
        }
        if ((inSelectedDomain != null) && (inSelectedDomain.getId() != null)) {

            selectedDomain = entityManager.find(Domain.class, inSelectedDomain.getId());

            Query q = entityManager
                    .createQuery("select mp from Hl7MessageProfile mp where mp.domain = :inDomain  order by mp.messageType");
            q.setParameter("inDomain", selectedDomain);
            hl7MessageProfilesForDP = q.getResultList();
            return "/tf/domain/domainDeleteSideEffects.seam";
        } else {
            return null;
        }
    }

    /**
     * Delete the selected domain This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedDomain : domain to delete
     * @return String : JSF page to render
     */

    @Override
    public String deleteDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteDomain");
        }
        if (selectedDomain != null) {
            try {
                selectedDomain = entityManager.find(Domain.class, selectedDomain.getId());
                for (Hl7MessageProfile hl7msg : hl7MessageProfilesForDP) {
                    Hl7MessageProfile mp = entityManager.find(Hl7MessageProfile.class, hl7msg.getId());
                    entityManager.remove(mp);
                }
                entityManager.remove(selectedDomain);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Domain removed");
                return listDomains();
            } catch (Exception e) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem deleting domain : " + e.getMessage());
                LOG.error("", e);
                return listDomains();
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Selected domain is null");
            return listDomains();
        }
    }

    /**
     * Render the selected domain
     *
     * @param selectedDomain : Domain to render
     * @return String : JSF page to render
     */
    @Override
    @SuppressWarnings("unchecked")
    public String viewDomain(Domain inDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewDomain");
        }
        selectedDomain = entityManager.find(Domain.class, inDomain.getId());
        viewing = true;
        return "/tf/domain/showDomain.seam";
    }

    /**
     * Edit the selected domain's informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedDomain : domain to edit
     * @return String : JSF page to render
     */
    @Override
    @SuppressWarnings("unchecked")
    public String editDomain(Domain inSelectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editDomain");
        }
        selectedDomain = entityManager.find(Domain.class, inSelectedDomain.getId());
        setShowProfiles("true");
        editTitle = "gazelle.tf.domain.edit.EditTitle";
        editSubTitle = "gazelle.tf.domain.edit.EditSubTitle";
        viewing = false;
        return "/tf/domain/editDomain.seam";
    }

    @Override
    public String editDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editDomain");
        }
        if (selectedDomain != null) {
            setShowProfiles("true");
            editTitle = "gazelle.tf.domain.edit.EditTitle";
            editSubTitle = "gazelle.tf.domain.edit.EditSubTitle";
            viewing = false;
            return "/tf/domain/editDomain.seam";
        } else {
            return null;
        }
    }

    @Override
    public String editDomain(final Domain sd, boolean e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editDomain");
        }
        if (e) {
            return editDomain(sd);
        } else {
            return viewDomain(sd);
        }
    }

    /**
     * Create a new domain. This method is used by a Java client This operation is allowed for some granted users (check the security.drl)
     *
     * @param d : domain to create
     */
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void createDomain(final Domain d) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createDomain");
        }
        entityManager.persist(d);
        entityManager.flush();
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    @Override
    public String getDomainTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainTitle");
        }
        if (Identity.instance().hasPermission("MasterModel", "edit", null)) {
            lstTitle = "gazelle.tf.menu.DomainManagement";
        } else {
            lstTitle = "gazelle.tf.menu.DomainBrowsing";
        }
        return messages.get(lstTitle);
    }

    /**
     * Redirect to a list domains page.
     */
    @Override
    public String listDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listDomains");
        }
        return "/tf/domain/listDomains.seam";
    }

    /**
     * When Add Domain button is clicked, the method called to redirect to editDomain page to add new domain
     *
     * @return String of web page to display
     */
    @Override
    public String addNewDomainButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewDomainButton");
        }
        selectedDomain = new Domain();
        editTitle = "gazelle.tf.domain.edit.AddTitle";
        editSubTitle = "gazelle.tf.domain.edit.AddSubTitle";
        setShowProfiles("false");
        viewing = false;
        return "/tf/domain/editDomain.seam";
    }

    /**
     * Search all the Domains corresponding to the beginning of a String (filled in the domain list box). Eg. If we search the String 'A' we will
     * find the domain : cArdiology, ... This operation is
     * allowed for some granted users (check the security.drl)
     *
     * @param event : Key up event caught from the JSF
     * @return List of Domain objects : List of all domains to display into the list box
     */

    @Override
    public List<Domain> domainAutoComplete(Object event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("domainAutoComplete");
        }

        List<Domain> returnList = null;
        Session s = (Session) entityManager.getDelegate();
        Criteria c = s.createCriteria(Domain.class);

        String searchedString = event.toString();

        if (searchedString.trim().equals("*")) {
            c.addOrder(Order.asc("name"));
            returnList = c.list();
            Domain fakeAllDomain = new Domain();
            fakeAllDomain.setName("*");
            returnList.add(0, fakeAllDomain);
        } else {
            c.add(Restrictions.or(Restrictions.ilike("name", "%" + searchedString + "%"),
                    Restrictions.ilike("keyword", "%" + searchedString + "%")));

            c.addOrder(Order.asc("name"));
            returnList = c.list();

        }
        return returnList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String viewProfiles(Domain inDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewProfiles");
        }

        selectedDomain = inDomain;
        return "/tf/domain/showDomainProfiles.seam";
    }

    /**
     * Action method for link domain profiles button Sets up data for listShuttle to maintain selectedDomain - The domain currently being displayed
     * profilesSource - Integration profiles NOT currently
     * linked to domain profilesTarget - Integration profiles currently linked to domain
     *
     * @return String of web page to display (listShuttle page)
     */
    @Override
    public String linkDomainProfiles(List<IntegrationProfile> integrationProfilesForDP) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkDomainProfiles");
        }
        profilesTarget = new LinkedList<IntegrationProfile>(integrationProfilesForDP);
        Collections.sort(profilesTarget);

        return "/tf/domain/linkDomainProfiles.seam";
    }

    @Override
    public String linkDomainProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkDomainProfiles");
        }
        profilesTarget = new LinkedList<IntegrationProfile>(getIntegrationProfilesForDP().getAllItems(FacesContext.getCurrentInstance()));
        Collections.sort(profilesTarget);

        return "/tf/domain/linkDomainProfiles.seam";
    }

    public FilterDataModel<IntegrationProfile> getIntegrationProfilesForDP() {
        return new FilterDataModel<IntegrationProfile>(getIPFilter()) {
            @Override
            protected Object getId(IntegrationProfile integrationProfile) {
                return integrationProfile.getId();
            }
        };
    }

    private Filter<IntegrationProfile> getIPFilter() {
        if (integrationProfilesFilter == null) {
            IntegrationProfileQuery q = new IntegrationProfileQuery();
            HQLCriterionsForFilter<IntegrationProfile> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<IntegrationProfile>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<IntegrationProfile> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedDomain != null) {
                        IntegrationProfileQuery q = new IntegrationProfileQuery();
                        hqlQueryBuilder.addRestriction(q.domainsForDP().id().eqRestriction(selectedDomain.getId()));
                    }
                }
            });
            this.integrationProfilesFilter = new Filter<IntegrationProfile>(result);
        }
        return this.integrationProfilesFilter;
    }

    public FilterDataModel<Document> getDocuments() {
        return new FilterDataModel<Document>(getDocFilter()) {
            @Override
            protected Object getId(Document doc) {
                return doc.getId();
            }
        };
    }

    private Filter<Document> getDocFilter() {
        if (documentsFilter == null) {
            DocumentQuery q = new DocumentQuery();
            HQLCriterionsForFilter<Document> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<Document>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<Document> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedDomain != null) {
                        DocumentQuery q = new DocumentQuery();
                        hqlQueryBuilder.addRestriction(q.domain().id().eqRestriction(selectedDomain.getId()));
                    }
                }
            });
            this.documentsFilter = new Filter<Document>(result);
        }
        return this.documentsFilter;
    }

    /**
     * Save changes to profiles linked to this domain
     */
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String saveDomainProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveDomainProfiles");
        }
        selectedDomain.setIntegrationProfilesForDP(profilesTarget);
        entityManager.merge(selectedDomain);
        entityManager.flush();

        if (viewing) {
            return "/tf/domain/showDomain.seam";
        }
        return "/tf/domain/editDomain.seam";
    }

    /**
     * Cancel changes to profiles linked to this domain
     */
    @Override
    public String cancelDomainProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelDomainProfiles");
        }
        if (viewing) {
            return "/tf/domain/showDomain.seam";
        }
        return "/tf/domain/editDomain.seam";
    }

    @Override
    public String renderLinkProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("renderLinkProfiles");
        }
        return showProfiles;
    }

    @Override
    public String renderDelete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("renderDelete");
        }
        return showProfiles;
    }

    /**
     * Get domain from keyword got in URL Change selected domain with domain returned Load the viewing domain Method used by redirection link
     */
    @Override
    public void getSpecificDomainFromKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpecificDomainFromKeyword");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String domainkeyword = params.get("keyword");
        selectedDomain = Domain.getDomainByKeyword(domainkeyword);
        this.editDomain(selectedDomain, false);
    }

    public boolean isNameAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isNameAvailable");
        }

        DomainQuery query = new DomainQuery();
        if (selectedDomain.getId() != null) {
            query.id().neq(selectedDomain.getId());
        }
        query.name().eq(selectedDomain.getName());

        if (query.getCount() == 0) {

            return true;

        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("name", StatusMessage.Severity.WARN,
                    "A Domain with this name already exists",
                    "A Domain with this name already exists");
            return false;
        }
    }

    public boolean isKeywordAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isKeywordAvailable");
        }

        DomainQuery query = new DomainQuery();
        if (selectedDomain.getId() != null) {
            query.id().neq(selectedDomain.getId());
        }
        query.keyword().eq(selectedDomain.getKeyword());

        if (query.getCount() == 0) {

            return true;

        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("keyword", StatusMessage.Severity.WARN,
                    "A Domain with this keyword already exists",
                    "A Domain with this keyword already exists");
            return false;
        }
    }
}
