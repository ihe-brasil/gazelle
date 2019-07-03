package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

public class ProxyStartTestInstanceJob implements ProxyStartTestInstanceJobInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyStartTestInstanceJob.class);

    public void proxyStartTestInstanceJob(int testInstanceId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("proxyStartTestInstanceJob");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        TestInstanceQuery query = new TestInstanceQuery();
        query.id().eq(testInstanceId);
        TestInstance selectedTestInstance = query.getUniqueResult();

        try {
            if (!ProxyManager.startTestInstance(selectedTestInstance)) {
                selectedTestInstance.setProxyUsed(false);
                entityManager.merge(selectedTestInstance);
            }
        } catch (Exception e) {
            LOG.error("" + e.getMessage());
            selectedTestInstance.setProxyUsed(false);
            entityManager.merge(selectedTestInstance);
        }
    }

}
