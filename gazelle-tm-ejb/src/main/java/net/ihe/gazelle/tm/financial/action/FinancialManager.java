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
package net.ihe.gazelle.tm.financial.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.action.TFDependenciesManager;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.constraints.AipoRule;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.datamodel.InvoiceDataModel;
import net.ihe.gazelle.tm.financial.FinancialSummary;
import net.ihe.gazelle.tm.financial.FinancialSummaryOneSystem;
import net.ihe.gazelle.tm.financial.model.Invoice;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.users.model.ConnectathonParticipant;
import net.ihe.gazelle.users.model.*;
import net.ihe.gazelle.users.model.Role;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * <b>Class Description : </b>FinancialManager<br>
 * <br>
 * This session bean manage financial activities, such as generate contract and invoice. It corresponds to the Business Layer. All operations to
 * implement are done in this class : <li>generateContract
 * </li> <li>generateInvoice</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel (INRIA, FR) - Jean-Baptiste Meyer (INRIA, FR)
 * @version 1.0 - July 15, 2008
 * @class FinancialManager.java
 * @package net.ihe.gazelle.tm.financial.action
 */

@Scope(ScopeType.SESSION)
@Name("financialManager")
@Synchronized(timeout = 10000)
@GenerateInterface("FinancialManagerLocal")
public class FinancialManager implements Serializable, FinancialManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1L;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(FinancialManager.class);

    // ~ IN/OUT Attributes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;

    /**
     * FinancialSummary contains informations for billing (nb systems/nb domains/ fees amounts)
     */
    @Out(scope = ScopeType.SESSION, required = false)
    private FinancialSummary financialSummary;

    /**
     * List of Financial summary for EACH system of a company (nb domains/ fees amounts), used to display financial details of systems
     */
    @Out(scope = ScopeType.SESSION, required = false)
    private List<FinancialSummaryOneSystem> financialSummaryForOneSystem;

    /**
     * Institution object : Choosen Institution, that admin selected in the institution list
     */
    private Institution selectedInstitution;

    /**
     * List of all invoices for selected testing session
     */

    private InvoiceDataModel listOfInvoices;

    private invoiceTotal invoicesTotals;

    // ~ Attributes //

    private PersonFunction billing;
    private PersonFunction commercial;
    private PersonFunction technical;

    private List<Person> billingContactsForFinancialSummary;
    private List<Person> commercialContactsForFinancialSummary;
    private List<Person> technicalContactsForFinancialSummary;

    private String emailAddressFirstBillingContact;

    private Invoice invoiceSelected;

    public static FinancialSummary getFinancialSummaryWithCalculatedAmountsStatic(BigDecimal feeFirstSystem,
                                                                                  BigDecimal feeAdditionalSystem, BigDecimal participantFee,
                                                                                  TestingSession activatedTestingSession,
                                                                                  Institution selectedInstitution, EntityManager em) {
        if (activatedTestingSession == null) {
            activatedTestingSession = TestingSession.getSelectedTestingSession();
        }
        if ((activatedTestingSession == null) || (!activatedTestingSession.isContractRequired())) {
            LOG.error("FinancialSummary : No contract is required for this testing session.");
            return null;
        }
        if (selectedInstitution == null) {
            LOG.error("FinancialSummary : No institution was selected to be invoiced.");
            return null;
        }
        List<ConnectathonParticipant> lcp = ConnectathonParticipant.filterConnectathonParticipant(em,
                selectedInstitution, activatedTestingSession);
        List<SystemInSession> lsis = SystemInSession.getSystemsInSessionForCompanyForSession(em, selectedInstitution,
                activatedTestingSession);
        FinancialSummary financialSummary = null;
        List<FinancialSummaryOneSystem> lfs = null;
        if (lsis != null) {
            boolean first = true;
            BigDecimal systemFee = null;
            lfs = new ArrayList<FinancialSummaryOneSystem>();
            for (SystemInSession systemInSession : lsis) {
                if (first) {
                    systemFee = feeFirstSystem;
                } else {
                    systemFee = feeAdditionalSystem;
                }
                FinancialSummaryOneSystem fsstmp = getFinancialSummaryOneSystem(systemInSession, systemFee, em);
                lfs.add(fsstmp);
                first = false;
            }
            financialSummary = getInitialFinancialSummaryFromSIS(lfs, lcp, participantFee, selectedInstitution, activatedTestingSession);
            Invoice invoice = updateInvoiceFromInitialFinancialSummary(financialSummary, lcp.size(),
                    selectedInstitution, activatedTestingSession, em);
            updateFinancialSummaryFromInvoice(invoice, financialSummary, em);
        }
        return financialSummary;
    }

    // ~ Methods //

    private static void updateFinancialSummaryFromInvoice(Invoice invoice, FinancialSummary financialSummary,
                                                          EntityManager em) {
        financialSummary.setFeeVAT(invoice.getVatAmount());
        if (!invoice.getFeesDiscount().equals(new BigDecimal("0.00"))) {
            financialSummary.setFeeDiscount(invoice.getFeesDiscount());
            financialSummary.setIsDiscount(true);
            financialSummary.setReasonForDiscount(invoice.getReasonsForDiscount());
        }
        financialSummary.setTotalFeePaid(invoice.getFeesPaid());
        financialSummary.setTotalFeeDue(invoice.getFeesDue().setScale(2));
        financialSummary.setFeeVAT(invoice.getVatAmount());
        em.clear();
        invoice = em.find(Invoice.class, invoice.getId());
        financialSummary.setInvoice(invoice);
    }

    private static Invoice updateInvoiceFromInitialFinancialSummary(FinancialSummary financialSummary,
                                                                    Integer lcpNumber, Institution selectedInstitution, TestingSession
                                                                            activatedTestingSession, EntityManager em) {
        Invoice invoice = Invoice.getInvoiceForAnInstitutionAndATestingSession(em, selectedInstitution,
                activatedTestingSession);
        if (invoice == null) {

            invoice = new Invoice(selectedInstitution, activatedTestingSession);
            invoice.setFeesDiscount(new BigDecimal("0.00"));
            invoice.setFeesPaid(new BigDecimal("0.00"));
            invoice.setCurrency(activatedTestingSession.getCurrency());
        }

        invoice.setNumberSystem(financialSummary.getFinancialSummaryOneSystems().size());
        invoice.setNumberParticipant(lcpNumber);
        invoice.setNumberExtraParticipant(financialSummary.getNumberAdditionalParticipant(activatedTestingSession));

        invoice.setFeesAmount(financialSummary.getFee());
        invoice = FinancialCalc.calculateVatAmountForCompany(invoice, em);
        invoice = Invoice.mergeInvoiceIfNotSent(invoice, em);
        return invoice;
    }

    private static FinancialSummary getInitialFinancialSummaryFromSIS(List<FinancialSummaryOneSystem> lfs,
                                                                      List<ConnectathonParticipant> lcp, BigDecimal participantFee, Institution
                                                                              selectedInstitution, TestingSession activatedTestingSession) {
        FinancialSummary financialSummary = new FinancialSummary();
        financialSummary.setFinancialSummaryOneSystems(lfs);
        financialSummary.setDomains(getAllDomainAsStringFromListFinancialSummaryOneSystem(lfs));
        financialSummary.setHasASystemGotMissingDependencies(choosenInstitHasMissingDependencies(lfs));
        financialSummary.setFee(calculateTotalFee(lfs, lcp, participantFee, activatedTestingSession));
        financialSummary.setNumberParticipant(lcp.size());
        return financialSummary;
    }

    private static BigDecimal calculateTotalFee(List<FinancialSummaryOneSystem> lfs, List<ConnectathonParticipant> lcp,
                                                BigDecimal participantFee, TestingSession activatedTestingSession) {
        BigDecimal res = new BigDecimal("0.00");
        for (FinancialSummaryOneSystem financialSummaryOneSystem : lfs) {
            if (!financialSummaryOneSystem.getSystemInSession().getSystem().getIsTool()) {
                res = res.add(financialSummaryOneSystem.getSystemFee());
            }
        }
        if (lcp != null) {
            int numb = lcp.size() - (lfs.size() * activatedTestingSession.getNbParticipantsIncludedInSystemFees());
            if (numb > 0) {
                res = res.add(participantFee.multiply(new BigDecimal(numb)));
            }
        }
        return res;
    }

    private static boolean choosenInstitHasMissingDependencies(List<FinancialSummaryOneSystem> lfs) {
        for (FinancialSummaryOneSystem financialSummaryOneSystem : lfs) {
            if (financialSummaryOneSystem.getHasMissingDependecies()) {
                return true;
            }
        }
        return false;
    }

    private static String getAllDomainAsStringFromListFinancialSummaryOneSystem(List<FinancialSummaryOneSystem> lf) {
        StringBuffer buf = new StringBuffer();
        List<String> lst = new ArrayList<String>();
        if (lf != null) {
            for (FinancialSummaryOneSystem financialSummaryOneSystem : lf) {
                String[] lss = financialSummaryOneSystem.getDomainsForSystem().split(" ");
                if (lss.length > 0) {
                    for (String string : lss) {
                        if (!lst.contains(string)) {
                            lst.add(string);
                        }
                    }
                }
            }
        }
        boolean first = true;
        for (String string : lst) {
            if (!first) {
                buf.append(" ");
            }
            buf.append(string);
            first = false;
        }
        return buf.toString();
    }

    private static FinancialSummaryOneSystem getFinancialSummaryOneSystem(SystemInSession systemInSession,
                                                                          BigDecimal systemFee, EntityManager em) {
        FinancialSummaryOneSystem fsstmp = new FinancialSummaryOneSystem();
        fsstmp.setSystemInSession(systemInSession);
        fsstmp.setDomainsForSystem(FinancialManager.getDomainsAsString(findAllDomainsforSystem(
                systemInSession.getSystem(), em)));
        fsstmp.setHasMissingDependecies(hasMissingDependencies(systemInSession, em));
        if (!systemInSession.getSystem().getIsTool()) {
            fsstmp.setSystemFee(systemFee);
        }
        return fsstmp;
    }

    private static boolean hasMissingDependencies(SystemInSession sis, EntityManager em) {
        List<ActorIntegrationProfileOption> vAIPO = SystemActorProfiles.getListOfActorIntegrationProfileOptions(
                sis.getSystem(), em);
        if ((vAIPO == null) || (vAIPO.size() == 0)) {
            return true;
        }
        try {
            List<AipoRule> invalidRules = TFDependenciesManager.getInvalidRules(sis.getSystem());
            return (invalidRules.size() > 0);
        } catch (Exception e) {
            LOG.error("", e);
            return false;
        }
    }

    /**
     * Static method is returning the list of Domains as String corresponding to a system
     *
     * @param UserPreferences curSystem
     * @return Set<String> : Domains as String objects corresponding to a system
     */
    @SuppressWarnings("unchecked")
    private static Set<String> findAllDomainsforSystem(System curSystem, EntityManager entityManager) {
        List<IntegrationProfile> ipList;
        Set<String> domainSet = new HashSet<String>();
        Query query = entityManager
                .createQuery("select distinct sap.actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile from SystemActorProfiles" +
                        " sap where sap.system.id = "
                        + curSystem.getId());
        ipList = query.getResultList();
        Iterator<IntegrationProfile> ipIt = ipList.iterator();
        while (ipIt.hasNext()) {
            IntegrationProfile curProfile = ipIt.next();
            List<Domain> domainList = curProfile.getDomainsForDP();
            for (Domain domain : domainList) {
                domainSet.add(domain.getKeyword());
            }
        }
        return domainSet;
    }

    /**
     * Static method is returning the Domains as a string
     *
     * @param domainsAsSet : List of domains
     * @return String : Domains as String
     */
    private static String getDomainsAsString(Set<String> domainsAsSet) {
        StringBuffer domainsAsString = new StringBuffer();
        for (String string : domainsAsSet) {
            domainsAsString.append(string).append(" ");
        }
        return domainsAsString.toString();
    }

    public static void calculateFinancialSummaryDTOByTestingSessionByInstitution(TestingSession activatedTestingSession, Institution ins) {
        if (activatedTestingSession == null) {
            return;
        }
        if (ins == null) {
            return;
        }

        BigDecimal feeFirstSystem = activatedTestingSession.getFeeFirstSystem();
        BigDecimal feeAdditionalSystem = activatedTestingSession.getFeeAdditionalSystem();

        FinancialSummary financialSummary = FinancialManager
                .getFinancialSummaryWithCalculatedAmountsStatic(feeFirstSystem, feeAdditionalSystem, TestingSession
                        .getSelectedTestingSession().getFeeParticipant(), activatedTestingSession, ins, EntityManagerService.provideEntityManager());

        List<FinancialSummaryOneSystem> financialSummaryForOneSystem;
        if (financialSummary != null) {
            financialSummaryForOneSystem = financialSummary.getFinancialSummaryOneSystems();
        } else {
            financialSummaryForOneSystem = null;
        }

        // FIXME : never read, useless ???
        Contexts.getSessionContext().set("financialSummary", financialSummary);
        Contexts.getSessionContext().set("financialSummaryForOneSystem", financialSummaryForOneSystem);
    }

    @Override
    public void setup() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setup");
        }
        billing = PersonFunction.getBillingFunction(entityManager);
        commercial = PersonFunction.getMarketingFunction(entityManager);
        technical = PersonFunction.getTechnicalFunction(entityManager);
    }

    @Override
    public void getFinancialSummaryWithNoReturn() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFinancialSummaryWithNoReturn");
        }
        try {
            if ((Role.isLoggedUserVendorAdmin() || Role.isLoggedUserVendorUser()) && (selectedInstitution == null)) {
                selectedInstitution = Institution.getLoggedInInstitution();
            }
            Contexts.getSessionContext().set("selectedInstitution", selectedInstitution);
            calculateFinancialSummaryDTO();
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
        }
    }

    @Override
    public FinancialSummary getFinancialSummary() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFinancialSummary");
        }
        return financialSummary;
    }

    @Override
    public Invoice getInvoiceSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInvoiceSelected");
        }
        if (invoiceSelected == null) {

            invoiceSelected = new Invoice();
        }

        return invoiceSelected;
    }

    @Override
    public void setInvoiceSelected(Invoice invoiceSelected) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInvoiceSelected");
        }
        this.invoiceSelected = invoiceSelected;
    }

    /**
     * Method called for an admin to get the choosen institution
     *
     * @return choosen institution
     */
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
    public void calculateFinancialSummaryDTO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculateFinancialSummaryDTO");
        }

        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        BigDecimal feeFirstSystem = activatedTestingSession.getFeeFirstSystem();
        BigDecimal feeAdditionalSystem = activatedTestingSession.getFeeAdditionalSystem();
        BigDecimal participantFee = activatedTestingSession.getFeeParticipant();

        financialSummary = FinancialManager.getFinancialSummaryWithCalculatedAmountsStatic(feeFirstSystem,
                feeAdditionalSystem, participantFee, activatedTestingSession, this.getSelectedInstitution(),
                entityManager);
        if (financialSummary != null) {
            financialSummaryForOneSystem = financialSummary.getFinancialSummaryOneSystems();
        } else {
            financialSummaryForOneSystem = null;
        }
    }

    /**
     * This method gets called when the user clicks on Download contract button on Financial Summary page
     *
     * @see net.ihe.gazelle.tm.financial.action.FinancialManagerLocal#generateContractPDF()
     */
    @Override
    public void generateContractPDF() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateContractPDF");
        }
        try {
            File gazelleDataContracts = new File(ApplicationManager.instance().getGazelleContractsPath());

            if (this.selectedInstitution == null) {

                return;
            }
            calculateFinancialSummaryDTO();
            Institution institutionForContract = entityManager.find(Institution.class, selectedInstitution.getId());

            Long res = ContractExporter.exportContractToPDF(institutionForContract.getId(),
                    institutionForContract.getKeyword(), TestingSession.getSelectedTestingSession(),
                    gazelleDataContracts.getAbsolutePath(), entityManager);

            Invoice invoice = Invoice.getInvoiceForAnInstitutionAndATestingSession(entityManager, institutionForContract,
                    TestingSession.getSelectedTestingSession());
            if (invoice != null && res != null) {
                invoice.setLastGenerationDate(res);
                invoice.setContractOutOfDate(false);
                entityManager.merge(invoice);
                entityManager.flush();
            }
        } catch (FileNotFoundException e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "File not found ! : " + e.getMessage());
            LOG.error("" + e.getMessage());
        } catch (JRException e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to generate contract !");
            LOG.error("" + e.getMessage());
        }
    }

    /**
     * Retrieve contacts for the purpose of generating Financial Summary
     */
    @Override
    @SuppressWarnings("unchecked")
    public void prepareContactsForFinancialSummary() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("prepareContactsForFinancialSummary");
        }
        List<Person> persons = null;
        if ((Identity.instance().hasRole(Role.ADMINISTRATOR_ROLE_STRING) || Identity.instance().hasRole(
                Role.ACCOUNTING_ROLE_STRING))) {
            if (this.getSelectedInstitution() != null) {
                Query query = entityManager
                        .createQuery("SELECT p FROM Person p WHERE p.institution.name = :institutionName");
                query.setParameter("institutionName", selectedInstitution.getName());
                persons = query.getResultList();
            } else {
                Query query = entityManager
                        .createQuery("SELECT p FROM Person p WHERE p.institution.name = :institutionName");
                query.setParameter("institutionName", Institution.getLoggedInInstitution().getName());
                persons = query.getResultList();
            }
        } else {
            if (Institution.getLoggedInInstitution() == null) {
                throw new Exception("Logged in user's institution is not found");
            }
            Query query = entityManager.createQuery("SELECT p FROM Person p WHERE p.institution.id = :institutionId");
            query.setParameter("institutionId", Institution.getLoggedInInstitution().getId());
            persons = query.getResultList();
        }

        billingContactsForFinancialSummary = new Vector<Person>();
        technicalContactsForFinancialSummary = new Vector<Person>();
        commercialContactsForFinancialSummary = new Vector<Person>();

        this.setup();

        for (int i = 0; i < persons.size(); i++) {

            if (persons.get(i).getPersonFunction().contains(billing)) {
                billingContactsForFinancialSummary.add(persons.get(i));

            }
            if (persons.get(i).getPersonFunction().contains(commercial)) {

                commercialContactsForFinancialSummary.add(persons.get(i));
            }
            if (persons.get(i).getPersonFunction().contains(technical)) {

                technicalContactsForFinancialSummary.add(persons.get(i));
            }

        }
    }

    @Override
    public List<Person> getBillingContactsForFinancialSummary() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBillingContactsForFinancialSummary");
        }
        return billingContactsForFinancialSummary;
    }

    @Override
    public void setBillingContactsForFinancialSummary(List<Person> billingContactsForFinancialSummary) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setBillingContactsForFinancialSummary");
        }
        this.billingContactsForFinancialSummary = billingContactsForFinancialSummary;
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

    @Override
    public List<Person> getCommercialContactsForFinancialSummary() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCommercialContactsForFinancialSummary");
        }

        return commercialContactsForFinancialSummary;
    }

    @Override
    public void setCommercialContactsForFinancialSummary(List<Person> commercialContactsForFinancialSummary) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCommercialContactsForFinancialSummary");
        }
        this.commercialContactsForFinancialSummary = commercialContactsForFinancialSummary;
    }

    @Override
    public List<Person> getTechnicalContactsForFinancialSummary() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTechnicalContactsForFinancialSummary");
        }
        return technicalContactsForFinancialSummary;
    }

    @Override
    public void setTechnicalContactsForFinancialSummary(List<Person> technicalContactsForFinancialSummary) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTechnicalContactsForFinancialSummary");
        }
        this.technicalContactsForFinancialSummary = technicalContactsForFinancialSummary;
    }

    @Override
    public boolean ifBillingContactsHasAddress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ifBillingContactsHasAddress");
        }

        if (billingContactsForFinancialSummary != null) {

            if (billingContactsForFinancialSummary.size() == 0) {
                return false;
            } else {
                for (int i = 0; i < billingContactsForFinancialSummary.size(); i++) {
                    Address currentAddress = billingContactsForFinancialSummary.get(i).getAddress();
                    if (currentAddress == null) {
                        return false;
                    } else if ((currentAddress.getAddress() != null)
                            && (currentAddress.getAddress().trim().length() > 0) && (currentAddress.getCity() != null)
                            && (currentAddress.getCity().trim().length() > 0)
                            && (currentAddress.getIso3166CountryCode() != null)
                            && (currentAddress.getIso3166CountryCode().getName().length() > 0)) {
                        return true;
                    }
                }
            }
            return false;
        } else {

            return false;
        }
    }

    @Override
    public List<FinancialSummaryOneSystem> getFinancialSummaryForOneSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFinancialSummaryForOneSystem");
        }
        return financialSummaryForOneSystem;
    }

    @Override
    public void setselectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setselectedInstitution");
        }
        if ((Identity.instance().hasRole(Role.ADMINISTRATOR_ROLE_STRING) || Identity.instance().hasRole(
                Role.ACCOUNTING_ROLE_STRING))
                && (selectedInstitution == null)) {
            selectedInstitution = new Institution();
        }
    }

    /**
     * This method gets called when the admin click on the Search button on Financial Summary
     *
     * @see net.ihe.gazelle.tm.financial.action.FinancialManagerLocal#doFindCompanies()
     */
    @Override
    public String doFindCompanies() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doFindCompanies");
        }

        if ((Identity.instance() != null)
                && (Identity.instance().hasRole(Role.ADMINISTRATOR_ROLE_STRING) || Identity.instance().hasRole(
                Role.ACCOUNTING_ROLE_STRING))) {
            if ((selectedInstitution != null) && selectedInstitution.getName().equals("")) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No company selected. Please choose one company and search again.");
                selectedInstitution = Institution.getLoggedInInstitution();
            } else if ((selectedInstitution != null) && (selectedInstitution.getName() != null)
                    && selectedInstitution.getName().trim().equals("*")) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Please choose one company and search.");
                selectedInstitution = Institution.getLoggedInInstitution();
                try {
                    getFinancialSummaryWithNoReturn();
                } catch (Exception e) {
                    ExceptionLogging.logException(e, LOG);
                }
            } else {
                Session session = (Session) entityManager.getDelegate();
                Criteria c = session.createCriteria(Institution.class);
                c.add(Restrictions.eq("name", selectedInstitution.getName()));
                c.setProjection(Projections.rowCount());

                Integer count = (Integer) c.uniqueResult();
                int rowCount = count.intValue();

                c.setProjection(null);
                c.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
                if (rowCount > 0) {
                    selectedInstitution = (Institution) c.add(Restrictions.eq("name", selectedInstitution.getName()))
                            .uniqueResult();
                } else {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No such Company.");
                    selectedInstitution = Institution.getLoggedInInstitution();
                }
                try {
                    getFinancialSummaryWithNoReturn();
                } catch (Exception e) {
                    ExceptionLogging.logException(e, LOG);

                }
            }
        }
        return "/financial/financialSummary.xhtml";
    }

    @Override
    public void doFindCompaniesWithNoReturn() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doFindCompaniesWithNoReturn");
        }
        this.doFindCompanies();
    }

    @Override
    public InvoiceDataModel getListOfInvoices() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfInvoices");
        }
        if (listOfInvoices == null) {
            listOfInvoices = new InvoiceDataModel();
        }
        return listOfInvoices;
    }

    @Override
    public void setListOfInvoices(InvoiceDataModel listOfInvoices) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfInvoices");
        }
        this.listOfInvoices = listOfInvoices;
    }

    @Override
    public String editInvoice(Invoice invoice) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editInvoice");
        }
        invoiceSelected = invoice;

        try {
            selectedInstitution = invoiceSelected.getInstitution();

            prepareContactsForFinancialSummary();

            if (billingContactsForFinancialSummary.size() > 0) {
                emailAddressFirstBillingContact = billingContactsForFinancialSummary.get(0).getEmail();
            }

            if (invoiceSelected.getFeesDiscount() == null) {
                invoiceSelected.setFeesDiscount(new BigDecimal("0.00"));
            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);

        }
        return "/financial/editInvoiceDetails.xhtml";

    }

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public String showInvoice(Invoice invoice) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showInvoice");
        }
        invoiceSelected = invoice;

        try {
            selectedInstitution = invoiceSelected.getInstitution();

            prepareContactsForFinancialSummary();

            if (billingContactsForFinancialSummary.size() > 0) {
                emailAddressFirstBillingContact = billingContactsForFinancialSummary.get(0).getEmail();
            }

            if (invoiceSelected.getFeesDiscount() == null) {
                invoiceSelected.setFeesDiscount(new BigDecimal("0.00"));
            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);

        }
        return "/financial/showInvoiceDetails.xhtml";

    }

    @Override
    public String editInvoiceForCurrentSession(Institution inInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editInvoiceForCurrentSession");
        }
        selectedInstitution = inInstitution;
        Contexts.getSessionContext().set("selectedInstitution", selectedInstitution);
        EntityManager em = EntityManagerService.provideEntityManager();
        invoiceSelected = Invoice.getInvoiceForAnInstitutionAndATestingSession(em, inInstitution,
                TestingSession.getSelectedTestingSession());
        if (invoiceSelected != null) {
            if (invoiceSelected.getFeesDiscount() == null) {
                invoiceSelected.setFeesDiscount(new BigDecimal("0.00"));
            }

            return "/financial/editInvoiceDetails.xhtml";
        }

        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No invoice for this institution");

        return null;

    }

    @Override
    public void updateFeesDue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateFeesDue");
        }
        if (invoiceSelected.getFeesDiscount() == null) {
            invoiceSelected.setFeesDiscount(BigDecimal.ZERO);
        }
        if (invoiceSelected.getFeesPaid() != null) {
            FinancialCalc.calculateVatAmountForCompany(invoiceSelected, entityManager);
            invoiceSelected.setFeesDue((invoiceSelected.getFeesAmount().add(invoiceSelected.getVatAmount())).subtract(
                    invoiceSelected.getFeesDiscount()).subtract(invoiceSelected.getFeesPaid()));
        }
    }

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public String saveInvoice() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveInvoice");
        }
        if (invoiceSelected.getContractReceivedDate() != null) {
            invoiceSelected.setContractReceived(true);
        }
        invoiceSelected = Invoice.mergeInvoice(invoiceSelected, entityManager);
        // Calculate new amounts for invoice - Fees and VAT changes when a system is added

        try {
            calculateFinancialSummaryDTO();
            invoiceSelected.setVatAmount(financialSummary.getFeeVAT());
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
        }
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Invoice saved.");
        return "/administration/listInvoices.seam";
    }

    private Integer getNextInvoiceNumberAvailable() {
        return TestingSession.getNextInvoiceNumberStatic(invoiceSelected.getTestingSession());
    }

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public void generateInvoice() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateInvoice");
        }
        File gazelleDataReports = new File(ApplicationPreferenceManager.instance().getGazelleReportsPath());
        File gazelleDataInvoices = new File(ApplicationManager.instance().getGazelleInvoicesPath());
        int invoiceNumber;

        if (invoiceSelected.getInvoiceNumber() == null) {
            invoiceNumber = getNextInvoiceNumberAvailable();
            while (Invoice.getInvoiceInSessionWithNumber(invoiceSelected.getTestingSession(), invoiceNumber) != null) {
                invoiceNumber = getNextInvoiceNumberAvailable();
            }
        } else {
            invoiceNumber = invoiceSelected.getInvoiceNumber().intValue();
        }

        try {
            if (!gazelleDataReports.exists()) {
                throw new FileNotFoundException("Path " + gazelleDataReports.getCanonicalPath() + " not found!");
            }
            if (!gazelleDataInvoices.exists()) {
                if (!gazelleDataInvoices.mkdirs()) {
                    throw new Exception("Impossible to create " + gazelleDataInvoices.getCanonicalPath());
                }

            }
            String reportSource = gazelleDataReports.getAbsolutePath() + File.separatorChar
                    + invoiceSelected.getTestingSession().getPathToInvoiceTemplate();
            String reportName = "CAT" + invoiceSelected.getTestingSession().getYear() + "-" + invoiceNumber + ".pdf";
            String reportDest = gazelleDataInvoices.getAbsolutePath() + File.separatorChar + reportName;
            File f = new File(reportDest);

            if (f.exists()) {
                String extension = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                String fileName = f.getName().substring(0, f.getName().lastIndexOf("."));
                File newFile = new File(gazelleDataInvoices.getAbsolutePath() + File.separatorChar + fileName + "_"
                        + f.lastModified() + "." + extension);

                if (f.renameTo(newFile)) {
                    LOG.info("File renamed");
                } else {
                    LOG.error("Failed to rename file");
                }

            }

            if (entityManager.isOpen()) {
                invoiceSelected.setInvoiceGenerationDate(new Date());
                invoiceSelected.setInvoiceNumber(invoiceNumber);

                /** feesAmount + vatAmount - feesDiscount - feesPaid = feesDue */
                BigDecimal totalDue = invoiceSelected.getFeesAmount().add(
                        invoiceSelected.getVatAmount().add(invoiceSelected.getFeesDiscount().negate()));
                invoiceSelected.setFeesDue(totalDue);
                invoiceSelected = Invoice.mergeInvoice(invoiceSelected, entityManager);
                invoiceSelected.setInvoiceRelativePath(reportName);
                invoiceSelected = Invoice.mergeInvoice(invoiceSelected, entityManager);

                ContractExporter.exportInvoiceToPDF(entityManager, invoiceNumber, reportSource,
                        ApplicationPreferenceManager.instance().getGazelleReportsPath(), reportName, reportDest,
                        invoiceSelected.getInstitution().getKeyword(), invoiceSelected.getTestingSession());
            }
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Invoice generated");
        } catch (HibernateException e) {
            ExceptionLogging.logException(e, LOG);
        } catch (JRException e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to generate invoice !");
            LOG.error("" + e.getMessage());
        } catch (FileNotFoundException e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "File not found ! : " + e.getMessage());
            LOG.error("" + e.getMessage());
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : " + e.getMessage());
            LOG.error("" + e.getMessage());
        }
    }

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public void regenerateInvoice() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("regenerateInvoice");
        }
        int invoiceNumber = invoiceSelected.getInvoiceNumber();
        while (Invoice.getInvoiceInSessionWithNumber(invoiceSelected.getTestingSession(), invoiceNumber) != null) {
            invoiceNumber = getNextInvoiceNumberAvailable();
        }
        invoiceSelected.setInvoiceNumber(invoiceNumber);
        invoiceSelected = Invoice.mergeInvoice(invoiceSelected, entityManager);
        generateInvoice();
    }

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public void displayInvoice(Invoice invoiceToUse) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayInvoice");
        }
        InputStream reportStream = null;
        try {
            String fileName = invoiceToUse.getInvoiceRelativePath();
            reportStream = new FileInputStream(ApplicationManager.instance().getGazelleInvoicesPath()
                    + File.separatorChar + fileName);

            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

            response.setContentType("application/pdf");

            response.setHeader("Content-Disposition", "attachment;filename=\"invoice-"
                    + invoiceToUse.getInstitution().getKeyword() + "-" + invoiceToUse.getTestingSession().getYear()
                    + ".pdf\"");
            ServletOutputStream servletOutputStream;

            servletOutputStream = response.getOutputStream();

            int length = reportStream.available();
            byte[] bytes = new byte[length];

            reportStream.read(bytes);

            servletOutputStream.write(bytes);

            servletOutputStream.flush();
            servletOutputStream.close();

            context.responseComplete();

        } catch (FileNotFoundException e) {

            invoiceToUse.setInvoiceRelativePath(null);
            invoiceToUse.setInvoiceGenerationDate(null);
            Invoice.mergeInvoice(invoiceToUse, entityManager);

            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to display invoice : " + e.getMessage());
        } catch (IOException e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to display invoice : " + e.getMessage());
        } finally {
            try {
                if (reportStream != null) {
                    reportStream.close();
                }
            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to close stream : " + e.getMessage());

            }
        }
    }

    @Override
    public String getEmailAddressFirstBillingContact() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEmailAddressFirstBillingContact");
        }
        return emailAddressFirstBillingContact;
    }

    @Override
    public void setEmailAddressFirstBillingContact(String emailAddressFirstBillingContact) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEmailAddressFirstBillingContact");
        }
        this.emailAddressFirstBillingContact = emailAddressFirstBillingContact;
    }

    @Override
    public void generateFeeStatusSummary() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateFeeStatusSummary");
        }

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            Object testingSessionObject = listOfInvoices.getFilter().getRealFilterValue("testingSession");
            if (testingSessionObject != null) {
                TestingSession testingSession = (TestingSession) testingSessionObject;
                params.put("testingSessionId", testingSession.getId());
            }

            ReportExporterManager.exportToPDF("feesStatus", "feesStatus.pdf", params);

        } catch (JRException e) {
            ExceptionLogging.logException(e, LOG);
        }

    }

    /**
     * Check if we need to render a decorate region, we hide if (the user is admin and if no institution has been choosen by admin)
     *
     * @return Boolean : true if we need to render the decorate region
     */
    @Override
    public Boolean isFinancialSummaryDecorateRendered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isFinancialSummaryDecorateRendered");
        }

        if (Identity.instance().hasRole(Role.ADMINISTRATOR_ROLE_STRING)
                || Identity.instance().hasRole(Role.ACCOUNTING_ROLE_STRING)) {
            if ((selectedInstitution == null) || (selectedInstitution.getName() == null)
                    || (selectedInstitution.getName().trim() == null)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateVATNumber() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateVATNumber");
        }
        if (invoiceSelected == null) {
            return false;
        }
        boolean validate = FinancialCalc.validateVATNumber(invoiceSelected.getVatNumber(),
                invoiceSelected.getVatCountry(), ApplicationPreferenceManager.getApilayerAccessKey());
        invoiceSelected.setVatValidity(validate);
        return validate;
    }

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public void deleteInvoiceSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteInvoiceSelected");
        }
        if (this.invoiceSelected != null) {
            if (this.invoiceSelected.getId() != null) {
                this.invoiceSelected = entityManager.find(Invoice.class, this.invoiceSelected.getId());
                entityManager.remove(this.invoiceSelected);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Selected Invoice was deleted.");
            }
        }
    }

    @Override
    public BigDecimal calculateTotalSystemsFee() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculateTotalSystemsFee");
        }
        BigDecimal res = BigDecimal.ZERO;
        for (FinancialSummaryOneSystem fso : this.financialSummaryForOneSystem) {
            res = res.add(fso.getSystemFee());
        }
        return res;
    }

    @Override
    public void recalculateFees() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("recalculateFees");
        }
        if ((invoiceSelected != null) && (invoiceSelected.getId() != null)) {
            FinancialCalc.recalculateFees(invoiceSelected, entityManager);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Invoice was updated.");
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error occures, no invoice was selected.");
        }
    }

    @Override
    public void reloadInvoice() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reloadInvoice");
        }
        if ((invoiceSelected != null) && (invoiceSelected.getId() != null)) {
            FinancialCalc.reloadInvoice(invoiceSelected, entityManager);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Invoice was updated.");
        }
    }

    @Override
    public void updateSelectedInstitutionForAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedInstitutionForAdmin");
        }
        if (this.selectedInstitution == null) {
            this.selectedInstitution = Institution.getLoggedInInstitution();
        }
    }

    @Override
    public void forceSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("forceSelectedInstitution");
        }
        this.selectedInstitution = Institution.getLoggedInInstitution();
    }

    @Override
    public boolean userCanDownloadContract() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("userCanDownloadContract");
        }
        if ((this.selectedInstitution == null) || (this.selectedInstitution.getMailingAddress() == null)) {
            return false;
        }
        if (this.financialSummary == null) {
            return false;
        }
        if (financialSummary.getFinancialSummaryOneSystems() == null) {
            return false;
        }
        if ((financialSummary.getDomains() == null) || (financialSummary.getDomains().length() == 0)) {
            return false;
        }
        if (financialSummary.getFinancialSummaryOneSystems().size() == 0) {
            return false;
        }
        if ((this.billingContactsForFinancialSummary == null) || (this.billingContactsForFinancialSummary.size() == 0)) {
            return false;
        }
        if ((this.technicalContactsForFinancialSummary == null)
                || (this.technicalContactsForFinancialSummary.size() == 0)) {
            return false;
        }
        if ((this.commercialContactsForFinancialSummary == null)
                || (this.commercialContactsForFinancialSummary.size() == 0)) {
            return false;
        }
        if (financialSummary.isDomainExist() == false) {
            return false;
        }
        if (!this.ifBillingContactsHasAddress()) {
            return false;
        }
        if (this.financialSummary.getHasASystemGotMissingDependencies()) {
            return false;
        }
        return true;
    }

    @Override
    public void validateVATCountryFromFinancialContact(Invoice invoice) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateVATCountryFromFinancialContact");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        invoice.setValidVATCountryFromFinancialContact(FinancialCalc.matchVATCountryToInstitutionInSession(
                invoice.getInstitution(), invoice.getVatCountry(), entityManager));
    }

    @Override
    public List<Invoice> getListOfInvoicesAsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfInvoicesAsList");
        }
        List<Invoice> allInvoices = (List<Invoice>) this.getListOfInvoices().getAllItems(
                FacesContext.getCurrentInstance());
        computeTotals(allInvoices);
        return allInvoices;
    }

    private void computeTotals(List<Invoice> allInvoices) {
        invoicesTotals = new invoiceTotal();
        for (Invoice invoice : allInvoices) {
            invoicesTotals.addSystem(numberSystemByInstitution(invoice));
            invoicesTotals.addFeeAmount(invoice.getFeesAmount());
            invoicesTotals.addFeeDiscount(invoice.getFeesDiscount());
            invoicesTotals.addFeeDue(invoice.getFeesDue());
            invoicesTotals.addFeePaid(invoice.getFeesPaid());
        }
    }

    public invoiceTotal getInvoicesTotals() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInvoicesTotals");
        }
        return invoicesTotals;
    }

    public void setInvoicesTotals(invoiceTotal invoicesTotals) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInvoicesTotals");
        }
        this.invoicesTotals = invoicesTotals;
    }

    @Override
    public Integer numberSystemByInstitution(Invoice currentInvoice) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("numberSystemByInstitution");
        }
        List<SystemInSession> lsis = SystemInSession.getSystemsInSessionForCompanyForSession(entityManager,
                currentInvoice.getInstitution(), currentInvoice.getTestingSession());
        if (lsis != null) {
            return lsis.size();
        }
        return 0;
    }

    @Override
    public Person getBillingContact(Invoice inv) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBillingContact");
        }
        return Person.getUniqueContactByFunctionNameByInstitution(entityManager, inv.getInstitution(),
                PersonFunction.BillingFunctionName);
    }

    @Override
    public Person getPrimaryMarketingContact(Invoice inv) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPrimaryMarketingContact");
        }
        return Person.getUniqueContactByFunctionNameByInstitution(entityManager, inv.getInstitution(),
                PersonFunction.MarketingFunctionName);
    }

    @Override
    public Person getPrimaryTechnicalContact(Invoice inv) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPrimaryTechnicalContact");
        }
        if (Person.getUniqueContactByFunctionNameByInstitution(entityManager, inv.getInstitution(),
                PersonFunction.primaryTechnicalFunctionName) != null) {
            return Person.getUniqueContactByFunctionNameByInstitution(entityManager, inv.getInstitution(),
                    PersonFunction.primaryTechnicalFunctionName);
        } else {
            return Person.getUniqueContactByFunctionNameByInstitution(entityManager, inv.getInstitution(),
                    PersonFunction.TechnicalFunctionName);
        }
    }

    @Override
    public void saveSpecialInstructionFromFinancialSummary() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveSpecialInstructionFromFinancialSummary");
        }
        if (this.financialSummary != null) {
            if ((this.financialSummary.getInvoiceSpecialInstruction() != null)
                    && (this.financialSummary.getInvoice() != null)
                    && (this.financialSummary.getInvoice().getId() != null)) {
                Invoice invToUpdate = this.entityManager
                        .find(Invoice.class, this.financialSummary.getInvoice().getId());
                if (invToUpdate != null) {
                    invToUpdate.setSpecialInstructions(this.financialSummary.getInvoiceSpecialInstruction());
                    entityManager.merge(invToUpdate);
                    entityManager.flush();
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "Special instructions were saved !");
                }
            }
        }
    }

    @Override
    public String getDemonstrations(Invoice inv) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDemonstrations");
        }
        if ((inv != null) && (inv.getInstitution() != null) && (inv.getTestingSession() != null)) {
            SystemQuery systemQuery = new SystemQuery();
            systemQuery.institutionSystems().institution().id().eq(inv.getInstitution().getId());
            systemQuery.systemsInSession().testingSession().id().eq(inv.getTestingSession().getId());

            List<String> demos = systemQuery.systemsInSession().demonstrations().name().getListDistinct();
            do {
                //
            } while (demos.remove(null));

            Collections.sort(demos);

            return StringUtils.join(demos, ", ");
        } else {
            return "";
        }
    }

    class invoiceTotal {
        private int numberOfSystems = 0;
        private BigDecimal feeAmount = BigDecimal.ZERO;
        private BigDecimal feeDiscount = BigDecimal.ZERO;
        private BigDecimal feeDue = BigDecimal.ZERO;
        private BigDecimal feePaid = BigDecimal.ZERO;

        void addSystem(int numberOfSystemsToAdd) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addSystem");
            }
            numberOfSystems += numberOfSystemsToAdd;
        }

        void addFeeAmount(BigDecimal feeToAdd) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addFeeAmount");
            }
            feeAmount = feeAmount.add(feeToAdd);
        }

        void addFeeDiscount(BigDecimal feeDiscountToAdd) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addFeeDiscount");
            }
            feeDiscount = feeDiscount.add(feeDiscountToAdd);
        }

        void addFeeDue(BigDecimal feeDueToAdd) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addFeeDue");
            }
            feeDue = feeDue.add(feeDueToAdd);
        }

        void addFeePaid(BigDecimal feePaidToAdd) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addFeePaid");
            }
            feePaid = feePaid.add(feePaidToAdd);
        }

        public int getNumberOfSystems() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getNumberOfSystems");
            }
            return numberOfSystems;
        }

        public void setNumberOfSystems(int numberOfSystems) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setNumberOfSystems");
            }
            this.numberOfSystems = numberOfSystems;
        }

        public BigDecimal getFeeAmount() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFeeAmount");
            }
            return feeAmount;
        }

        public void setFeeAmount(BigDecimal feeAmount) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setFeeAmount");
            }
            this.feeAmount = feeAmount;
        }

        public BigDecimal getFeeDiscount() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFeeDiscount");
            }
            return feeDiscount;
        }

        public void setFeeDiscount(BigDecimal feeDiscount) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setFeeDiscount");
            }
            this.feeDiscount = feeDiscount;
        }

        public BigDecimal getFeeDue() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFeeDue");
            }
            return feeDue;
        }

        public void setFeeDue(BigDecimal feeDue) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setFeeDue");
            }
            this.feeDue = feeDue;
        }

        public BigDecimal getFeePaid() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFeePaid");
            }
            return feePaid;
        }

        public void setFeePaid(BigDecimal feePaid) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setFeePaid");
            }
            this.feePaid = feePaid;
        }
    }

}
