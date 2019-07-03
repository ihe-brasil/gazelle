package net.ihe.gazelle.tf.ws;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Local
@Path("/")
public interface DocumentWebServiceApi {

    @GET
    @Path("/openPdf/{encodedUri}")
    @Produces("text/plain")
    public String openPdf(@PathParam("encodedUri") String uri);
}
