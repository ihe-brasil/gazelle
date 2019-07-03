package net.ihe.gazelle.tm.gazelletest.ws;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.TestingSession;

import javax.ejb.Remote;
import javax.xml.soap.SOAPException;
import java.util.List;

@Remote
public interface GazelleMonitorWSRemote {

    /**
     * Returns the whole list of test instances claimed by the monitor and with status set to "To be verified"
     *
     * @param monitorLogin
     * @param testingSessionIdString TODO
     * @return
     * @throws SOAPException
     */
    public List<TestInstanceInformation> getClaimedTestsForMonitor(String monitorLogin, String testingSessionIdString)
            throws SOAPException;

    /**
     * Changes test instance status and returns true if the operation succeeds
     *
     * @param monitorLogin
     * @param testInstanceId
     * @param testInstanceStatus
     * @return
     * @throws SOAPException
     */
    public Boolean changeTestStepsStatus(String monitorLogin, List<TestStepsInstance> testStepInstanceList)
            throws SOAPException;

    /**
     * Changes test step instance status and returns true if the operation succeeds
     *
     * @param comment           TODO
     * @param testStepInstances
     * @return
     * @throws SOAPException
     */
    public Boolean changeTestInstanceStatus(String monitorLogin, String testInstanceId, String testInstanceStatus,
                                            String comment) throws SOAPException;

    /**
     * Returns the list of testing session the user is monitor for.
     *
     * @param monitorLogin
     * @return
     * @throws SOAPException
     */
    public List<TestingSession> getMonitorTestingSessions(String monitorLogin) throws SOAPException;

    /**
     * Authenticates the user thanks to his/her password when the certificate is not present
     *
     * @param userLogin
     * @param password
     * @return
     * @throws SOAPException
     */
    public Boolean authenticateUserByPassword(String userLogin, String password) throws SOAPException;

    /**
     * When the user accesses the GazelleMonitorApp using the test instance QR code, we must 1. check the user is authenticated and is a monitor 2.
     * this instance is not already claimed by somebody
     * else 3. assign the test instance to this monitor (if 2 is OK) 4. send informations about this test instance
     *
     * @param testInstanceId
     * @param monitorLogin
     * @return
     * @throws SOAPException
     */
    public TestInstanceInformation claimTestInstanceById(String testInstanceId, String monitorLogin)
            throws SOAPException;

    public Boolean unclaimTestInstanceById(String testInstanceId, String monitorLogin) throws SOAPException;
}
