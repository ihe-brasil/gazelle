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
package net.ihe.gazelle.tm.users.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.financial.FinancialSummary;
import net.ihe.gazelle.tm.financial.FinancialSummaryOneSystem;
import net.ihe.gazelle.tm.financial.model.Invoice;
import net.ihe.gazelle.tm.systems.model.InstitutionSystem;
import net.ihe.gazelle.tm.systems.model.InstitutionSystemQuery;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.action.PersonManager;
import net.ihe.gazelle.users.model.*;
import net.ihe.gazelle.users.model.Role;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>Class Description : </b>InstitutionManagerTM<br>
 * <br>
 * This class manage the Institution object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add an
 * institution</li> <li>Delete an institution</li> <li>
 * Show an institution</li> <li>Edit an institution</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @class InstitutionManagerTM.java
 * @package net.ihe.gazelle.tm.users.action
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */

@Name("institutionManagerTM")
@Scope(ScopeType.SESSION)
@GenerateInterface("InstitutionManagerTMLocal")
public class InstitutionManagerTM implements Serializable, InstitutionManagerTMLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = 1764766380971875773L;
    private static final Logger LOG = LoggerFactory.getLogger(InstitutionManagerTM.class);
    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;
    private Institution selectedInstitution;

    /**
     * selectedInvoiceForInstitution object managed my this manager bean
     */
    @In(required = false)
    @Out(required = false)
    private Invoice selectedInvoiceForInstitution;

    /**
     * Person object managed my this manager bean as the current object (after creation/edition)
     */
    private Person selectedContact;

    /**
     * FinancialSummary contains informations for billing (nb systems/nb domains/ fees amounts)
     */
    private FinancialSummary financialSummary;

    /**
     * List of Financial summary for EACH system of a company (nb domains/ fees amounts), used to display financial details of systems
     */
    private List<FinancialSummaryOneSystem> financialSummaryForOneSystem;

    /**
     * Institution object : Choosen Institution, that admin selected in the institution list
     */
    @In(required = false)
    private Institution choosenInstitutionForAdmin;

    private System systemToRemove;

    private Institution institutionToRemove;

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

    @Override
    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }

        if (selectedInstitution == null) {
            selectedInstitution = (Institution) Component.getInstance("selectedInstitution");
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
    public Invoice getSelectedInvoiceForInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInvoiceForInstitution");
        }
        return selectedInvoiceForInstitution;
    }

    @Override
    public void setSelectedInvoiceForInstitution(Invoice selectedInvoiceForInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInvoiceForInstitution");
        }
        this.selectedInvoiceForInstitution = selectedInvoiceForInstitution;
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
    public FinancialSummary getFinancialSummary() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFinancialSummary");
        }
        return financialSummary;
    }

    @Override
    public void setFinancialSummary(FinancialSummary financialSummary) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFinancialSummary");
        }
        this.financialSummary = financialSummary;
    }

    @Override
    public List<FinancialSummaryOneSystem> getFinancialSummaryForOneSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFinancialSummaryForOneSystem");
        }
        return financialSummaryForOneSystem;
    }

    @Override
    public void setFinancialSummaryForOneSystem(List<FinancialSummaryOneSystem> financialSummaryForOneSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFinancialSummaryForOneSystem");
        }
        this.financialSummaryForOneSystem = financialSummaryForOneSystem;
    }

    /**
     * Delete the selected instution(allowed by admin in insatitutions mgmt page)
     */
    @Override
    public void deleteInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteInstitution");
        }

        deleteInstitution(selectedInstitution);
    }

    @Override
    public void deleteInstitution(Institution institution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteInstitution");
        }
        try {

            if (institution != null) {
                net.ihe.gazelle.tm.systems.model.System.deleteSystemsForCompany(institution);

                Person.deleteAllContacts(selectedInstitution);

                User.deleteAllUsers(selectedInstitution);

                Invoice.deleteAllInvoicesForCompany(selectedInstitution);

                Institution.deleteInstitutionWithoutDependencies(selectedInstitution);

                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Company " + selectedInstitution.getName() + " deleted");

                selectedInstitution = null;
            } else {
                LOG.error("deleteInstitution - institution is null !!!");
            }

        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem deleting an institution : " + e.getMessage());
        }
    }

    /**
     * Check if an invoice exists for that institution and that testing session, if not, we instanciate the invoice object correlated to that
     * institution
     *
     * @param Institution institution
     */
    @Override
    public void checkInvoiceIntanciation(Institution institution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkInvoiceIntanciation");
        }
        // Check invoice instanciation
        try {
            TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

            // We check if the invoice exists (cause if the testing session changed, the invoice may not exist)

            EntityManager em = EntityManagerService.provideEntityManager();
            selectedInvoiceForInstitution = Invoice.getInvoiceForAnInstitutionAndATestingSession(em, institution,
                    activatedTestingSession);

            // if it does not exist, we create it
            if (selectedInvoiceForInstitution == null) {

                selectedInvoiceForInstitution = new Invoice(institution, activatedTestingSession);
                selectedInvoiceForInstitution.setFeesPaid(BigDecimal.ZERO);
                selectedInvoiceForInstitution.setFeesDue(BigDecimal.ZERO);
                selectedInvoiceForInstitution.setFeesDiscount(BigDecimal.ZERO);
                selectedInvoiceForInstitution.setVatAmount(BigDecimal.ZERO);

            }
            // else we update the invoice (cause number of systems may have changed, number of domains too) :
            else {

            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem checking Invoice instanciation : " + e.getMessage());
        }
    }

    @Override
    public String saveInvoiceInformationsAndInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveInvoiceInformationsAndInstitution");
        }
        try {

            if (!ApplicationManager.instance().isProductRegistry()) {
                if (selectedInvoiceForInstitution != null) {

                    selectedInvoiceForInstitution = entityManager.merge(selectedInvoiceForInstitution);
                    entityManager.flush();
                    // Calculate new amounts for invoice - Fees and VAT changes when a system is deleted

                    calculateFinancialSummaryDTO();

                    if (selectedContact != null) {

                        if ((!selectedContact.getFirstname().trim().equals(""))
                                || (!selectedContact.getLastname().trim().equals(""))
                                || (!selectedContact.getEmail().trim().equals(""))
                                || (!selectedContact.getPersonalFax().trim().equals(""))
                                || (!selectedContact.getPersonalPhone().trim().equals(""))
                                || (!selectedContact.getCellPhone().trim().equals(""))) {

                            boolean isContactValid = net.ihe.gazelle.users.action.PersonManager
                                    .validateFinancialContactInformations(selectedContact);
                            if (!isContactValid) {

                                return null;
                            }

                            selectedContact = entityManager.merge(selectedContact);
                            entityManager.flush();

                        }

                    } else {
                        LOG.error("saveInvoiceInformationsAndInstitution - instanciation error for selectedContact (null)");
                    }
                } else {
                    LOG.error("selectedInvoiceForInstitution == null !!");
                }
            }
            if (selectedInstitution != null) {
                selectedInstitution = entityManager.merge(selectedInstitution);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, selectedInstitution.getKeyword() + " saved");
            }

        } catch (ConstraintViolationException e) {

            ExceptionLogging.logException(e, LOG);
            LOG.error("Error persisting invoice : " + e.getMessage());
        } catch (Exception e) {

            ExceptionLogging.logException(e, LOG);
            LOG.error("Error persisting invoice : " + e.getMessage());
        }

        return "/users/institution/editInstitution.seam";
    }

    /**
     * WARNING: This method initializes variables before calling a static method. This small calling method also WARNING: exists in others beans
     * (Institution, System...). If you update
     * getFinancialSummaryDTO in a bean, don't WARNING: forget also to update other beans (use the Eclipse finder with a 'Search' :
     * getFinancialSummaryDTO ) This method is called to calculate the new
     * due amounts (including TVA, total fees, discount amount...) and create/update invoice objects. This method is called when : 1. When a user
     * or admin add/remove a system 2. When a user or admin
     * add/remove an actor/profile 3. When a user or admin edit company demographics (cause billing country impacts TVA amount) 4. When an admin
     * save invoice informations (because discount as to be
     * taken in consideration).. It initializes variables before calling getFinancialSummaryStatic method which will calculate amounts
     */
    @Override
    public void calculateFinancialSummaryDTO() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculateFinancialSummaryDTO");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        BigDecimal feeFirstSystem = activatedTestingSession.getFeeFirstSystem();
        BigDecimal feeAdditionalSystem = activatedTestingSession.getFeeAdditionalSystem();

        financialSummary = net.ihe.gazelle.tm.financial.action.FinancialManager
                .getFinancialSummaryWithCalculatedAmountsStatic(feeFirstSystem, feeAdditionalSystem,
                        activatedTestingSession.getFeeParticipant(), activatedTestingSession,
                        choosenInstitutionForAdmin, entityManager);
        if (financialSummary != null) {
            financialSummaryForOneSystem = financialSummary.getFinancialSummaryOneSystems();
        } else {
            financialSummaryForOneSystem = null;
        }

        Contexts.getSessionContext().set("financialSummary", financialSummary);
        Contexts.getSessionContext().set("financialSummaryForOneSystem", financialSummaryForOneSystem);

    }

    /**
     * TestManagement : Edit an institution from the database. This method is called when the users clicks on the 'Edit' button This method
     * retrieves all the needed variables for editing the current
     * institution. After that, this methods return the URL of the JSF page to render. This operation is allowed for some granted users (check the
     * security.drl)
     *
     * @param u : Institution to edit
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'updateInstitution', null)}")
    public String updateInstitutionForTM(final int inSelectedInstitutionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateInstitutionForTM");
        }

        selectedInstitution = entityManager.find(Institution.class, inSelectedInstitutionId);

        Contexts.getSessionContext().set("selectedInstitution", selectedInstitution);
        // Initialization for Address

        checkInvoiceIntanciation(selectedInstitution);

        List<Person> foundContacts = Person.listAllBillingContacts(entityManager, selectedInstitution);

        if ((foundContacts == null) || (foundContacts.size() == 0)) {

            selectedContact = new Person();
            selectedContact.setInstitution(selectedInstitution);

            List<PersonFunction> functions = new ArrayList<PersonFunction>();
            functions.add(PersonFunction.getBillingFunction(entityManager));
            selectedContact.setPersonFunction(functions);

        } else if (foundContacts.size() > 1) {
            LOG.error("-----------------------------------------------------------------------------");
            LOG.error("Institution = Id(" + selectedInstitution.getId() + ") " + selectedInstitution.getName());
            LOG.error("Several FINANCIAL contacts found for that institution ! ONE IS REQUIRED ! FATAL Error ! FOUND = "
                    + foundContacts.size());

            for (int l = 0; l < foundContacts.size(); l++) {
                LOG.error(l + " - FINANCIAL contact = Id(" + foundContacts.get(l).getId() + ") "
                        + foundContacts.get(l).getLastname() + " " + foundContacts.get(l).getFirstname());

            }

            LOG.error("-----------------------------------------------------------------------------");

            selectedContact = foundContacts.get(0);

        } else if (foundContacts.size() == 1) {
            selectedContact = foundContacts.get(0);
        }

        if (selectedContact != null) {
            selectedContact.getAddress();
        }
        // We check the financial contact if it exists, we set it as currentConcact
        Contexts.getSessionContext().set("selectedContact", selectedContact);
        Contexts.getSessionContext().set("selectedInstitution", selectedInstitution);

        return "/users/institution/editInstitution.seam";
    }

    @Override
    public String updateInstitutionForTMForLoggedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateInstitutionForTMForLoggedUser");
        }
        return updateInstitutionForTM(Institution.getLoggedInInstitution().getId());
    }

    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'updateInstitution', null)}")
    public String updateMissingContacts(final Institution inSelectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateMissingContacts");
        }
        PersonManager pm = new PersonManager();
        return pm.displayContactsForCompany(inSelectedInstitution);
    }

    @Override
    public void initEditInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initEditInstitution");
        }
        Institution institution = (Institution) Contexts.getSessionContext().get("selectedInstitution");
        if (institution == null) {
            institution = Institution.getLoggedInInstitution();
        }
        updateInstitutionForTM(institution.getId());
    }

    /**
     * TestManagement ONLY !! Render the selected institution This operation is allowed for some granted users (check the security.drl)
     *
     * @param inst : institution to render
     * @return String : JSF page to render
     */
    @Override
    @SuppressWarnings("unchecked")
    @Restrict("#{s:hasPermission('InstitutionManager', 'viewInstitution', null)}")
    public String viewInstitutionForTM(final Institution inSelectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewInstitutionForTM");
        }
        if (inSelectedInstitution == null) {
            return null;
        }
        selectedInstitution = entityManager.find(Institution.class, inSelectedInstitution.getId());
        Query query = entityManager
                .createQuery("select p from Person p join p.personFunction function, Institution i where p.institution = i and p.institution = " +
                        ":inInstitution and function = :inFunction");
        query.setParameter("inFunction", PersonFunction.getBillingFunction(entityManager));
        query.setParameter("inInstitution", selectedInstitution);

        List<Person> foundContacts = query.getResultList();

        if (foundContacts.size() > 1) {
            LOG.error("-----------------------------------------------------------------------------");
            LOG.error("Institution = Id(" + selectedInstitution.getId() + ") " + selectedInstitution.getName());
            LOG.error("Several FINANCIAL contacts found for that institution ! ONE IS REQUIRED ! FATAL Error ! FOUND = "
                    + foundContacts.size());

            for (int l = 0; l < foundContacts.size(); l++) {
                LOG.error(l + " - FINANCIAL contact = Id(" + foundContacts.get(l).getId() + ") "
                        + foundContacts.get(l).getLastname() + " " + foundContacts.get(l).getFirstname());

            }
            LOG.error("-----------------------------------------------------------------------------");
            selectedContact = foundContacts.get(0);

        } else if (foundContacts.size() == 0) {

            selectedContact = new Person();

        } else if (foundContacts.size() == 1) {
            selectedContact = foundContacts.get(0);
        }

        checkInvoiceIntanciation(selectedInstitution);

        return "/users/institution/showInstitution.seam";
    }

    /**
     * TestManagement ONLY !! Render the selected institution page, depending on the logged in user (admin or vendor) This operation is allowed for
     * some granted users (check the security.drl)
     *
     * @param inst : institution to render
     * @return String : JSF page to render
     */
    @Override
    @Restrict("#{s:hasPermission('InstitutionManager', 'viewInstitution', null)}")
    public String viewInstitutionForTM() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewInstitutionForTM");
        }
        String jsfPage;

        choosenInstitutionForAdmin = (Institution) Component.getInstance("choosenInstitutionForAdmin");

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager()) {
            jsfPage = viewInstitutionForTM(choosenInstitutionForAdmin);
        } else {
            jsfPage = viewInstitutionForTM(Institution.getLoggedInInstitution());
        }

        return jsfPage;
    }

    /**
     * Action performed when admin wants to consult institution details from a datatable where the institution name/keyword is rendered
     *
     * @return String : JSF page to render
     */
    @Override
    @SuppressWarnings("unchecked")
    public String viewInstitutionDetailsFromDatatable(String institutionKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewInstitutionDetailsFromDatatable");
        }

        if ((institutionKeyword == null) || (institutionKeyword.length() == 0)) {
            LOG.error("Cannot display company details - institutionKeyword given as paramer is null");
            return "/error.seam";
        }

        List<Institution> list = null;
        Session session = (Session) entityManager.getDelegate();
        Criteria c = session.createCriteria(Institution.class);
        c.add(Restrictions.eq("keyword", institutionKeyword));

        list = c.list();
        if (list.size() == 0) {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "gazelle.users.institution.CannotRenderThisPageCauseThisSystemBelongToSeveralCompany");
            return "/systems/listSystems.xhtml";
        } else {
            selectedInstitution = list.get(0);
            return "/users/institution/showInstitution.seam";
        }
    }

    @Override
    public void retrieveOutjectedVariables() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveOutjectedVariables");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String institutionId = params.get("id");
        if ((institutionId != null) && !"".equals(institutionId)) {
            selectedInstitution = Institution.findInstitutionWithKeyword(institutionId);
            selectedContact = null;
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Unable to find insitution");
        }
    }

    @Override
    public void prepareRemoveInstitution(System system, Institution institution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("prepareRemoveInstitution");
        }
        systemToRemove = system;
        institutionToRemove = institution;
    }

    @Override
    public void removeInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeInstitution");
        }
        InstitutionSystemQuery query = new InstitutionSystemQuery();
        query.system().id().eq(systemToRemove.getId());
        query.institution().id().eq(institutionToRemove.getId());
        List<InstitutionSystem> list = query.getList();
        for (InstitutionSystem institutionSystem : list) {
            entityManager.remove(institutionSystem);
        }
        entityManager.flush();
    }

    public boolean isAllowedToUpdate(Institution ins) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllowedToUpdate");
        }
        Boolean result = false;
        Boolean sameInstitution = false;
        if (Institution.getLoggedInInstitution().equals(ins)) {
            sameInstitution = true;
        }
        if (Role.isLoggedUserAdmin() || (Role.isLoggedUserVendorAdmin() && sameInstitution)) {
            result = true;
        }
        return result;
    }
}
