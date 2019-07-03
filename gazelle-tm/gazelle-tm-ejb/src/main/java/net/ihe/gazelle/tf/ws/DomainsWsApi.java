package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tm.ws.data.domain.DomainsWrapper;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBException;

@Local
@Path("/domains")
public interface DomainsWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    public DomainsWrapper getDomains() throws JAXBException;

    @GET
    @Path("/withTests")
    @Produces("application/xml")
    public DomainsWrapper getDomainsWithTests() throws JAXBException;

    @GET
    @Path("/withAuditMessage")
    @Produces("application/xml")
    public DomainsWrapper getDomainsWithAuditMessage() throws JAXBException;

    @GET
    @Path("/withRules")
    @Produces("application/xml")
    public DomainsWrapper getDomainsWithRules() throws JAXBException;

}
