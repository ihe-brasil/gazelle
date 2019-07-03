package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.ws.data.RuleWrapper;
import net.ihe.gazelle.tf.ws.data.RulesWrapper;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBException;

@Local
@Path("/rules")
public interface RulesWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    public RulesWrapper getRules() throws JAXBException;

    @GET
    @Path("/integrationProfile/{keyword}")
    @Produces("application/xml")
    public RulesWrapper getRules(@PathParam("keyword") String integrationProfileKeyword) throws JAXBException;

    @GET
    @Path("/{id}")
    @Produces("application/xml")
    public RuleWrapper getRule(@PathParam("id") String id) throws JAXBException;

    @GET
    @Path("/consequence/integrationProfile/{integrationProfile}/actor/{actor}")
    @Produces("application/xml")
    public RuleWrapper getRulesWithConsequence(@PathParam("integrationProfile") String integrationProfile,
                                               @PathParam("actor") String actor) throws JAXBException;

    @GET
    @Path("/cause/integrationProfile/{integrationProfile}/actor/{actor}")
    @Produces("application/xml")
    public RuleWrapper getRulesWithCause(@PathParam("integrationProfile") String integrationProfile,
                                         @PathParam("actor") String actor) throws JAXBException;
}
