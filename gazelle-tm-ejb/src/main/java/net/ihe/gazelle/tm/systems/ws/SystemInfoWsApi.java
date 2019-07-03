package net.ihe.gazelle.tm.systems.ws;

import net.ihe.gazelle.tm.ws.data.SystemsInSessionWrapper;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Local
@Path("/")
public interface SystemInfoWsApi {

    @GET
    @Path("/username/{username}/systems/apikey/{apikey}")
    @Produces("text/xml")
    public SystemsInSessionWrapper getSystemsForTestingSessionsForUser(@PathParam("username") String username,
                                                                       @PathParam("apikey") String apiKey);

    @GET
    @Path("/username/{username}/company/apikey/{apikey}")
    @Produces("plain/text")
    public String getInstitutionKeywordForUser(@PathParam("username") String username,
                                               @PathParam("apikey") String apiKey);
}
