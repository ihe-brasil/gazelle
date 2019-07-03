package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.ws.data.AuditMessageWrapper;
import net.ihe.gazelle.tf.ws.data.AuditMessagesWrapper;

import javax.ejb.Local;
import javax.ws.rs.*;
import javax.xml.bind.JAXBException;

@Local
@Path("/auditMessagesText")
public interface AuditMessagesTextWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    public AuditMessagesWrapper getAuditMessages(@QueryParam("domain") String domain,
                                                 @QueryParam("transactionKeyword") String transactionKeyword,
                                                 @QueryParam("actorKeyword") String actorKeyword) throws JAXBException;

    @GET
    @Path("/{id}")
    @Produces("application/xml")
    public AuditMessageWrapper getAuditMessage(@PathParam("id") String id) throws JAXBException;

}
