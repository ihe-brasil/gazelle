package net.ihe.gazelle.tf.ws;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Local
@Path("/")
public interface AuditMessageWsApi {

    @GET
    @Path("/auditMessages")
    @Produces("text/xml")
    public Response getAuditMessages();

    @GET
    @Path("/auditMessages/{actor}/{transaction}/{event}")
    @Produces("text/xml")
    public Response getAuditMessagesFiltered(@PathParam("actor") String actorKeyword,
                                             @PathParam("transaction") String transactionKeyword, @PathParam("event") String event);

    @GET
    @Path("/{system}/auditMessages")
    @Produces("text/xml")
    @Deprecated
    // it appears that system keywords are not unique in database, thus this method might  not return accurate results
    public Response getAuditMessagesToBeProducedBySystemInSession(@PathParam("system") String systemKeyword);

    @GET
    @Path("{testingSessionId}/{system}/auditMessages")
    @Produces("text/xml")
    public Response getAuditMessagesToBeProducedBySystemForSession(@PathParam("system") String systemKeyword, @PathParam("testingSessionId") String
            testingSessionId);
}
