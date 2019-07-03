package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.ws.data.TransactionNamesWrapper;
import net.ihe.gazelle.tf.ws.data.TransactionWrapper;
import net.ihe.gazelle.tf.ws.data.TransactionsWrapper;

import javax.ejb.Local;
import javax.ws.rs.*;
import javax.xml.bind.JAXBException;

@Local
@Path("/transactions")
public interface TransactionWsApi {

    @GET
    @Path("/")
    @Produces("application/xml")
    public TransactionsWrapper getTransactions(@QueryParam("domain") String domain) throws JAXBException;

    @GET
    @Path("/{id}")
    @Produces("application/xml")
    public TransactionWrapper getTransaction(@PathParam("id") String id) throws JAXBException;

    @GET
    @Path("/withAuditMessage/names")
    @Produces("application/xml")
    public TransactionNamesWrapper getTransactionNamesWithAuditMessage(@QueryParam("domain") String domain,
                                                                       @QueryParam("actorKeyword") String actorKeyword) throws JAXBException;

    @GET
    @Path("/withTest/names")
    @Produces("application/xml")
    public TransactionNamesWrapper getTransactionNamesWithTest(@QueryParam("domain") String domain,
                                                               @QueryParam("actorKeyword") String actorKeyword) throws JAXBException;
}
