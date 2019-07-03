package net.ihe.gazelle.ws.tm.tests;

import net.ihe.gazelle.tm.ws.data.*;

import javax.ejb.Local;
import javax.ws.rs.*;
import javax.xml.bind.JAXBException;

@Local
@Path("/tests")
public interface TestsWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    public TestsWrapper getTests(@QueryParam("integrationProfileKeyword") String integrationProfileKeyword,
                                 @QueryParam("domain") String domain, @QueryParam("testType") String testType, @QueryParam("actor") String actor,
                                 @QueryParam("transaction") String transaction) throws JAXBException;

    @GET
    @Path("/types")
    @Produces("application/xml")
    public TestTypesWrapper getTestsTypes(@QueryParam("domain") String domain,
                                          @QueryParam("integrationProfile") String integrationProfile) throws JAXBException;

    @GET
    @Path("/{testId}")
    @Produces("application/xml")
    public TestWrapper getTest(@PathParam("testId") int testId) throws JAXBException;

    @GET
    @Path("/{testId}/description/{language}")
    @Produces("application/xml")
    public TestDescriptionWrapper getTestDescription(@PathParam("testId") int testId,
                                                     @PathParam("language") String languageKeyword) throws JAXBException;

    @GET
    @Path("/{testId}/testSteps")
    @Produces("application/xml")
    public TestStepsWrapper getTestSteps(@PathParam("testId") int testId) throws JAXBException;
}
