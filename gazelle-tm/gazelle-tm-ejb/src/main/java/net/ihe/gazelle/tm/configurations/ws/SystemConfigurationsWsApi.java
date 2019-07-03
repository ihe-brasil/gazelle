package net.ihe.gazelle.tm.configurations.ws;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Local
@Path("/")
public interface SystemConfigurationsWsApi {

    @GET
    @Path("/{tsid}/{institution}/{system}/{actor}/configurations/{type}")
    @Produces("text/csv")
    public Response listSystemConfigurationsFiltered(@PathParam("tsid") String testingSessionId,
                                                     @PathParam("system") String systemKeyword, @PathParam("type") String configType,
                                                     @PathParam("actor") String actorkeyword, @PathParam("institution") String institutionKeyword);

    @GET
    @Path("/{tsid}/{institution}/{system}/{actor}/oids/{reqlabel}")
    @Produces("text/csv")
    public Response getOIDBySystemByRequirement(@PathParam("system") String systemKeyword,
                                                @PathParam("reqlabel") String oidRequiremenLabel, @PathParam("tsid") String testingSessionIdString,
                                                @PathParam("institution") String institutionKeyword, @PathParam("actor") String actorKeyword);

    @GET
    @Path("/{system}/tfConfigurations")
    @Produces("text/xml")
    @Deprecated
    public Response listTFConfigurationsForSystemInSession(@PathParam("system") String systemKeyword);

    @GET
    @Path("/{testingSessionId}/{system}/tfConfigurations")
    @Produces("text/xml")
    public Response listTFConfigurationsForSystemInSession(@PathParam("system") String systemKeyword, @PathParam("testingSessionId") String
            testingSessionId);

    @GET
    @Path("/{system}/inbounds/{communicationType}")
    @Produces("text/xml")
    @Deprecated
    public Response listInboundsForSystemInSession(@PathParam("system") String systemKeyword,
                                                   @PathParam("communicationType") String networkCommunicationTypeKeyword);

    @GET
    @Path("/{system}/outbounds/{communicationType}")
    @Produces("text/xml")
    @Deprecated
    public Response listOutboundsForSystemInSession(@PathParam("system") String systemKeyword,
                                                    @PathParam("communicationType") String networkCommunicationTypeKeyword);

    @GET
    @Path("/{testingSessionId}/{system}/inbounds/{communicationType}")
    @Produces("text/xml")
    public Response listInboundsForSystemInSession(@PathParam("system") String systemKeyword,
                                                   @PathParam("communicationType") String networkCommunicationTypeKeyword,
                                                   @PathParam("testingSessionId") String testingSessionId);

    @GET
    @Path("/{testingSessionId}/{system}/outbounds/{communicationType}")
    @Produces("text/xml")
    public Response listOutboundsForSystemInSession(@PathParam("system") String systemKeyword,
                                                    @PathParam("communicationType") String networkCommunicationTypeKeyword,
                                                    @PathParam("testingSessionId") String testingSessionId);

}
