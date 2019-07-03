package net.ihe.gazelle.tf.ws;

import org.junit.Ignore;
import org.junit.Test;

import javax.xml.soap.SOAPException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@Ignore
public class Hl7MessageProfileTest {

    @Test
    public void testHl7MessageProfile() throws SOAPException {
        Hl7MessageProfile hl7 = new Hl7MessageProfile();
        net.ihe.gazelle.tf.model.Hl7MessageProfile profile = hl7.getHL7MessageProfileByOID("1.3.6.1.4.12559.11.1.1.1");
        assertEquals("1.3.6.1.4.12559.11.1.1.1", profile.getProfileOid());
        assertNotNull(profile.getTransaction());
        assertNotNull(profile.getTransaction().getTransactionLinks());
    }
}
