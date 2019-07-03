package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.xml.soap.SOAPException;
import java.util.List;

@Stateless
@Name("gazelleTRM")
@WebService(name = "gazelleTRM", serviceName = "GazelleTRMService", portName = "GazelleTRMPort")
public class GazelleTRM implements GazelleTRMRemote {

    @Override
    @WebMethod
    @WebResult(name = "testInstance")
    public net.ihe.gazelle.tm.gazelletest.ws.trm.TRMTestInstance getTestInstanceById(
            @WebParam(name = "testInstanceId") int testInstanceId) throws SOAPException {

        if (testInstanceId <= 0) {
            throw new SOAPException(testInstanceId + " is not valid test Instance Id");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        TestInstance testInstance = em.find(TestInstance.class, testInstanceId);

        if (null == testInstance) {
            throw new SOAPException(testInstanceId + " is not valid test Instance Id");
        }
        TRMTestInstance testInstanceTRM = new TRMTestInstance();
        testInstanceTRM.copyFromTestInstance(testInstance);

        return testInstanceTRM;
    }

    @Override
    @WebMethod
    @WebResult(name = "testInstances")
    public net.ihe.gazelle.tm.gazelletest.ws.trm.TRMTestInstances getTestInstancesByTestingSession(
            @WebParam(name = "testingSessionId") int testingSessionId) throws SOAPException {

        HQLQueryBuilder<TestInstance> querybuilder = new HQLQueryBuilder<TestInstance>(TestInstance.class);
        querybuilder.addRestriction(HQLRestrictions.eq("testingSession.id", testingSessionId));
        List<TestInstance> list = querybuilder.getList();

        TRMTestInstances testInstancesTRM = new TRMTestInstances();
        testInstancesTRM.copyFromTestInstance(list);
        return testInstancesTRM;

    }


    @Override
    @WebMethod
    @WebResult(name = "isAtool")
    public boolean getSystemIsATool(
            @WebParam(name = "systemKeyword") String systemKeyword,
            @WebParam(name = "testingSessionId") int testingSessionId) throws SOAPException {

        if (testingSessionId <= 0) {
            throw new SOAPException(testingSessionId + " is not a valid testingSessionId.");
        }
        if (null == systemKeyword) {
            throw new SOAPException("You have to specify the systemKeyword.");
        }
        SystemInSessionQuery q = new SystemInSessionQuery();
        q.testingSession().id().eq(testingSessionId);
        q.system().keyword().eq(systemKeyword);
        q.system().isTool().eq(true);
        return q.getCount() > 0;
    }

}
