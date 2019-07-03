package net.ihe.gazelle.tm.financial.action;

import net.ihe.gazelle.tm.financial.model.Invoice;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.users.model.ConnectathonParticipant;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Iso3166CountryCode;
import net.ihe.gazelle.users.model.Person;
import net.ihe.gazelle.util.Pair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author abderrazek boufahja
 */
public abstract class FinancialCalc {

    private static final Logger LOG = LoggerFactory.getLogger(FinancialCalc.class);
    private static String urlRESTForVATValidation = "http://www.apilayer.net/api/validate?";

    public static void updateInvoiceIfPossible(Institution ins, TestingSession activatedTestingSession, EntityManager em) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void updateInvoiceIfPossible");
        }
        if (ins == null) {
            ins = Institution.getLoggedInInstitution();
        }
        if (activatedTestingSession == null) {
            activatedTestingSession = TestingSession.getSelectedTestingSession();
        }

        BigDecimal feeFirstSystem = activatedTestingSession.getFeeFirstSystem();
        BigDecimal feeAdditionalSystem = activatedTestingSession.getFeeAdditionalSystem();
        BigDecimal participantFee = activatedTestingSession.getFeeParticipant();
        FinancialManager.getFinancialSummaryWithCalculatedAmountsStatic(feeFirstSystem, feeAdditionalSystem, participantFee,
                activatedTestingSession, ins, em);
    }

    public static BigDecimal calculateTotalFee(TestingSession ts, int numberSystem, int numberParticipant) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("BigDecimal calculateTotalFee");
        }
        BigDecimal res = BigDecimal.ZERO;
        if (numberSystem > 0) {
            res = ts.getFeeAdditionalSystem().multiply(new BigDecimal(numberSystem - 1));
            res = res.add(ts.getFeeFirstSystem());
        }
        if (numberParticipant > (ts.getNbParticipantsIncludedInSystemFees() * numberSystem)) {
            BigDecimal participantFee = ts.getFeeParticipant();
            BigDecimal feePart = participantFee.multiply(new BigDecimal(numberParticipant - (ts.getNbParticipantsIncludedInSystemFees() *
                    numberSystem)));
            res = res.add(feePart);
        }
        return res;
    }

    /**
     * Calculate VAT amount (depending on billing country-address)
     * <p/>
     * We have 4 cases for the computation of the VAT, checking with the Billing Country (in billing address) : 1. You are from Belgium and then it
     * is always 21% VAT 2. You are from a country not in
     * the European Community. You will NOT have to pay the 21 % of Belgium VAT 3. You are from a country within the European Community AND you
     * have a valid VAT number (see EU Taxation and Customs
     * Union for more information) then you will not have to pay the Belgium VAT. 4. You are from a country within the European Community BUT you
     * do NOT have a valid VAT number. Then you will be
     * charged for the Belgium VAT amount (21%). 5. None VAT number is entered, then VAT amount reaches 21%
     *
     * @param invoice : Invoice to update
     * @return Invoice : invoice with new VAT amount
     */
    public static Invoice calculateVatAmountForCompany(Invoice invoice, EntityManager em) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoice calculateVatAmountForCompany");
        }
        if ((invoice == null) || (invoice.getInstitution() == null) || (invoice.getInstitution().getId() == null)) {
            return invoice;
        }
        List<Iso3166CountryCode> liso = new ArrayList<Iso3166CountryCode>();
        if (invoice.getTestingSession() != null) {
            liso = invoice.getTestingSession().getListVATCountry(em);
        }
        Pair<Boolean, BigDecimal> resvat = FinancialCalc.calculateVatAmount(invoice.getTestingSession(), invoice.getFeesAmount(), invoice
                        .getFeesDiscount(), invoice.getVatNumber(), liso,
                invoice.getVatCountry());
        if (resvat != null) {
            if (resvat.getObject2() != null) {
                invoice.setVatAmount(resvat.getObject2());
            } else {
                invoice.setVatAmount(BigDecimal.ZERO);
            }
            invoice.setVatDue(resvat.getObject1());
        }
        return invoice;
    }

    /**
     * Calculate VAT amount (depending on billing country-address)
     * <p/>
     * We have 4 cases for the computation of the VAT, checking with the Billing Country (in billing address) : 1. You are from Belgium and then it
     * is always 21% VAT 2. You are from a country not in
     * the European Community. You will NOT have to pay the 21 % of Belgium VAT 3. You are from a country within the European Community AND you
     * have a valid VAT number (see EU Taxation and Customs
     * Union for more information) then you will not have to pay the Belgium VAT. 4. You are from a country within the European Community BUT you
     * do NOT have a valid VAT number. Then you will be
     * charged for the Belgium VAT amount (21%). 5. None VAT number is entered, then VAT amount reaches 21%
     *
     * @param invoice : Invoice to update
     * @return Invoice : invoice with new VAT amount
     */
    public static Pair<Boolean, BigDecimal> calculateVatAmount(TestingSession ts, BigDecimal feesAmount, BigDecimal feesDiscount, String vatNumber,
                                                               List<Iso3166CountryCode> liso,
                                                               Iso3166CountryCode iso) {
        Boolean vatDue = null;
        BigDecimal vatAmount = null;
        if (iso != null) {
            // Case 1
            if (liso.contains(iso)) {
                BigDecimal feesWithDiscount = feesAmount.add(feesDiscount.negate());
                vatAmount = feesWithDiscount.multiply(ts.getVatPercent()).setScale(2);
                vatDue = true;
            }
            // Case 2
            else if ((iso.getEc() == null) || (!iso.getEc())) {
                vatAmount = BigDecimal.ZERO;
                vatDue = false;

            }
            // Case 3 and 4
            else {
                // TODO Important Note : For now we don't check the VAT number validity !!! so case 3 and 4 are the same.
                if ((vatNumber != null) && (!vatNumber.equals(""))) {
                    vatAmount = BigDecimal.ZERO;
                    vatDue = false;
                }
                // Case 5
                else {
                    BigDecimal feesWithoutDiscount = feesAmount.add(feesDiscount.negate());
                    vatAmount = feesWithoutDiscount.multiply(ts.getVatPercent()).setScale(2);
                    vatDue = true;
                }
            }
        }
        if ((vatDue != null) && (vatAmount != null)) {
            return new Pair<Boolean, BigDecimal>(vatDue, vatAmount);
        } else {
            return new Pair<Boolean, BigDecimal>(false, BigDecimal.ZERO);
        }
    }

    public static boolean matchVATCountryToInstitutionInSession(Institution inst, Iso3166CountryCode isoInvoice, EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean matchVATCountryToInstitutionInSession");
        }
        if (isoInvoice == null) {
            return false;
        }
        if (inst == null) {
            return false;
        }
        Iso3166CountryCode iso = null;
        List<Person> listOfPerson = Person.listAllBillingContacts(entityManager, inst);
        if ((listOfPerson != null) && (listOfPerson.size() > 0)) {
            Person p = listOfPerson.get(0);
            if (p.getAddress() != null) {
                iso = p.getAddress().getIso3166CountryCode();
            } else {
                return false;
            }
        }
        return isoInvoice.equals(iso);
    }

    public static boolean validateVATNumber(String vatNumber, Iso3166CountryCode vatCountry, String accessKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean validateVATNumber");
        }
        if ((vatNumber == null) || (vatNumber.trim().length() == 0)) {
            return false;
        } else {
            if (vatCountry == null) {
                return false;
            }
            String url = urlRESTForVATValidation + "access_key=" + accessKey + "&vat_number=" + vatCountry.getIso() + vatNumber;
            LOG.info(url);
            InputStream is;
            BufferedReader buff = null;
            try {
                is = connect(url);
                buff = new BufferedReader(new InputStreamReader(is));
                String response = buff.readLine();
                JSONObject obj = (JSONObject) JSONValue.parse(response);
                return (Boolean) obj.get("valid");
            } catch (NullPointerException e) {
                LOG.error("" + e.getMessage());
                return false;
            } catch (IOException e) {
                LOG.error("" + e.getMessage());
                return false;
            } finally {
                if (buff != null) {
                    try {
                        buff.close();
                    } catch (IOException e) {
                        LOG.error("" + e.getMessage());
                    }
                }
            }
        }
    }

    private static InputStream connect(String url) throws IOException {
        try {
            URLConnection conn = new URL(url).openConnection();
            InputStream in = conn.getInputStream();
            return in;
        } catch (IOException e) {
            throw e;
        }
    }

    public static Invoice recalculateFees(Invoice invoice, EntityManager em) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoice recalculateFees");
        }
        if ((invoice != null) && (invoice.getId() != null)) {
            invoice.setFeesAmount(FinancialCalc.calculateTotalFee(invoice.getTestingSession(), invoice.getNumberSystem(), invoice
                    .getNumberParticipant()));
            invoice.setNumberExtraParticipant(invoice.calculateNumberExtraParticipant());
            invoice = FinancialCalc.calculateVatAmountForCompany(invoice, em);
            invoice = Invoice.mergeInvoice(invoice, em);
        }
        return invoice;
    }

    public static Invoice reloadInvoice(Invoice invoice, EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoice reloadInvoice");
        }
        if ((invoice != null) && (invoice.getId() != null)) {
            List<SystemInSession> lsis = SystemInSession.getSystemsInSessionForCompanyForSession(entityManager, invoice.getInstitution(), invoice
                    .getTestingSession());
            List<ConnectathonParticipant> lcp = ConnectathonParticipant.filterConnectathonParticipant(entityManager, invoice.getInstitution(),
                    invoice.getTestingSession());
            invoice.setNumberSystem(lsis.size());
            invoice.setNumberParticipant(lcp.size());
            invoice.setFeesAmount(FinancialCalc.calculateTotalFee(invoice.getTestingSession(), lsis.size(), lcp.size()));
            invoice.setNumberExtraParticipant(invoice.calculateNumberExtraParticipant());
            invoice = FinancialCalc.calculateVatAmountForCompany(invoice, entityManager);
            invoice = Invoice.mergeInvoice(invoice, entityManager);
        }
        return invoice;
    }

}
