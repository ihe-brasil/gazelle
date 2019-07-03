/**
 *
 */
package net.ihe.gazelle.tm.configurations.data;

import net.ihe.gazelle.common.utils.HibernateConfiguration;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Hl7MessageProfile;
import net.ihe.gazelle.tf.model.Transaction;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.tee.model.MessageDirection;
import net.ihe.gazelle.tm.tee.model.MessageValidationService;
import net.ihe.gazelle.tm.tee.model.TmTestStepMessageProfile;
import net.ihe.gazelle.tm.tee.model.ValidationService;
import net.ihe.gazelle.tm.tee.parser.HL7Message;
import net.ihe.gazelle.tm.tee.parser.HL7MessageParser;
import net.ihe.gazelle.tm.tee.parser.HL7MsgParserFactory;
import net.ihe.gazelle.tm.tee.util.CollectionUtil;
import net.ihe.gazelle.tm.tee.util.FileUtil;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * This class is responsible for loading migrating Preconnectathon data into Interop Testbed.
 *
 * @author tnabeel
 */
public class PreconnectathonDataMigrator {

    private static final Logger LOG = LoggerFactory.getLogger(PreconnectathonDataMigrator.class);
    private static final String PRECONNECTATHON_SELECT_STATEMENT = "select tc.id_test, te.id_event, te.event_order, tc.title_test, tc" +
            ".description_test, tc.steps_test, tc.precondition, tc.version, "
            + "ta.name_actor as test_actor_name, ta.value_actor as test_actor_value, tea.name_actor as event_actor_name, tea.value_actor as " +
            "event_actor_value, "
            + "te.description_event from testcase tc "
            + "left join testevent te on te.test_id=tc.id_test "
            + "left join actor tea on tea.id_actor=te.actor_id "
            + "join actor ta on ta.id_actor=tc.main_actor order by tc.id_test, te.event_order;";
    private static final File PC_DATA_DIR = new File("../../data/preconnectathon/testcases/");
    // Stores PreConnectathon Tool Actor to Gazelle Actor mapping
    // Needed for finding tf_hl7_message_profile records
    private static Map<String, String> actorMapping = new HashMap<String, String>();

    static {
        initiatlizeActorMappings();
    }

    private String DB_PSSWD = "NHIE-Gateway";
    private String DB_LOGIN = "root";
    private EntityManager itbEntityManager;
    private Connection preConnectathonConn;
    private Set<ProfileAttributes> missingProfiles = new TreeSet<ProfileAttributes>();
    private PrintWriter logWriter;
    private int totalSteps;
    private int totalResolvedSteps;
    private int totalMsgProfiles;
    private int totalResolvedMsgProfiles;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            PreconnectathonDataMigrator loader = new PreconnectathonDataMigrator();
            loader.load();
            System.out.println("\nPreconnectathon data migration completed");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("\nPreconnectathon data migration failed.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void initiatlizeActorMappings() {
        actorMapping.put("PixConsumerResponder", "PAT_IDENTITY_X_REF_MGR"); //?
        actorMapping.put("PixConsumer", "PAT_IDENTITY_CONSUMER");
        actorMapping.put("PixManager", "PAT_IDENTITY_X_REF_MGR");
        actorMapping.put("PixSource", "PAT_IDENTITY_SRC");
        actorMapping.put("PdqConsumer", "PDC");
        actorMapping.put("PdqSupplier", "PDS");
    }

    /**
     * This method carries out all the migration from Preconnectathon DB to Gazelle DB
     */
    private void load() {
        if (!PC_DATA_DIR.exists()) {
            throw new IllegalStateException(PC_DATA_DIR.getAbsolutePath() + " does not exist");
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            createConnections();
            stmt = getPreConnectathonConn().prepareStatement(PRECONNECTATHON_SELECT_STATEMENT);
            rs = stmt.executeQuery();
            int previousTestId = -1;
            getItbEntityManager().getTransaction().begin();
            Map<String, String> requestResponseMsgTypeMap = new HashMap<String, String>();
            logWriter = new PrintWriter(new File("messages.LOG"));
            Map<String, TestRoles> testRoleMap = new HashMap<String, TestRoles>();
            printMessage("Using '" + PC_DATA_DIR.getAbsolutePath() + "' for import.");
            Test test = null;
            while (rs.next()) {
                TestStepRecord stepRecord = initializeTestStepRecord(rs);
                logWriter.flush();
                Thread.sleep(20);
                if (!"v2".equals(stepRecord.getTestVersion())) {
                    continue;
                }
                totalSteps++;
                if (previousTestId != stepRecord.getTestId()) {
                    testRoleMap.clear();
                    printMessage("\nStarting import of test '" + stepRecord.getTestName() + "'.............");
                    test = importTest(stepRecord);
                }
                StepMsgProfileInfo stepMsgProfileInfo = new StepMsgProfileInfo();

                loadExampleMessage(stepMsgProfileInfo, stepRecord, MessageDirection.REQUEST);
                loadExampleMessage(stepMsgProfileInfo, stepRecord, MessageDirection.RESPONSE);

                if (stepMsgProfileInfo.getRequestMsgType() == null) {
                    printImportFailureForRequest(stepMsgProfileInfo.getRequestMsgFile(), stepRecord);
                    previousTestId = stepRecord.getTestId();
                    continue;
                }

                if (stepMsgProfileInfo.getResponseMsgType() == null) {
                    stepMsgProfileInfo.setResponseMsgType(requestResponseMsgTypeMap.get(stepMsgProfileInfo
                            .getRequestMsgType()));
                    if (stepMsgProfileInfo.getResponseMsgType() == null) {
                        printImportFailureForResponse(stepMsgProfileInfo.getResponseMsgFile(), stepRecord,
                                stepMsgProfileInfo.getRequestMsgType());
                        previousTestId = stepRecord.getTestId();
                        continue;
                    }
                }
                requestResponseMsgTypeMap.put(stepMsgProfileInfo.getRequestMsgType(),
                        stepMsgProfileInfo.getResponseMsgType());
                resolveTransactionProfileInfo(stepMsgProfileInfo, stepRecord);
                TestRoles initiatorTestRole = createInitiatorTestRole(test, testRoleMap, stepMsgProfileInfo);
                TestRoles responderTestRole = createResponderTestRole(test, testRoleMap, stepMsgProfileInfo);

                // Create the test step
                TestSteps testStep = createTestStep(test, stepRecord, stepMsgProfileInfo, initiatorTestRole,
                        responderTestRole);

                // Create the test step message profiles
                loadValidationContextFile(stepMsgProfileInfo, stepRecord);
                createStepMsgProfiles(testStep, stepMsgProfileInfo);

                printImportStepSuccess(stepRecord, stepMsgProfileInfo);
                previousTestId = stepRecord.getTestId();
            }
            getItbEntityManager().getTransaction().commit();
            printMissingData();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt);
            closeConnections();
            if (logWriter != null) {
                logWriter.close();
            }
        }
    }

    private void loadValidationContextFile(StepMsgProfileInfo stepMsgProfileInfo, TestStepRecord stepRecord) {
        stepMsgProfileInfo.setValidationContextFile(getValidationContextFile(stepRecord));
        printMessage("Validation context file=" + stepMsgProfileInfo.getValidationContextFile().getAbsolutePath());
        if (stepMsgProfileInfo.getValidationContextFile().exists()) {
            stepMsgProfileInfo.setValidationContextFileContents(FileUtil.loadFileContents(stepMsgProfileInfo
                    .getValidationContextFile().getAbsolutePath()));
        }
    }

    private void loadExampleMessage(StepMsgProfileInfo stepMsgProfileInfo, TestStepRecord stepRecord,
                                    MessageDirection messageDirection) {
        switch (messageDirection) {
            case REQUEST:
                stepMsgProfileInfo.setRequestMsgFile(getMessageFile(stepRecord, messageDirection));
                if (stepMsgProfileInfo.getRequestMsgFile().exists()) {
                    stepMsgProfileInfo.setRequestMsgExampleContents(FileUtil.loadFileContents(stepMsgProfileInfo
                            .getRequestMsgFile().getAbsolutePath()));
                    stepMsgProfileInfo.setRequestMsgType(extractMessageType(stepMsgProfileInfo
                            .getRequestMsgExampleContents()));
                }
                break;
            case RESPONSE:
                stepMsgProfileInfo.setResponseMsgFile(getMessageFile(stepRecord, messageDirection));
                if (stepMsgProfileInfo.getResponseMsgFile().exists()) {
                    stepMsgProfileInfo.setResponseMsgExampleContents(FileUtil.loadFileContents(stepMsgProfileInfo
                            .getResponseMsgFile().getAbsolutePath()));
                    stepMsgProfileInfo.setResponseMsgType(extractMessageType(stepMsgProfileInfo
                            .getResponseMsgExampleContents()));
                }
                break;
        }
    }

    /**
     * This method is responsible for resolving requestMsgProfile, responseMsgProfile, and transactionKeyword
     *
     * @param stepRecord
     * @return
     */
    private void resolveTransactionProfileInfo(StepMsgProfileInfo stepMsgProfileInfo, TestStepRecord stepRecord) {
        String actor = getGazelleActorKeyword(stepRecord.getStepActorValue());
        String requestMsgType = stepMsgProfileInfo.getRequestMsgType();
        String responseMsgType = stepMsgProfileInfo.getResponseMsgType();
        Hl7MessageProfile requestMsgProfile = findMsgProfileByMsgTypeAndActor(requestMsgType, actor);
        Hl7MessageProfile responseMsgProfile = null;
        String transactionKeyword = null;
        if (requestMsgProfile != null) {
            transactionKeyword = getTransactionKeywordFromProfile(requestMsgProfile);
            responseMsgProfile = findMsgProfileByMsgTypeAndTransaction(responseMsgType, transactionKeyword);
        } else {
            responseMsgProfile = findMsgProfileByMsgTypeAndActor(responseMsgType, actor);
            if (responseMsgProfile != null) {
                transactionKeyword = getTransactionKeywordFromProfile(responseMsgProfile);
            }
            if (transactionKeyword != null) {
                requestMsgProfile = findMsgProfileByMsgTypeAndTransaction(requestMsgType, transactionKeyword);
            }
        }
        if (requestMsgProfile == null) {
            requestMsgProfile = findFirstMsgProfileForMsgType(requestMsgType);
        }
        if (responseMsgProfile == null) {
            responseMsgProfile = findFirstMsgProfileForMsgType(responseMsgType);
        }
        if ((requestMsgProfile != null) && (transactionKeyword == null)) {
            transactionKeyword = getTransactionKeywordFromProfile(requestMsgProfile);
        }
        if ((responseMsgProfile != null) && (transactionKeyword == null)) {
            transactionKeyword = getTransactionKeywordFromProfile(responseMsgProfile);
            if (transactionKeyword != null) {
                if (requestMsgProfile == null) {
                    requestMsgProfile = findMsgProfileByMsgTypeAndTransaction(requestMsgType, transactionKeyword);
                }
            }
        }
        if (transactionKeyword == null) {
            throw new IllegalStateException("Could not determine transaction for test '" + stepRecord.getTestName()
                    + "' requestMsgType='" + requestMsgType + "' responseMsgType='" + responseMsgType + "' actor='"
                    + actor + "'");
        }
        if (requestMsgProfile == null) {
            missingProfiles.add(new ProfileAttributes(requestMsgType, transactionKeyword));
        }
        if (responseMsgProfile == null) {
            if (responseMsgType.startsWith("ACK")) {
                // Some like ACK_31 and ACK_28 share common profile
                responseMsgProfile = findMsgProfileByMsgTypeAndTransaction("ACK", transactionKeyword);
            }
            if (responseMsgProfile == null) {
                printError("missing profile for test '" + stepRecord.getTestName() + "' requestMsgType='"
                        + requestMsgType + "' responseMsgType='" + responseMsgType + "' actor='" + actor
                        + "' transactionKeyword='" + transactionKeyword + "'");
                missingProfiles.add(new ProfileAttributes(responseMsgType, transactionKeyword));
            }
        }
        stepMsgProfileInfo.setTransactionKeyword(transactionKeyword);
        stepMsgProfileInfo.setRequestMsgProfile(requestMsgProfile);
        stepMsgProfileInfo.setResponseMsgProfile(responseMsgProfile);
        stepMsgProfileInfo.setRequestMsgType(requestMsgType);
        stepMsgProfileInfo.setResponseMsgType(responseMsgType);
    }

    /**
     * Extracts transaction keyword from the hl7MsgProfile record
     *
     * @param hl7MsgProfile
     * @return
     */
    private String getTransactionKeywordFromProfile(Hl7MessageProfile hl7MsgProfile) {
        if ((hl7MsgProfile.getTransaction() == null) || (hl7MsgProfile.getTransaction().getKeyword() == null)) {
            throw new IllegalStateException("No transaction in DB for hl7MsgProfile " + hl7MsgProfile.getId());
        }
        return hl7MsgProfile.getTransaction().getKeyword();
    }

    /**
     * Creates the request and response tm_test_step_message_profile records
     *
     * @param tmTestStep
     * @param stepMsgProfileInfo
     */
    private void createStepMsgProfiles(TestSteps tmTestStep, StepMsgProfileInfo stepMsgProfileInfo) {
//         When Response-x.txt is present in the data dump the validaiton context files pertain to response message
//         otherwise they pertain to the request message
        if (stepMsgProfileInfo.getResponseMsgFile().exists()) {
            createStepMsgProfile(tmTestStep, stepMsgProfileInfo.getRequestMsgProfile(), MessageDirection.REQUEST,
                    stepMsgProfileInfo.getRequestMsgFile(), stepMsgProfileInfo.getRequestMsgExampleContents(), null,
                    null);

            createStepMsgProfile(tmTestStep, stepMsgProfileInfo.getResponseMsgProfile(), MessageDirection.RESPONSE,
                    stepMsgProfileInfo.getResponseMsgFile(), stepMsgProfileInfo.getResponseMsgExampleContents(),
                    stepMsgProfileInfo.getValidationContextFile(),
                    stepMsgProfileInfo.getValidationContextFileContents());

        } else {
            createStepMsgProfile(tmTestStep, stepMsgProfileInfo.getRequestMsgProfile(), MessageDirection.REQUEST,
                    stepMsgProfileInfo.getRequestMsgFile(), stepMsgProfileInfo.getRequestMsgExampleContents(),
                    stepMsgProfileInfo.getValidationContextFile(),
                    stepMsgProfileInfo.getValidationContextFileContents());
            createStepMsgProfile(tmTestStep, stepMsgProfileInfo.getResponseMsgProfile(), MessageDirection.RESPONSE,
                    stepMsgProfileInfo.getResponseMsgFile(), stepMsgProfileInfo.getResponseMsgExampleContents(), null,
                    null);
        }
    }

    /**
     * Creates tm_test_step_message_profile record based on the provided tmTestStep, tfHl7MessageProfile, and direction
     *
     * @param tmTestStep
     * @param tfHl7MessageProfile
     * @param direction
     */
    private void createStepMsgProfile(TestSteps tmTestStep, Hl7MessageProfile tfHl7MessageProfile,
                                      MessageDirection direction, File exampleFile, String exampleFileContents, File validationContextFile,
                                      String validationContextFileContents) {
        totalMsgProfiles++;
        if (tfHl7MessageProfile != null) {
            totalResolvedMsgProfiles++;
        }
        TmTestStepMessageProfile stepMsgProfile = new TmTestStepMessageProfile();
        stepMsgProfile.setAutoValidate((byte) 1);
        stepMsgProfile.setDirection(direction);
        stepMsgProfile.setTfHl7MessageProfile(tfHl7MessageProfile);
        stepMsgProfile.setTmTestStep(tmTestStep);
        stepMsgProfile.setExampleMsgFileName((exampleFile != null) && exampleFile.exists() ? exampleFile.getName()
                : null);
        stepMsgProfile.setExampleMsgContent(exampleFileContents);
        Validate.notNull(tmTestStep);
        Validate.notNull(tmTestStep.getTestParent());
        stepMsgProfile
                .setValidationContextFileName((validationContextFile != null) && validationContextFile.exists() ? validationContextFile
                        .getName() : null);
        stepMsgProfile.setValidationContextContent(validationContextFileContents);
        getItbEntityManager().persist(stepMsgProfile);

        MessageValidationService msgValidationService = new MessageValidationService();
        msgValidationService.setTmTestStepMessageProfile(stepMsgProfile);
        msgValidationService.setValidationService(findDefaultValidationService());
        getItbEntityManager().persist(msgValidationService);
    }

    /**
     * Creates a tm_test_steps record in the gazelle database based on the provided test, stepRecord, stepMsgProfileInfo,
     * initiatorTestRole, and responderTestRole
     *
     * @param test
     * @param stepRecord
     * @param stepMsgProfileInfo
     * @param initiatorTestRole
     * @param responderTestRole
     * @return
     */
    private TestSteps createTestStep(Test test, TestStepRecord stepRecord, StepMsgProfileInfo stepMsgProfileInfo,
                                     TestRoles initiatorTestRole, TestRoles responderTestRole) {

        if (stepMsgProfileInfo.getTransactionKeyword() == null) {
            return null;
        }
        TestSteps testStep = new TestSteps();
        testStep.setAutoComplete(true);
        testStep.setAutoTriggered(true);
        testStep.setDescription(stepRecord.getStepDescription());
        testStep.setExpectedMessageCount(2);
        testStep.setHl7Version((stepMsgProfileInfo.getRequestMsgProfile() != null ? stepMsgProfileInfo
                .getRequestMsgProfile().getHl7Version() : null));
        testStep.setLastChanged(new Date());
        testStep.setLastModifierId("rettema");
        testStep.setMessageType(stepMsgProfileInfo.getRequestMsgType());
        testStep.setResponderMessageType(stepMsgProfileInfo.getResponseMsgType());
        testStep.setSecured(false);
        testStep.setStepIndex(stepRecord.getStepIndex() * 10);
        testStep.setTestParent(test);
        Validate.notNull(initiatorTestRole);
        Validate.notNull(responderTestRole);
        testStep.setTestRolesInitiator(initiatorTestRole);
        testStep.setTestRolesResponder(responderTestRole);
        testStep.setTestStepsOption(findDefaultTestStepsOption());
        testStep.setTransaction(findTransaction(stepMsgProfileInfo.getTransactionKeyword()));

        getItbEntityManager().persist(testStep);
        return testStep;

    }

    /**
     * Retrieves the Hl7MessageProfile instance representing the tf_hl7_message_profile record in the gazelle database
     * for the given message type and actor value in preconnectathon database.
     *
     * @param msgType
     * @param gazelleActorKeyword
     * @return
     */
    private Hl7MessageProfile findMsgProfileByMsgTypeAndActor(String msgType, String gazelleActorKeyword) {
        Query query = null;
        if (gazelleActorKeyword != null) {
            query = getItbEntityManager()
                    .createQuery(
                            "select distinct msgProfile from Hl7MessageProfile msgProfile join fetch msgProfile.actor actor join fetch msgProfile" +
                                    ".transaction trans join fetch msgProfile.domain domain where msgProfile.messageType=:messageType and actor" +
                                    ".keyword=:actorKeyword and domain.keyword=:domainKeyword");
            query.setParameter("actorKeyword", gazelleActorKeyword);
        } else {
            query = getItbEntityManager()
                    .createQuery(
                            "select distinct msgProfile from Hl7MessageProfile msgProfile join fetch msgProfile.actor actor join fetch msgProfile" +
                                    ".transaction trans join fetch msgProfile.domain domain where msgProfile.messageType=:messageType and domain" +
                                    ".keyword=:domainKeyword");
        }
        query.setParameter("messageType", msgType);
        query.setParameter("domainKeyword", "ITI");
        @SuppressWarnings("unchecked")
        List<Hl7MessageProfile> list = query.getResultList();
        Hl7MessageProfile profile = CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
        return profile;
    }

    private String getGazelleActorKeyword(String preConnectathonActorValue) {
        String gazelleActorKeyword = actorMapping.get(preConnectathonActorValue);
        if (gazelleActorKeyword == null) {
            printMessage("Could not find actor '" + preConnectathonActorValue + "' in actorMappings.");
        }
        return gazelleActorKeyword;
    }

    /**
     * Retrieves the Hl7MessageProfile instance representing the tf_hl7_message_profile record in the gazelle database
     * for the given actor value in preconnectathon database.
     *
     * @param preConnectathonActorValue
     * @return
     */
    private Hl7MessageProfile findMsgProfileByActor(String preConnectathonActorValue) {
        String gazelleActorKeyword = actorMapping.get(preConnectathonActorValue);
        if (gazelleActorKeyword == null) {
            printMessage("Could not find actor '" + preConnectathonActorValue + "' in actorMappings.");
        }
        Query query = getItbEntityManager()
                .createQuery(
                        "select distinct msgProfile from Hl7MessageProfile msgProfile join fetch msgProfile.actor actor join fetch msgProfile" +
                                ".transaction trans join fetch msgProfile.domain domain where actor.keyword=:actorKeyword and domain" +
                                ".keyword=:domainKeyword");
        query.setParameter("actorKeyword", gazelleActorKeyword);
        query.setParameter("domainKeyword", "ITI");
        @SuppressWarnings("unchecked")
        List<Hl7MessageProfile> list = query.getResultList();
        Hl7MessageProfile profile = CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
        return profile;
    }

    /**
     * Retrieves the Hl7MessageProfile instance representing the tf_hl7_message_profile record in the gazelle database
     * for the given message type and transaction
     *
     * @param msgType
     * @param transKeyword
     * @return
     */
    private Hl7MessageProfile findMsgProfileByMsgTypeAndTransaction(String msgType, String transKeyword) {
        Query query = getItbEntityManager()
                .createQuery(
                        "select distinct msgProfile from Hl7MessageProfile msgProfile join fetch msgProfile.actor actor join fetch msgProfile" +
                                ".transaction trans join fetch msgProfile.domain domain where msgProfile.messageType=:messageType and trans" +
                                ".keyword=:transKeyword and domain.keyword=:domainKeyword");
        query.setParameter("messageType", msgType);
        query.setParameter("transKeyword", transKeyword);
        query.setParameter("domainKeyword", "ITI");
        @SuppressWarnings("unchecked")
        List<Hl7MessageProfile> list = query.getResultList();
        Hl7MessageProfile profile = CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
        return profile;
    }

    /**
     * Retrieves the Hl7MessageProfile instance representing the first tf_hl7_message_profile record in the gazelle database
     * for the given message type
     *
     * @param msgType
     * @return
     */
    private Hl7MessageProfile findFirstMsgProfileForMsgType(String msgType) {
        Query query = getItbEntityManager()
                .createQuery(
                        "select distinct msgProfile from Hl7MessageProfile msgProfile join fetch msgProfile.actor actor join fetch msgProfile" +
                                ".transaction trans join fetch msgProfile.domain domain where msgProfile.messageType=:messageType and domain" +
                                ".keyword=:domainKeyword");
        query.setParameter("messageType", msgType);
        query.setParameter("domainKeyword", "ITI");
        @SuppressWarnings("unchecked")
        List<Hl7MessageProfile> list = query.getResultList();
        Hl7MessageProfile profile = CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
        return profile;
    }

    /**
     * Extracts the message type from the example message contents.
     *
     * @param messageContents
     * @return
     */
    private String extractMessageType(String messageContents) {
        HL7MessageParser hl7MsgParser = HL7MsgParserFactory.getInstance().createHL7MsgParser(messageContents);
        HL7Message message = hl7MsgParser.parseMessage();
        return message.getMessageType();
    }

    /**
     * Creates a File instance representing the example message file for the given stepRecord loaded from preconnectathon database.
     *
     * @param stepRecord
     * @param request
     * @return
     */
    private File getMessageFile(TestStepRecord stepRecord, MessageDirection messageDirection) {
        File testDir = getTestCasesDirectory(stepRecord);
        File messageFile = null;
        switch (messageDirection) {
            case REQUEST:
                messageFile = new File(testDir.getAbsolutePath() + "/TestMessage-" + stepRecord.getStepIndex() + ".txt");
                break;
            case RESPONSE:
                messageFile = new File(testDir.getAbsolutePath() + "/Response-" + stepRecord.getStepIndex() + ".txt");
                break;
        }
        return messageFile;
    }

    private File getValidationContextFile(TestStepRecord stepRecord) {
        File testDir = getTestCasesDirectory(stepRecord);
        return new File(testDir.getAbsolutePath() + "/MessageValidationContext-" + stepRecord.getStepIndex() + ".xml");
    }

    private File getTestCasesDirectory(TestStepRecord stepRecord) {
        File testDir = new File(PC_DATA_DIR.getAbsolutePath() + "/v2/" + stepRecord.getTestName());
        if (!testDir.exists()) {
            throw new IllegalStateException(testDir.getAbsolutePath() + " does not exist");
        }
        return testDir;
    }

    /**
     * Extracts the transaction keyword from the given test name.
     *
     * @param testName
     * @return
     */
    private String extractTransactionKeyword(String testName) {
        String[] tokens = testName.split("-");
        return tokens[0] + "-" + tokens[1];
    }

    /**
     * Creates a tm_test record in the gazelle database off of the given stepRecord that was loaded
     * from preconnectathon database.
     *
     * @param stepRecord
     * @return
     */
    private Test importTest(TestStepRecord stepRecord) {

        Test test = new Test();
        test.setAutoComplete(true);
        test.setKeyword(stepRecord.getTestName());
        test.setLastChanged(new Date()); // today
        test.setLastModifierId("rettema"); // rettema
        test.setName(stepRecord.getTestName());
        test.setOrchestrable(false);
        test.setShortDescription(stepRecord.getTestName());
        test.setTestPeerType(findDefaultTestPeerType()); //"P2P_TEST"
        test.setTestStatus(findDefaultTestStatus()); //ready
        test.setTestType(findDefaultTestType());
        test.setValidated(true);
        test.setVersion("NA2011"); //"NA2011"
        getItbEntityManager().persist(test);

        // Create test description using stepRecord.getTestDescription(), testStepsDescription, and testPrecondition
        createTestDescription(test, stepRecord.getTestDescription(), stepRecord.getTestPrecondition(),
                stepRecord.getTestStepsDescription());

        return test;
    }

    /**
     * Creates an initiator tm_test_roles record representing the role of that the test step is playing in the test.
     *
     * @param test
     * @param testRoleMap
     * @param stepMsgProfileInfo
     * @return
     */
    private TestRoles createInitiatorTestRole(Test test, Map<String, TestRoles> testRoleMap,
                                              StepMsgProfileInfo stepMsgProfileInfo) {
        return createTestRole(test, testRoleMap, stepMsgProfileInfo, "Initiator");
    }

    /**
     * Creates a responder tm_test_roles record representing the role of that the test step is playing in the test.
     *
     * @param test
     * @param testRoleMap
     * @param stepMsgProfileInfo
     * @return
     */
    private TestRoles createResponderTestRole(Test test, Map<String, TestRoles> testRoleMap,
                                              StepMsgProfileInfo stepMsgProfileInfo) {
        return createTestRole(test, testRoleMap, stepMsgProfileInfo, "Responder");
    }

    /**
     * Creates a tm_test_roles record representing the role of that the test step is playing in the test.
     *
     * @param test
     * @param testRoleMap
     * @param stepMsgProfileInfo
     * @param type
     * @return
     */
    private TestRoles createTestRole(Test test, Map<String, TestRoles> testRoleMap,
                                     StepMsgProfileInfo stepMsgProfileInfo, String type) {
        String roleName = stepMsgProfileInfo.getTransactionKeyword() + " " + type;
        TestRoles testRole = testRoleMap.get(roleName);
        if (testRole == null) {
            RoleInTest roleInTest = findRoleInTest(roleName);
            testRole = createTestRole(test, roleInTest);
            testRoleMap.put(roleName, testRole);
        }
        return testRole;
    }

    /**
     * Creates a tm_test_roles record representing the role of that the test step is playing in the test.
     *
     * @param test
     * @param roleInTest
     * @return
     */
    private TestRoles createTestRole(Test test, RoleInTest roleInTest) {
        if (roleInTest != null) {
            TestRoles testRole = new TestRoles();
            testRole.setRoleInTest(roleInTest);
            testRole.setTest(test);
            testRole.setCardMax(1);
            testRole.setCardMin(1);
            testRole.setLastChanged(new Date());
            testRole.setLastModifierId("rettema");
            testRole.setNumberOfTestsToRealize(1);
            testRole.setTestOption(findDefaultTestOption());
            getItbEntityManager().persist(testRole);
            return testRole;
        } else {
            return null;
        }
    }

    /**
     * Creates a tm_test_description record representing a description entry for the test in the gazelle database.
     *
     * @param test
     * @param strTestDescription
     * @param testPrecondition
     * @param testStepsDescription
     */
    private void createTestDescription(Test test, String strTestDescription, String testPrecondition,
                                       String testStepsDescription) {
        TestDescription testDescription = new TestDescription();
        StringBuilder testDescBuilder = new StringBuilder();
        testDescBuilder.append("<h2>Special Instructions</h2>");
        if ((testStepsDescription != null) && !testStepsDescription.trim().isEmpty()) {
            testDescBuilder.append("<p>");
            testDescBuilder.append(testPrecondition);
            testDescBuilder.append("</p>");
        }
        if ((testStepsDescription != null) && !testStepsDescription.trim().isEmpty()) {
            testDescBuilder.append("<p>");
            testDescBuilder.append(testStepsDescription);
            testDescBuilder.append("</p>");
        }
        testDescBuilder.append("<br/>");
        testDescBuilder.append("<h2>Description</h2>");
        if ((strTestDescription != null) && !strTestDescription.trim().isEmpty()) {
            testDescBuilder.append("<p>");
            testDescBuilder.append(strTestDescription);
            testDescBuilder.append("</p>");
        }
        testDescription.setDescription(testDescBuilder.toString());
        testDescription.setTestParent(test);
        testDescription.setGazelleLanguage(findDefaultGazelleLanguage());
        getItbEntityManager().persist(testDescription);
    }

    /**
     * Retrieves the tf_transaction record from the gazelle dabase by the provided transaction keyword.
     * If the tf_transaction record was not found, then one is created on the fly.
     *
     * @param transactionKeyword
     * @return
     */
    private Transaction findTransaction(String transactionKeyword) {
        try {
            return (Transaction) getItbEntityManager()
                    .createQuery("select t from Transaction t where t.keyword=:keyword")
                    .setParameter("keyword", transactionKeyword).setHint("org.hibernate.cacheable", true)
                    .getSingleResult();
        } catch (NoResultException e) {
            Transaction transaction = new Transaction();
            transaction.setKeyword(transactionKeyword);
            transaction.setDescription(transactionKeyword);
            transaction.setName(transactionKeyword);
            transaction.setLastChanged(new Date());
            transaction.setLastModifierId("rettema");
            getItbEntityManager().persist(transaction);
            System.err.println("******************************Created Transaction '" + transactionKeyword + "'");
            return transaction;
        }
    }

    /**
     * Retrieves the validation_service record from the gazelle database that messages should be validated with by default.
     *
     * @return
     */
    private ValidationService findDefaultValidationService() {
        // NIST Validation Service
        return (ValidationService) getItbEntityManager()
                .createQuery("select vs from ValidationService vs where vs.key='NISTHL7V2VALIDATOR'")
                .setHint("org.hibernate.cacheable", true).getSingleResult();
    }

    /**
     * Retrieves the default tm_test_steps_option record from the gazelle database.
     *
     * @return
     */
    private TestStepsOption findDefaultTestStepsOption() {
        //Required
        return (TestStepsOption) getItbEntityManager()
                .createQuery(
                        "select testStepOption from TestStepsOption testStepOption where testStepOption.keyword='R'")
                .setHint("org.hibernate.cacheable", true).getSingleResult();
    }

    /**
     * Retrieves the default tm_test_option record from the gazelle database.
     *
     * @return
     */
    private TestOption findDefaultTestOption() {
        //Required
        return (TestOption) getItbEntityManager()
                .createQuery("select testOption from TestOption testOption where testOption.keyword='R'")
                .setHint("org.hibernate.cacheable", true).getSingleResult();
    }

    /**
     * Retrieves the default usr_gazelle_language record from the gazelle database.
     *
     * @return
     */
    private GazelleLanguage findDefaultGazelleLanguage() {
        //English
        return (GazelleLanguage) getItbEntityManager()
                .createQuery("select gl from GazelleLanguage gl where gl.keyword='English'")
                .setHint("org.hibernate.cacheable", true).getSingleResult();
    }

    /**
     * Retrieves the default tm_test_type record from the gazelle database.
     *
     * @return
     */
    private TestType findDefaultTestType() {
        //itb
        return (TestType) getItbEntityManager()
                .createQuery("select tt from TestType tt where tt.keyword='interoperability-testbed'")
                .setHint("org.hibernate.cacheable", true).getSingleResult();
    }

    /**
     * Retrieves the default tm_test_status record from the gazelle database.
     *
     * @return
     */
    private TestStatus findDefaultTestStatus() {
        //ready
        return (TestStatus) getItbEntityManager()
                .createQuery("select status from TestStatus status where status.keyword='ready'")
                .setHint("org.hibernate.cacheable", true).getSingleResult();
    }

    /**
     * Retrieves the default tm_test_peer_type record from the gazelle database.
     *
     * @return
     */
    private TestPeerType findDefaultTestPeerType() {
        //P2P_TEST
        return (TestPeerType) getItbEntityManager()
                .createQuery("select tpt from TestPeerType tpt where tpt.keyword='P2P_TEST'")
                .setHint("org.hibernate.cacheable", true).getSingleResult();
    }

    /**
     * Retrieves the default tm_role_in_test record from the gazelle database.
     *
     * @param keyword
     * @return
     */
    private RoleInTest findRoleInTest(String keyword) {
        try {
            return (RoleInTest) getItbEntityManager()
                    .createQuery("select roleInTest from RoleInTest roleInTest where roleInTest.keyword=:keyword")
                    .setParameter("keyword", keyword).setHint("org.hibernate.cacheable", true).getSingleResult();
        } catch (NoResultException e) {
            RoleInTest roleInTest = new RoleInTest();
            roleInTest.setIsRolePlayedByATool(false);
            roleInTest.setKeyword(keyword);
            roleInTest.setLastChanged(new Date());
            roleInTest.setLastModifierId("rettema");
            getItbEntityManager().persist(roleInTest);
            System.err.println("******************************Created RoleInTest '" + keyword + "'");
            return roleInTest;
        }
    }

    public EntityManager getItbEntityManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getItbEntityManager");
        }
        return itbEntityManager;
    }

    public void setItbEntityManager(EntityManager itbEntityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setItbEntityManager");
        }
        this.itbEntityManager = itbEntityManager;
    }

    public Connection getPreConnectathonConn() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPreConnectathonConn");
        }
        return preConnectathonConn;
    }

    public void setPreConnectathonConn(Connection preConnectathonConn) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPreConnectathonConn");
        }
        this.preConnectathonConn = preConnectathonConn;
    }

    /**
     * Closes database-related resources.
     */
    public void closeConnections() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeConnections");
        }
        if (getItbEntityManager() != null) {
            try {
                getItbEntityManager().close();
            } catch (Exception e) {
            }
        }
        if (getPreConnectathonConn() != null) {
            try {
                getPreConnectathonConn().close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Closes database-related resources.
     *
     * @param rs
     * @param stmt
     */
    private void closeResources(ResultSet rs, PreparedStatement stmt) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Creates the connections to the preconnectathon and gazelle databases.
     *
     * @throws SQLException
     */
    private void createConnections() throws SQLException {
        HibernateConfiguration.setConfigOnCurrentThread("jdbc:postgresql://" + "localhost" + "/" + "gazelle", "auto",
                false);
        setItbEntityManager(EntityManagerService.provideEntityManager());
        setPreConnectathonConn(DriverManager.getConnection("jdbc:mysql://localhost:3306/pixpdqtool", DB_LOGIN,
                DB_PSSWD));
    }

    /**
     * Initializes an instance of TestStepRecord off of the retrieved preconnectathon test step record
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private TestStepRecord initializeTestStepRecord(ResultSet rs) throws SQLException {
        return new TestStepRecord(rs.getInt("id_test"), rs.getInt("id_event"), rs.getInt("event_order"),
                rs.getString("title_test"), rs.getString("description_test"), rs.getString("steps_test"),
                rs.getString("precondition"), rs.getString("version"), rs.getString("test_actor_name"),
                rs.getString("test_actor_value"), rs.getString("event_actor_name"), rs.getString("event_actor_value"),
                rs.getString("description_event"));
    }

    private void printMissingData() {
        printMessage("\nImported " + ((totalResolvedSteps * 100) / totalSteps) + "% of test steps.\nResolved "
                + ((totalResolvedMsgProfiles * 100) / totalMsgProfiles)
                + "% of message profiles for imported test steps.");
        printMessage("tf_hl7_message_profile records missing for:" + missingProfiles);
    }

    private void printError(String message) {
        logWriter.println(message);
        System.err.println(message);
    }

    private void printMessage(String message) {
        logWriter.println(message);
        System.out.println(message);
    }

    private void printImportFailureForResponse(File responseMsgFile, TestStepRecord stepRecord, String requestMsgType) {
        printError("Import Failed for Step="
                + (stepRecord.getStepIndex() * 10)
                + " actor='"
                + stepRecord.getStepActorName()
                + "'reqMsgType='"
                + requestMsgType
                + "'.  Test Step and Step Message Profile will need to be manually created.\n\t\t Error: Response Message Type could not be " +
                "resolved.  Response message '"
                + responseMsgFile.getName() + "' does not exist in testcases directory for this step.");
    }

    private void printImportFailureForRequest(File requestMsgFile, TestStepRecord stepRecord) {
        printError("Import Failed for Step="
                + (stepRecord.getStepIndex() * 10)
                + " actor='"
                + stepRecord.getStepActorName()
                + "'. Test Step and Step Message Profile will need to be manually created.\n\t\tError: Request message '"
                + requestMsgFile.getName() + "' does not exist in testcases directory for this step.");
    }

    private void printImportStepSuccess(TestStepRecord stepRecord, StepMsgProfileInfo stepMsgProfileInfo) {
        totalResolvedSteps++;
        printMessage("Imported Step="
                + (stepRecord.getStepIndex() * 10)
                + " actor='"
                + stepRecord.getStepActorName()
                + "' trans='"
                + stepMsgProfileInfo.getTransactionKeyword()
                + "' reqMsgType='"
                + stepMsgProfileInfo.getRequestMsgType()
                + "' respMsgType='"
                + stepMsgProfileInfo.getResponseMsgType()
                + "' reqProfile="
                + (stepMsgProfileInfo.getRequestMsgProfile() != null ? "'"
                + stepMsgProfileInfo.getRequestMsgProfile().getProfileOid() + "'" : null)
                + " respProfile="
                + (stepMsgProfileInfo.getResponseMsgProfile() != null ? "'"
                + stepMsgProfileInfo.getResponseMsgProfile().getProfileOid() + "'" : null));
    }

    /**
     * Keeps track of missing message type and transaction combinations
     */
    private class ProfileAttributes implements Comparable<ProfileAttributes> {
        private String msgType;
        private String transactionKeyword;

        private ProfileAttributes(String msgType, String transactionKeyword) {
            this.msgType = msgType;
            this.transactionKeyword = transactionKeyword;
        }

        @Override
        public int hashCode() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("hashCode");
            }
            final int prime = 31;
            int result = 1;
            result = (prime * result) + getOuterType().hashCode();
            result = (prime * result) + ((msgType == null) ? 0 : msgType.hashCode());
            result = (prime * result) + ((transactionKeyword == null) ? 0 : transactionKeyword.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("equals");
            }
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ProfileAttributes other = (ProfileAttributes) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (msgType == null) {
                if (other.msgType != null) {
                    return false;
                }
            } else if (!msgType.equals(other.msgType)) {
                return false;
            }
            if (transactionKeyword == null) {
                if (other.transactionKeyword != null) {
                    return false;
                }
            } else if (!transactionKeyword.equals(other.transactionKeyword)) {
                return false;
            }
            return true;
        }

        private PreconnectathonDataMigrator getOuterType() {
            return PreconnectathonDataMigrator.this;
        }

        @Override
        public String toString() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("toString");
            }
            return transactionKeyword + " " + msgType + "\n";
        }

        @Override
        public int compareTo(ProfileAttributes o) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("compareTo");
            }
            return this.toString().compareTo(o.toString());
        }
    }

    /**
     * Class representing the resolved message profile, transaction, and message types.
     */
    private class StepMsgProfileInfo {
        private Hl7MessageProfile requestMsgProfile;
        private Hl7MessageProfile responseMsgProfile;
        private String transactionKeyword;
        private String requestMsgType;
        private String responseMsgType;
        private File requestMsgFile;
        private File responseMsgFile;
        private String requestMsgExampleContents;
        private String responseMsgExampleContents;
        private File validationContextFile;
        private String validationContextFileContents;

        Hl7MessageProfile getRequestMsgProfile() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getRequestMsgProfile");
            }
            return requestMsgProfile;
        }

        void setRequestMsgProfile(Hl7MessageProfile requestMsgProfile) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setRequestMsgProfile");
            }
            this.requestMsgProfile = requestMsgProfile;
        }

        Hl7MessageProfile getResponseMsgProfile() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getResponseMsgProfile");
            }
            return responseMsgProfile;
        }

        void setResponseMsgProfile(Hl7MessageProfile responseMsgProfile) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setResponseMsgProfile");
            }
            this.responseMsgProfile = responseMsgProfile;
        }

        public String getTransactionKeyword() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTransactionKeyword");
            }
            return transactionKeyword;
        }

        public void setTransactionKeyword(String transactionKeyword) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setTransactionKeyword");
            }
            this.transactionKeyword = transactionKeyword;
        }

        String getRequestMsgType() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getRequestMsgType");
            }
            return requestMsgType;
        }

        void setRequestMsgType(String requestMsgType) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setRequestMsgType");
            }
            this.requestMsgType = requestMsgType;
        }

        String getResponseMsgType() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getResponseMsgType");
            }
            return responseMsgType;
        }

        void setResponseMsgType(String responseMsgType) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setResponseMsgType");
            }
            this.responseMsgType = responseMsgType;
        }

        File getRequestMsgFile() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getRequestMsgFile");
            }
            return requestMsgFile;
        }

        void setRequestMsgFile(File requestMsgFile) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setRequestMsgFile");
            }
            this.requestMsgFile = requestMsgFile;
        }

        File getResponseMsgFile() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getResponseMsgFile");
            }
            return responseMsgFile;
        }

        void setResponseMsgFile(File responseMsgFile) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setResponseMsgFile");
            }
            this.responseMsgFile = responseMsgFile;
        }

        String getRequestMsgExampleContents() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getRequestMsgExampleContents");
            }
            return requestMsgExampleContents;
        }

        void setRequestMsgExampleContents(String requestMsgExampleContents) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setRequestMsgExampleContents");
            }
            this.requestMsgExampleContents = requestMsgExampleContents;
        }

        String getResponseMsgExampleContents() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getResponseMsgExampleContents");
            }
            return responseMsgExampleContents;
        }

        void setResponseMsgExampleContents(String responseMsgExampleContents) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setResponseMsgExampleContents");
            }
            this.responseMsgExampleContents = responseMsgExampleContents;
        }

        File getValidationContextFile() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getValidationContextFile");
            }
            return validationContextFile;
        }

        void setValidationContextFile(File validationContextFile) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setValidationContextFile");
            }
            this.validationContextFile = validationContextFile;
        }

        String getValidationContextFileContents() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getValidationContextFileContents");
            }
            return validationContextFileContents;
        }

        void setValidationContextFileContents(String validationContextFileContents) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setValidationContextFileContents");
            }
            this.validationContextFileContents = validationContextFileContents;
        }
    }

    /**
     * Class representing the preconnectathon test step record
     */
    private class TestStepRecord {
        private int testId;
        private int stepId;
        private int stepIndex;
        private String testName;
        private String testDescription;
        private String testStepsDescription;
        private String testPrecondition;
        private String testVersion;
        private String testActorName;
        private String testActorValue;
        private String stepActorName;
        private String stepActorValue;
        private String stepDescription;

        TestStepRecord(int testId, int stepId, int stepIndex, String testName, String testDescription,
                       String testStepsDescription, String testPrecondition, String testVersion, String testActorName,
                       String testActorValue, String stepActorName, String stepActorValue, String stepDescription) {
            super();
            this.testId = testId;
            this.stepId = stepId;
            this.stepIndex = stepIndex;
            this.testName = testName;
            this.testDescription = testDescription;
            this.testStepsDescription = testStepsDescription;
            this.testPrecondition = testPrecondition;
            this.testVersion = testVersion;
            this.testActorName = testActorName;
            this.testActorValue = testActorValue;
            this.stepActorName = stepActorName;
            this.stepActorValue = stepActorValue;
            this.stepDescription = stepDescription;
        }

        public int getTestId() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTestId");
            }
            return testId;
        }

        @SuppressWarnings("unused")
        public int getStepId() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getStepId");
            }
            return stepId;
        }

        public int getStepIndex() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getStepIndex");
            }
            return stepIndex;
        }

        public String getTestName() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTestName");
            }
            return testName;
        }

        public String getTestDescription() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTestDescription");
            }
            return testDescription;
        }

        public String getTestStepsDescription() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTestStepsDescription");
            }
            return testStepsDescription;
        }

        public String getTestPrecondition() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTestPrecondition");
            }
            return testPrecondition;
        }

        public String getTestVersion() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTestVersion");
            }
            return testVersion;
        }

        @SuppressWarnings("unused")
        public String getTestActorName() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTestActorName");
            }
            return testActorName;
        }

        @SuppressWarnings("unused")
        public String getTestActorValue() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTestActorValue");
            }
            return testActorValue;
        }

        public String getStepActorName() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getStepActorName");
            }
            return stepActorName;
        }

        public String getStepActorValue() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getStepActorValue");
            }
            return stepActorValue;
        }

        public String getStepDescription() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getStepDescription");
            }
            return stepDescription;
        }
    }
}
