package net.ihe.gazelle.tm.financial.action;

import net.ihe.gazelle.users.model.Iso3166CountryCode;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jlabbe on 09/12/15.
 */

@Ignore
public class FinancialCalcTest {

    @Test
    public void testValidateCorrectVATNumber() throws Exception {
        assertTrue(FinancialCalc.validateVATNumber("26375245", new Iso3166CountryCode("LU", "Luxembourg", "Luxembourg"),
                "1cbd57053d547fe13a7df3a65951fe68"));
    }

    @Test
    public void testValidateWrongVATNumber() throws Exception {
        assertFalse(FinancialCalc.validateVATNumber("26375246", new Iso3166CountryCode("LU", "Luxembourg", "Luxembourg"),
                "1cbd57053d547fe13a7df3a65951fe68"));
    }
}