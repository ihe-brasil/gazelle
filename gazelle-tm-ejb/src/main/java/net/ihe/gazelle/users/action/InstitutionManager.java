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

package net.ihe.gazelle.users.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.users.model.*;
import net.ihe.gazelle.users.model.Role;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * <b>Class Description : </b>InstitutionManager<br>
 * <br>
 * This class manage the Institution object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add an
 * institution</li> <li>Delete an institution</li> <li>
 * Show an institution</li> <li>Edit an institution</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @class InstitutionManager.java
 * @package net.ihe.gazelle.users.action
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */

@Name("institutionManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("InstitutionManagerLocal")
public class InstitutionManager implements Serializable, InstitutionManagerLocal {

    // ~ Statics variables and Class initializer ///////////////////////////////////////////////////////////////////////

    private static final String UNCHECKED = "unchecked";

    private static final String QUERY2 = "query = ";

    private static final String USERS_INSTITUTION_SHOW_INSTITUTION_SEAM = "/users/institution/showInstitution.seam";

    private static final String USERS_INSTITUTION_EDIT_INSTITUTION_SEAM = "/users/institution/editInstitution.seam";

    private static final String USERS_INSTITUTION_CREATE_INSTITUTION_SEAM = "/users/institution/createInstitution.seam";

    private static final String ERROR_VALIDATING_THE_INSTITUTION_NAME2 = "Error validating the institution name : ";

    private static final String ERROR_VALIDATING_THE_INSTITUTION_NAME = "Error validating the institution name !!! ";

    private static final String INSTITUTION_ALREADY_EXISTING = " - institution already existing (";

    private static final String RESULTS2 = " results)";

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450911336361283760L;

    private static final Logger LOG = LoggerFactory.getLogger(InstitutionManager.class);
    // ~ Attributes ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;

    /**
     * Institution object managed by this manager bean during creation and edition
     */
    private Institution selectedInstitution;

    /**
     * List of all institution objects to be managed by this manager bean
     */
    private List<Institution> institutions;

    private Filter<Institution> filter;

    /**
     * Address object managed by this manager bean
     */
    private Address selectedAddress;

    /**
     * Selected institution choosen by the admin in a management page (users/systems/contacts..) - Institution object managed by this bean ,
     * variable used between business and presentation layer
     */
    @In(required = false)
    @Out(required = false)
    private Institution choosenInstitutionForAdmin;

    /**
     * Person object managed my this manager bean as the current object (after creation/edition)
     */
    private Person selectedContact;

    private Boolean addAllInstitution;

    // ~ Methods ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings(UNCHECKED)
    public static boolean validateInstitutionName(String currentInstitutionName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean validateInstitutionName");
        }

        EntityManager em = EntityManagerService.provideEntityManager();

        Query query = null;
        Integer results = 0;
        List<Institution> listInstitutions = null;

        query = em.createQuery("SELECT distinct inst FROM Institution inst where inst.name = :institutionname");
        query.setParameter("institutionname", currentInstitutionName);

        listInstitutions = query.getResultList();
        results = listInstitutions.size();

        if (results == 0) {

            return true;

        } else {
            return false;
        }

    }

    @SuppressWarnings(UNCHECKED)
    public static boolean validateInstitutionKeyword(String currentInstitutionKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean validateInstitutionKeyword");
        }

        EntityManager em = EntityManagerService.provideEntityManager();
        Query query = null;
        Integer results = 0;
        List<Institution> listInstitutions = null;

        // We retrieve the institution from the database with the same keyword
        query = em.createQuery("SELECT distinct inst FROM Institution inst where inst.keyword = :institutionkeyword");
        query.setParameter("institutionkeyword", currentInstitutionKeyword);

        listInstitutions = query.getResultList();
        results = listInstitutions.size();

        // We check results/cases to determinate if the validation is right or not
        if (results == 0) {

            return true;

        } else {

            return false;
        }
    }

    /**
     * Find all institutions existing in the database
     */
    @Override
    public void findInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findInstitutions");
        }
        institutions = Institution.listAllInstitutions();
    }

    public Filter<Institution> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter<Institution>(getHQLCriterion());
        }
        return filter;
    }

    private HQLCriterionsForFilter<Institution> getHQLCriterion() {
        InstitutionQuery query = new InstitutionQuery();
        HQLCriterionsForFilter<Institution> criterionsForFilter = query.getHQLCriterionsForFilter();
        return criterionsForFilter;
    }

    public FilterDataModel<Institution> getFilteredInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilteredInstitutions");
        }
        return new FilterDataModel<Institution>(getFilter()) {
            @Override
            protected Object getId(Institution institution) {
                return institution.getId();
            }
        };
    }

    public Integer computeNbOfInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("computeNbOfInstitutions");
        }
        InstitutionQuery query = new InstitutionQuery();
        return query.getCount();
    }

    @Override
    public Person getSelectedContact() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedContact");
        }
        return selectedContact;
    }

    @Override
    public void setSelectedContact(Person selectedContact) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedContact");
        }
        this.selectedContact = selectedContact;
    }

    @Override
    public Address getSelectedAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedAddress");
        }
        return selectedAddress;
    }

    @Override
    public void setSelectedAddress(Address selectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedAddress");
        }
        this.selectedAddress = selectedAddress;
    }

    @Override
    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }
        return selectedInstitution;
    }

    @Override
    public void setSelectedInstitution(Institution selectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitution");
        }
        this.selectedInstitution = selectedInstitution;
    }

    @Override
    public List<Institution> getInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutions");
        }
        return institutions;
    }

    @Override
    public void setInstitutions(List<Institution> institutions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInstitutions");
        }
        this.institutions = institutions;
    }

    @Override
    public Institution getChoosenInstitutionForAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChoosenInstitutionForAdmin");
        }

        choosenInstitutionForAdmin = (Institution) Component.getInstance("choosenInstitutionForAdmin");

        return choosenInstitutionForAdmin;
    }

    @Override
    public void setChoosenInstitutionForAdmin(Institution choosenInstitutionForAdmin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setChoosenInstitutionForAdmin");
        }
        this.choosenInstitutionForAdmin = choosenInstitutionForAdmin;
        Contexts.getSessionContext().set("choosenInstitutionForAdmin", choosenInstitutionForAdmin);

    }

    /**
     * Return the name of the logged in Institution
     *
     * @return String : Institution logged in name
     */
    @Override
    public String getInstitutionLoggedName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionLoggedName");
        }
        return Institution.getLoggedInInstitution().getName();
    }

    /**
     * Return the keyword of the logged in Institution
     *
     * @return String : Institution logged in keyword
     */
    @Override
    public String getInstitutionLoggedKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionLoggedKeyword");
        }
        return Institution.getLoggedInInstitution().getKeyword();
    }

    /**
     * Create an institution into the database This operation is allowed for some granted users (check the security.drl)
     *
     * @param u : Institution to create
     * @param a : Mailing Address to create
     * @param i : Country to map with the mailing address
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'addInstitution', null)}")
    public String createInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createInstitution");
        }
        if (selectedInstitution == null) {
            return null;
        }

        User loginUser = User.loggedInUser();

        List<Role> roles;
        Address addressReadyToBePersisted;

        // If the institution does not exist, we create it
        if (selectedInstitution.getId() == null) {

            // This way won't crash the application if institution type is not selected
            if (selectedInstitution.getInstitutionType() == null) {

                StatusMessages.instance().addToControlFromResourceBundleOrDefault("inplaceSelect",
                        StatusMessage.Severity.WARN, "gazelle.validator.users.institution.typeNotSelected",
                        "The type of institution has to be selected. Please select an institution type...");
                return USERS_INSTITUTION_CREATE_INSTITUTION_SEAM;
            }

            // We prepare the address ( merging the address object and the Iso3166CountryCode object)
            addressReadyToBePersisted = net.ihe.gazelle.users.action.AddressManager
                    .prepareAddressForPersistence(selectedInstitution.getMailingAddress());

            if (addressReadyToBePersisted == null) {

                return USERS_INSTITUTION_CREATE_INSTITUTION_SEAM;
            }

            // We create the mailing address

            addressReadyToBePersisted = entityManager.merge(addressReadyToBePersisted);

            entityManager.flush();

            selectedInstitution.setMailingAddress(addressReadyToBePersisted);

            if (selectedInstitution.getAddresses() == null) {
                selectedInstitution.setAddresses(new ArrayList<Address>());
            }

            selectedInstitution.getAddresses().add(addressReadyToBePersisted);

            // We create the billing address -
            // For institution creation, we set the Billing address by default as 0 (null address, country US).
            // For TestManagement/Gazelle, a xhtml page (createFinancialForInstitution.seam) asks the billing address, the the new billing address
            // is updated at that time.
            // Billing address is not used for ProductRegistry

            // If the user has no role, then he is a brand new user. We declare him with vendor_admin and vendor_user roles
            if ((!Role.isLoggedUserAdmin()) && (!Role.isLoggedUserProjectManager())) {

                roles = new Vector<Role>();
                roles.add(Role.getVENDOR_ADMIN_ROLE());
                roles.add(Role.getVENDOR_ROLE());

                loginUser = entityManager.find(User.class, loginUser.getId());

                loginUser.setRoles(roles);

                loginUser = entityManager.merge(loginUser);

                Identity.instance().addRole(Role.getVENDOR_ADMIN_ROLE().getName());
                Identity.instance().addRole(Role.getVENDOR_ROLE().getName());

            } else {

            }

            selectedInstitution = entityManager.merge(selectedInstitution);

            // If the user is not an admin/project manager/ or editor, then it means that a new user has created a new institution
            // In that case, we map the new user with the brand new institution
            if ((!Role.isLoggedUserAdmin()) && (!Role.isLoggedUserProjectManager())) {

                loginUser.setInstitution(selectedInstitution);
                // Note JRC : we cannot persist this user before, because we need to persist the institution before.
                loginUser = entityManager.merge(loginUser);
            } else {

                choosenInstitutionForAdmin = selectedInstitution;

            }

            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                    "gazelle.users.institution.faces.InstitutionSuccessfullyCreated", selectedInstitution.getName());
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.users.institution.faces.NewIconsAvailable");

        } else {
            LOG.error("method createInstitution called, but id should be null - id already exists = "
                    + selectedInstitution.getId());
            return USERS_INSTITUTION_CREATE_INSTITUTION_SEAM;
        }

        return null;

    }

    /**
     * Create an institution into the ProductRegistry database. For PR, we do not need to enter Financial informations, contrary to TestManagement.
     * This method has been done to separate behaviors
     * between PR and TM.
     *
     * @return String : JSF page to render
     */
    @Override
    public String createInstitutionInPR() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createInstitutionInPR");
        }

        if (selectedInstitution == null) {
            return null;
        }
        String returnedString = null;

        returnedString = createInstitution();

        if (returnedString == null) {
            // For PR, when creation is done, we show entered information (For TM/Gazelle, we enter financial informations after institution creation)
            return USERS_INSTITUTION_SHOW_INSTITUTION_SEAM;
        }

        return returnedString;
    }

    /**
     * Create an institution into the TestManagement database. For TM, we need to enter Financial informations, contrary to ProductRegistry. This
     * method has been done to separate behaviors between PR
     * and TM.
     *
     * @return String : JSF page to render
     */
    @Override
    public String createInstitutionInTM() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createInstitutionInTM");
        }
        if (selectedInstitution == null) {
            return null;
        }

        createInstitution();

        selectedContact = new Person();
        Address nullAddress = entityManager.find(Address.class, 0);

        selectedContact.setAddress(nullAddress);
        selectedContact.setInstitution(selectedInstitution);

        List<PersonFunction> personFunctions = new ArrayList<PersonFunction>();
        personFunctions.add(PersonFunction.getBillingFunction(entityManager));
        selectedContact.setPersonFunction(personFunctions);

        selectedInstitution = entityManager.merge(selectedInstitution);

        entityManager.flush();

        return USERS_INSTITUTION_SHOW_INSTITUTION_SEAM;

    }

    /**
     * Update an institution from the database This operation is allowed for some granted users (check the security.drl)
     *
     * @param u : Institution to add (create/update)
     * @return String : JSF page to render
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    @Restrict("#{s:hasPermission('InstitutionManager', 'addInstitution', null)}")
    public String editInstitution(Institution u, Person contact) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editInstitution");
        }

        User loginUser = User.loggedInUser();

        Address addressReadyToBePersisted;

        Boolean isContactValid;

        List<PersonFunction> foundPersonFunctions;

        Iso3166CountryCode countryForMailing = u.getMailingAddress().getCopyIso3166CountryCode();

        countryForMailing.setName(countryForMailing.getName().toUpperCase());

        // If the institution exists, then we update it
        if (u.getId() != null) {

            Institution existing = entityManager.find(Institution.class, u.getId());
            u.setId(existing.getId());

            // This way won't crash the application if institution type is not selected
            if (u.getInstitutionType() == null) {

                StatusMessages.instance().addToControlFromResourceBundleOrDefault("inplaceSelect",
                        StatusMessage.Severity.WARN, "gazelle.validator.users.institution.typeNotSelected",
                        "The type of institution has to be selected. Please select an institution type...");
                return USERS_INSTITUTION_EDIT_INSTITUTION_SEAM;
            }

            // We validate Mailing address fields
            Boolean addressValid = true;
            if (u.getMailingAddress() != null) {

                if (((u.getMailingAddress().getCity() != null) && !u.getMailingAddress().getCity().trim().equals(""))
                        || ((u.getMailingAddress().getAddress() != null) && !u.getMailingAddress().getAddress().trim()
                        .equals(""))
                        || ((u.getMailingAddress().getZipCode() != null) && !u.getMailingAddress().getZipCode().trim()
                        .equals(""))
                        || ((u.getMailingAddress().getState() != null) && !u.getMailingAddress().getState().trim()
                        .equals(""))
                        || ((countryForMailing.getName() != null) && !countryForMailing.getName().trim().equals(""))) {

                    if (((u.getMailingAddress().getCity() == null) || !u.getMailingAddress().getCity().trim()
                            .equals(""))
                            && ((u.getMailingAddress().getAddress() == null) || !u.getMailingAddress().getAddress()
                            .trim().equals(""))
                            && ((u.getMailingAddress().getZipCode() == null) || !u.getMailingAddress().getZipCode()
                            .trim().equals(""))
                            && ((u.getMailingAddress().getState() == null) || !u.getMailingAddress().getState().trim()
                            .equals(""))
                            && ((countryForMailing.getName() == null) || !countryForMailing.getName().trim().equals(""))) {

                    } else {
                        // One or several fields are missing
                        if ((u.getMailingAddress().getCity() == null)
                                || u.getMailingAddress().getCity().trim().equals("")) {

                            StatusMessages.instance().addToControlFromResourceBundleOrDefault(
                                    "addressCityKeywordMailingAddress", StatusMessage.Severity.WARN,
                                    "gazelle.validator.users.institution.CityNotSelected",
                                    "The city has to be filled. Please enter a city name...");
                            addressValid = false;
                        }
                        if ((countryForMailing.getName() == null) || countryForMailing.getName().trim().equals("")) {

                            StatusMessages.instance().addToControlFromResourceBundleOrDefault(
                                    "countryTextMailingAddress", StatusMessage.Severity.WARN,
                                    "gazelle.validator.users.institution.CountryNotSelected",
                                    "The country has to be filled. Please enter a country...");
                            addressValid = false;
                        }
                        if ((u.getMailingAddress().getAddress() == null)
                                || u.getMailingAddress().getAddress().trim().equals("")) {

                            StatusMessages.instance().addToControlFromResourceBundleOrDefault(
                                    "addressAddressKeywordMailingAddress", StatusMessage.Severity.WARN,
                                    "gazelle.validator.users.institution.AddressNotSelected",
                                    "The address field has to be filled. Please enter an address...");
                            addressValid = false;
                        }
                    }
                }
            }

            if (!addressValid) {
                return USERS_INSTITUTION_EDIT_INSTITUTION_SEAM;
            }

            Address existingMailingAddress = entityManager.find(Address.class, u.getMailingAddress().getId());
            addressReadyToBePersisted = u.getMailingAddress();

            // We update the country if it changed, and specially the foreign key in the address table to Iso3166CountryCode table.
            if (!existingMailingAddress.getIso3166CountryCode().getName().equals(countryForMailing.getName())) {
                // We prepare the address ( merging the address object and the Iso3166CountryCode object)

                addressReadyToBePersisted = net.ihe.gazelle.users.action.AddressManager.prepareAddressForPersistence(u
                        .getMailingAddress());

                if (addressReadyToBePersisted == null) {

                    return USERS_INSTITUTION_EDIT_INSTITUTION_SEAM;
                }

            }

            // We update the mailing address
            addressReadyToBePersisted.setId(existingMailingAddress.getId());
            // We update the mailing address in the database
            entityManager.merge(addressReadyToBePersisted);
            u.setMailingAddress(addressReadyToBePersisted);

            // We update the billing address (Gazelle)
            // For TestManagement/Gazelle, a xhtml page (createFinancialForInstitution.seam) asks the billing address, the new billing address is
            // updated at that time.
            // Billing address is not used for ProductRegistry

            // We update the contact
            if (contact != null) {
                // We validate the financial contact information : contact is not required, but if a field is entered, some others become required

                isContactValid = net.ihe.gazelle.users.action.PersonManager
                        .validateFinancialContactInformations(contact);

                if (!isContactValid) {

                    return USERS_INSTITUTION_EDIT_INSTITUTION_SEAM;
                }

                // If fields used for billing contact registrations are empty, and if there is a billing contact in the database for that company,
                // then we delete it
                if ((contact.getLastname().equals("")) && (contact.getFirstname().equals(""))
                        && (contact.getEmail().equals(""))) {

                    Query queryContact = entityManager
                            .createQuery("select p from Person p, Institution i where p.institution.id = i.id and p.isBillingContact = 'true' and i" +
                                    ".id = :institutionId ");
                    queryContact.setParameter("institutionId", u.getId());

                    List<Person> foundContacts = queryContact.getResultList();

                    if (foundContacts.size() == 1) {

                        entityManager.remove(foundContacts.get(0));
                    }
                    selectedContact = new Person();

                }
                // We create the financial contact if fields are entered
                else {

                    contact.setInstitution(selectedInstitution);
                    contact.setAddress(entityManager.find(Address.class, u.getMailingAddress().getId()));

                    // We set the professional function for this contact : Financial contact
                    if (contact.getPersonFunction() == null) {
                        foundPersonFunctions = new Vector<PersonFunction>();
                        foundPersonFunctions.add(PersonFunction.getBillingFunction(entityManager));
                        contact.setPersonFunction(foundPersonFunctions);
                    }

                    // We persist the institution with financial informations
                    selectedContact = entityManager.merge(contact);

                    // We make sure that there is no more than ONE financial contact in database
                    Query queryFinanacial = entityManager
                            .createQuery("select p from Person p, Institution i where p.institution.id = i.id and p.isBillingContact = 'true' and i" +
                                    ".id = :institutionId ");
                    queryFinanacial.setParameter("institutionId", u.getId());

                    List<Person> foundContacts = queryFinanacial.getResultList();

                    if (foundContacts.size() > 1) {

                        for (Person foundContact : foundContacts) {
                            if (!foundContact.getId().equals(selectedInstitution.getId())) {
                                Person oldFinancialContact = entityManager.find(Person.class, foundContact
                                        .getId());
                                entityManager.merge(oldFinancialContact);
                            }
                        }
                    }

                }

            }

            // We update the institution in the database
            selectedInstitution = entityManager.merge(u);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                    "gazelle.users.institution.faces.InstitutionSuccessfullyUpdated", selectedInstitution.getName());

            // If the updated institution is the logged-in institution (because institution could be also updated by the admin)

            if (selectedInstitution.getId().compareTo(loginUser.getInstitution().getId()) == 0) {

                entityManager.merge(u);
            }

        } else {
            LOG.error("method updateInstitution called, but id should be not null - id = null ");
        }

        return USERS_INSTITUTION_SHOW_INSTITUTION_SEAM;
    }

    /**
     * Update an institution This operation is allowed for some granted users (check the security.drl)
     *
     * @param inInstitution : Institution to add (create/update)
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'addInstitution', null)}")
    public String editInstitutionForPR(Institution inInstitution, Person contact) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editInstitutionForPR");
        }
        User loginUser = User.loggedInUser();

        Address addressReadyToBePersisted;

        // If the institution exists, then we update it
        if (inInstitution.getId() != null) {

            Institution existing = entityManager.find(Institution.class, inInstitution.getId());
            inInstitution.setId(existing.getId());

            // This way won't crash the application if institution type is not selected
            if (inInstitution.getInstitutionType() == null) {

                StatusMessages.instance().addToControlFromResourceBundleOrDefault("inplaceSelect",
                        StatusMessage.Severity.WARN, "gazelle.validator.users.institution.typeNotSelected",
                        "The type of institution has to be selected. Please select an institution type...");
                return USERS_INSTITUTION_EDIT_INSTITUTION_SEAM;
            }

            // We validate Mailing address fields
            Boolean addressValid = true;
            if (inInstitution.getMailingAddress() != null) {

                if (((inInstitution.getMailingAddress().getCity() != null) && !inInstitution.getMailingAddress()
                        .getCity().equals(""))
                        || ((inInstitution.getMailingAddress().getAddress() != null) && !inInstitution
                        .getMailingAddress().getAddress().equals(""))
                        || ((inInstitution.getMailingAddress().getZipCode() != null) && !inInstitution
                        .getMailingAddress().getZipCode().equals(""))
                        || ((inInstitution.getMailingAddress().getState() != null) && !inInstitution
                        .getMailingAddress().getState().equals(""))) {

                    if (((inInstitution.getMailingAddress().getCity() == null) || !inInstitution.getMailingAddress()
                            .getCity().equals(""))
                            && ((inInstitution.getMailingAddress().getAddress() == null) || !inInstitution
                            .getMailingAddress().getAddress().equals(""))
                            && ((inInstitution.getMailingAddress().getZipCode() == null) || !inInstitution
                            .getMailingAddress().getZipCode().equals(""))
                            && ((inInstitution.getMailingAddress().getState() == null) || !inInstitution
                            .getMailingAddress().getState().equals(""))) {

                    } else {
                        // One or several fields are missing
                        if ((inInstitution.getMailingAddress().getCity() == null)
                                || inInstitution.getMailingAddress().getCity().equals("")) {

                            StatusMessages.instance().addToControlFromResourceBundleOrDefault(
                                    "addressCityKeywordMailingAddress", StatusMessage.Severity.WARN,
                                    "gazelle.validator.users.institution.CityNotSelected",
                                    "The city has to be filled. Please enter a city name...");
                            addressValid = false;
                        }

                        if ((inInstitution.getMailingAddress().getAddress() == null)
                                || inInstitution.getMailingAddress().getAddress().equals("")) {

                            StatusMessages.instance().addToControlFromResourceBundleOrDefault(
                                    "addressAddressKeywordMailingAddress", StatusMessage.Severity.WARN,
                                    "gazelle.validator.users.institution.AddressNotSelected",
                                    "The address field has to be filled. Please enter an address...");
                            addressValid = false;
                        }
                    }
                }
            }
            if (!addressValid) {
                return USERS_INSTITUTION_EDIT_INSTITUTION_SEAM;
            }

            Address existingMailingAddress = entityManager.find(Address.class, inInstitution.getMailingAddress()
                    .getId());
            addressReadyToBePersisted = inInstitution.getMailingAddress();

            // We update the mailing address
            addressReadyToBePersisted.setId(existingMailingAddress.getId());
            // We update the mailing address in the database
            entityManager.merge(addressReadyToBePersisted);
            inInstitution.setMailingAddress(addressReadyToBePersisted);

            // We update the institution in the database
            selectedInstitution = entityManager.merge(inInstitution);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                    "gazelle.users.institution.faces.InstitutionSuccessfullyUpdated", selectedInstitution.getName());

            // If the updated institution is the logged-in institution (because institution could be also updated by the admin)

            if (selectedInstitution.getId().compareTo(loginUser.getInstitution().getId()) == 0) {

                entityManager.merge(inInstitution);
            }
        } else {
            LOG.error("method updateInstitution called, but id should be not null - id = null ");
        }

        return USERS_INSTITUTION_SHOW_INSTITUTION_SEAM;
    }

    /**
     * Add an institution button (from the management list of institutions) This operation is allowed for some granted users (check the security
     * .drl) (This does not add a button, but rather adds an
     * institution !)
     *
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'addNewInstitutionButton', null)}")
    public String addNewInstitutionButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewInstitutionButton");
        }

        selectedInstitution = new Institution();
        Iso3166CountryCode mailingCountry = new Iso3166CountryCode(entityManager.find(Iso3166CountryCode.class, "-"));

        Address address = new Address();
        address.setIso3166CountryCode(mailingCountry);

        selectedInstitution.setMailingAddress(address);

        selectedContact = new Person();
        Iso3166CountryCode mailingForPerson = new Iso3166CountryCode(entityManager.find(Iso3166CountryCode.class, "-"));

        Address addressForPersonn = new Address();
        addressForPersonn.setIso3166CountryCode(mailingForPerson);
        selectedContact.setAddress(addressForPersonn);

        selectedAddress = new Address();
        selectedAddress.setIso3166CountryCode(mailingCountry);

        return USERS_INSTITUTION_CREATE_INSTITUTION_SEAM;
    }

    /**
     * ProductRegistry ONLY !! Render the selected institution This operation is allowed for some granted users (check the security.drl)
     *
     * @param inst : institution to render
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'viewInstitution', null)}")
    public String viewInstitution(final Institution inst) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewInstitution");
        }

        selectedInstitution = entityManager.find(Institution.class, inst.getId());

        return USERS_INSTITUTION_SHOW_INSTITUTION_SEAM;
    }

    /**
     * Edit an institution from the session to the database : We get the institution id (for the logged user), and then we edit it. This operation
     * is allowed for some granted users (check the
     * security.drl)
     *
     * @param u : Institution to edit
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'editInstitutionSession', null)}")
    public String editInstitutionSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editInstitutionSession");
        }

        selectedInstitution = Institution.getLoggedInInstitution();

        return USERS_INSTITUTION_EDIT_INSTITUTION_SEAM;
    }

    @Override
    public String viewInstitutionSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewInstitutionSession");
        }

        selectedInstitution = Institution.getLoggedInInstitution();

        return USERS_INSTITUTION_SHOW_INSTITUTION_SEAM;
    }

    /**
     * Create a new institution. This method is used by a Java client This operation is allowed for some granted users (check the security.drl)
     *
     * @param inst : institution to create
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'createInstitution', null)}")
    public void createInstitution(final Institution inst) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createInstitution");
        }

        entityManager.persist(inst);
        entityManager.flush();
    }

    /**
     * Create financial informations for the current institution. For TM, we need to enter Financial informations, contrary to ProductRegistry.
     *
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'createFinancialInformationsForInstitution', null)}")
    public String createFinancialInformationsForInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createFinancialInformationsForInstitution");
        }

        if ((selectedAddress == null) || (selectedContact == null)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem : selected address or selected contact is null. Cannot go further");
            return null;
        }
        Address addressReadyToBePersisted;
        Boolean isContactValid = true;
        PersonFunction financialFunction;
        List<PersonFunction> foundPersonFunctions;

        // We prepare the address ( merging the address object and the Iso3166CountryCode object)
        addressReadyToBePersisted = net.ihe.gazelle.users.action.AddressManager
                .prepareAddressForPersistence(selectedAddress);

        if (addressReadyToBePersisted == null) {

            return "/users/institution/createFinancialForInstitution.seam";
        }

        // We create the billing address
        addressReadyToBePersisted = entityManager.merge(addressReadyToBePersisted);
        entityManager.flush();

        // We retrieve the institution object sync/ with entity
        selectedInstitution = entityManager.find(Institution.class, selectedInstitution.getId());

        // We set the billing address to that institution
        Address billingAddress = entityManager.merge(addressReadyToBePersisted);

        // We validate the financial contact information : contact is not required, but if a field is entered, some others become required

        isContactValid = net.ihe.gazelle.users.action.PersonManager
                .validateFinancialContactInformations(selectedContact);

        if (!isContactValid) {
            return "/users/institution/createFinancialForInstitution.seam";
        }

        // We create the financial contact if fields are entered
        if (!selectedContact.getLastname().equals("")) {

            selectedContact.setInstitution(selectedInstitution);
            selectedContact.setAddress(billingAddress);

            // We set the professional function for this contact : Financial contact

            financialFunction = PersonFunction.getBillingFunction(entityManager);

            if (selectedContact.getPersonFunction() != null) {

                foundPersonFunctions = selectedContact.getPersonFunction();
            } else {

                foundPersonFunctions = new Vector<PersonFunction>();
                foundPersonFunctions.add(PersonFunction.getBillingFunction(entityManager));
            }

            foundPersonFunctions.add(financialFunction);
            selectedContact.setPersonFunction(foundPersonFunctions);

            selectedContact = entityManager.merge(selectedContact);

            // We persist the institution with financial informations
            selectedInstitution = entityManager.merge(selectedInstitution);

            entityManager.flush();

        } else {

            selectedContact = new Person();

        }

        return USERS_INSTITUTION_SHOW_INSTITUTION_SEAM;
    }

    /**
     * Validate the institution name filled in the JSF (length, unique)
     *
     * @param currentInstitutionName : String corresponding to the institution name to validate
     * @param institutionId          : Integer corresponding to the institution id to validate
     * @return boolean : if the validation is done without error (true)
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    @Restrict("#{s:hasPermission('InstitutionManager', 'validateInstitutionName', null)}")
    public boolean validateInstitutionName(String currentInstitutionName, Integer institutionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateInstitutionName");
        }

        Query query = null;
        Integer results = 0;
        List<Institution> listInstitutions = null;

        query = entityManager
                .createQuery("SELECT distinct inst FROM Institution inst where inst.name = :institutionname");
        query.setParameter("institutionname", currentInstitutionName);

        listInstitutions = query.getResultList();
        results = listInstitutions.size();

        if (results == 0) {

            return true;

        } else if (results == 1) {
            if (institutionId.intValue() == listInstitutions.get(0).getId().intValue()) {
                // We found one user in the database with the same name, but it's the current institution to update > NO error

                return true;

            } else {
                // We found one user in the database with the same name, but it's NOT the current institution to update > ERROR
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("institutionName",
                        StatusMessage.Severity.WARN, "gazelle.validator.users.institution.name.alreadyExists",
                        "An institution with that name already exists. Please choose another name...");

                return false;
            }
        } else {

            LOG.error(ERROR_VALIDATING_THE_INSTITUTION_NAME + currentInstitutionName + INSTITUTION_ALREADY_EXISTING
                    + results + RESULTS2);
            return false;

        }

    }

    /**
     * Validate the institution keyword filled in the JSF (length, unique)
     *
     * @param currentInstitutionKeyword : String corresponding to the institution keyword to validate
     * @param institutionId             : Integer corresponding to the institution id to validate
     * @return boolean : if the validation is done without error (true)
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    @Restrict("#{s:hasPermission('InstitutionManager', 'validateInstitutionKeyword', null)}")
    public boolean validateInstitutionKeyword(String currentInstitutionKeyword, Integer institutionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateInstitutionKeyword");
        }

        Query query = null;
        Integer results = 0;
        List<Institution> listInstitutions = null;

        // We retrieve the institution from the database with the same keyword
        query = entityManager
                .createQuery("SELECT distinct inst FROM Institution inst where inst.keyword = :institutionkeyword");
        query.setParameter("institutionkeyword", currentInstitutionKeyword);

        listInstitutions = query.getResultList();
        results = listInstitutions.size();

        // We check results/cases to determinate if the validation is right or not
        if (results == 0) {

            return true;

        } else if (results == 1) {
            if (institutionId.intValue() == listInstitutions.get(0).getId().intValue()) {
                // We found one user in the database with the same keyword, but it's the current institution to update > NO error

                return true;

            } else {
                // We found one user in the database with the same keyword, but it's NOT the current institution to update > ERROR
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("institutionKeyword",
                        StatusMessage.Severity.WARN, "gazelle.validator.users.institution.keyword.alreadyExists",
                        "An institution with that keyword already exists. Please choose another keyword...");

                return false;
            }
        } else {

            LOG.error("Error validating the institution keyword !!! " + currentInstitutionKeyword
                    + INSTITUTION_ALREADY_EXISTING + results + RESULTS2);
            return false;

        }

    }

    /**
     * Validate the institution address filled in the JSF (length, unique)
     *
     * @param currentInstitutionAddress : String corresponding to the institution address to validate
     * @param institutionId             : Integer corresponding to the institution id to validate
     * @return boolean : if the validation is done without error (true)
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'validateInstitutionAddress', null)}")
    @Deprecated
    public boolean validateInstitutionAddress(String currentInstitutionAddress, Integer institutionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateInstitutionAddress");
        }

        return true;
    }

    /**
     * Search all the Institutions corresponding to the beginning of a String (filled in the countries list box). Eg. If we search the String 'A'
     * we will find the countries : Agfa, Apple,... This
     * operation is allowed for some granted users (check the security.drl)
     *
     * @param event : Key up event caught from the JSF
     * @return List of Institution objects : List of all institutions to display into the list box
     */

    @Override
    @SuppressWarnings(UNCHECKED)
    public List<Institution> institutionAutoComplete(Object event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("institutionAutoComplete");
        }

        List<Institution> returnList = null;
        Session s = (Session) entityManager.getDelegate();
        Criteria c = s.createCriteria(Institution.class);

        String searchedString = event.toString();

        if ((searchedString.trim().equals("*")) || (searchedString.trim().equals(""))
                || (searchedString.trim().equals(" ")) || (searchedString.trim().equals("all"))
                || (searchedString.trim().equals("ALL"))) {
            c.addOrder(Order.asc("name"));
            c.add(Restrictions.gt("id", 0));
            returnList = c.list();
        } else {
            c.add(Restrictions.and(
                    Restrictions.gt("id", 0),
                    Restrictions.or(Restrictions.ilike("name", "%" + searchedString + "%"),
                            Restrictions.ilike("keyword", "%" + searchedString + "%"))));

            c.addOrder(Order.asc("name"));
            returnList = c.list();

        }
        return returnList;
    }

    @Override
    public Boolean getAddAllInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAddAllInstitution");
        }
        return addAllInstitution;
    }

    @Override
    public void setAddAllInstitution(Boolean addAllInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAddAllInstitution");
        }
        this.addAllInstitution = addAllInstitution;
    }

    /**
     * Find and get all institutions existing in the database
     *
     * @return institutions : List of all institutions
     */
    @Override
    public List<Institution> getAllInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllInstitutions");
        }

        institutions = Institution.listAllInstitutions();

        return institutions;
    }

    /**
     * Action performed wwhen admin wants to consult institution details from a datatable where the institution name/keyword is rendered
     *
     * @return String : JSF page to render
     */
    @Override
    public String viewInstitutionDetailsFromDatatable(String institutionKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewInstitutionDetailsFromDatatable");
        }

        List<Institution> list = null;

        Session session = (Session) entityManager.getDelegate();

        Criteria c = session.createCriteria(Institution.class);
        c.add(Restrictions.eq("keyword", institutionKeyword));

        list = c.list();
        if (list.size() != 1) {
            LOG.error("Cannot display company details - 0 or several company with the same keyword !!!!");
            return "/error.seam";
        } else {
            selectedInstitution = list.get(0);
            return USERS_INSTITUTION_SHOW_INSTITUTION_SEAM;
        }
    }

    /**
     * Destroy the Manager bean when the session is over.
     */
    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
