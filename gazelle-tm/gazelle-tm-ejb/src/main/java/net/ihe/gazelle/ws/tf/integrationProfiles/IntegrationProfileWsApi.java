package net.ihe.gazelle.ws.tf.integrationProfiles;

import net.ihe.gazelle.tf.ws.data.IntegrationProfileNamesWrapper;
import net.ihe.gazelle.tf.ws.data.IntegrationProfileWrapper;
import net.ihe.gazelle.tf.ws.data.IntegrationProfilesWrapper;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import javax.ejb.Local;
import javax.ws.rs.*;
import javax.xml.bind.JAXBException;

@Local
@Path("/integrationProfiles")
public interface IntegrationProfileWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    @Wrapped
    public IntegrationProfilesWrapper getIntegrationProfiles(@QueryParam("domain") String domain,
                                                             @QueryParam("actor") String actor) throws JAXBException;

    @GET
    @Path("/{id}")
    @Produces("application/xml")
    @Wrapped
    public IntegrationProfileWrapper getIntegrationProfile(@PathParam("id") String id) throws JAXBException;

    @GET
    @Path("/names")
    @Produces("application/xml")
    @Wrapped
    public IntegrationProfileNamesWrapper getIntegrationProfilesNames(@QueryParam("domain") String domain,
                                                                      @QueryParam("actor") String actor) throws JAXBException;

    @GET
    @Path("/withRules/names")
    @Produces("application/xml")
    @Wrapped
    public IntegrationProfileNamesWrapper getIntegrationProfilesNamesWithRules(@QueryParam("domain") String domain)
            throws JAXBException;

    @GET
    @Path("/withTests/names")
    @Produces("application/xml")
    @Wrapped
    public IntegrationProfileNamesWrapper getIntegrationProfilesNamesWithTests(@QueryParam("domain") String domain)
            throws JAXBException;
}
