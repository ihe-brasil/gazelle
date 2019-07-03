package net.ihe.gazelle.tm.gazelletest.ws.trm;

import javax.ejb.Remote;
import javax.xml.soap.SOAPException;

@Remote
public interface GazelleTRMRemote {

    TRMTestInstance getTestInstanceById(int testInstanceId) throws SOAPException;

    TRMTestInstances getTestInstancesByTestingSession(int testingSessionId) throws SOAPException;

    boolean getSystemIsATool(String systemKeyword,int testingSessionId) throws SOAPException;

}
