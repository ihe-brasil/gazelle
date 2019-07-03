package net.ihe.gazelle.ws.tf.ActorIntegrationProfileOption;

import net.ihe.gazelle.tf.ws.data.AipoWrapper;
import net.ihe.gazelle.tf.ws.data.AiposWrapper;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import javax.ejb.Local;
import javax.ws.rs.*;
import javax.xml.bind.JAXBException;

@Local
@Path("/actorIntegrationProfileOptions")
public interface ActorIntegrationProfileOptionWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    @Wrapped
    public AiposWrapper getActorIntegrationProfileOptions(@QueryParam("domain") String domain, @QueryParam("actor") String actor, @QueryParam
            ("integrationProfile") String integrationProfile,
                                                          @QueryParam("integrationProfileOption") String integrationProfileOption) throws
            JAXBException;

    @GET
    @Path("/{id}")
    @Produces("application/xml")
    @Wrapped
    public AipoWrapper getActorIntegrationProfileOption(@PathParam("id") String id) throws JAXBException;

}
