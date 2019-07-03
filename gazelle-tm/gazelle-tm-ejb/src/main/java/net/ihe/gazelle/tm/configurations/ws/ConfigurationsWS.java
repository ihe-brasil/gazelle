package net.ihe.gazelle.tm.configurations.ws;

import net.ihe.gazelle.csv.CSVExporter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tm.configurations.model.AbstractConfiguration;
import net.ihe.gazelle.tm.configurations.model.OIDRequirement;
import net.ihe.gazelle.tm.configurations.model.OIDSystemAssignment;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.soap.SOAPException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("GazelleProxyWebService")
@WebService(name = "GazelleProxyWebService", serviceName = "GazelleProxyWebServiceName", portName = "GazelleProxyWebServicePort")
public class ConfigurationsWS implements ConfigurationsWSRemote, Serializable {

    private static final String
            UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG = "Unknown configuration type : Valid configuration types are : DicomSCU, DicomSCP, HL7V2Initiator, HL7V2Responder,HL7V3Initiator, HL7V3Responder, Webservice,  Syslog, RAW";

    private static final String TESTING_SESSION_ID = "testingSessionId";

    private static final String TESTING_SESSION_ID_SHALL_BE_AN_INTEGER = "testingSessionId shall be an integer";

    private static final String TESTING_SESSION_ID_IS_A_REQUIRED_ARGUMENT = "testingSessionId is a required argument";

    private static final String RESULT = "result";

    private static final String CONFIGURATION_TYPE = "configurationType";

    private static final long serialVersionUID = 90961245318015544L;
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsWS.class);
    @In
    EntityManager entityManager;

    @WebMethod
    public String listConfigurationsBySystemBySessionByTypeAsCSV(
            @WebParam(name = "systemKeyword") String systemKeyword,
            @WebParam(name = TESTING_SESSION_ID) @XmlElement(required= true) String testingSessionIdString,
            @WebParam(name = CONFIGURATION_TYPE) @XmlElement(required= true) String configurationType) throws SOAPException {

        Integer testingSessionId;

        if ((null == testingSessionIdString) || (testingSessionIdString.isEmpty())) {
            throw new SOAPException(TESTING_SESSION_ID_IS_A_REQUIRED_ARGUMENT);
        }

        if ((null == configurationType) || (configurationType.isEmpty())) {
            throw new SOAPException(
                    "configurationType is a required argument : DicomSCU, DicomSCP, HL7V2Initiator, HL7V2Responder,HL7V3Initiator, HL7V3Responder, " +
                            "Webservice,  Syslog, Raw");
        }

        try{
            testingSessionId = Integer.parseInt(testingSessionIdString);

        } catch (NumberFormatException e) {
            LOG.error(e.getMessage());
            throw new SOAPException(TESTING_SESSION_ID_SHALL_BE_AN_INTEGER, e.getCause());
        }

        if((null != systemKeyword) && (!systemKeyword.equals(""))){
                System getValueSys = System.getSystemByAllKeyword(systemKeyword);
                if(null == getValueSys) {
                    throw new SOAPException("The systemKeyword is invalid");
                }
        }

        Class<?> selectedClass = null;
        Class<?>[] configurationTypes = AbstractConfiguration.GetAllClassNameArrayOfConfiguration();
        for (Class<?> clazz : configurationTypes) {
            if (clazz.getCanonicalName().toLowerCase().contains(configurationType.toLowerCase())) {
                selectedClass = clazz;

                break;
            }
        }

        if (selectedClass == null) {
            throw new SOAPException(
                    UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG);
        }

        testingSessionId = Integer.parseInt(testingSessionIdString);
        TestingSession selectedTestingSession = TestingSession.GetSessionById(testingSessionId);
        System sys = System.getSystemByAllKeyword(systemKeyword);

        List<AbstractConfiguration> lac = AbstractConfiguration.getConfigurationsFiltered(selectedClass, null, sys,
                null, selectedTestingSession, null, null, null, null, null, null);

        if (lac == null) {
            return null;
        }

        List exportables = lac;
        return CSVExporter.exportCSV(exportables);

    }

    @WebMethod
    @WebResult(name = RESULT)
    public String listAllConfigurationsForAnActorAndTypeAsCSV(@WebParam(name = "actorKeyword") @XmlElement(required= true) String actorKeyword,
                                                              @WebParam(name = CONFIGURATION_TYPE) @XmlElement(required= true) String configurationType,
                                                              @WebParam(name = TESTING_SESSION_ID) @XmlElement(required = true) String testingSessionIdString) throws
            SOAPException {

        Integer testingSessionId;

        if ((null == configurationType) || (configurationType.isEmpty())) {
            throw new SOAPException("configurationType is a required argument : DicomSCU, DicomSCP, HL7V2Initiator, HL7V2Responder,HL7V3Initiator, " +
                    "HL7V3Responder, Webservice,  Syslog, Raw");
        }
        if ((null == actorKeyword) || (actorKeyword.isEmpty())) {
            throw new SOAPException("actorKeyword is a required argument");
        }
        if ((null == testingSessionIdString) || (testingSessionIdString.isEmpty())) {
            throw new SOAPException(TESTING_SESSION_ID_IS_A_REQUIRED_ARGUMENT);
        }

        try {
            testingSessionId = Integer.parseInt(testingSessionIdString);

            Class<?> selectedClass = null;
            Class<?>[] configurationTypes = AbstractConfiguration.GetAllClassNameArrayOfConfiguration();
            for (Class<?> clazz : configurationTypes) {
                if (clazz.getCanonicalName().toLowerCase().contains(configurationType.toLowerCase())) {
                    selectedClass = clazz;

                    break;
                }
            }

            if (selectedClass == null) {
                throw new SOAPException(
                        UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG);
            }

            Actor a = Actor.findActorWithKeyword(actorKeyword);
            if (null == a) {
                throw new SOAPException("Unable to retrieve actor from the actorKeyword :" + actorKeyword);
            }
            List<AbstractConfiguration> listToUse;
            TestingSession selectedTestingSession = TestingSession.GetSessionById(testingSessionId);
            if (selectedTestingSession == null) {
                throw new SOAPException(
                        "Unable to access the requested testing session using the provided testingSessionId :"
                                + Integer.toString(testingSessionId));
            }

            listToUse = AbstractConfiguration.getConfigurationsFiltered(selectedClass, null, null, null,
                    selectedTestingSession, null, a, null, null, null);
            new StringBuffer();

            if (listToUse == null) {
                return null;
            }

            List exportables = listToUse;
            return CSVExporter.exportCSV(exportables);

        } catch (NumberFormatException e) {
            LOG.error(e.getMessage());
            throw new SOAPException(TESTING_SESSION_ID_SHALL_BE_AN_INTEGER, e.getCause());
        }
    }

    @WebMethod
    @WebResult(name = RESULT)
    public String listAllConfigurationsForATypeAsCSV(@WebParam(name = CONFIGURATION_TYPE) @XmlElement(required= true) String configurationType,
                                                     @WebParam(name = TESTING_SESSION_ID) @XmlElement(required= true) String testingSessionIdString) throws SOAPException {

        Integer testingSessionId;

        if ((null == configurationType) || (configurationType.isEmpty())) {
            throw new SOAPException(
                    UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG);
        }
        if ((null == testingSessionIdString) || (testingSessionIdString.isEmpty())) {
            throw new SOAPException(TESTING_SESSION_ID_IS_A_REQUIRED_ARGUMENT);
        }

        try {
            testingSessionId = Integer.parseInt(testingSessionIdString);

            Class<?> selectedClass = null;
            Class<?>[] configurationTypes = AbstractConfiguration.GetAllClassNameArrayOfConfiguration();
            for (Class<?> clazz : configurationTypes) {
                if (clazz.getCanonicalName().toLowerCase().contains(configurationType.toLowerCase())) {
                    selectedClass = clazz;

                    break;
                }
            }

            if (selectedClass == null) {
                throw new SOAPException(
                        UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG);
            }

            List<AbstractConfiguration> listToUse;
            TestingSession selectedTestingSession = TestingSession.GetSessionById(testingSessionId);
            if (selectedTestingSession == null) {
                throw new SOAPException(
                        "Unable to access the requested testing session using the provided testingSessionId :"
                                + Integer.toString(testingSessionId));
            }

            listToUse = AbstractConfiguration.getConfigurationsFiltered(selectedClass, null, null, null,
                    selectedTestingSession, null, null, null, null, null);
            new StringBuffer();

            if (listToUse == null) {
                return null;
            }
            List exportables = listToUse;
            return CSVExporter.exportCSV(exportables);

        } catch (NumberFormatException e) {
            LOG.error(e.getMessage());
            throw new SOAPException(TESTING_SESSION_ID_SHALL_BE_AN_INTEGER, e.getCause());
        }
    }

    @WebMethod
    @WebResult(name = RESULT)
    public String listAllConfigurationsForAnActorAnInstitutionAndATypeAsCSV(
            @WebParam(name = "actorKeyword") @XmlElement(required= true) String actorKeyword,
            @WebParam(name = "institutionKeyword") @XmlElement(required= true) String institutionKeyword,
            @WebParam(name = CONFIGURATION_TYPE) @XmlElement(required= true) String configurationType,
            @WebParam(name = TESTING_SESSION_ID) @XmlElement(required= true) String testingSessionIdString) throws SOAPException {

        Integer testingSessionId;


        if ((null == actorKeyword) || (actorKeyword.isEmpty())) {
            throw new SOAPException("actorKeyword is a required argument");
        }
        if ((null == institutionKeyword) || (institutionKeyword.isEmpty())) {
            throw new SOAPException("institutionKeyword is a required argument");
        }
        if ((null == configurationType) || (configurationType.isEmpty())) {
            throw new SOAPException(
                    UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG);
        }
        if ((null == testingSessionIdString) || (testingSessionIdString.isEmpty())) {
            throw new SOAPException(TESTING_SESSION_ID_IS_A_REQUIRED_ARGUMENT);
        }

        try {
            testingSessionId = Integer.parseInt(testingSessionIdString);

            Class<?> selectedClass = null;
            Class<?>[] configurationTypes = AbstractConfiguration.GetAllClassNameArrayOfConfiguration();
            for (Class<?> clazz : configurationTypes) {
                if (clazz.getCanonicalName().toLowerCase().contains(configurationType.toLowerCase())) {
                    selectedClass = clazz;

                    break;
                }
            }

            if (selectedClass == null) {
                throw new SOAPException(
                        UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG);
            }
            Actor a = Actor.findActorWithKeyword(actorKeyword);
            if (a == null) {
                throw new SOAPException("Unable to retrieve actor from the actorKeyword :" + actorKeyword);
            }

            Institution institution = Institution.findInstitutionWithKeyword(institutionKeyword);
            if (institution == null) {
                throw new SOAPException("Unable to retrieve institution from the institutionKeyword :"
                        + institutionKeyword);
            }

            List<AbstractConfiguration> listToUse = new ArrayList<AbstractConfiguration>();
            TestingSession selectedTestingSession = TestingSession.GetSessionById(testingSessionId);
            if (selectedTestingSession == null) {
                throw new SOAPException("Selected testing session is null");
            }

            listToUse = AbstractConfiguration.getConfigurationsFiltered(selectedClass, null, null, institution,
                    selectedTestingSession, null, a, null, null, null);
            new StringBuffer();

            if (listToUse == null) {
                return null;
            }

            List exportables = listToUse;
            return CSVExporter.exportCSV(exportables);

        } catch (NumberFormatException e) {
            LOG.error(e.getMessage());
            throw new SOAPException(TESTING_SESSION_ID_SHALL_BE_AN_INTEGER, e.getCause());
        }
    }

    @WebMethod
    @WebResult(name = RESULT)
    public String listAllConfigurationsForAnInstitutionAndATypeAsCSV(
            @WebParam(name = "institutionKeyword") @XmlElement(required= true) String institutionKeyword,
            @WebParam(name = CONFIGURATION_TYPE) @XmlElement(required= true) String configurationType,
            @WebParam(name = TESTING_SESSION_ID) @XmlElement(required= true) String testingSessionIdString) throws SOAPException {

        Integer testingSessionId;

        if ((null == institutionKeyword) || (institutionKeyword.isEmpty())) {
            throw new SOAPException("institutionKeyword is a required argument");
        }
        if ((null == configurationType) || (configurationType.isEmpty())) {
            throw new SOAPException(
                    UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG);
        }
        if ((null == testingSessionIdString) || (testingSessionIdString.isEmpty())) {
            throw new SOAPException(TESTING_SESSION_ID_IS_A_REQUIRED_ARGUMENT);
        }

        try {
            testingSessionId = Integer.parseInt(testingSessionIdString);
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage());
            throw new SOAPException(TESTING_SESSION_ID_SHALL_BE_AN_INTEGER, e.getCause());
        }

            Class<?> selectedClass = null;
            Class<?>[] configurationTypes = AbstractConfiguration.GetAllClassNameArrayOfConfiguration();
            for (Class<?> clazz : configurationTypes) {
                if (clazz.getCanonicalName().toLowerCase().contains(configurationType.toLowerCase())) {
                    selectedClass = clazz;

                    break;
                }
            }

            if (selectedClass == null) {
                throw new SOAPException(
                        UNKNOWN_CONFIGURATION_TYPE_VALID_CONFIGURATION_TYPES_ARE_DICOM_SCU_DICOM_SCP_HL7V2_INITIATOR_HL7V2_RESPONDER_HL7V3_INITIATOR_HL7V3_RESPONDER_WEBSERVICE_SYSLOG);
            }

            Institution institution = Institution.findInstitutionWithKeyword(institutionKeyword);
            if (institution == null) {
                throw new SOAPException("Unable to retrieve institution from the institutionKeyword :"
                        + institutionKeyword);
            }
            List<AbstractConfiguration> listToUse;

            TestingSession selectedTestingSession = TestingSession.GetSessionById(testingSessionId);
            if (selectedTestingSession == null) {
                throw new SOAPException("Selected testing session is null");
            }

            listToUse = AbstractConfiguration.getConfigurationsFiltered(selectedClass, null, null, institution,
                    selectedTestingSession, null, null, null, null, null);

            new StringBuffer();

            if (listToUse == null) {
                return null;
            }

            List exportables = listToUse;
            return CSVExporter.exportCSV(exportables);

    }

    @WebMethod
    public String getOIDBySystemByRequirement(@WebParam(name = "systemKeyword") String systemKeyword,
                                              @WebParam(name = TESTING_SESSION_ID) @XmlElement(required= true) String testingSessionId,
                                              @WebParam(name = "oidRequirementLabel") String oidRequirementLabel) throws SOAPException {

        Integer testingId;

        if ((null == testingSessionId) || (testingSessionId.isEmpty())) {
            throw new SOAPException("You have to specify a testingSessionId.");
        }

        try {
            testingId = Integer.parseInt(testingSessionId);

        if((null != systemKeyword) && (!systemKeyword.isEmpty())){
                System getValueSys = System.getSystemByAllKeyword(systemKeyword);
                if(null == getValueSys) {
                    throw new SOAPException("The systemKeyword is invalid");
                }
        }

        System sys = System.getSystemByAllKeyword(systemKeyword);
        TestingSession ts = TestingSession.GetSessionById(Integer.valueOf(testingSessionId));
        EntityManager em = EntityManagerService.provideEntityManager();
        List<SystemInSession> lsis = SystemInSession.getSystemInSessionFiltered(em, sys, ts, null, null, null, null,
                null, null, null, null, null, null);
        if (lsis == null) {
            return null;
        }
        if (lsis.size() == 0) {
            throw new SOAPException("The oidRequirementLabel is invalid");
        }
        SystemInSession sis = lsis.get(0);
        List<OIDSystemAssignment> losa = OIDSystemAssignment.getOIDSystemAssignmentFiltered(sis, oidRequirementLabel,
                ts, null);

        if ((losa != null) && (losa.size() > 0)) {
                return losa.get(0).getOid();
            }
        return null;

        } catch (NumberFormatException e) {
            LOG.error(e.getMessage());
            throw new SOAPException("The testingSessionId is invalid.", e.getCause());
        }
    }

    @WebMethod
    public String getListOIDBySystemByRequirementAsCSV(@WebParam(name = "systemKeyword") String systemKeyword,
                                                       @WebParam(name = TESTING_SESSION_ID) @XmlElement(required= true) String testingSessionId,
                                                       @WebParam(name = "oidRequirementLabel") @XmlElement(required= true) String oidRequirementLabel) throws SOAPException {
        Integer testingId;

        if ((oidRequirementLabel != null) && (oidRequirementLabel.equals("")) ){
                oidRequirementLabel = null;
        }
        if ((testingSessionId != null) && (testingSessionId.equals("")) ){
                testingSessionId = null;
        }


        if (null == testingSessionId) {
            throw new SOAPException("You have to specify a testingSessionId.");
        }

        if ((null == systemKeyword) && (null == oidRequirementLabel)){
                throw new SOAPException("You have to specify the systemKeyword or the oidRequirementLabel.");
        }

        if (null == oidRequirementLabel) {
            throw new SOAPException("You have to specify the oidRequirementLabel.");
        }

        try {
            testingId = Integer.parseInt(testingSessionId);

        } catch (NumberFormatException e) {
            LOG.error(e.getMessage());
            throw new SOAPException(TESTING_SESSION_ID_SHALL_BE_AN_INTEGER, e.getCause());
        }

        if ((systemKeyword != null) && (!systemKeyword.equals(""))) {
                System getValueSys = System.getSystemByAllKeyword(systemKeyword);
                if(null == getValueSys) {
                    throw new SOAPException("The systemKeyword is invalid");
                }
                else if (systemKeyword.equals("")) {
                    systemKeyword = null;
            }
        }

        EntityManager em = EntityManagerService.provideEntityManager();
        TestingSession ts = TestingSession.GetSessionById(Integer.valueOf(testingSessionId));
        SystemInSession sis = null;
        if (systemKeyword != null) {
            System sys = System.getSystemByAllKeyword(systemKeyword);
            List<SystemInSession> lsis = SystemInSession.getSystemInSessionFiltered(em, sys, ts, null, null, null,
                    null, null, null, null, null, null, null);
            if (lsis == null) {
                lsis = new ArrayList<SystemInSession>();
            }
            if ((lsis.size() == 0) && (null == oidRequirementLabel) ){
                    throw new SOAPException(
                            "You have to specify the systemKeyword or the oidRequirementLabel. The specified systemKeyword is not well.");
            }
            sis = lsis.get(0);
        }

        if (null != oidRequirementLabel) {
            List<OIDRequirement> lor = OIDRequirement.getOIDRequirementFiltered(null, oidRequirementLabel);
            if (lor == null) {
                throw new SOAPException("The specified oidRequirementLabel is not well.");
            }
            if (lor.size() == 0) {
                throw new SOAPException("The specified oidRequirementLabel is not well.");
            }
        }

        List<OIDSystemAssignment> losa = OIDSystemAssignment.getOIDSystemAssignmentFiltered(sis, oidRequirementLabel,
                ts, null);

        List exportables = losa;
        return CSVExporter.exportCSV(exportables);
    }

}
