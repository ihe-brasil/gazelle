package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Transaction;
import net.ihe.gazelle.tf.model.TransactionQuery;
import net.ihe.gazelle.tf.ws.data.TransactionNamesWrapper;
import net.ihe.gazelle.tf.ws.data.TransactionWrapper;
import net.ihe.gazelle.tf.ws.data.TransactionsWrapper;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("transactionWs")
public class TransactionWs implements TransactionWsApi {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionWs.class);

    @Override
    public TransactionsWrapper getTransactions(String domain) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactions");
        }
        TransactionQuery query = new TransactionQuery();
        if (domain != null) {
            query.profileLinks().actorIntegrationProfile().integrationProfile().domainsForDP().keyword().eq(domain);
        }

        List<Transaction> transactions = query.getListDistinct();

        List<TransactionWrapper> transactionWrapperList = new ArrayList<TransactionWrapper>();

        for (Transaction transaction : transactions) {
            transactionWrapperList
                    .add(new TransactionWrapper(transaction.getId(), transaction.getKeyword(), transaction.getName()));
        }
        TransactionsWrapper transactionsWrapper = new TransactionsWrapper(transactionWrapperList);

        transactionsWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());

        return transactionsWrapper;
    }

    @Override
    public TransactionWrapper getTransaction(String id) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransaction");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Transaction transaction = em.find(Transaction.class, Integer.valueOf(id));
        TransactionWrapper transactionWrapper = new TransactionWrapper(transaction.getId(), transaction.getKeyword(),
                transaction.getName(), transaction.getDescription());

        transactionWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());
        return transactionWrapper;
    }

    @Override
    public TransactionNamesWrapper getTransactionNamesWithAuditMessage(String domain, String actorKeyword)
            throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionNamesWithAuditMessage");
        }
        TransactionQuery query = new TransactionQuery();
        if (domain != null) {
            query.profileLinks().actorIntegrationProfile().integrationProfile().domainsForDP().keyword().eq(domain);
        }
        if (actorKeyword != null) {
            query.profileLinks().actorIntegrationProfile().actor().keyword().eq(actorKeyword);
        }
        query.auditMessages().isNotEmpty();
        List<Transaction> transactions = query.getListDistinct();

        List<String> transactionNames = new ArrayList<String>();

        for (Transaction transaction : transactions) {
            transactionNames.add(transaction.getKeyword());
        }

        return new TransactionNamesWrapper(transactionNames);
    }

    public TransactionNamesWrapper getTransactionNamesWithTest(@QueryParam("domain") String domain,
                                                               @QueryParam("actorKeyword") String actorKeyword) throws JAXBException {
        TestQuery query = new TestQuery();
        if (domain != null && !"".equals(domain)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().integrationProfile().domainsProfile().domain().keyword().eq(domain);
        }
        if (actorKeyword != null && !"".equals(actorKeyword)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().actor().keyword().eq(actorKeyword);
        }
        query.testStepsList().transaction().isNotNull();
        List<String> transactionNames = query.testStepsList().transaction().keyword().getListDistinct();

        return new TransactionNamesWrapper(transactionNames);
    }
}
