package net.ihe.gazelle.ws.tm.testSteps;

import net.ihe.gazelle.tm.ws.data.TestStepWrapper;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBException;

@Local
@Path("/testSteps")
public interface TestStepsWsApi {

    @GET
    @Path("/{id}")
    @Produces("application/xml")
    public TestStepWrapper getTestStep(@PathParam("id") int id) throws JAXBException;
}
