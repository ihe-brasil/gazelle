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

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.users.model.Address;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Iso3166CountryCode;
import net.ihe.gazelle.users.model.Person;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * <b>Class Description : </b>AddressManager<br>
 * <br>
 * This class manage the Address object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add an
 * address</li> <li>Delete an address</li> <li>Show an
 * address</li> <li>Edit an address</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @class AddressManager.java
 * @package net.ihe.gazelle.users.action
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */

@Name("addressManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("AddressManagerLocal")
public class AddressManager implements Serializable, AddressManagerLocal {
    // ~ Statics variables and Class initializer ///////////////////////////////////////////////////////////////////////

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450911336361283760L;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AddressManager.class);

    // ~ Attributes ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;

    /**
     * Institution of the logged in user, variable used between business and presentation layer
     */

    private Institution selectedInstitution;
    private List<Address> addresses;

    private Address selectedAddress;
    /**
     * Selected Mailing Address for an institution, variable used between business and presentation layer
     */
    private Address selectedMailingAddress;

    /**
     * Selected Billing Address for an institution, variable used between business and presentation layer
     */
    private Address selectedBillingAddress;

    /**
     * Selected Iso3166CountryCode (for creation, initialization, etc... ), variable used between business and presentation layer
     */
    private Iso3166CountryCode newIsoCountryCode;

    /**
     * Selected institution choosen by the admin in a management page (users/systems/contacts..) - Institution object managed by this bean ,
     * variable used between business and presentation layer
     */
    @In(required = false)
    private Institution choosenInstitutionForAdmin;

    private Boolean displayAddressPanelForMailing;
    private Boolean displayAddEditAddressPanelForMailing;
    private Boolean displayDeleteConfirmationAddressPanelForMailing;

    private Boolean displayAddressPanelForBilling;
    private Boolean displayAddEditAddressPanelForBilling;
    private Boolean displayDeleteConfirmationAddressPanelForBilling;

    private List<Person> contactsUsingAddressToDelete = null;

    /**
     * Person object managed my this manager bean as the current object (after creation/edition)
     */

    private Person selectedContact;

    // ~ Methods ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Static method - Find all addresses corresponding to a company
     *
     * @return List <Address>
     */
    public static List<Address> getAddressesForSelectedInstitution(Institution currentInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAddressesForSelectedInstitution");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.clear();

        List<Address> institutionAddresses = null;

        if ((currentInstitution != null) && (currentInstitution.getId() != null)) {

            currentInstitution = entityManager.find(Institution.class, currentInstitution.getId());

            institutionAddresses = currentInstitution.getAddresses();

        }

        return institutionAddresses;
    }

    /**
     * Create an address ready to be persisted/merged in the DB from an address (without the country) and a country. This operation is allowed for
     * all users (nothing to check the security.drl)
     *
     * @param Address a : this is the working address (with or without an Iso3166CountryCode address...)
     * @return Address ready to be persisted, containing all the address informations including the country object (Iso3166CountryCode)
     */
    @SuppressWarnings("unchecked")
    public static Address prepareAddressForPersistence(Address a) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Address prepareAddressForPersistence");
        }
        if ((a == null) || (a.getIso3166CountryCode() == null)) {
            return null;
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        List<Iso3166CountryCode> foundIso3166CountryCode;
        Query query;
        String countryName;

        // patch for USA - we persist the address with UNITED STATES even if the user enters "USA" instead of "UNITED STATED"
        if ((a.getIso3166CountryCode().getName() == null)) {

            return null;
        }

        if (a.getIso3166CountryCode().getName().equals("USA")) {
            countryName = "UNITED STATES";
        } else {
            countryName = a.getIso3166CountryCode().getName();
        }

        query = entityManager
                .createQuery("SELECT country FROM Iso3166CountryCode country where country.name = :countryname");
        query.setParameter("countryname", countryName);

        foundIso3166CountryCode = query.getResultList();
        // This way won't crash the application if no country found
        if (foundIso3166CountryCode.size() == 1) {
            a.setIso3166CountryCode(foundIso3166CountryCode.get(0));
        } else {

            FacesMessages.instance().addToControlFromResourceBundleOrDefault("countryText",
                    StatusMessage.Severity.WARN, "gazelle.validator.users.address.countryDoesNotExist",
                    "A country with that name does not exist. Please select another country...");
            return null;
        }

        if (a.getAddressLine2() == null) {
            a.setAddressLine2("");
        }

        return a;
    }

    /**
     * Validate an address. An address is valid if all fields are blank or it misses no required field (country, address, city)
     *
     * @param Address a : this is the working address (with or without an Iso3166CountryCode address...)
     * @return Boolean. True if the validation is done successfully or not
     */
    public static Boolean validateAddress(Address a) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Boolean validateAddress");
        }

        Boolean addressValid = true;

        if (a != null) {

            if ((!a.getCity().equals("")) || (!a.getAddress().equals("")) || (!a.getZipCode().equals(""))
                    || (!a.getState().equals("")) || (!a.getIso3166CountryCode().getName().equals(""))) {

                if ((!a.getCity().equals("")) && (!a.getAddress().equals("")) && (!a.getZipCode().equals(""))
                        && (!a.getState().equals("")) && (!a.getIso3166CountryCode().getName().equals(""))) {

                } else {
                    if (a.getCity().equals("")) {

                        StatusMessages.instance().addToControlFromResourceBundleOrDefault("addressCity",
                                StatusMessage.Severity.WARN, "gazelle.validator.users.institution.CityNotSelected",
                                "The city has to be filled. Please enter a city name...");
                        addressValid = false;
                    }
                    if (a.getIso3166CountryCode().getName().equals("")) {

                        StatusMessages.instance().addToControlFromResourceBundleOrDefault("countryText",
                                StatusMessage.Severity.WARN, "gazelle.validator.users.institution.CountryNotSelected",
                                "The country has to be filled. Please enter a country...");
                        addressValid = false;
                    }
                    if (a.getAddress().equals("")) {

                        StatusMessages.instance().addToControlFromResourceBundleOrDefault("addressAddress",
                                StatusMessage.Severity.WARN, "gazelle.validator.users.institution.AddressNotSelected",
                                "The address field has to be filled. Please enter an address...");
                        addressValid = false;
                    }
                }
            }
        }

        return addressValid;
    }

    /**
     * Validate an address. An address is valid if all fields are blank or it misses no required field (country, address, city)
     *
     * @param Address a : this is the working address (with or without an Iso3166CountryCode address...)
     * @return Boolean. True if the validation is done successfully or not
     */
    public static Boolean validateAddressWithGlobalMessages(Address a) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Boolean validateAddressWithGlobalMessages");
        }

        Boolean addressValid = true;

        if (a != null) {

            if ((!a.getCity().equals("")) || (!a.getAddress().equals("")) || (!a.getIso3166CountryCode().getName().equals(""))) {

                if ((!a.getCity().equals("")) && (!a.getAddress().equals("")) && (!a.getIso3166CountryCode().getName().equals(""))) {

                } else {
                    if (a.getCity().equals("")) {

                        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.validator.users.institution.CityNotSelected");
                        addressValid = false;
                    }
                    if (a.getIso3166CountryCode().getName().equals("")) {

                        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.validator.users.institution.CountryNotSelected");
                        addressValid = false;
                    }
                    if (a.getAddress().equals("")) {

                        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.validator.users.institution.AddressNotSelected");
                        addressValid = false;
                    }
                }
            }
        }

        return addressValid;
    }

    /**
     * Find all addresses corresponding to a company
     */
    @Override
    public void getAddressesForSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAddressesForSelectedInstitution");
        }

        if (selectedInstitution == null) {
            selectedInstitution = (Institution) Component.getInstance("selectedInstitution");
        }

        if (selectedInstitution != null) {
            selectedInstitution = entityManager.find(Institution.class, selectedInstitution.getId());

            addresses = getAddressesForSelectedInstitution(selectedInstitution);
            selectedMailingAddress = selectedInstitution.getMailingAddress();
        } else {
            LOG.error("selectedInstitution is null !!!!");
        }
    }

    @Override
    public List<Address> getAddresses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAddresses");
        }
        return addresses;
    }

    @Override
    public void setAddresses(List<Address> addresses) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAddresses");
        }
        this.addresses = addresses;
    }

    @Override
    public Boolean getDisplayAddressPanelForMailing() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayAddressPanelForMailing");
        }
        return displayAddressPanelForMailing;
    }

    @Override
    public void setDisplayAddressPanelForMailing(Boolean displayAddressPanelForMailing) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAddressPanelForMailing");
        }
        this.displayAddressPanelForMailing = displayAddressPanelForMailing;
    }

    @Override
    public Boolean getDisplayAddEditAddressPanelForMailing() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayAddEditAddressPanelForMailing");
        }
        return displayAddEditAddressPanelForMailing;
    }

    @Override
    public void setDisplayAddEditAddressPanelForMailing(Boolean displayAddEditAddressPanelForMailing) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAddEditAddressPanelForMailing");
        }
        this.displayAddEditAddressPanelForMailing = displayAddEditAddressPanelForMailing;
    }

    @Override
    public Boolean getDisplayDeleteConfirmationAddressPanelForMailing() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayDeleteConfirmationAddressPanelForMailing");
        }
        return displayDeleteConfirmationAddressPanelForMailing;
    }

    @Override
    public void setDisplayDeleteConfirmationAddressPanelForMailing(
            Boolean displayDeleteConfirmationAddressPanelForMailing) {
        this.displayDeleteConfirmationAddressPanelForMailing = displayDeleteConfirmationAddressPanelForMailing;
    }

    @Override
    public Boolean getDisplayAddressPanelForBilling() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayAddressPanelForBilling");
        }
        return displayAddressPanelForBilling;
    }

    @Override
    public void setDisplayAddressPanelForBilling(Boolean displayAddressPanelForBilling) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAddressPanelForBilling");
        }
        this.displayAddressPanelForBilling = displayAddressPanelForBilling;
    }

    @Override
    public Boolean getDisplayAddEditAddressPanelForBilling() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayAddEditAddressPanelForBilling");
        }
        return displayAddEditAddressPanelForBilling;
    }

    @Override
    public void setDisplayAddEditAddressPanelForBilling(Boolean displayAddEditAddressPanelForBilling) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayAddEditAddressPanelForBilling");
        }
        this.displayAddEditAddressPanelForBilling = displayAddEditAddressPanelForBilling;
    }

    @Override
    public Boolean getDisplayDeleteConfirmationAddressPanelForBilling() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayDeleteConfirmationAddressPanelForBilling");
        }
        return displayDeleteConfirmationAddressPanelForBilling;
    }

    @Override
    public void setDisplayDeleteConfirmationAddressPanelForBilling(
            Boolean displayDeleteConfirmationAddressPanelForBilling) {
        this.displayDeleteConfirmationAddressPanelForBilling = displayDeleteConfirmationAddressPanelForBilling;
    }

    /**
     * Add (create/update) an address to the database This operation is allowed for some granted users (check the security.drl)
     *
     * @param a : Address to add (create/update)
     * @return String : JSF page to render
     */
    @Override
    //@Restrict("#{s:hasPermission('AddressManager', 'addAddress', null)}")
    public void addAddress(Address inAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addAddress");
        }

        if (inAddress == null) {
            return;
        }

        selectedAddress = inAddress;

        selectedAddress = prepareAddressForPersistence(selectedAddress);

        if (!validateAddressWithGlobalMessages(selectedAddress)) {
            LOG.warn("Address validation has failed ");
            return;
        }

        if (selectedAddress.getId() == null) {

            selectedAddress = entityManager.merge(selectedAddress);

            if (selectedInstitution.getAddresses() == null) {
                selectedInstitution.setAddresses(new ArrayList<Address>());
            }

            selectedInstitution.getAddresses().add(selectedAddress);
            addresses.add(selectedAddress);
            entityManager.merge(selectedInstitution);

            entityManager.flush();
        } else {
            entityManager.merge(selectedAddress);
            entityManager.flush();
        }

        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.users.address.created");
        displayListAddresses();
    }

    /**
     * Update the address informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param a : Address to be updated
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'updateAddress', null)}")
    public void updateAddress(final Address a) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateAddress");
        }
        entityManager.merge(a);
    }

    /**
     * Delete the selected address This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedAddress : Address to delete
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'deleteAddress', null)}")
    public String deleteAddress(final Address selectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAddress");
        }

        entityManager.remove(selectedAddress);
        entityManager.flush();

        return "/users/address/listAddresses.seam";
    }

    /**
     * Delete the selected address This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedAddress : Address to delete
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'deleteAddress', null)}")
    public void deleteSelectedAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedAddress");
        }

        if (selectedAddress == null) {
            return;
        }
        Boolean okForDelete = true;

        okForDelete = checkSelectedAddressBeforeDeleting(selectedAddress);

        selectedAddress = entityManager.find(Address.class, selectedAddress.getId());

        if (okForDelete) {

            if (selectedInstitution != null) {
                selectedInstitution.getAddresses().remove(selectedAddress);
                addresses.remove(selectedAddress);
            }
            entityManager.merge(selectedInstitution);
            entityManager.remove(selectedAddress);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.users.address.deleted");
            entityManager.flush();
            displayListAddresses();
            contactsUsingAddressToDelete = null;

        } else {
            LOG.warn("Address cannot be deleted because is used: " + selectedAddress.getAddress() + " - "
                    + selectedAddress.getCity());

        }

    }

    @Override
    public void deleteSelectedAddress(Address inAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedAddress");
        }
        selectedAddress = inAddress;
        deleteSelectedAddress();
    }

    /**
     * Check the selected address before deleting - we cannot delete an address used as mailing or billing or by a contact. This operation is
     * allowed for some granted users (check the security.drl)
     *
     * @param selectedAddress : Address to delete
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'deleteAddress', null)}")
    public Boolean checkSelectedAddressBeforeDeleting(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkSelectedAddressBeforeDeleting");
        }

        Boolean okForDelete = true;
        this.selectedAddress = inSelectedAddress;

        if (selectedAddress != null) {
            List<Institution> listOfInstitutions = Address.listCompaniesWithThatAddress(selectedAddress);

            if (listOfInstitutions.size() == 0) {
                okForDelete = true;
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Address already used by the company");
                return false;
            }

            // Check if this address is used by a contact

            List<Person> companyContacts = Person.listAllContactsForCompany(selectedInstitution);

            contactsUsingAddressToDelete = new Vector<Person>();

            for (int i = 0; i < companyContacts.size(); i++) {
                if (companyContacts.get(i).getAddress() != null) {
                    if (companyContacts.get(i).getAddress().getId().equals(selectedAddress.getId())) {
                        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Address already used Address used for contact "
                                + companyContacts.get(i).getFirstname() + " "
                                + companyContacts.get(i).getLastname());

                        contactsUsingAddressToDelete.add(companyContacts.get(i));
                        okForDelete = false;
                    }

                }
            }

        }

        return okForDelete;

    }

    @Override
    public void checkSelectedAddressBeforeDeletingFromMailing(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkSelectedAddressBeforeDeletingFromMailing");
        }
        if (checkSelectedAddressBeforeDeleting(inSelectedAddress)) {
            selectedMailingAddress = inSelectedAddress;
            displayAddressPanelForMailing = false;
            displayAddEditAddressPanelForMailing = false;
            displayDeleteConfirmationAddressPanelForMailing = true;
        }
    }

    @Override
    public void checkSelectedAddressBeforeDeletingFromBilling(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkSelectedAddressBeforeDeletingFromBilling");
        }
        if (checkSelectedAddressBeforeDeleting(inSelectedAddress)) {
            selectedBillingAddress = inSelectedAddress;
            displayAddressPanelForBilling = false;
            displayAddEditAddressPanelForBilling = false;
            displayDeleteConfirmationAddressPanelForBilling = true;
        }

    }

    /**
     * Render the selected address This operation is allowed for some granted users (check the security.drl)
     *
     * @param a : Address to render
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'viewAddress', null)}")
    public String viewAddress(final Address a) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewAddress");
        }
        return "/users/address/showAddress.seam";
    }

    /**
     * Edit the selected address' informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param selectedAddress : address to edit
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'editAddress', null)}")
    public String editAddress(final Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editAddress");
        }
        selectedAddress = inSelectedAddress;

        return "/users/address/editAddress.seam";
    }

    @Override
    public void editSelectedAddressFromMailing(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSelectedAddressFromMailing");
        }

        if (inSelectedAddress != null) {
            selectedMailingAddress = entityManager.find(Address.class, inSelectedAddress.getId());

            displayAddressPanelForMailing = false;
            displayAddEditAddressPanelForMailing = true;
            displayDeleteConfirmationAddressPanelForMailing = false;

        }
    }

    @Override
    public void editSelectedAddressFromBilling(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSelectedAddressFromBilling");
        }

        if (inSelectedAddress != null) {
            selectedBillingAddress = entityManager.find(Address.class, inSelectedAddress.getId());

            displayAddressPanelForBilling = false;
            displayAddEditAddressPanelForBilling = true;
            displayDeleteConfirmationAddressPanelForBilling = false;

        }
    }

    /**
     * Create a new address. This method is used by a Java client This operation is allowed for some granted users (check the security.drl)
     *
     * @param a : address to create
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'createAddress', null)}")
    public void createAddress(final Address inAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createAddress");
        }
        entityManager.persist(inAddress);
        entityManager.flush();
    }

    /**
     * Search all the countries corresponding to the beginning of a String (filled in the countries list box). Eg. If we search the String 'F' we
     * will find the countries : Finlande, France,... This
     * operation is allowed for some granted users (check the security.drl)
     *
     * @param event : Key up event caught from the JSF
     * @return List of Iso3166CountryCode : List of all countries to display into the list box
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'countryAutoComplete', null)}")
    public List<Iso3166CountryCode> countryAutoComplete(Object event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("countryAutoComplete");
        }

        Iso3166CountryCode isoUSA = new Iso3166CountryCode("US", "USA", "United States of America", "flags/us.gif");

        String searchedString = event.toString();
        List<Iso3166CountryCode> foundSuggestionCountries = new ArrayList<Iso3166CountryCode>();
        Iterator<Iso3166CountryCode> iterator = getIso3166CountryCodes().iterator();

        while (iterator.hasNext()) {
            Iso3166CountryCode elem = (iterator.next());

            if (((elem.getName() != null) && (elem.getName().toLowerCase().indexOf(searchedString.toLowerCase()) == 0))
                    || "".equals(searchedString)) {
                foundSuggestionCountries.add(elem);
            }

        }

        // patch for USA - we display USA even if the user enter "USA" instead of "UNITED STATED"
        // If user enters USA or UNITED STATES, address will be registered with "US" as country id (in both cases).
        if (("us".equals(searchedString.toLowerCase())) || ("usa".equals(searchedString.toLowerCase()))) {
            // patch for USA - we display USA even if the user enters "USA" instead of "UNITED STATED"
            foundSuggestionCountries.add(isoUSA);
        }

        return foundSuggestionCountries;
    }

    /**
     * Get all the countries existing in the database This operation is allowed for some granted users (check the security.drl)
     *
     * @return List of Iso3166CountryCode : List of all countries
     */
    @Override
    @Restrict("#{s:hasPermission('AddressManager', 'getIso3166CountryCodes', null)}")
    public List<Iso3166CountryCode> getIso3166CountryCodes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIso3166CountryCodes");
        }
        List<Iso3166CountryCode> iso3166CountryCodes;

        Query query = entityManager.createQuery("SELECT countries FROM Iso3166CountryCode countries");
        iso3166CountryCodes = query.getResultList();

        return iso3166CountryCodes;
    }

    /**
     * Update the selected country in the institution. This operation is allowed for some granted users (check the security.drl)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void updateCountry() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateCountry");
        }
        List<Iso3166CountryCode> foundIso3166CountryCode;
        Query query;

        query = entityManager
                .createQuery("SELECT country FROM Iso3166CountryCode country where country.name = :countryname");
        query.setParameter("countryname", selectedInstitution.getMailingAddress().getIso3166CountryCode().getName());

        foundIso3166CountryCode = query.getResultList();

        // This way won't crash the application if no country is found
        if (foundIso3166CountryCode.size() == 1) {
            (selectedInstitution.getMailingAddress())
                    .setIso3166CountryCode((Iso3166CountryCode) foundIso3166CountryCode.get(0).clone());
        }

    }

    @Override
    public Address getSelectedMailingAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedMailingAddress");
        }
        return selectedMailingAddress;
    }

    @Override
    public void setSelectedMailingAddress(Address inAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedMailingAddress");
        }
        selectedMailingAddress = inAddress;
    }

    @Override
    public void updateSelectedMailingAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedMailingAddress");
        }

        if ((selectedInstitution != null) && (selectedMailingAddress != null)) {
            selectedInstitution.setMailingAddress(selectedMailingAddress);
            entityManager.merge(selectedInstitution);
            displayAddressPanelForMailing = true;
            displayAddEditAddressPanelForMailing = false;
            displayDeleteConfirmationAddressPanelForMailing = false;
        }
    }

    @Override
    public void updateSelectedMailingAddress(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedMailingAddress");
        }

        if (inSelectedAddress == null) {
            return;
        } else {
            selectedMailingAddress = inSelectedAddress;
        }

        updateSelectedMailingAddress();
    }

    @Override
    public void removeSelectedInstitutionMailingAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeSelectedInstitutionMailingAddress");
        }
        if (selectedInstitution != null) {
            selectedInstitution.setMailingAddress(null);
        }
        selectedInstitution = entityManager.merge(selectedInstitution);
        selectedMailingAddress = null;
    }

    @Override
    public Address getSelectedBillingAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedBillingAddress");
        }
        return selectedBillingAddress;
    }

    @Override
    public void setSelectedBillingAddress(Address selectedBillingAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedBillingAddress");
        }
        this.selectedBillingAddress = selectedBillingAddress;
    }

    @Override
    public void updateSelectedBillingAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedBillingAddress");
        }

        if (selectedContact != null) {
            if (selectedContact.getId() != null) {

                selectedContact.setAddress(selectedBillingAddress);
                entityManager.merge(selectedInstitution);
                entityManager.merge(selectedContact);

                displayAddressPanelForBilling = true;
                displayAddEditAddressPanelForBilling = false;
                displayDeleteConfirmationAddressPanelForBilling = false;
            } else {
                LOG.error("selectedContact is null!");
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No contact is registered, please add one before !");
            }
        }
    }

    @Override
    public void updateSelectedBillingAddress(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedBillingAddress");
        }

        if (inSelectedAddress == null) {
            return;
        } else {
            selectedBillingAddress = inSelectedAddress;
        }

        updateSelectedBillingAddress();
    }

    /**
     * Set the selected Billing Address to a contact
     *
     * @param Address : selected Billing address
     */
    @Override
    public void updateSelectedContactAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedContactAddress");
        }

        if (selectedMailingAddress != null) {

        }

        if (selectedContact != null) {
            if (selectedContact.getId() != null) {
                try {
                    selectedContact.setAddress(selectedMailingAddress);

                    displayAddressPanelForMailing = true;
                    displayAddEditAddressPanelForMailing = false;
                    displayDeleteConfirmationAddressPanelForMailing = false;

                } catch (Exception e) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : " + e.getMessage());
                    LOG.error("Error :  updateSelectedContactAddress( ) " + e.getMessage());
                }
            } else {

                selectedContact.setAddress(selectedMailingAddress);

                displayAddressPanelForMailing = true;
                displayAddEditAddressPanelForMailing = false;
                displayDeleteConfirmationAddressPanelForMailing = false;
            }
        } else {
            LOG.error("selectedContact is null!");
        }
    }

    @Override
    public void updateSelectedContactAddress(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedContactAddress");
        }

        if (inSelectedAddress != null) {
            selectedMailingAddress = inSelectedAddress;
        } else {
            return;
        }

        updateSelectedContactAddress();
    }

    @Override
    public void deleteAddressForCurrentContact() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAddressForCurrentContact");
        }
        if (selectedContact != null) {
            selectedContact.setAddress(null);
        }
        selectedContact = entityManager.merge(selectedContact);
    }

    /**
     * New address - Action performed when user clicks on link 'Add new address'
     */

    @Override
    public void addNewAddressFromMailingPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewAddressFromMailingPanel");
        }

        displayAddressPanelForMailing = false;
        displayAddEditAddressPanelForMailing = true;
        displayDeleteConfirmationAddressPanelForMailing = false;

        selectedMailingAddress = new Address();
        selectedMailingAddress.setIso3166CountryCode(new Iso3166CountryCode(entityManager.find(
                Iso3166CountryCode.class, "-")));

    }

    @Override
    public void addNewAddressFromBillingPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewAddressFromBillingPanel");
        }

        displayAddressPanelForBilling = false;
        displayAddEditAddressPanelForBilling = true;
        displayDeleteConfirmationAddressPanelForBilling = false;

        selectedBillingAddress = new Address();
        selectedBillingAddress.setIso3166CountryCode(new Iso3166CountryCode(entityManager.find(
                Iso3166CountryCode.class, "-")));

    }

    @Override
    public void cancelNewAddressFromBillingPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelNewAddressFromBillingPanel");
        }

        displayAddressPanelForBilling = true;
        displayAddEditAddressPanelForBilling = false;
        displayDeleteConfirmationAddressPanelForBilling = false;

    }

    @Override
    public void cancelNewAddressFromMailingPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelNewAddressFromMailingPanel");
        }

        displayAddressPanelForMailing = true;
        displayAddEditAddressPanelForMailing = false;
        displayDeleteConfirmationAddressPanelForMailing = false;

    }

    @Override
    public Iso3166CountryCode getNewIsoCountryCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNewIsoCountryCode");
        }

        if (newIsoCountryCode == null) {

        }
        return newIsoCountryCode;
    }

    @Override
    public void setNewIsoCountryCode(Iso3166CountryCode newIsoCountryCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNewIsoCountryCode");
        }

        this.newIsoCountryCode = newIsoCountryCode;
    }

    @Override
    public List<Person> getContactsUsingAddressToDelete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getContactsUsingAddressToDelete");
        }
        return contactsUsingAddressToDelete;
    }

    @Override
    public void setContactsUsingAddressToDelete(List<Person> contactsUsingAddressToDelete) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setContactsUsingAddressToDelete");
        }
        this.contactsUsingAddressToDelete = contactsUsingAddressToDelete;
    }

    @Override
    public Address getSelectedAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedAddress");
        }
        return selectedAddress;
    }

    @Override
    public void setSelectedAddress(Address inSelectedAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedAddress");
        }
        this.selectedAddress = inSelectedAddress;

    }

    @Override
    public void retrieveOutjectedVariables() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveOutjectedVariables");
        }

        selectedInstitution = (Institution) Component.getInstance("selectedInstitution");
        selectedContact = (Person) Component.getInstance("selectedContact");
        if (selectedInstitution.getId() != null) {
            selectedInstitution = entityManager.find(Institution.class, selectedInstitution.getId());
        }
        if (selectedContact.getId() != null) {
            selectedContact = entityManager.find(Person.class, selectedContact.getId());
        }

    }

    @Override
    public void displayListAddresses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayListAddresses");
        }
        getAddressesForSelectedInstitution();
        displayAddressPanelForMailing = true;
        displayAddEditAddressPanelForMailing = false;
        displayDeleteConfirmationAddressPanelForMailing = false;

        displayAddressPanelForBilling = true;
        displayAddEditAddressPanelForBilling = false;
        displayDeleteConfirmationAddressPanelForBilling = false;
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
