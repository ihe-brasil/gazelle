package net.ihe.gazelle.ws.tf.integrationProfileOptions;

import net.ihe.gazelle.tf.ws.data.IntegrationProfileOptionNamesWrapper;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;

@Local
@Path("/integrationProfileOptions")
public interface IntegrationProfileOptionWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    @Wrapped
    public IntegrationProfileOptionNamesWrapper getIntegrationProfileOptions(@QueryParam("domain") String domain, @QueryParam("actor") String actor,
                                                                             @QueryParam("integrationProfile") String integrationProfile) throws
            JAXBException;

}
