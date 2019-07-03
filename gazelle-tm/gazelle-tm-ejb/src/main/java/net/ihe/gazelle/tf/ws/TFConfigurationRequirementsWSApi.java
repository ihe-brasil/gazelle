package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.ws.data.TFConfigurationTypesWrapper;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Local
@Path("/")
public interface TFConfigurationRequirementsWSApi {

    @GET
    @Path("/tfConfigurations/{integrationprofile}/{actor}/{option}")
    @Produces("text/xml")
    public TFConfigurationTypesWrapper listTFConfigurationsForAIPO(@PathParam("actor") String actorKeyword,
                                                                   @PathParam("integrationprofile") String integrationprofileKeyword, @PathParam
                                                                           ("option") String option);

}
