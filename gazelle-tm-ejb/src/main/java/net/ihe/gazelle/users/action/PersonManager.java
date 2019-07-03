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
import net.ihe.gazelle.util.Md5Encryption;
import net.ihe.gazelle.util.Pair;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Class Description : </b>PersonManager<br>
 * <br>
 * This class manage the Person object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add a
 * Contact</li> <li>Delete a Contact</li> <li>Show Contacts</li>
 * <li>Edit Contact</li> <li>etc...</li>
 *
 * @author Jean-Baptiste Meyer/INRIA
 * @version 1.0 - May 06, 2008
 * @class PersonManager.java
 * @package net.ihe.gazelle.users.action
 * @see >
 */

@Scope(ScopeType.SESSION)
@Name("personManager")
@GenerateInterface("PersonManagerLocal")
// @MeasureCalls
public class PersonManager implements Serializable, PersonManagerLocal {
    // ~ Statics variables and Class initializer ///////////////////////////////////////////////////////////////////////

    private static final String SELECTED_INSTITUTION = "selectedInstitution";

    private static final String USERS_CONTACT_LIST_CONTACTS_SEAM = "/users/contact/listContacts.seam";

    private static final String USERS_CONTACT_CREATE_CONTACT_SEAM = "/users/contact/createContact.seam";

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450911236542873760L;

    private static final Logger LOG = LoggerFactory.getLogger(PersonManager.class);
    // ~ Attributes ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * List of all domain objects to be managed my this manager bean
     */
    private List<Person> contacts;
    private Filter<Person> filter;
    /**
     * Person object managed my this manager bean as the current object (after creation/edition)
     */
    private Person selectedContact;
    private List<Pair<Boolean, PersonFunction>> personFunctionsForSelectedContact;

    /**
     * Selected Address object managed by this manager bean
     */
    private Address selectedAddress;

    private Institution selectedInstitution;

    /**
     * PersonFunction object managed my this manager bean
     */
    private PersonFunction personFunction;

    private Boolean billingAssignedForThisCompany;

    @In(required = false)
    private Institution choosenInstitutionForAdmin;

    private Boolean displayViewContactsPanel;
    private Boolean displayAddEditContactsPanel;
    private Boolean displayListOfContactsPanel;

    /**
     * Validate a contact information : This methods through faces messages if there is a validity problem for that contact Then it returns false,
     * to indicate that the validation is failed
     *
     * @param contact : Contact to validate
     * @return Boolean isContactValid : JSF page to render
     */
    public static Boolean validateContactInformations(Person contact) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Boolean validateContactInformations");
        }

        Boolean isContactValid = true;

        if ((contact.getEmail() == null) || (contact.getEmail().equals(""))) {

            StatusMessages.instance().addToControlFromResourceBundleOrDefault("emailContact",
                    StatusMessage.Severity.WARN, "gazelle.validator.users.contact.email.faces.missing",
                    "Please enter an email.");
            isContactValid = false;
        }
        if ((contact.getLastname() == null) || (contact.getLastname().equals(""))) {

            StatusMessages.instance().addToControlFromResourceBundleOrDefault("lastNameContact",
                    StatusMessage.Severity.WARN, "gazelle.validator.users.contact.lastname.faces.missing",
                    "Please enter a lastname.");
            isContactValid = false;
        }
        if ((contact.getFirstname() == null) || (contact.getFirstname().equals(""))) {

            StatusMessages.instance().addToControlFromResourceBundleOrDefault("firstNameContact",
                    StatusMessage.Severity.WARN, "gazelle.validator.users.contact.firstname.faces.missing",
                    "Please enter a firstname.");
            isContactValid = false;
        }

        return isContactValid;

    }

    /**
     * Validate the financial contact information : Financial contact is not required, but if the user enter a field, then some other fields are
     * becoming required. This methods through faces messages
     * if there is a validity problem for that contact Then it returns false, to indicate that the validation is failed
     *
     * @param contact       : Contact to validate
     * @param FacesMessages facesMessages (we pass the FacesMessages object as parameter - because it is a static method)
     * @return Boolean isContactValid : JSF page to render
     */
    public static Boolean validateFinancialContactInformations(Person contact) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Boolean validateFinancialContactInformations");
        }

        Boolean isContactValid = true;

        if (contact != null) {

            if ((!contact.getFirstname().trim().equals("")) || (!contact.getLastname().trim().equals(""))
                    || (!contact.getEmail().trim().equals("")) || (!contact.getPersonalFax().trim().equals(""))
                    || (!contact.getPersonalPhone().trim().equals("")) || (!contact.getCellPhone().trim().equals(""))) {

                isContactValid = true;

                if ((contact.getFirstname() == null) || (contact.getFirstname().trim().equals(""))) {

                    StatusMessages.instance().addToControlFromResourceBundleOrDefault("firstNameFinancialContact",
                            StatusMessage.Severity.WARN, "gazelle.validator.users.contact.firstname.faces.missing",
                            "Please enter a firstname.");
                    isContactValid = false;
                }
                if ((contact.getLastname() == null) || (contact.getLastname().trim().equals(""))) {

                    StatusMessages.instance().addToControlFromResourceBundleOrDefault("lastNameFinancialContact",
                            StatusMessage.Severity.WARN, "gazelle.validator.users.contact.lastname.faces.missing",
                            "Please enter a lastname.");
                    isContactValid = false;
                }
                if ((contact.getEmail() == null) || (contact.getEmail().trim().equals(""))) {

                    StatusMessages.instance().addToControlFromResourceBundleOrDefault("emailFinancialContact",
                            StatusMessage.Severity.WARN, "gazelle.validator.users.contact.email.faces.missing",
                            "Please enter an email.");
                    isContactValid = false;
                }

            }
        } else {
            LOG.error("Error - validateFinancialContactInformations : contact is null");
        }
        return isContactValid;

    }

    public Filter<Person> getFilter() {
        if (filter == null) {
            filter = new Filter<Person>(getHQLCriterionsForFilter());
        }
        return filter;
    }

    private HQLCriterionsForFilter<Person> getHQLCriterionsForFilter() {
        PersonQuery query = new PersonQuery();
        HQLCriterionsForFilter<Person> criterionsForFilter = query.getHQLCriterionsForFilter();
        if (Identity.instance().isLoggedIn() && Identity.instance().hasRole("admin_role")) {
            InstitutionManagerLocal local = (InstitutionManagerLocal) Component.getInstance("institutionManager");
            criterionsForFilter.addPath("institution", query.institution(), local.getChoosenInstitutionForAdmin());
        } else {
            criterionsForFilter.addPath("institution", query.institution(), User.loggedInUser().getInstitution());
        }
        return criterionsForFilter;
    }

    public FilterDataModel<Person> getFilteredPersons() {
        return new FilterDataModel<Person>(getFilter()) {
            @Override
            protected Object getId(Person person) {
                return person.getId();
            }
        };
    }

    @Override
    public Boolean getDisplayViewContactsPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayViewContactsPanel");
        }

        return displayViewContactsPanel;
    }

    @Override
    public void setDisplayViewContactsPanel(Boolean displayViewContactsPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayViewContactsPanel");
        }
        this.displayViewContactsPanel = displayViewContactsPanel;
    }

    @Override
    public Boolean getDisplayAddEditContactsPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayAddEditContactsPanel");
        }

        return displayAddEditContactsPanel;
    }

    @Override
    public void setDisplayAddEditContactsPanel(Boolean displayAddEditContactsPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAddEditContactsPanel");
        }
        this.displayAddEditContactsPanel = displayAddEditContactsPanel;
    }

    @Override
    public Boolean getDisplayListOfContactsPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayListOfContactsPanel");
        }
        return displayListOfContactsPanel;
    }

    @Override
    public void setDisplayListOfContactsPanel(Boolean displayListOfContactsPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayListOfContactsPanel");
        }
        this.displayListOfContactsPanel = displayListOfContactsPanel;
    }

    @Override
    public String displayContactsForCompany(Institution i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayContactsForCompany");
        }
        choosenInstitutionForAdmin = i;
        Contexts.getSessionContext().set("choosenInstitutionForAdmin", choosenInstitutionForAdmin);
        return USERS_CONTACT_LIST_CONTACTS_SEAM;
    }

    @Override
    public Institution getChoosenInstitutionForAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChoosenInstitutionForAdmin");
        }
        return choosenInstitutionForAdmin;
    }

    @Override
    public void setChoosenInstitutionForAdmin(Institution choosenInstitutionForAdmin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setChoosenInstitutionForAdmin");
        }
        this.choosenInstitutionForAdmin = choosenInstitutionForAdmin;
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
    public PersonFunction getPersonFunction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPersonFunction");
        }
        return personFunction;
    }

    @Override
    public void setPersonFunction(PersonFunction personFunction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPersonFunction");
        }
        this.personFunction = personFunction;
    }

    // ~ Methods ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<Pair<Boolean, PersonFunction>> getPersonFunctionsForSelectedContact() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPersonFunctionsForSelectedContact");
        }
        return personFunctionsForSelectedContact;
    }

    @Override
    public void setPersonFunctionsForSelectedContact(
            List<Pair<Boolean, PersonFunction>> personFunctionsForSelectedContact) {
        this.personFunctionsForSelectedContact = personFunctionsForSelectedContact;
    }

    @Override
    public void findContactsForCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findContactsForCompany");
        }

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserAccounting() || Role.isLoggedUserTestingSessionAdmin()) {
            if ((choosenInstitutionForAdmin != null) && (choosenInstitutionForAdmin.getName() != null)) {

                contacts = Person.listAllContactsForCompanyWithName(choosenInstitutionForAdmin.getName());
            } else if ((choosenInstitutionForAdmin == null) || (choosenInstitutionForAdmin.getName() == null)) {
                contacts = Person.listAllContacts();

            }
        }
    }

    @Override
    public void getContactsListDependingInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getContactsListDependingInstitution");
        }
        if (choosenInstitutionForAdmin == null) {
            choosenInstitutionForAdmin = new Institution();
        }

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserAccounting() || Role.isLoggedUserTestingSessionAdmin()) {
            findContactsForCompany();
        } else if (Role.isLoggedUserVendorAdmin() || Role.isLoggedUserVendorUser()) {
            if (contacts != null) {
                contacts.clear();
            }
            contacts = Person.listAllContactsForCompany(Institution.getLoggedInInstitution());
        } else {

        }
    }

    /**
     * Redirect to a contact creation page. Variables are also initialized here before creation
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'addNewContactButton', null)}")
    public void addNewContactButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewContactButton");
        }

        selectedContact = new Person();
        selectedContact.setInstitution(getInstitution());
        setContactAddressToInstitutionAddress();
        selectedContact.setPersonFunction(new ArrayList<PersonFunction>());
        selectedInstitution = selectedContact.getInstitution();
        List<PersonFunction> allPersonFunction = PersonFunction.listPersonFunctions();
        personFunctionsForSelectedContact = new ArrayList<Pair<Boolean, PersonFunction>>();

        for (PersonFunction pf : allPersonFunction) {
            if (selectedContact.getPersonFunction().contains(pf)) {
                personFunctionsForSelectedContact.add(new Pair<Boolean, PersonFunction>(Boolean.TRUE, pf));
            } else {
                personFunctionsForSelectedContact.add(new Pair<Boolean, PersonFunction>(Boolean.FALSE, pf));
            }
        }

        Contexts.getSessionContext().set("selectedContact", selectedContact);
        Contexts.getSessionContext().set(SELECTED_INSTITUTION, selectedInstitution);

        openAddEditPanel();

    }

    /**
     * Set the institution address as default contact address
     */
    private void setContactAddressToInstitutionAddress() {
        if (!selectedContact.getInstitution().getAddresses().isEmpty()) {
            selectedContact.setAddress(selectedContact.getInstitution().getAddresses().get(0));
        } else {
            selectedContact.setAddress(null);
        }
    }

    private Institution getInstitution() {
        Institution institution = null;
        if (Role.isLoggedUserVendorAdmin() || Role.isLoggedUserVendorUser()) {
            institution = Institution.getLoggedInInstitution();
        } else {
            if (choosenInstitutionForAdmin != null) {
                institution = choosenInstitutionForAdmin;
            } else {
                institution = Institution.getLoggedInInstitution();

            }
        }
        return institution;
    }

    /**
     * Redirect to a contact management page. Variables are also initialized here before contacts listing
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'listContacts', null)}")
    public String listContacts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listContacts");
        }
        initListContacts();
        return USERS_CONTACT_LIST_CONTACTS_SEAM;
    }

    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'listContacts', null)}")
    public void initListContacts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initListContacts");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        // Initialization for Address

        selectedAddress = new Address();
        selectedAddress
                .setIso3166CountryCode(new Iso3166CountryCode(entityManager.find(Iso3166CountryCode.class, "-")));

    }

    /**
     * Add (create/update) a contact in the database
     *
     * @param u : Person to add (create/update)
     * @return String : JSF page to render
     */
    // @Restrict("#{s:hasPermission('PersonManager', 'addPerson', null)}")
    @Override
    public String addPerson(final Person u) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addPerson");
        }

        Boolean isContactValid;
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        // We validate the contact information : contact is not required, but if a field is entered, some others become required
        isContactValid = net.ihe.gazelle.users.action.PersonManager.validateFinancialContactInformations(u);
        if (!isContactValid) {

            return "/users/contact/editContact.seam";
        }

        // If the Person does not exist, we create it
        if (u.getId() == null) {

            entityManager.persist(u);
            entityManager.flush();
            selectedContact = entityManager.merge(u);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Person #{contact.firstname} created");
        } else {
            // If the Person exists, we update it

            Person existing = entityManager.find(Person.class, u.getId());
            u.setId(existing.getId());

            // We retrieve and keep the unchanged institution
            u.setInstitution(existing.getInstitution());

            // We merge the updated object in database
            selectedContact = entityManager.merge(u);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Person #{contact.firstname} updated");

        }

        return "/users/contact/showContact.seam";
    }

    /**
     * Update the user's informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param u : Person to be updated
     */
    // @Restrict("#{s:hasPermission('PersonManager', 'updatePerson', null)}")
    @Override
    public void updatePerson(final Person u) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updatePerson");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.merge(u);
    }

    /**
     * Delete the selected user This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedPerson : domain to delete
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'deleteContact', null)}")
    public String deleteContact(final Person selectedPerson) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteContact");
        }

        /**
         * The following code to delete contact's address will need to be fixed
         */

        Person.deleteContactWithFind(selectedPerson);

        if (Role.isLoggedUserAdmin()) {

            findContactsForCompany();

        }

        return USERS_CONTACT_LIST_CONTACTS_SEAM;
    }

    /**
     * Delete the current contact : This action is called from the confirmation modal panel when a user clicks on the button 'confirm' button This
     * operation is allowed for some granted users (check
     * the security.drl)
     *
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'deleteContact', null)}")
    public String deleteContact() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteContact");
        }

        if (selectedContact == null) {
            return null;
        }

        String sReturned = deleteContact(selectedContact);

        findContactsForCompany();

        return sReturned;
    }

    /**
     * Render the selected user This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedPerson : Person to render
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'viewContact', null)}")
    public void viewContact(final Person inSelectedPerson) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewContact");
        }
        if (inSelectedPerson == null) {
            return;
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        selectedContact = entityManager.find(Person.class, inSelectedPerson.getId());
        selectedInstitution = selectedContact.getInstitution();

        Contexts.getSessionContext().set("selectedContact", selectedContact);
        Contexts.getSessionContext().set(SELECTED_INSTITUTION, selectedInstitution);

        openViewPanel();
    }

    /**
     * Edit the selected user's informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedPerson : user to edit
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'editContact', null)}")
    public void saveContact() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveContact");
        }

        if (selectedContact == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Selected contact is null... Cannot persist it");
            return;
        }
        Boolean isContactValid;

        EntityManager entityManager = EntityManagerService.provideEntityManager();

        // We validate the contact information : contact is not required, but if a field is entered, some others become required
        isContactValid = net.ihe.gazelle.users.action.PersonManager
                .validateFinancialContactInformations(selectedContact);
        if (((selectedContact.getFirstname().trim().length() == 0)
                && (selectedContact.getLastname().trim().length() == 0) && (selectedContact.getEmail().trim().length() == 0))
                || (!isContactValid)) {

            StatusMessages.instance().addToControlFromResourceBundleOrDefault("fields", StatusMessage.Severity.WARN,
                    "Please enter the fields above", "Please fill the fields above...");
            return;
        }

        if (selectedContact.getAddress() == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.users.address.PleaseAddAddress']}");
            return;
        }

        List<PersonFunction> listPersonFunction = new ArrayList<PersonFunction>();
        for (Pair<Boolean, PersonFunction> pair : personFunctionsForSelectedContact) {
            if (pair.getObject1()) {
                listPersonFunction.add(pair.getObject2());
            }
        }

        selectedContact.setPersonFunction(listPersonFunction);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Contact firstName:" + selectedContact.getFirstname() + " Lastame:" + selectedContact.getLastname()
                    + " email:" + selectedContact.getEmail() + " personalFax:" + selectedContact.getPersonalFax()
                    + " PersonalPhone :" + selectedContact.getPersonalPhone() + " cellPhone :"
                    + selectedContact.getCellPhone());
        }
        entityManager.merge(selectedContact);

        entityManager.flush();

        if (Role.isLoggedUserAdmin()) {

            findContactsForCompany();

        }

        closeAddEditPanel();
    }

    /**
     * Edit the selected contact's information This operation is allowed for some granted users (check the security.drl)
     *
     * @param currentContact : contact to be edited
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'updateContact', null)}")
    public void updateContact(Person curContact) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateContact");
        }

        if (curContact == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : selected person is null ");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (curContact != null) {
            selectedContact = entityManager.find(Person.class, curContact.getId());
        }
        if (selectedContact != null) {
            selectedInstitution = selectedContact.getInstitution();

            Contexts.getSessionContext().set("selectedContact", selectedContact);
            Contexts.getSessionContext().set(SELECTED_INSTITUTION, selectedInstitution);

            personFunctionsForSelectedContact = new ArrayList<Pair<Boolean, PersonFunction>>();

            List<PersonFunction> allPersonFunction = PersonFunction.listPersonFunctions();
            boolean hasCompanyABillingCompany = Person.companyAlreadyContainsBillingContact(selectedContact
                    .getInstitution());

            for (PersonFunction pf : allPersonFunction) {
                if (pf.equals(PersonFunction.getBillingFunction(entityManager)) && hasCompanyABillingCompany) {
                    if (!selectedContact.getPersonFunction().contains(pf)) {
                        continue;
                    }
                }
                if (selectedContact.getPersonFunction().contains(pf)) {
                    personFunctionsForSelectedContact.add(new Pair<Boolean, PersonFunction>(Boolean.TRUE, pf));
                } else {
                    personFunctionsForSelectedContact.add(new Pair<Boolean, PersonFunction>(Boolean.FALSE, pf));
                }
            }

            openAddEditPanel();
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : selected contact is null ");
        }

    }

    @Override
    public void updateContact() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateContact");
        }

        if (selectedContact != null) {
            updateContact(selectedContact);
        }

    }

    /**
     * Create a new user. This method is used by a Java client This operation is allowed for some granted users (check the security.drl)
     *
     * @param u : user to create
     */
    // @Restrict("#{s:hasPermission('PersonManager', 'createPerson', null)}")
    @Override
    public void createPerson(final Person u) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createPerson");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.persist(u);
        entityManager.flush();
    }

    /**
     * Create a new Contact
     *
     * @param newContact : Contact to create
     * @param addr       : Mailing Address to create
     * @param i          : Country to map with the mailing address
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'createContact', null)}")
    public String createContact(Person newContact, Address addr, Iso3166CountryCode country) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createContact");
        }

        String returnedString = "";
        Boolean isContactValid;
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        // We validate the contact information : contact is not required, but if a field is entered, some others become required
        isContactValid = net.ihe.gazelle.users.action.PersonManager.validateContactInformations(newContact);
        if (!isContactValid) {

            return USERS_CONTACT_CREATE_CONTACT_SEAM;
        }

        if (country != null) {
            country.setName(country.getName().toUpperCase());

            // If Contact does not exist, we will create one
            if (newContact.getId() == null) {

                newContact.setAddress(selectedAddress);

                // We associate the current institution to that contact
                if (Identity.instance().hasRole(Role.ADMINISTRATOR_ROLE_STRING)) {
                    Institution tmp = Institution.findInstitutionWithName(choosenInstitutionForAdmin.getName());
                    if (tmp == null) {
                        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Invalid company");
                        return USERS_CONTACT_CREATE_CONTACT_SEAM;
                    } else {
                        choosenInstitutionForAdmin = tmp;
                        newContact.setInstitution(choosenInstitutionForAdmin);
                    }
                } else {
                    Institution loginUserInstitution = Institution.getLoggedInInstitution();
                    newContact.setInstitution(loginUserInstitution);
                }

                List<PersonFunction> allPersonFunction = PersonFunction.listPersonFunctions();
                boolean hasCompanyABillingCompany = Person.companyAlreadyContainsBillingContact(selectedContact
                        .getInstitution());

                for (PersonFunction pf : allPersonFunction) {
                    if (pf.equals(PersonFunction.getBillingFunction(entityManager)) && hasCompanyABillingCompany) {
                        continue;
                    }

                    personFunctionsForSelectedContact.add(new Pair<Boolean, PersonFunction>(Boolean.FALSE, pf));
                }

                // We create the contact

                entityManager.persist(newContact);
                entityManager.flush();

                returnedString = USERS_CONTACT_LIST_CONTACTS_SEAM;

            } else {
                LOG.error("method createInstitution called, but id should be null - id already exists = "
                        + newContact.getId());
                return USERS_CONTACT_CREATE_CONTACT_SEAM;
            }

            if (returnedString.isEmpty()) {
                // For TM/Gazelle, we enter financial informations after institution creation (For PR, when creation is done, we show entered
                // information)
                return USERS_CONTACT_CREATE_CONTACT_SEAM;
            }

        }

        return returnedString;
    }

    /**
     * Create a new Contact
     *
     * @param newContact : Contact to create
     * @param addr       : Mailing Address to create
     * @param i          : Country to map with the mailing address
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('PersonManager', 'createContact', null)}")
    public String createContactFromNewContactPage(Person newContact, Address addr, Iso3166CountryCode country) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createContactFromNewContactPage");
        }

        String returnedString = USERS_CONTACT_LIST_CONTACTS_SEAM;
        Boolean isContactValid = false;
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        // We validate the contact information : contact is not required, but if a field is entered, some others become required
        isContactValid = net.ihe.gazelle.users.action.PersonManager.validateContactInformations(newContact);
        if (!isContactValid) {

            return USERS_CONTACT_CREATE_CONTACT_SEAM;
        }

        // If Contact does not exist, we will create one
        if (newContact.getId() == null) {

            newContact.setAddress(selectedAddress);

            // We associate the current institution to that contact
            if (Identity.instance().hasRole(Role.ADMINISTRATOR_ROLE_STRING)) {
                Institution tmp = Institution.findInstitutionWithName(choosenInstitutionForAdmin.getName());
                if (tmp == null) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Invalid company");
                    return USERS_CONTACT_CREATE_CONTACT_SEAM;
                } else {
                    choosenInstitutionForAdmin = tmp;
                    newContact.setInstitution(choosenInstitutionForAdmin);
                }
            } else {
                Institution loginUserInstitution = Institution.getLoggedInInstitution();
                newContact.setInstitution(loginUserInstitution);
            }

            List<PersonFunction> listPersonFunction = new ArrayList<PersonFunction>();
            for (Pair<Boolean, PersonFunction> pair : personFunctionsForSelectedContact) {
                if (pair.getObject1()) {
                    listPersonFunction.add(pair.getObject2());
                }
            }

            newContact.setPersonFunction(listPersonFunction);
            // We create the contact

            entityManager.persist(newContact);
            entityManager.flush();

            returnedString = USERS_CONTACT_LIST_CONTACTS_SEAM;

        } else {
            LOG.error("method createInstitution called, but id should be null - id already exists = "
                    + newContact.getId());
            return USERS_CONTACT_CREATE_CONTACT_SEAM;
        }

        return returnedString;
    }

    /**
     * This method is used to hash a password using the MD5 encryption.
     *
     * @param password : Password to encrypt
     * @return String : Encrypted password
     */
    @Override
    public String hashPassword(final String password) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashPassword");
        }
        // We hash the password
        String hashPassword = Md5Encryption.hashPassword(password);

        return hashPassword;
    }

    /**
     * Find all person functions existing in the database
     */

    /**
     * Add (create/update) a PersonFunction in the database
     *
     * @param pFunction : PersonFunction to add (create/update)
     * @return String : JSF page to render
     */
    // @Restrict("#{s:hasPermission('PersonManager', 'addPerson', null)}")
    @Override
    public String addPersonFunction(final PersonFunction pFunction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addPersonFunction");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        // If the PersonFunction does not exist, we create it
        if (pFunction.getId() == null) {

            entityManager.persist(pFunction);
            entityManager.flush();
            personFunction = entityManager.merge(pFunction);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Person function #{personFunction.name} created");
        } else {
            // If the PersonFunction exists, we update it

            Person existing = entityManager.find(Person.class, pFunction.getId());
            pFunction.setId(existing.getId());

            // We merge the updated object in database
            personFunction = entityManager.merge(pFunction);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Person function #{personFunction.name} updated");

        }

        return "/showPersonFunction.seam";
    }

    /**
     * Update the PersonFunction informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param pFunction : PersonFunction to be updated
     */
    @Override
    @Restrict("#{s:hasPermission('PersonFunctionManager', 'updatePersonFunction', null)}")
    public void updatePersonFunction(final PersonFunction pFunction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updatePersonFunction");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.merge(pFunction);
    }

    /**
     * Delete the selected PersonFunction This operation is allowed for some granted users (check the security.drl)
     *
     * @param pFunction : PersonFunction to delete
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('PersonFunctionManager', 'deletePersonFunction', null)}")
    public String deletePersonFunction(final PersonFunction pFunction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deletePersonFunction");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.remove(personFunction);

        return "/listPersonFunctions.seam";
    }

    /**
     * Render the selected PersonFunction This operation is allowed for some granted users (check the security.drl)
     *
     * @param pFunction : PersonFunction to render
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('PersonFunctionManager', 'viewPersonFunction', null)}")
    public String viewPersonFunction(final PersonFunction pFunction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewPersonFunction");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        personFunction = entityManager.find(PersonFunction.class, pFunction.getId());

        return "/showPersonFunction.seam";
    }

    /**
     * Edit the selected PersonFunction informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param pFunction : PersonFunction to edit
     * @return String : JSF page to render
     */
    // @Restrict("#{s:hasPermission('PersonFunctionManager', 'editPersonFunction', null)}")
    @Override
    public String editPersonFunction(final PersonFunction pFunction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editPersonFunction");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        personFunction = entityManager.find(PersonFunction.class, pFunction.getId());

        return "/editPersonFunction.seam";
    }

    /**
     * Create a new PersonFunction. This method is used by a Java client This operation is allowed for some granted users (check the security.drl)
     *
     * @param pFunction : PersonFunction to create
     */
    @Override
    @Restrict("#{s:hasPermission('PersonFunctionManager', 'createPersonFunction', null)}")
    public void createPersonFunction(final PersonFunction pFunction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createPersonFunction");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.persist(pFunction);
        entityManager.flush();
    }

    @Override
    public String displayPersonFunction(Person person) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayPersonFunction");
        }
        if (person != null) {
            if (person.getPersonFunction() != null) {
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < person.getPersonFunction().size(); i++) {
                    s.append(person.getPersonFunction().get(i).getName()).append(" ");
                }
                return s.toString();
            } else {
                return null;
            }
        } else {
            return "";
        }

    }

    @Override
    public Boolean getBillingAssignedForThisCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBillingAssignedForThisCompany");
        }
        return billingAssignedForThisCompany;
    }

    @Override
    public void setBillingAssignedForThisCompany(Boolean billingAssignedForThisCompany) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setBillingAssignedForThisCompany");
        }
        this.billingAssignedForThisCompany = billingAssignedForThisCompany;
    }

    @Override
    public List<Person> getContacts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getContacts");
        }
        return contacts;
    }

    @Override
    public void setContacts(List<Person> contacts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setContacts");
        }
        this.contacts = contacts;
    }

    @Override
    public void initializePanelsAndGetInjections() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializePanelsAndGetInjections");
        }
        displayAddEditContactsPanel = false;
        displayListOfContactsPanel = true;
        displayViewContactsPanel = false;

    }

    @Override
    public void openViewPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openViewPanel");
        }

        displayAddEditContactsPanel = false;
        displayListOfContactsPanel = false;
        displayViewContactsPanel = true;
    }

    @Override
    public void closeViewPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeViewPanel");
        }

        displayAddEditContactsPanel = false;
        displayListOfContactsPanel = true;
        displayViewContactsPanel = false;
    }

    @Override
    public void openAddEditPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openAddEditPanel");
        }

        displayAddEditContactsPanel = true;
        displayListOfContactsPanel = false;
        displayViewContactsPanel = false;
    }

    @Override
    public void closeAddEditPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeAddEditPanel");
        }

        closeViewPanel();
    }

    @Override
    public void outjectInstitutionChoosen() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("outjectInstitutionChoosen");
        }
        if (selectedContact != null) {
            Contexts.getSessionContext().set(SELECTED_INSTITUTION, selectedContact.getInstitution());
        }
        setContactAddressToInstitutionAddress();
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

    public boolean hasContact() {
        return getFilteredPersons().size() > 0;
    }

}