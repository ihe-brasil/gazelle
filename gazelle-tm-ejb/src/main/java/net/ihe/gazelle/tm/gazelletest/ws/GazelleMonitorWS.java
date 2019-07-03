package net.ihe.gazelle.tm.gazelletest.ws;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.xml.soap.SOAPException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Stateless
@Name("gazelleMonitorWS")
@WebService(name = "GazelleMonitorWS", serviceName = "GazelleMonitorWSService", portName = "GazelleMonitorWSPort")
public class GazelleMonitorWS implements GazelleMonitorWSRemote {

    private static final Logger LOG = LoggerFactory.getLogger(GazelleMonitorWS.class);

    @Override
    @WebMethod
    @WebResult(name = "validationSuccess")
    public Boolean changeTestStepsStatus(@WebParam(name = "monitorLogin") String monitorLogin,
                                         @WebParam(name = "testStepInstanceList") List<TestStepsInstance> testStepInstanceList) throws SOAPException {
        if ((monitorLogin == null) || monitorLogin.isEmpty()) {
            throw new SOAPException("You must provide the monitor's Login");
        } else {
            User monitorUser = User.FindUserWithUsername(monitorLogin);
            if (monitorUser == null) {
                throw new SOAPException("User with username " + monitorLogin
                        + " is not registered in this application: "
                        + ApplicationPreferenceManager.instance().getApplicationUrl());
            } else {
                User.remoteLogin.set(monitorLogin);
                if ((testStepInstanceList != null) && !testStepInstanceList.isEmpty()) {
                    EntityManager em = EntityManagerService.provideEntityManager();
                    try {
                        for (TestStepsInstance step : testStepInstanceList) {
                            TestStepsInstance currentStep = em.find(TestStepsInstance.class, step.getId());
                            currentStep.setLastModifierId(monitorLogin);
                            if (step.getStatus() != null) {
                                Status stepStatus = Status.getStatusByKeyword(step.getStatus().getKeyword());
                                currentStep.setStatus(stepStatus);
                                em.merge(currentStep);
                                em.flush();
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                } else {
                    return true;
                }
            }
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "validationSuccess")
    public Boolean changeTestInstanceStatus(@WebParam(name = "monitorLogin") String monitorLogin,
                                            @WebParam(name = "testInstanceId") String testInstanceId,
                                            @WebParam(name = "testInstanceStatus") String testInstanceStatus, @WebParam(name = "comment") String
                                                    comment)
            throws SOAPException {
        if ((monitorLogin == null) || monitorLogin.isEmpty()) {
            throw new SOAPException("You must provide the monitor's login");
        } else if ((testInstanceId == null) || testInstanceId.isEmpty()) {
            throw new SOAPException("You must provide the testInstanceId");
        } else if ((testInstanceStatus == null) || testInstanceStatus.isEmpty()) {
            throw new SOAPException("You must provide the test Instance Status");
        } else {
            User monitorUser = User.FindUserWithUsername(monitorLogin);
            if (monitorUser == null) {
                throw new SOAPException("User with username " + monitorLogin
                        + " is not registered in this application: "
                        + ApplicationPreferenceManager.instance().getApplicationUrl());
            } else {
                User.remoteLogin.set(monitorLogin);
                EntityManager em = EntityManagerService.provideEntityManager();
                TestInstance testInstance = em.find(TestInstance.class, Integer.parseInt(testInstanceId));
                Status status = Status.getStatusByKeyword(testInstanceStatus);
                if (status == null) {
                    throw new SOAPException(testInstanceStatus + " is not a valid test instance status");
                } else if (null == testInstance) {
                    throw new SOAPException("You must provide a valid testInstanceId");
                } else {

                    // this lastModifierId will be used for comments
                    testInstance.setLastModifierId(monitorLogin);

                    testInstance.setLastStatus(status);
                    // addition comments
                    if ((comment != null) && !comment.isEmpty()) {
                        testInstance.addComment(comment);
                    }
                    em.merge(testInstance);
                    em.flush();
                    TestingSession ts = testInstance.getTestingSession();
                    Integer counter = ts.getNbOfTestInstancesVerifiedOnSmartphone();
                    if (counter != null) {
                        ts.setNbOfTestInstancesVerifiedOnSmartphone(counter + 1);
                    } else {
                        ts.setNbOfTestInstancesVerifiedOnSmartphone(1);
                    }
                    em.merge(ts);
                    em.flush();
                    return true;
                }
            }

        }
    }

    @Override
    @WebMethod
    @WebResult(name = "testInstance")
    public List<TestInstanceInformation> getClaimedTestsForMonitor(
            @WebParam(name = "monitorLogin") String monitorLogin,
            @WebParam(name = "testingSessionIdString") String testingSessionIdString) throws SOAPException {
        if ((monitorLogin == null) || monitorLogin.isEmpty()) {
            throw new SOAPException("You must provide the monitor's login");
        } else {
            User monitorUser = User.FindUserWithUsername(monitorLogin);
            if (monitorUser == null) {
                throw new SOAPException("User with username " + monitorLogin
                        + " is not registered in this application: "
                        + ApplicationPreferenceManager.instance().getApplicationUrl());
            } else {
                User.remoteLogin.set(monitorLogin);
                if ((testingSessionIdString == null) || (testingSessionIdString.isEmpty())) {
                    throw new SOAPException("No testing session with is id: " + testingSessionIdString);
                }
                TestingSession testingSession = TestingSession.GetSessionById(Integer.parseInt(testingSessionIdString));
                if (testingSession == null) {
                    throw new SOAPException("No testing session with is id: " + testingSessionIdString);
                } else {
                    MonitorInSession monitorInSession = MonitorInSession
                            .getActivatedMonitorInSessionForATestingSessionByUser(testingSession, monitorUser);
                    if (monitorInSession == null) {
                        throw new SOAPException("User " + monitorLogin + " is not a monitor for this testing session: "
                                + testingSession.getDescription());
                    } else {
                        List<TestInstance> testInstances = new ArrayList<TestInstance>();
                        // if test are set partially verified, they must appear at the end of the list
                        testInstances.addAll(TestInstance.getTestInstancesFiltered(monitorInSession,
                                Status.getSTATUS_PARTIALLY_VERIFIED(), null, null, null, null, null, null, null,
                                monitorInSession.getTestingSession(), null));

                        // tests which need to be verified
                        testInstances.addAll(0, TestInstance.getTestInstancesFiltered(monitorInSession,
                                Status.getSTATUS_COMPLETED(), null, null, null, null, null, null, null,
                                monitorInSession.getTestingSession(), null));

                        // if the critical status is enabled for the selected testing session, tests with this status are put in top of the list
                        if (testingSession.getIsCriticalStatusEnabled()) {
                            testInstances.addAll(0, TestInstance.getTestInstancesFiltered(monitorInSession,
                                    Status.getSTATUS_CRITICAL(), null, null, null, null, null, null, null,
                                    monitorInSession.getTestingSession(), null));
                        }
                        // if the list of test is not null, create a list of TestInstanceInformation
                        if ((testInstances != null) && !testInstances.isEmpty()) {
                            List<TestInstanceInformation> informations = new ArrayList<TestInstanceInformation>();
                            for (TestInstance instance : testInstances) {
                                TestInstanceInformation info = new TestInstanceInformation();
                                info.setTestInstanceId(Integer.toString(instance.getId()));
                                info.setTestInstanceStatus(instance.getLastStatus().getKeyword());
                                info.setTestKeyword(instance.getTest().getKeyword());
                                info.setTestingSession(instance.getTestingSession());
                                // get list of test steps and sort it according their step index
                                List<TestStepsInstance> stepInstances = instance.getTestStepsInstanceList();
                                Collections.sort(stepInstances);
                                info.setTestStepsInstances(stepInstances);
                                HQLQueryBuilder<TestInstanceParticipants> builder = new HQLQueryBuilder<TestInstanceParticipants>(
                                        TestInstanceParticipants.class);
                                builder.addEq("testInstance.id", instance.getId());
                                info.setTestInstanceParticipants(builder.getList());
                                informations.add(info);
                            }
                            return informations;
                        } else {

                            return null;
                        }
                    }

                }
            }
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "testingSession")
    public List<TestingSession> getMonitorTestingSessions(@WebParam(name = "monitorLogin") String monitorLogin)
            throws SOAPException {
        if ((monitorLogin == null) || monitorLogin.isEmpty()) {
            throw new SOAPException("You must provide the monitor's login");
        } else {
            User user = User.FindUserWithUsername(monitorLogin);
            if (user == null) {
                throw new SOAPException("No user with username " + monitorLogin
                        + " registered in this Gazelle application: "
                        + ApplicationPreferenceManager.instance().getApplicationName());
            } else {
                List<MonitorInSession> monitorsInSession = MonitorInSession.getAllActivatedMonitorsByUser(user);
                if ((monitorsInSession == null) || monitorsInSession.isEmpty()) {
                    throw new SOAPException("You are not set as monitor for any of the running testing sessions");
                } else {
                    List<TestingSession> sessions = new ArrayList<TestingSession>();
                    Date now = new Date();
                    for (MonitorInSession monitor : monitorsInSession) {
                        Date beginDate = monitor.getTestingSession().getBeginningSession();
                        Date endDate = monitor.getTestingSession().getEndingSession();
                        if (now.before(endDate) && now.after(beginDate)) {
                            sessions.add(monitor.getTestingSession());
                        }
                    }
                    if (sessions.isEmpty()) {
                        throw new SOAPException("You are not set as monitor for any of the running testing sessions");
                    } else {
                        return sessions;
                    }
                }
            }
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "authenticate")
    public Boolean authenticateUserByPassword(@WebParam(name = "userLogin") String userLogin,
                                              @WebParam(name = "md5Password") String password) throws SOAPException {

        if ((userLogin == null) || userLogin.isEmpty()) {
            throw new SOAPException("You must provide your login");
        } else if ((password == null) || password.isEmpty()) {
            throw new SOAPException("You must provide your password (password must be encrypted using MD5)");
        } else {
            User user = User.FindUserWithUsername(userLogin);
            if (user == null) {
                return false;
            } else {
                if (user.getPassword().equals(password)) {
                    return true;
                } else {
                    return false;
                }
            }
        }

    }

    @Override
    @WebMethod
    @WebResult(name = "unclaimed")
    public Boolean unclaimTestInstanceById(@WebParam(name = "testInstanceId") String testInstanceId,
                                           @WebParam(name = "monitorLogin") String monitorLogin) throws SOAPException {
        if ((monitorLogin == null) || monitorLogin.isEmpty()) {
            throw new SOAPException("You must first logging in");
        } else if ((testInstanceId == null) || testInstanceId.isEmpty()) {
            throw new SOAPException("The test instance id is null !");
        } else {
            EntityManager em = EntityManagerService.provideEntityManager();
            try {
                Integer id = Integer.parseInt(testInstanceId);
                TestInstance testInstance = em.find(TestInstance.class, id);
                User user = User.FindUserWithUsername(monitorLogin);
                MonitorInSession monitor;

                // check test instance exists
                if (testInstance == null) {
                    throw new SOAPException("This id does not match an existing test instance");
                }

                // check user exists and is a monitor for the testing session of the given test instance
                if (user != null) {
                    monitor = MonitorInSession.getActivatedMonitorInSessionForATestingSessionByUser(
                            testInstance.getTestingSession(), user);
                    if (null == monitor) {
                        throw new SOAPException("You are not allowed to monitor this test instance");
                    }
                } else {

                    throw new SOAPException("Bad user login");
                }

                // check the connectathon for this testing session is not over
                if (testInstance.getTestingSession().getEndingSession().before(new Date())) {
                    throw new SOAPException(
                            "This connectathon is over, you are not allowed to modify this test instance anymore");
                }

                User.remoteLogin.set(monitorLogin);
                // check test instance is not claimed by this monitor
                if (!testInstance.getMonitorInSession().equals(monitor)) {
                    throw new SOAPException("You are not monitor for this test instance");
                } else {
                    testInstance.setMonitorInSession(null);
                    em.merge(testInstance);
                    em.flush();
                    return true;
                }
            } catch (NumberFormatException e) {
                throw new SOAPException("This is not a valid test instance id", e);
            }
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "testInstance")
    public TestInstanceInformation claimTestInstanceById(@WebParam(name = "testInstanceId") String testInstanceId,
                                                         @WebParam(name = "monitorLogin") String monitorLogin) throws SOAPException {

        if ((monitorLogin == null) || monitorLogin.isEmpty()) {
            throw new SOAPException("You must first logging in");
        } else if ((testInstanceId == null) || testInstanceId.isEmpty()) {
            throw new SOAPException("The test instance id is null !");
        } else {
            EntityManager em = EntityManagerService.provideEntityManager();
            try {
                Integer id = Integer.parseInt(testInstanceId);
                TestInstance testInstance = em.find(TestInstance.class, id);

                // check test instance exists
                if (testInstance == null) {
                    throw new SOAPException("This id does not match an existing test instance");
                }
                TestingSession ts = testInstance.getTestingSession();
                User user = User.FindUserWithUsername(monitorLogin);
                MonitorInSession monitor;

                // check user exists and is a monitor for the testing session of the given test instance
                if (user != null) {
                    monitor = MonitorInSession.getActivatedMonitorInSessionForATestingSessionByUser(
                            testInstance.getTestingSession(), user);
                    if (monitor == null) {
                        throw new SOAPException("You are not allowed to monitor this test instance");
                    }
                } else {
                    throw new SOAPException("Bad user login");
                }

                // check the connectathon for this testing session is not over
                if (testInstance.getTestingSession().getEndingSession().before(new Date())) {
                    throw new SOAPException(
                            "This connectathon is over, you are not allowed to modify this test instance anymore");
                }

                User.remoteLogin.set(monitorLogin);
                // check test instance is not already claimed by another monitor
                if ((testInstance.getMonitorInSession() != null) && !testInstance.getMonitorInSession().equals(monitor)) {
                    throw new SOAPException("This test instance has already been claimed by "
                            + testInstance.getMonitorInSession().getUser().getFirstname() + " "
                            + testInstance.getMonitorInSession().getUser().getLastname());
                }

                // check test instance is ready to be verified
                if (testInstance.getLastStatus().equals(Status.getSTATUS_COMPLETED())
                        || testInstance.getLastStatus().equals(Status.getSTATUS_CRITICAL())
                        || testInstance.getLastStatus().equals(Status.getSTATUS_PARTIALLY_VERIFIED())) {
                    TestInstanceInformation info = new TestInstanceInformation();
                    info.setTestInstanceId(testInstanceId);
                    info.setTestInstanceStatus(testInstance.getLastStatus().getKeyword());
                    info.setTestKeyword(testInstance.getTest().getKeyword());
                    info.setTestingSession(testInstance.getTestingSession());
                    // get list of test steps and sort it according their step index
                    List<TestStepsInstance> stepInstances = testInstance.getTestStepsInstanceList();
                    Collections.sort(stepInstances);
                    info.setTestStepsInstances(stepInstances);
                    HQLQueryBuilder<TestInstanceParticipants> builder = new HQLQueryBuilder<TestInstanceParticipants>(
                            TestInstanceParticipants.class);
                    builder.addEq("testInstance.id", testInstance.getId());
                    info.setTestInstanceParticipants(builder.getList());
                    return info;
                } else if (testInstance.getLastStatus().equals(Status.getSTATUS_VERIFIED())
                        || testInstance.getLastStatus().equals(Status.getSTATUS_FAILED())) {
                    throw new SOAPException("This test instance has already been verified by a monitor");
                } else {
                    throw new SOAPException("This test instance is not ready to be verified yet");
                }
            } catch (NumberFormatException e) {
                throw new SOAPException("No test instance with such an id", e);
            }

        }
    }
}
