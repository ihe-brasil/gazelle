package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.ws.data.StandardWrapper;
import net.ihe.gazelle.tf.ws.data.StandardsWrapper;

import javax.ejb.Local;
import javax.ws.rs.*;
import javax.xml.bind.JAXBException;

@Local
@Path("/standards")
public interface StandardWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    public StandardsWrapper getStandards(@QueryParam("domain") String domain, @QueryParam("transaction") String transaction, @QueryParam
            ("communicationType") String communicationType)
            throws JAXBException;

    @GET
    @Path("/{id}")
    @Produces("application/xml")
    public StandardWrapper getStandard(@PathParam("id") String id) throws JAXBException;

}