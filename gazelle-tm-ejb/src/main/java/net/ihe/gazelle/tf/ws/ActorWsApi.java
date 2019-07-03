package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.ws.data.ActorWrapper;
import net.ihe.gazelle.tf.ws.data.ActorsNameWrapper;
import net.ihe.gazelle.tf.ws.data.ActorsWrapper;

import javax.ejb.Local;
import javax.ws.rs.*;
import javax.xml.bind.JAXBException;

@Local
@Path("/actors")
public interface ActorWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    public ActorsWrapper getActors(@QueryParam("domain") String domain,
                                   @QueryParam("integrationProfile") String integrationProfile) throws JAXBException;

    @GET
    @Path("/{id}")
    @Produces("application/xml")
    public ActorWrapper getActor(@PathParam("id") String id) throws JAXBException;

    @GET
    @Path("/names")
    @Produces("application/xml")
    public ActorsNameWrapper getActorsNames(@QueryParam("domain") String domain,
                                            @QueryParam("integrationProfile") String integrationProfile,
                                            @QueryParam("transactionKeyword") String transactionKeyword) throws JAXBException;

    @GET
    @Path("/withAuditMessage/names")
    @Produces("application/xml")
    public ActorsNameWrapper getActorsNamesWithAuditMessage(@QueryParam("domain") String domain,
                                                            @QueryParam("integrationProfile") String integrationProfile,
                                                            @QueryParam("transactionKeyword") String transactionKeyword) throws JAXBException;

    @GET
    @Path("/withTest/names")
    @Produces("application/xml")
    public ActorsNameWrapper getActorsNamesWithTest(@QueryParam("domain") String domain,
                                                    @QueryParam("integrationProfile") String integrationProfile,
                                                    @QueryParam("transactionKeyword") String transactionKeyword) throws JAXBException;
}
