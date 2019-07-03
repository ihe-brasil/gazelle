package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.documents.DocumentManager;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Stateless
@Name("documentWebService")
public class DocumentWebService implements DocumentWebServiceApi {

    @Override
    @GET
    @Path("/openPdf/{encodedUri}")
    @Produces("text/plain")
    public String openPdf(@PathParam("encodedUri") String uri) {
        return DocumentManager.showPDFWebservice(uri);
    }
}
