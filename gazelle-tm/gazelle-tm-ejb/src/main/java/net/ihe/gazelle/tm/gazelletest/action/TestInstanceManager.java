package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.util.File;
import net.ihe.gazelle.hql.HQLReloader;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.jira.issue.create.JiraClientCallback;
import net.ihe.gazelle.jira.issue.create.JiraManager;
import net.ihe.gazelle.objects.model.ObjectFileType;
import net.ihe.gazelle.proxy.ws.Configuration;
import net.ihe.gazelle.simulator.ws.ConfigurationForWS;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.WSTransactionUsage;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.configurations.model.AbstractConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.SyslogConfiguration;
import net.ihe.gazelle.tm.configurations.model.WebServiceConfiguration;
import net.ihe.gazelle.tm.gazelletest.model.definition.ContextualInformation;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestSteps;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.gazelletest.testStepData.TestStepDataWrapper;
import net.ihe.gazelle.tm.systems.action.NoteManager;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.tee.dao.StepInstanceMsgDAO;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import net.ihe.gazelle.tm.tee.status.dao.TestInstanceDAO;
import net.ihe.gazelle.tm.tee.status.dao.TestInstanceExecutionStatusDAO;
import net.ihe.gazelle.tm.tee.status.dao.TestStepsInstanceExecutionStatusDAO;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.util.HTMLFilter;
import net.ihe.gazelle.util.Pair;
import net.ihe.gazelle.util.URLParser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("testInstanceManager")
@Scope(ScopeType.PAGE)
@Synchronized(timeout = 10000)
public class TestInstanceManager implements Serializable, JiraClientCallback {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceManager.class);
    protected List<AbstractConfiguration> hl7InitiatorConfigurationsList;
    protected List<AbstractConfiguration> hl7ResponderConfigurationsList;
    protected List<AbstractConfiguration> hl7V3InitiatorConfigurationsList;
    protected List<AbstractConfiguration> hl7V3ResponderConfigurationsList;
    protected List<AbstractConfiguration> dicomSCUConfigurationsList;
    protected List<AbstractConfiguration> dicomSCPConfigurationsList;
    protected List<AbstractConfiguration> webServicesConfigurationsList;
    protected List<AbstractConfiguration> syslogConfigurationsList;
    protected AbstractConfiguration selectedResponderConfiguration;
    private boolean pageLoaded;
    private User selectedUser;
    private TestInstance selectedTestInstance;
    private String descriptionToAdd;
    private String commentTobeAdded;
    private String failExplanation;
    private Status selectedStatus;
    private String backPage;
    private String backMessage;
    private Boolean isConnectedUserAllowedToValidateTestInstance;
    private Boolean isConnectedUserAllowedToEditTestInstance;
    private String valueURL;
    private String commentURL;
    private String commentFile;
    private TestStepsInstance selectedTestStepsInstance;
    private boolean selectedCommentWasModified = false;
    private File fich = null;
    private int counter = 0;
    private ContextualInformationInstance selectedContextualInformationInstance;

    // Configurations for simulators
    private String selectedComment;
    private boolean urlRelatedToSelectedTestStepsInstance;
    private String fichName;
    private TestStepsData selectedTsd;
    private TestStepsInstance tsiLinkedToTsd;
    private TestInstance tiLinkedToTsd;
    private String evsClientUrl = null;
    private Map<Integer, Boolean> addsShown = new HashMap<Integer, Boolean>();
    private Map<Integer, Boolean> addsUrl = new HashMap<Integer, Boolean>();
    private boolean showUrl;
    private boolean showComment;

    private boolean blockPostComment = true;

    private boolean displayJiraPanel = false;

    private JiraManager jiraManager;

    // captures user selected execution status from the UI drop-down
    private TestInstanceExecutionStatusEnum selectedExecutionStatus;

    private TestInstanceExecutionStatusDAO testInstanceExecutionStatusDAO;

    private TestInstanceDAO testInstanceDAO;

    private TmStepInstanceMessage selectedTestStepMessage;

    private Set<Integer> activeProxyPorts = new HashSet<Integer>();

    private Map<Integer, List<TmStepInstanceMessage>> tsiMessagesMap = new HashMap<Integer, List<TmStepInstanceMessage>>();

    // controls at least once refresh after execution completes
    private boolean refreshedOnceAfterCompleted = false;
    private String commentToAdd;

    public static void main(String[] args) {
        HttpClient client = new HttpClient();
        PostMethod filePost = new PostMethod("http://gazelle.ihe.net/EVSClient/upload");

        // use the type as file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String messageType = "comment" + sdf.format(new Date());
        String comment = "Hello !";
        PartSource partSource = new ByteArrayPartSource(messageType, comment.getBytes(StandardCharsets.UTF_8));
        Part[] parts = {new FilePart("message", partSource)};
        filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
        int status = -1;
        try {
            status = client.executeMethod(filePost);
        } catch (Exception e) {
            status = -1;
            LOG.error("" + e.getMessage());
        }
    }

    public boolean isRefreshedOnceAfterCompleted() {
        return refreshedOnceAfterCompleted;
    }

    public void setRefreshedOnceAfterCompleted(boolean refreshedOnceAfterCompleted) {
        this.refreshedOnceAfterCompleted = refreshedOnceAfterCompleted;
    }

    public TmStepInstanceMessage getSelectedTestStepMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestStepMessage");
        }
        return selectedTestStepMessage;
    }

    public void setSelectedTestStepMessage(TmStepInstanceMessage message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestStepMessage");
        }
        this.selectedTestStepMessage = message;
    }

    public TestInstanceExecutionStatusEnum getSelectedExecutionStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedExecutionStatus");
        }
        return selectedExecutionStatus;
    }

    public void setSelectedExecutionStatus(TestInstanceExecutionStatusEnum selectedExecutionStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedExecutionStatus");
        }
        this.selectedExecutionStatus = selectedExecutionStatus;
    }

    public boolean isblockPostComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isblockPostComment");
        }
        return blockPostComment;
    }

    public void setblockPostComment(boolean blockPostComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setblockPostComment");
        }
        this.blockPostComment = blockPostComment;
    }

    public void authorizePost() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("authorizePost");
        }
        setblockPostComment(false);
    }

    public boolean isShowUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowUrl");
        }
        return showUrl;
    }

    public void setShowUrl(boolean showUrl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowUrl");
        }
        this.showUrl = showUrl;
    }

    public boolean isShowComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowComment");
        }
        return showComment;
    }

    public void setShowComment(boolean showComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowComment");
        }
        this.showComment = showComment;
    }

    public boolean isSelectedCommentWasModified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isSelectedCommentWasModified");
        }
        return selectedCommentWasModified;
    }

    public void setSelectedCommentWasModified(boolean selectedCommentWasModified) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedCommentWasModified");
        }
        this.selectedCommentWasModified = selectedCommentWasModified;
    }

    public String getSelectedComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedComment");
        }
        return selectedComment;
    }

    public void setSelectedComment(String selectedComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedComment");
        }
        this.selectedComment = selectedComment;
    }

    public ContextualInformationInstance getSelectedContextualInformationInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedContextualInformationInstance");
        }
        return selectedContextualInformationInstance;
    }

    public void setSelectedContextualInformationInstance(
            ContextualInformationInstance selectedContextualInformationInstance) {
        this.selectedContextualInformationInstance = selectedContextualInformationInstance;
    }

    public int getCounter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCounter");
        }
        return counter;
    }

    public void setCounter(int counter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCounter");
        }
        this.counter = counter;
    }

    public File getFich() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFich");
        }
        return fich;
    }

    public void setFich(File fich) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFich");
        }
        this.fich = fich;
    }

    public TestStepsInstance getSelectedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestStepsInstance");
        }
        return selectedTestStepsInstance;
    }

    public void setSelectedTestStepsInstance(TestStepsInstance selectedTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestStepsInstance");
        }
        this.selectedTestStepsInstance = selectedTestStepsInstance;
    }

    public String getCommentURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCommentURL");
        }
        return commentURL;
    }

    public void setCommentURL(String commentURL) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCommentURL");
        }
        this.commentURL = commentURL;
    }

    public String getCommentFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCommentFile");
        }
        return commentFile;
    }

    public void setCommentFile(String commentFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCommentFile");
        }
        this.commentFile = commentFile;
    }

    public String getFichName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFichName");
        }
        return fichName;
    }

    public void setFichName(String fichName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFichName");
        }
        this.fichName = fichName;
    }

    public String getValueURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValueURL");
        }
        return valueURL;
    }

    public void setValueURL(String valueURL) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setValueURL");
        }
        this.valueURL = valueURL;
    }

    public String getBackPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBackPage");
        }
        return backPage;
    }

    public void setBackPage(String backPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setBackPage");
        }
        this.backPage = backPage;
    }

    public String getBackMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBackMessage");
        }
        return backMessage;
    }

    public void setBackMessage(String backMessage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setBackMessage");
        }
        this.backMessage = backMessage;
    }

    public User getSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedUser");
        }
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedUser");
        }
        this.selectedUser = selectedUser;
    }

    public String getCommentTobeAdded() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCommentTobeAdded");
        }
        return commentTobeAdded;
    }

    public void setCommentTobeAdded(String commentTobeAdded) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCommentTobeAdded");
        }
        this.commentTobeAdded = commentTobeAdded;
    }

    public String getDescriptionToAdd() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDescriptionToAdd");
        }
        return descriptionToAdd;
    }

    public void setDescriptionToAdd(String descriptionToAdd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDescriptionToAdd");
        }
        this.descriptionToAdd = descriptionToAdd;
    }

    public TestInstance getSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestInstance");
        }
        return selectedTestInstance;
    }

    public void setSelectedTestInstance(TestInstance selectedTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestInstance");
        }
        this.selectedTestInstance = selectedTestInstance;
    }

    public String getFailExplanation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFailExplanation");
        }
        return failExplanation;
    }

    public void setFailExplanation(String failExplanation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFailExplanation");
        }
        this.failExplanation = failExplanation;
    }

    public Status getSelectedStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedStatus");
        }
        return selectedStatus;
    }

    public void setSelectedStatus(Status selectedStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedStatus");
        }
        this.selectedStatus = selectedStatus;
    }

    public void initTestInstanceManagement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTestInstanceManagement");
        }
        backPage = (String) Contexts.getSessionContext().get("backPage");
        backMessage = (String) Contexts.getSessionContext().get("backMessage");
        this.selectedTestStepsInstance = null;
        this.selectedContextualInformationInstance = null;
        this.failExplanation = null;
        isConnectedUserAllowedToEditTestInstance = null;
        isConnectedUserAllowedToValidateTestInstance = null;
        this.testInstanceDAO = new TestInstanceDAO();
        this.testInstanceExecutionStatusDAO = new TestInstanceExecutionStatusDAO();
        this.selectedResponderConfiguration = null;
        this.selectedTestInstance = null;
        this.refreshedOnceAfterCompleted = false;
        this.selectedStatus = null;
        if (Identity.instance().isLoggedIn()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            String id = fc.getExternalContext().getRequestParameterMap().get("id");
            if (id != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                int idd = 0;
                try {
                    idd = Integer.parseInt(id);
                } catch (Exception e) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No Test Instance found with the given id.");
                }
                TestInstance testInstance = em.find(TestInstance.class, idd);
                if (testInstance != null) {
                    selectedStatus = testInstance.getLastStatus();
                    // added for ITB
                    selectedExecutionStatus = (testInstance.getExecutionStatus() != null) ? testInstance
                            .getExecutionStatus().getKey() : TestInstanceExecutionStatusEnum.ACTIVE;
                    showTestInstance(testInstance);
                    retrieveTestStatusAndMessages();
                } else {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No Test Instance found with the given id");
                }
            }
        } else {
            Redirect redirect = Redirect.instance();
            redirect.setViewId("/home.xhtml");
            redirect.execute();
        }
    }

    public void persistSelectedTestInstance(boolean withConfirmation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedTestInstance");
        }
        if (!withConfirmation && !selectedStatus.equals(selectedTestInstance.getLastStatus())) {
            if (selectedStatus.equals(Status.getSTATUS_FAILED())
                    || selectedTestInstance.getLastStatus().equals(Status.getSTATUS_CRITICAL())) {
                return;
            }
        }
        persistSelectedTestInstance();
    }

    public void persistSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedTestInstance");
        }
        TestInstance result = persistTestInstance(selectedTestInstance);
        if (result != null) {
            selectedTestInstance = result;
        }
    }

    public TestInstance persistTestInstance(TestInstance inTestInstanceParam) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTestInstance");
        }
        if (inTestInstanceParam != null) {
            TestInstance inTestInstance = inTestInstanceParam;
            if (!selectedStatus.equals(inTestInstance.getLastStatus())) {
                EntityManager entityManager = EntityManagerService.provideEntityManager();

                //Update monitor to current user
                if (MonitorInSession.isConnectedUserMonitorForSelectedSession()) {
                    if ((selectedStatus.equals(Status.getSTATUS_VERIFIED()) || selectedStatus.equals(Status.getSTATUS_FAILED())
                            || selectedStatus.equals(Status.getSTATUS_PARTIALLY_VERIFIED()))) {
                        inTestInstance.setMonitorInSession(MonitorInSession
                                .getActivatedMonitorInSessionForATestingSessionByUser(
                                        TestingSession.getSelectedTestingSession(), User.loggedInUser()));
                    }
                }

                // -- update simulator : call to stopTestInstance method------
                if ((inTestInstance.getLastStatus().equals(Status.getSTATUS_STARTED()) || (inTestInstance
                        .getLastStatus().equals(Status.getSTATUS_PAUSED())))
                        && !selectedStatus.equals(Status.getSTATUS_PAUSED())
                        && !selectedStatus.equals(Status.getSTATUS_STARTED()) && inTestInstance.isOrchestrated()) {
                    LocalGPClient.updateSimulatorsOfTestInstanceToCompleted(inTestInstance);
                }
                // ------------------------------------------------------------

                if (selectedStatus.equals(Status.getSTATUS_FAILED())) {
                    List<TestInstanceParticipants> testInstanceParticipantsList = TestInstanceParticipants
                            .getTestInstanceParticipantsListForATest(inTestInstance);
                    for (TestInstanceParticipants testInstanceParticipants : testInstanceParticipantsList) {
                        testInstanceParticipants.setStatus(TestInstanceParticipantsStatus.getSTATUS_FAILED());
                        testInstanceParticipants = entityManager.merge(testInstanceParticipants);
                    }
                }
                if (selectedStatus.equals(Status.getSTATUS_VERIFIED())) {
                    List<TestInstanceParticipants> testInstanceParticipantsList = TestInstanceParticipants
                            .getTestInstanceParticipantsListForATest(inTestInstance);
                    for (TestInstanceParticipants testInstanceParticipants : testInstanceParticipantsList) {
                        testInstanceParticipants.setStatus(TestInstanceParticipantsStatus.getSTATUS_PASSED());
                        testInstanceParticipants = entityManager.merge(testInstanceParticipants);
                    }
                }
                inTestInstance.setLastStatus(selectedStatus);
                if (selectedStatus.equals(Status.getSTATUS_FAILED()) && failExplanation != null && failExplanation.length() > 0) {
                    // Add fail message in TI comments and in each system notes
                    List<TestInstanceParticipants> currenTestInstanceParticipants = inTestInstance
                            .getTestInstanceParticipants();
                    for (TestInstanceParticipants tiParticipants : currenTestInstanceParticipants) {
                        SystemInSession systemInSession = tiParticipants.getSystemInSessionUser()
                                .getSystemInSession();
                        systemInSession.setAddCommentInNotes(true);
                    }
                    setCommentTobeAdded(failExplanation);
                    setSelectedTestInstance(inTestInstance);
                    addNotesOnSystemAndInTestInstance();
                }

                inTestInstance = entityManager.merge(inTestInstance);
                entityManager.flush();
            }
            return inTestInstance;
        }
        return null;
    }

    public boolean isConnectedUserAllowedToValidateTestInstance(TestInstance inTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isConnectedUserAllowedToValidateTestInstance");
        }
        if (inTestInstance != null) {
            return MonitorInSession.isUserAllowedToValidateTestInstance(User.loggedInUser(), inTestInstance);
        }
        return false;
    }

    public boolean isConnectedUserAllowedToValidateSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isConnectedUserAllowedToValidateSelectedTestInstance");
        }
        if (isConnectedUserAllowedToValidateTestInstance == null) {
            isConnectedUserAllowedToValidateTestInstance = MonitorInSession.isConnectedUserMonitorForSelectedSession();
        }
        return isConnectedUserAllowedToValidateTestInstance;
    }

    public boolean isConnectedUserAllowedToValidateTestInstance(String inTestInstanceId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isConnectedUserAllowedToValidateTestInstance");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestInstance testInstance = entityManager.find(TestInstance.class, Integer.valueOf(inTestInstanceId));
        if (testInstance != null) {
            return MonitorInSession.isUserAllowedToValidateTestInstance(User.loggedInUser(), testInstance);
        }
        return false;
    }

    public boolean isConnectedUserAllowedToChangeSystemManager(TestInstanceParticipants inTestInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isConnectedUserAllowedToChangeSystemManager");
        }
        if (inTestInstanceParticipants != null) {
            if (Role.isLoggedUserVendorAdmin()) {
                List<Institution> institutionList = System.getInstitutionsForASystem(inTestInstanceParticipants
                        .getSystemInSessionUser().getSystemInSession().getSystem());
                for (Institution institution : institutionList) {
                    if (User.loggedInUser().getInstitution().equals(institution)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addNotesOnSystemAndInTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNotesOnSystemAndInTestInstance");
        }

        TestInstance currentTi = getSelectedTestInstance();
        List<TestInstanceParticipants> currenTestInstanceParticipants = currentTi.getTestInstanceParticipants();
        if ((commentTobeAdded.trim().length() > 0) && (currenTestInstanceParticipants != null)) {
            for (TestInstanceParticipants tiParticipants : currenTestInstanceParticipants) {
                SystemInSession systemInSession = tiParticipants.getSystemInSessionUser().getSystemInSession();
                if (systemInSession.isAddCommentInNotes()) {
                    NoteManager noteManager;
                    try {
                        noteManager = new NoteManager();
                        noteManager.setSelectedSystemInSession(systemInSession);
                        noteManager.setNoteTobeAdded("(from TI-" + currentTi.getId().toString() + ") " + commentTobeAdded);
                        noteManager.addNotesOnSystem();
                    } catch (Exception e) {
                        LOG.error("" + e);
                    }
                }
            }
            addComment();
        } else {
            if (currenTestInstanceParticipants == null) {
                LOG.error("currentTestInstanceParticipants is null");
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error with your comment");
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Your comment is empty !");
            }
        }
    }

    public void addDescription(TestInstanceParticipants tip, System selectedSystem,
                               SystemInSession selectedSystemInSession, Status selectedStatusForEdition) {

        MesaTestManager mtm = new MesaTestManager();
        TestInstance ti = tip.getTestInstance();
        mtm.setCurrentTestInstanceParticipants(tip);
        mtm.setSelectedSystem(selectedSystem);
        mtm.setSelectedSystemInSession(selectedSystemInSession);
        mtm.setSelectedStatusForEdition(selectedStatusForEdition);

        if (descriptionToAdd.trim().length() > 0) {
            String description = StringEscapeUtils.escapeHtml(descriptionToAdd);
            description = description.replace("\r\n", "<br/>");
            description = description.replace("\n", "<br/>");
            description = description.replace("\r", "<br/>");

            ti.addDescription(description);
            int size = ti.getListTestInstanceEvent().size() - 1;
            ti.setLastChanged(ti.getListTestInstanceEvent().get(size).getDateOfEvent());
            ti.setLastModifierId(ti.getListTestInstanceEvent().get(size).getLastModifierId());
            mtm.persistTest(ti);
        }
        descriptionToAdd = null;
    }

    public void addFile(List<TestInstancePathToLogFile> listFileLogPathReturn, TestInstanceParticipants tip,
                        System selectedSystem, SystemInSession selectedSystemInSession, Status selectedStatusForEdition,
                        String fileName) {

        MesaTestManager mtm = new MesaTestManager();
        TestInstance ti = tip.getTestInstance();
        mtm.setCurrentTestInstanceParticipants(tip);
        mtm.setSelectedSystem(selectedSystem);
        mtm.setSelectedSystemInSession(selectedSystemInSession);
        mtm.setSelectedStatusForEdition(selectedStatusForEdition);

        String description = StringEscapeUtils.escapeHtml(fileName);
        description = description.replace("\r\n", "<br/>");
        description = description.replace("\n", "<br/>");
        description = description.replace("\r", "<br/>");

        ti.addFile(fileName);
        ti.setFileLogPathReturn(listFileLogPathReturn);

        int size = ti.getListTestInstanceEvent().size() - 1;
        ti.setLastChanged(ti.getListTestInstanceEvent().get(size).getDateOfEvent());
        ti.setLastModifierId(ti.getListTestInstanceEvent().get(size).getLastModifierId());

        mtm.persistTest(ti);
        fileName = null;
    }

    public void removeFile(List<TestInstancePathToLogFile> listFileLogPathReturn, TestInstanceParticipants tip,
                           System selectedSystem, SystemInSession selectedSystemInSession, Status selectedStatusForEdition,
                           String fileName) {

        MesaTestManager mtm = new MesaTestManager();
        TestInstance ti = tip.getTestInstance();
        mtm.setCurrentTestInstanceParticipants(tip);
        mtm.setSelectedSystem(selectedSystem);
        mtm.setSelectedSystemInSession(selectedSystemInSession);
        mtm.setSelectedStatusForEdition(selectedStatusForEdition);

        String description = StringEscapeUtils.escapeHtml(fileName);
        description = description.replace("\r\n", "<br/>");
        description = description.replace("\n", "<br/>");
        description = description.replace("\r", "<br/>");

        ti.removeFile(fileName);
        ti.setFileLogPathReturn(listFileLogPathReturn);

        int size = ti.getListTestInstanceEvent().size() - 1;
        ti.setLastChanged(ti.getListTestInstanceEvent().get(size).getDateOfEvent());
        ti.setLastModifierId(ti.getListTestInstanceEvent().get(size).getLastModifierId());

        mtm.persistTest(ti);
        fileName = null;
    }

    public List<User> getInstitutionUsersList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionUsersList");
        }
        return User.listUsersForCompany(Institution.getLoggedInInstitution());
    }

    public void updateTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestInstance");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        selectedTestInstance = entityManager.find(TestInstance.class, selectedTestInstance.getId());
    }

    public void updateSystemInSessionManagerForATestInstanceParticipant(
            TestInstanceParticipants inTestInstanceParticipants) {
        if (selectedUser != null) {
            if (inTestInstanceParticipants != null) {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                SystemInSessionUser systemInSessionUser = SystemInSessionUser
                        .getSystemInSessionUserBySystemInSessionByUser(inTestInstanceParticipants
                                .getSystemInSessionUser().getSystemInSession(), selectedUser);
                if (systemInSessionUser == null) {
                    systemInSessionUser = new SystemInSessionUser();
                    systemInSessionUser.setSystemInSession(inTestInstanceParticipants.getSystemInSessionUser()
                            .getSystemInSession());
                    systemInSessionUser.setUser(selectedUser);
                    systemInSessionUser = entityManager.merge(systemInSessionUser);
                }
                inTestInstanceParticipants.setSystemInSessionUser(systemInSessionUser);
                persistTestInstanceParitcipants(inTestInstanceParticipants);
            }
        }
    }

    public void updateSelectedTestInstanceStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedTestInstanceStatus");
        }
        Status status = selectedTestInstance.getLastStatus();
        getSelectedTestInstanceFromPage();
        selectedTestInstance = updateTestInstanceStatus(selectedTestInstance, status);
    }

    public void updateSelectedTestInstanceStatus(Status inStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedTestInstanceStatus");
        }
        getSelectedTestInstanceFromPage();
        if ((selectedTestInstance != null) && (inStatus != null)) {
            selectedTestInstance = updateTestInstanceStatus(selectedTestInstance, inStatus);
        }
    }

    public TestInstance updateTestInstanceStatus(String inTestInstanceId, Status inStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestInstanceStatus");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestInstance testInstance = entityManager.find(TestInstance.class, Integer.valueOf(inTestInstanceId));
        if (testInstance != null) {
            return updateTestInstanceStatus(testInstance, inStatus);
        }
        return null;
    }

    public TestInstance updateTestInstanceStatus(TestInstance inTestInstance, Status inStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestInstanceStatus");
        }
        if (inStatus != null) {
            EntityManagerService.provideEntityManager();
            inTestInstance = TestInstance.getTestInstanceWithAllAttributes(inTestInstance);
            selectedStatus = inStatus;
            return persistTestInstance(inTestInstance);
        }
        return null;
    }

    public void persistTestInstanceParitcipants(TestInstanceParticipants selectedTestInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTestInstanceParitcipants");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(selectedTestInstanceParticipants);
    }

    public List<TestInstanceParticipants> getTestInstanceParticipantAsSUTListForSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceParticipantAsSUTListForSelectedTestInstance");
        }
        if (selectedTestInstance != null) {
            List<TestInstanceParticipants> testInstanceParticipantsAsSutlist = new ArrayList<TestInstanceParticipants>();
            List<TestInstanceParticipants> testInstanceParticipantslist = TestInstanceParticipants
                    .getTestInstanceParticipantsListForATest(selectedTestInstance);
            if (testInstanceParticipantslist != null) {
                for (TestInstanceParticipants testInstanceParticipants : testInstanceParticipantslist) {
                    testInstanceParticipantsAsSutlist.add(testInstanceParticipants);
                }
                Collections.sort(testInstanceParticipantsAsSutlist, new Comparator<TestInstanceParticipants>() {
                    @Override
                    public int compare(TestInstanceParticipants o1, TestInstanceParticipants o2) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("compare");
                        }
                        return o1
                                .getSystemInSessionUser()
                                .getSystemInSession()
                                .getSystem()
                                .getKeyword()
                                .compareToIgnoreCase(
                                        o2.getSystemInSessionUser().getSystemInSession().getSystem().getKeyword());
                    }
                });
                return testInstanceParticipantsAsSutlist;
            }
        }
        return null;
    }

    public List<TestInstanceParticipants> getTestInstanceParticipantsWithSystemsListDistinct() {
        return removeTestInstanceParticipantAsSUTListForSelectedTestInstanceIfSameSystem();
    }

    public List<TestInstanceParticipants> removeTestInstanceParticipantAsSUTListForSelectedTestInstanceIfSameSystem() {
        List<TestInstanceParticipants> res = new ArrayList<TestInstanceParticipants>();
        List<TestInstanceParticipants> tmpList = getTestInstanceParticipantAsSUTListForSelectedTestInstance();
        List<SystemInSession> sisList = new ArrayList<SystemInSession>();

        for (TestInstanceParticipants tmpTip : tmpList) {
            if (!sisList.contains(tmpTip.getSystemInSessionUser().getSystemInSession())) {
                sisList.add(tmpTip.getSystemInSessionUser().getSystemInSession());
                res.add(tmpTip);
            }
        }

        return res;
    }

    public List<TestInstanceParticipants> getTestInstanceParticipantAsSimulatorListForSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceParticipantAsSimulatorListForSelectedTestInstance");
        }
        if (selectedTestInstance != null) {
            List<TestInstanceParticipants> testInstanceParticipantsAsSimulatorlist = new ArrayList<TestInstanceParticipants>();
            List<TestInstanceParticipants> testInstanceParticipantslist = TestInstanceParticipants
                    .getTestInstanceParticipantsListForATest(selectedTestInstance);
            if (testInstanceParticipantslist != null) {
                for (TestInstanceParticipants testInstanceParticipants : testInstanceParticipantslist) {
                    if (SimulatorInSession.class.isInstance(testInstanceParticipants.getSystemInSessionUser()
                            .getSystemInSession())) {
                        testInstanceParticipantsAsSimulatorlist.add(testInstanceParticipants);
                    }
                }
                if (testInstanceParticipantsAsSimulatorlist.size() > 0) {
                    return testInstanceParticipantsAsSimulatorlist;
                }
            }
        }
        return null;
    }

    public List<TestInstanceParticipants> getTestInstanceParticipantListForSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceParticipantListForSelectedTestInstance");
        }
        if (selectedTestInstance != null) {
            return TestInstanceParticipants.getTestInstanceParticipantsListForATest(selectedTestInstance);
        }
        return null;
    }

    public String getTestInstancePermalink(TestInstance inTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstancePermalink");
        }
        if (inTestInstance != null) {
            return ApplicationPreferenceManager.instance().getApplicationUrl() + "testInstance.seam?id="
                    + inTestInstance.getId();
        }
        return null;
    }

    public String getTestInstancePermalinkForSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstancePermalinkForSelectedTest");
        }
        return getTestInstancePermalink(selectedTestInstance);
    }

    public boolean isConnectedUserAllowedToEditTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isConnectedUserAllowedToEditTestInstance");
        }
        if (selectedTestInstance == null) {
            initTestInstanceManagement();
        }

        if (isConnectedUserAllowedToEditTestInstance == null) {
            User user = User.loggedInUser();
            if ((user != null) && (selectedTestInstance != null)) {
                if (Role.isLoggedUserAdmin()) {
                    isConnectedUserAllowedToEditTestInstance = true;
                } else if (Role.isLoggedUserMonitor()) {
                    MonitorInSession monitorInSession = MonitorInSession
                            .getActivatedMonitorInSessionForATestingSessionByUser(
                                    selectedTestInstance.getTestingSession(), user);
                    if (monitorInSession == null) {
                        isConnectedUserAllowedToEditTestInstance = false;
                    } else {
                        isConnectedUserAllowedToEditTestInstance = true;
                    }
                } else {
                    isConnectedUserAllowedToEditTestInstance = TestInstance.isInstitutionParticipatingToATestInstance(
                            selectedTestInstance, user.getInstitution());
                }
            }
            if (isConnectedUserAllowedToEditTestInstance == null) {
                isConnectedUserAllowedToEditTestInstance = false;
            }
        }
        return isConnectedUserAllowedToEditTestInstance;
    }

    public List<TestStepsInstance> rechargeAndGetSortedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("rechargeAndGetSortedTestStepsInstance");
        }
        getSelectedTestInstanceFromPage();
        selectedTestInstance = reloadTestInstance(selectedTestInstance);
        if (selectedTestInstance != null) {
            List<TestStepsInstance> listtmp = selectedTestInstance.getTestStepsInstanceList();
            if (listtmp != null) {
                Collections.sort(listtmp);
                List<TestStepsInstance> list = new ArrayList<TestStepsInstance>();
                for (TestStepsInstance tsi : listtmp) {
                    EntityManager entityManager = EntityManagerService.provideEntityManager();
                    TestStepsInstance tss = entityManager.find(TestStepsInstance.class, tsi.getId());
                    list.add(tss);
                }
                return list;
            }
        }
        return null;
    }

    public StatusStats getProgress() {
        List<TestStepsInstance> testStepsInstances = rechargeAndGetSortedTestStepsInstance();
        StatusStats stats = new StatusStats(testStepsInstances.size());

        for (TestStepsInstance testStepsInstance : testStepsInstances) {
            if (testStepsInstance.getTestStepsInstanceStatus().equals(TestStepsInstanceStatus.getSTATUS_DONE())) {
                stats.addDone();
            }
            if (testStepsInstance.getTestStepsInstanceStatus().equals(TestStepsInstanceStatus.getSTATUS_FAILED())) {
                stats.addFailed();
            }
            if (testStepsInstance.getTestStepsInstanceStatus().equals(TestStepsInstanceStatus.getSTATUS_SKIPPED())) {
                stats.addSkipped();
            }
        }
        return stats;
    }

    public List<Status> getTestInstanceStatusList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceStatusList");
        }
        getSelectedTestInstanceFromPage();
        List<Status> statusList = new ArrayList<Status>();
        statusList.add(Status.getSTATUS_STARTED());
        statusList.add(Status.getSTATUS_PAUSED());
        statusList.add(Status.getSTATUS_COMPLETED());
        statusList.add(Status.getSTATUS_ABORTED());
        if (TestingSession.getSelectedTestingSession().getIsCriticalStatusEnabled()) {
            statusList.add(Status.getSTATUS_CRITICAL());
        }
        if (Role.isLoggedUserAdmin() || MonitorInSession.isConnectedUserMonitorForSelectedSession()) {
            statusList.add(Status.getSTATUS_VERIFIED());
            statusList.add(Status.getSTATUS_PARTIALLY_VERIFIED());
            statusList.add(Status.getSTATUS_FAILED());
        }
        if (!Role.isLoggedUserAdmin() && MonitorInSession.isConnectedUserMonitorForSelectedSession()) {
            statusList.remove(Status.getSTATUS_ABORTED());
        }
        if (!statusList.contains(selectedTestInstance.getLastStatus())) {
            statusList.add(selectedTestInstance.getLastStatus());
        }
        return statusList;
    }

    public Actor getActorForTestStepsInstanceInitiator(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorForTestStepsInstanceInitiator");
        }

        if (inTestStepsInstance != null) {
            TestInstanceParticipants testInstanceParticipants = TestInstanceParticipants
                    .getTestInstanceParticipantsByTestInstanceBySiSByRoleInTest(
                            TestInstance.getTestInstanceByTestStepsInstance(inTestStepsInstance),
                            inTestStepsInstance.getSystemInSessionInitiator(), inTestStepsInstance.getTestSteps()
                                    .getTestRolesInitiator().getRoleInTest());
            if (testInstanceParticipants != null) {

                return testInstanceParticipants.getActorIntegrationProfileOption().getActorIntegrationProfile()
                        .getActor();
            }
        }
        return null;
    }

    public Actor getActorForTestStepsInstanceResponder(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorForTestStepsInstanceResponder");
        }
        if (inTestStepsInstance != null) {
            TestInstanceParticipants testInstanceParticipants = TestInstanceParticipants
                    .getTestInstanceParticipantsByTestInstanceBySiSByRoleInTest(
                            TestInstance.getTestInstanceByTestStepsInstance(inTestStepsInstance),
                            inTestStepsInstance.getSystemInSessionResponder(), inTestStepsInstance.getTestSteps()
                                    .getTestRolesResponder().getRoleInTest());
            if (testInstanceParticipants != null) {
                return testInstanceParticipants.getActorIntegrationProfileOption().getActorIntegrationProfile()
                        .getActor();
            }
        }
        return null;
    }

    public String showTestInstance(TestInstance inTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTestInstance");
        }

        selectedTestInstance = TestInstance.getTestInstanceWithAllAttributes(inTestInstance);
        if (selectedTestInstance.getExecutionStatus() == null) {
            selectedTestInstance.setExecutionStatus(testInstanceExecutionStatusDAO
                    .getTestInstanceExecutionStatusByEnum(TestInstanceExecutionStatusEnum.ACTIVE));
            selectedTestInstance = testInstanceDAO.mergeTestInstance(selectedTestInstance);
        }

        String returnUrl;

        if (selectedTestInstance.getTest().isTypeInteroperability()) {
            returnUrl = "/testingITB/test/test/TestInstance.xhtml?id=" + selectedTestInstance.getId();
        } else {
            returnUrl = "/testing/test/test/TestInstance.xhtml?id=" + selectedTestInstance.getId();// DONE
        }
        return returnUrl;
    }

    public String showTestInstance(TestInstance inTestInstance, String backMessage, String backPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTestInstance");
        }
        Contexts.getSessionContext().set("backPage", backPage);
        Contexts.getSessionContext().set("backMessage", backMessage);
        return showTestInstance(inTestInstance);
    }

    public void redirectToTestInstancePage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("redirectToTestInstancePage");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String id = fc.getExternalContext().getRequestParameterMap().get("id");
        Redirect redirect = Redirect.instance();
        Contexts.getSessionContext().set("backPage", null);
        Contexts.getSessionContext().set("backMessage", null);
        if (id != null) {
            redirect.setParameter("id", id);
            EntityManager em = EntityManagerService.provideEntityManager();
            TestInstance testInstance = em.find(TestInstance.class, Integer.valueOf(id));
            if (testInstance.getTest().isTypeInteroperability()) {
                redirect.setViewId("/testingITB/test/test/TestInstance.xhtml");
            } else {
                redirect.setViewId("/testing/test/test/TestInstance.xhtml"); // DONE
            }
        } else {
            redirect.setViewId("/testing/test/cat.xhtml");// DONE
        }
        redirect.execute();
    }

    public boolean renderAllStepsEditionButtons() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("renderAllStepsEditionButtons");
        }
        if (selectedTestInstance != null) {
            if (!selectedTestInstance.isOrchestrated()) {
                return true;
            }
            List<TestStepsInstance> list = selectedTestInstance.getTestStepsInstanceList();
            if (list != null) {
                for (TestStepsInstance testStepsInstance : list) {
                    if (testStepsInstance.getTestStepsInstanceStatus().equals(
                            TestStepsInstanceStatus.getSTATUS_DISACTIVATED())
                            || testStepsInstance.getTestStepsInstanceStatus().equals(
                            TestStepsInstanceStatus.getSTATUS_ACTIVATED())) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public void validateAllSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateAllSteps");
        }
        changeStatusForSteps(Status.getSTATUS_VERIFIED());
    }

    public void resetAllSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetAllSteps");
        }
        changeStatusForSteps(Status.getSTATUS_STARTED());
    }

    public void failAllSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("failAllSteps");
        }
        changeStatusForSteps(Status.getSTATUS_FAILED());
    }

    public void markAllStepsAsDone() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("markAllStepsAsDone");
        }
        changeTestStepsInstanceStatusForSteps(TestStepsInstanceStatus.getSTATUS_DONE());
    }

    public void markAllStepsAsActivated() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("markAllStepsAsActivated");
        }
        changeTestStepsInstanceStatusForSteps(TestStepsInstanceStatus.getSTATUS_ACTIVATED());
    }

    public void markAllStepsAsSkipped() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("markAllStepsAsSkipped");
        }
        changeTestStepsInstanceStatusForSteps(TestStepsInstanceStatus.getSTATUS_SKIPPED());
    }

    public void markAllStepsAsFailed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("markAllStepsAsFailed");
        }
        changeTestStepsInstanceStatusForSteps(TestStepsInstanceStatus.getSTATUS_FAILED());
    }

    public void changeTestStepsInstanceStatusForSteps(TestStepsInstanceStatus inTestStepsInstanceStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeTestStepsInstanceStatusForSteps");
        }
        if ((selectedTestInstance != null) && (inTestStepsInstanceStatus != null)) {
            List<TestStepsInstance> list = selectedTestInstance.getTestStepsInstanceList();
            if (list != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                for (TestStepsInstance testStepsInstance : list) {
                    testStepsInstance.setTestStepsInstanceStatus(inTestStepsInstanceStatus);
                    testStepsInstance = em.merge(testStepsInstance);
                }
                em.flush();
            }
        }
    }

    private void changeStatusForSteps(Status inStatus) {
        if ((selectedTestInstance != null) && (inStatus != null)) {
            List<TestStepsInstance> list = selectedTestInstance.getTestStepsInstanceList();
            if (list != null) {
                for (TestStepsInstance testStepsInstance : list) {
                    testStepsInstance.setStatus(inStatus);
                    EntityManager em = EntityManagerService.provideEntityManager();
                    testStepsInstance = em.merge(testStepsInstance);
                }
            }
        }
    }

    public String getProxyLink(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyLink");
        }
        // "/searchMessageStep.seam?id="
        return ProxyManager.getGazelleProxyURL() + ProxyManager.getGazelleProxyStep() + inTestStepsInstance.getId();
    }

    private void getSelectedTestInstanceFromPage() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String testId = fc.getExternalContext().getRequestParameterMap().get("id");
        if (testId != null) {
            selectedTestInstance = getTestInstanceById(Integer.valueOf(testId));
        }
    }

    public String getTestInstanceIdFromPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceIdFromPage");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String id = fc.getExternalContext().getRequestParameterMap().get("id");
        if (id == null) {
            id = fc.getExternalContext().getRequestParameterMap().get("globalFrom:testInstanceIdhiddenInput");
            if (id == null) {
                if ((selectedTestInstance != null) && (selectedTestInstance.getId() != null)) {
                    id = selectedTestInstance.getId().toString();
                } else {
                    LOG.error("no valid id found for test instance...");
                }
            }
        }
        return id;
    }

    private TestInstance getTestInstanceById(Integer testInstanceId) {
        if (testInstanceId != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            TestInstance testInstance = entityManager.find(TestInstance.class, testInstanceId);
            return TestInstance.getTestInstanceWithAllAttributes(testInstance);
        }
        return null;
    }

    public void updateSelectedStatusValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedStatusValue");
        }
        if (selectedTestInstance != null) {
            selectedStatus = selectedTestInstance.getLastStatus();
        }
    }

    public boolean isAllTestStepsInstanceFailedByMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllTestStepsInstanceFailedByMonitor");
        }
        return isAllTestStepsInstanceWithTheGivenStatus(Status.getSTATUS_FAILED());
    }

    public boolean isAllTestStepsInstanceVerifiedByMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllTestStepsInstanceVerifiedByMonitor");
        }
        return isAllTestStepsInstanceWithTheGivenStatus(Status.getSTATUS_VERIFIED());
    }

    public boolean isAllTestStepsInstanceDone() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllTestStepsInstanceDone");
        }
        return isAllTestStepsInstanceWithTheGivenTestStepsInstanceStatus(TestStepsInstanceStatus.getSTATUS_DONE());
    }

    public boolean isAllTestStepsInstanceSkipped() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllTestStepsInstanceSkipped");
        }
        return isAllTestStepsInstanceWithTheGivenTestStepsInstanceStatus(TestStepsInstanceStatus.getSTATUS_SKIPPED());
    }

    public boolean isAllTestStepsInstanceFailed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllTestStepsInstanceFailed");
        }
        return isAllTestStepsInstanceWithTheGivenTestStepsInstanceStatus(TestStepsInstanceStatus.getSTATUS_FAILED());
    }

    private boolean isAllTestStepsInstanceWithTheGivenTestStepsInstanceStatus(
            TestStepsInstanceStatus inTestStepsInstanceStatus) {
        if (selectedTestInstance != null) {
            List<TestStepsInstance> testStepsInstanceList = selectedTestInstance.getTestStepsInstanceList();
            if (testStepsInstanceList != null) {
                for (TestStepsInstance testStepsInstance : testStepsInstanceList) {
                    if (!testStepsInstance.getTestStepsInstanceStatus().equals(inTestStepsInstanceStatus)) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean isAllTestStepsInstanceWithTheGivenStatus(Status inStatus) {
        if (selectedTestInstance != null) {
            List<TestStepsInstance> testStepsInstanceList = selectedTestInstance.getTestStepsInstanceList();
            if (testStepsInstanceList != null) {
                for (TestStepsInstance testStepsInstance : testStepsInstanceList) {
                    if ((testStepsInstance.getStatus() == null) || !testStepsInstance.getStatus().equals(inStatus)) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public void initializeFailExplanation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeFailExplanation");
        }
        this.failExplanation = "";
        this.selectedStatus = Status.getSTATUS_FAILED();
    }

    public boolean selectedStatusIsFailed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectedStatusIsFailed");
        }
        boolean res = false;
        if (this.selectedStatus != null) {
            if (this.selectedStatus.equals(Status.getSTATUS_FAILED())) {
                res = true;
            }
        }

        return res;
    }

    public void persistSelectedTestInstanceIsNotFailed(boolean bb) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedTestInstanceIsNotFailed");
        }
        if (this.selectedStatus.equals(Status.getSTATUS_FAILED())) {
            this.initializeFailExplanation();
        } else {
            this.persistSelectedTestInstance(false);
        }
    }

    public boolean verifyIfStepsIsSecured(TestStepsInstance tsi) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("verifyIfStepsIsSecured");
        }
        if (tsi == null) {
            return false;
        }
        if (tsi.getVersionedTestSteps() == null) {
            return false;
        }
        if (tsi.getVersionedTestSteps().getSecured() == null) {
            return false;
        }
        return tsi.getVersionedTestSteps().getSecured();
    }

    public boolean simulatorParticipate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("simulatorParticipate");
        }
        boolean simpar = false;
        if (this.selectedTestInstance != null) {
            if (this.selectedTestInstance.getTestStepsInstanceList() != null) {
                for (TestStepsInstance tsi : this.selectedTestInstance.getTestStepsInstanceList()) {
                    if (tsi.getSystemInSessionInitiator() != null) {
                        if (tsi.getSystemInSessionInitiator() instanceof SimulatorInSession) {
                            simpar = true;
                            return simpar;
                        }
                    }
                    if (tsi.getSystemInSessionResponder() != null) {
                        if (tsi.getSystemInSessionResponder() instanceof SimulatorInSession) {
                            simpar = true;
                            return simpar;
                        }
                    }
                }
            }
        }
        return simpar;
    }

    /**
     * The logged in monitor claims the selected test instance
     */
    public void claimTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("claimTestInstance");
        }
        MonitorInSession monitorInSession = MonitorInSession.getActivatedMonitorInSessionForATestingSessionByUser(
                TestingSession.getSelectedTestingSession(), User.loggedInUser());
        if (monitorInSession != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedTestInstance.setMonitorInSession(monitorInSession);
            entityManager.merge(selectedTestInstance);
            entityManager.flush();
        } else {
            LOG.error("The logged in user is not monitor for this session");
        }
    }

    public void releaseTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("releaseTestInstance");
        }
        if (this.selectedTestInstance != null) {
            MonitorInSession monitorInSession = MonitorInSession.getActivatedMonitorInSessionForATestingSessionByUser(
                    TestingSession.getSelectedTestingSession(), User.loggedInUser());
            if (monitorInSession != null) {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                selectedTestInstance.setMonitorInSession(null);
                entityManager.merge(selectedTestInstance);
                entityManager.flush();
            } else {
                LOG.error("The logged in user is not monitor for this session");
            }
        }
    }

    public String getRowSpanForThisTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRowSpanForThisTest");
        }
        if (this.simulatorParticipate()) {
            return "4";
        }
        return "3";
    }

    public boolean canViewSequenceDiagram() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canViewSequenceDiagram");
        }
        if (this.selectedTestInstance != null) {
            if (this.selectedTestInstance.getTestStepsInstanceList() != null) {
                if (this.selectedTestInstance.getTestStepsInstanceList().size() < TestInstance.MAX_STEPS_NUMBER_TO_DISPLAY_IN_SEQUENCE_DIAGRAM) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean canValidateAllSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canValidateAllSteps");
        }
        if (this.selectedTestInstance != null) {
            if (this.selectedTestInstance.getTestStepsInstanceList() != null) {
                for (TestStepsInstance tsi : this.selectedTestInstance.getTestStepsInstanceList()) {
                    if (tsi.getStatus() == null) {
                        return true;
                    }
                    if (!tsi.getStatus().equals(Status.getSTATUS_VERIFIED())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canResetAllSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canResetAllSteps");
        }
        if (this.selectedTestInstance != null) {
            if (this.selectedTestInstance.getTestStepsInstanceList() != null) {
                for (TestStepsInstance tsi : this.selectedTestInstance.getTestStepsInstanceList()) {
                    if (tsi.getStatus() == null) {
                        return false;
                    }
                    if (!tsi.getStatus().equals(Status.getSTATUS_STARTED())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canMakeDoneAllSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canMakeDoneAllSteps");
        }
        if (this.selectedTestInstance != null) {
            if (this.selectedTestInstance.getTestStepsInstanceList() != null) {
                for (TestStepsInstance tsi : this.selectedTestInstance.getTestStepsInstanceList()) {
                    if (tsi.getTestStepsInstanceStatus() == null) {
                        return true;
                    }
                    if (!tsi.getTestStepsInstanceStatus().equals(TestStepsInstanceStatus.getSTATUS_DONE())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canMakeActivatedAllSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canMakeActivatedAllSteps");
        }
        if (this.selectedTestInstance != null) {
            if (this.selectedTestInstance.getTestStepsInstanceList() != null) {
                for (TestStepsInstance tsi : this.selectedTestInstance.getTestStepsInstanceList()) {
                    if (tsi.getTestStepsInstanceStatus() == null) {
                        return true;
                    }
                    if (!tsi.getTestStepsInstanceStatus().equals(TestStepsInstanceStatus.getSTATUS_ACTIVATED())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void updateSelectedCII() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedCII");
        }
        if (this.selectedContextualInformationInstance != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            this.selectedContextualInformationInstance = entityManager
                    .merge(this.selectedContextualInformationInstance);
            entityManager.flush();
        }
    }

    public boolean canEditCIIAsInput(ContextualInformationInstance currentCII, TestStepsInstance tsi) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditCIIAsInput");
        }
        List<ContextualInformation> loutput = null;
        if (tsi != null) {
            if (!(tsi.getTestStepsInstanceStatus().equals(TestStepsInstanceStatus.getSTATUS_DISACTIVATED()))
                    && !(tsi.getTestStepsInstanceStatus().equals(TestStepsInstanceStatus.getSTATUS_ACTIVATED_SIMU()))) {
                return false;
            }
            if (this.selectedTestInstance != null) {
                List<TestSteps> lts = new ArrayList<TestSteps>();
                try {
                    lts = this.selectedTestInstance.getTest().getTestStepsListAntecedent(tsi.getTestSteps());
                } catch (Exception e) {
                    LOG.error("", e);
                }
                loutput = new ArrayList<ContextualInformation>();
                for (TestSteps ts : lts) {
                    loutput.addAll(ts.getOutputContextualInformationList());
                }
            }

            if (loutput != null && loutput.contains(currentCII.getContextualInformation())) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void saveTestInstanceIfNotFailedStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveTestInstanceIfNotFailedStatus");
        }
        if (this.selectedTestInstance != null) {
            if (this.selectedStatus != null) {
                if (this.selectedStatus.equals(Status.getSTATUS_FAILED())) {
                    if (!this.selectedStatus.equals(this.selectedTestInstance.getLastStatus())) {
                        return;
                    }
                }
            }
            this.persistSelectedTestInstance();
        }
    }

    // ---------- related to configurations for sim

    public void updateSelectedTestInstanceStatusToFailed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedTestInstanceStatusToFailed");
        }
        if (this.failExplanation != null) {
            if (!this.failExplanation.equals("")) {
                this.updateSelectedTestInstanceStatus(Status.getSTATUS_FAILED());
                this.failExplanation = null;
                return;
            }
        }
        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "You have to specify an explanation if the test fail.");
        this.setSelectedStatus(this.selectedTestInstance.getLastStatus());
    }

    public void getDicomSCPConfigurationsForSelectedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDicomSCPConfigurationsForSelectedTestStepsInstance");
        }
        this.dicomSCPConfigurationsList = this.getConfigurationsForAType(DicomSCPConfiguration.class,
                this.selectedTestStepsInstance.getSystemInSessionInitiator(), this
                        .getActorForTestStepsInstanceResponder(selectedTestStepsInstance),
                this.selectedTestStepsInstance.getTestSteps().getWstransactionUsage());
    }

    public void getDicomSCUConfigurationsForSelectedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDicomSCUConfigurationsForSelectedTestStepsInstance");
        }
        this.dicomSCUConfigurationsList = this.getConfigurationsForAType(DicomSCUConfiguration.class,
                this.selectedTestStepsInstance.getSystemInSessionInitiator(), this
                        .getActorForTestStepsInstanceResponder(selectedTestStepsInstance),
                this.selectedTestStepsInstance.getTestSteps().getWstransactionUsage());
    }

    public void getHL7InitiatorConfigurationsForSelectedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7InitiatorConfigurationsForSelectedTestStepsInstance");
        }
        this.hl7InitiatorConfigurationsList = this.getConfigurationsForAType(HL7V2InitiatorConfiguration.class,
                this.selectedTestStepsInstance.getSystemInSessionResponder(), this
                        .getActorForTestStepsInstanceResponder(selectedTestStepsInstance),
                this.selectedTestStepsInstance.getTestSteps().getWstransactionUsage());
    }

    public void getHL7ResponderConfigurationsForSelectedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7ResponderConfigurationsForSelectedTestStepsInstance");
        }
        this.hl7ResponderConfigurationsList = this.getConfigurationsForAType(HL7V2ResponderConfiguration.class,
                this.selectedTestStepsInstance.getSystemInSessionResponder(), this
                        .getActorForTestStepsInstanceResponder(selectedTestStepsInstance),
                this.selectedTestStepsInstance.getTestSteps().getWstransactionUsage());
    }

    public void getHL7V3InitiatorConfigurationsForSelectedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7V3InitiatorConfigurationsForSelectedTestStepsInstance");
        }
        this.hl7V3InitiatorConfigurationsList = this.getConfigurationsForAType(HL7V3InitiatorConfiguration.class,
                this.selectedTestStepsInstance.getSystemInSessionResponder(), this
                        .getActorForTestStepsInstanceResponder(selectedTestStepsInstance),
                this.selectedTestStepsInstance.getTestSteps().getWstransactionUsage());
    }

    public void getHL7V3ResponderConfigurationsForSelectedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7V3ResponderConfigurationsForSelectedTestStepsInstance");
        }
        this.hl7V3ResponderConfigurationsList = this.getConfigurationsForAType(HL7V3ResponderConfiguration.class,
                this.selectedTestStepsInstance.getSystemInSessionResponder(), this
                        .getActorForTestStepsInstanceResponder(selectedTestStepsInstance),
                this.selectedTestStepsInstance.getTestSteps().getWstransactionUsage());
    }

    public void getWSConfigurationsForSelectedTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWSConfigurationsForSelectedTestStepsInstance");
        }
        this.webServicesConfigurationsList = this.getConfigurationsForAType(WebServiceConfiguration.class,
                this.selectedTestStepsInstance.getSystemInSessionResponder(), this
                        .getActorForTestStepsInstanceResponder(selectedTestStepsInstance),
                this.selectedTestStepsInstance.getTestSteps().getWstransactionUsage());
    }

    public void getSyslogConfigurationsForTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSyslogConfigurationsForTestStepsInstance");
        }
        this.syslogConfigurationsList = this.getConfigurationsForAType(SyslogConfiguration.class,
                this.selectedTestStepsInstance.getSystemInSessionResponder(), this
                        .getActorForTestStepsInstanceResponder(selectedTestStepsInstance),
                this.selectedTestStepsInstance.getTestSteps().getWstransactionUsage());
    }

    public void initializeSelectedResponderConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeSelectedResponderConfiguration");
        }
        if (this.selectedTestStepsInstance != null) {
            if (this.selectedResponderConfiguration == null) {
                this.selectedResponderConfiguration = this
                        .extractAbstractConfigurationFromTestStepsInstance(selectedTestStepsInstance);
            }
        } else {

        }
    }

    public boolean compareSelectedResponderConfiguration(AbstractConfiguration ac) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compareSelectedResponderConfiguration");
        }
        if (this.selectedResponderConfiguration != null) {
            if (ac != null) {
                if (this.selectedResponderConfiguration.getId().equals(ac.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean responderConfigurationIsUnique() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("responderConfigurationIsUnique");
        }
        int i = 0;
        if (this.dicomSCPConfigurationsList != null) {
            i += this.dicomSCPConfigurationsList.size();
        }
        if (this.dicomSCUConfigurationsList != null) {
            i += this.dicomSCUConfigurationsList.size();
        }
        if (this.hl7InitiatorConfigurationsList != null) {
            i += this.hl7InitiatorConfigurationsList.size();
        }
        if (this.hl7ResponderConfigurationsList != null) {
            i += this.hl7ResponderConfigurationsList.size();
        }
        if (this.hl7V3InitiatorConfigurationsList != null) {
            i += this.hl7V3InitiatorConfigurationsList.size();
        }
        if (this.hl7V3ResponderConfigurationsList != null) {
            i += this.hl7V3ResponderConfigurationsList.size();
        }
        if (this.webServicesConfigurationsList != null) {
            i += this.webServicesConfigurationsList.size();
        }
        if (this.syslogConfigurationsList != null) {
            i += this.syslogConfigurationsList.size();
        }
        if (i == 1) {
            return true;
        }
        return false;
    }

    public boolean hasTocOnfigureTestStepsInstance(TestStepsInstance currentTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasTocOnfigureTestStepsInstance");
        }
        List<AbstractConfiguration> lac = this.getConfigurationsForAType(AbstractConfiguration.class,
                currentTestStepsInstance.getSystemInSessionResponder(), this
                        .getActorForTestStepsInstanceResponder(currentTestStepsInstance), currentTestStepsInstance
                        .getTestSteps().getWstransactionUsage());
        if ((lac != null) && (lac.size() > 1)) {
            return true;
        }
        return false;
    }

    public boolean canResetSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canResetSteps");
        }
        boolean result = false;
        if ((this.selectedTestInstance != null) && (this.selectedTestInstance.isOrchestrated())) {
            if (this.selectedTestInstance.getLastStatus().equals(Status.getSTATUS_STARTED())
                    || this.selectedTestInstance.getLastStatus().equals(Status.getSTATUS_CRITICAL())
                    || this.selectedTestInstance.getLastStatus().equals(Status.getSTATUS_COMPLETED())
                    || this.selectedTestInstance.getLastStatus().equals(Status.getSTATUS_PARTIALLY_VERIFIED())) {
                result = true;
            }
        }
        return result;
    }

    // Test step data stuff

    public void resetCurrentTestInstanceSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetCurrentTestInstanceSteps");
        }
        if (this.selectedTestInstance != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            this.selectedTestInstance.initTestInstance(em);
            LocalGPClient.startTestInstanceActivating(this.selectedTestInstance, em);
        }
    }

    public void displayFileStepsOnScreen(TestStepsData tsd, boolean download) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayFileStepsOnScreen");
        }
        String filepath = tsd.getCompleteFilePath();
        net.ihe.gazelle.common.util.DocumentFileUpload.showFile(filepath, tsd.getValue(), download);
    }

    public String displayTitleForTestStepsDate(TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayTitleForTestStepsDate");
        }
        String res = "";
        if (tsd != null) {
            if (tsd.getDataType() != null) {
                res = res + tsd.getDataType().getKeyword() + " : ";
            }
            if (tsd.getValue() != null) {
                res = res + tsd.getValue();
                if (tsd.getComment() != null) {
                    if (!tsd.getComment().equals("")) {
                        res = res + " ( " + tsd.getComment() + " ) ";
                    }
                }
            }
        }
        return res;
    }

    public void deletedSelectedTestStepsData(TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deletedSelectedTestStepsData");
        }
        if (tsd != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            tsd = entityManager.find(TestStepsData.class, tsd.getId());

            if (urlRelatedToSelectedTestStepsInstance) {
                this.selectedTestStepsInstance.removeTestStepsDataList(tsd);
                this.selectedTestStepsInstance = entityManager.merge(this.selectedTestStepsInstance);
            } else {
                this.selectedTestInstance.removeTestStepsDataList(tsd);
                this.selectedTestInstance = entityManager.merge(this.selectedTestInstance);
                entityManager.flush();
            }

            entityManager.flush();
            deleteFileFromTestStepsData(tsd);
            entityManager.remove(tsd);
            entityManager.flush();
        }
    }

    public void deleteFileFromTestStepsData(TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteFileFromTestStepsData");
        }
        if (tsd.getDataType() != null) {
            if (tsd.getDataType().getKeyword().equals(DataType.FILE_DT)) {
                String pathToFile = tsd.getCompleteFilePath();
                java.io.File ftodel = new java.io.File(pathToFile);
                if (ftodel.exists()) {
                    if (ftodel.delete()) {
                        LOG.info("ftodel deleted");
                    } else {
                        LOG.error("Failed to delete ftodel");
                    }
                }
            }
        }
    }

    public void initDeleteUrlFor(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initDeleteUrlFor");
        }
        initUrlFor(o);
    }

    public void initAddUrlFor(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initAddUrlFor");
        }
        initUrlFor(o);
        initializeDownloadFileAndAddUrl();
        valueURL = "http://";
    }

    private void initUrlFor(Object o) {
        if (o instanceof TestStepsInstance) {
            urlRelatedToSelectedTestStepsInstance = true;
            selectedTestStepsInstance = (TestStepsInstance) o;
        } else {
            urlRelatedToSelectedTestStepsInstance = false;
        }
    }

    public List<TestStepsData> getDataListForDelete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDataListForDelete");
        }
        if (urlRelatedToSelectedTestStepsInstance) {
            if (selectedTestStepsInstance == null) {
                return null;
            }
            return selectedTestStepsInstance.getTestStepsDataList();
        } else {
            if (selectedTestInstance == null) {
                return null;
            }
            return selectedTestInstance.getTestStepsDataList();
        }
    }

    public boolean canDelete(TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canDelete");
        }
        if (selectedTestInstance.getTestingSession().testingSessionClosedForUser()) {
            return false;
        }
        User user = User.loggedInUser();
        if (user.getRoles().contains(Role.getADMINISTRATOR_ROLE())) {
            return true;
        }
        return user.getUsername().equals(tsd.getLastModifierId());
    }

    public void initDeleteFor(Object tsi, TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initDeleteFor");
        }
        tsiLinkedToTsd = null;
        tiLinkedToTsd = null;
        if (tsi instanceof TestStepsInstance) {
            tsiLinkedToTsd = (TestStepsInstance) tsi;
        } else {
            tiLinkedToTsd = (TestInstance) tsi;
        }

        selectedTsd = tsd;
    }

    public void deleteTestStepsData() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestStepsData");
        }
        if (((tsiLinkedToTsd != null) || (tiLinkedToTsd != null)) && (selectedTsd != null)) {
            if (selectedTsd.getId() == null) {
                TestStepsInstance tsi = HQLReloader.reloadDetached(tsiLinkedToTsd);
                tsi.setComments(null);
                EntityManagerService.provideEntityManager().merge(tsi);
            } else {
                List<TestStepsData> testStepsDataList;
                Object toMerge = null;

                if (tsiLinkedToTsd != null) {
                    TestStepsInstance tsi = HQLReloader.reloadDetached(tsiLinkedToTsd);
                    testStepsDataList = tsi.getTestStepsDataList();
                    toMerge = tsi;
                } else {
                    TestInstance ti = HQLReloader.reloadDetached(tiLinkedToTsd);
                    testStepsDataList = ti.getTestStepsDataList();
                    toMerge = ti;
                }

                TestStepsData tsd = HQLReloader.reloadDetached(selectedTsd);

                int iDelete = -1;
                for (int i = 0; i < testStepsDataList.size(); i++) {
                    TestStepsData testStepsData = testStepsDataList.get(i);
                    if (testStepsData.getId().equals(tsd.getId())) {
                        iDelete = i;
                    }
                }
                if (iDelete >= 0) {
                    deleteFileFromTestStepsData(tsd);
                    testStepsDataList.remove(iDelete);
                    EntityManagerService.provideEntityManager().merge(toMerge);
                }
            }
        }
    }

    public void showAddItems(TestStepsInstance testStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showAddItems");
        }
        setShowUrl(false);
        setShowComment(true);
        boolean showingTestsFor = isShowingAddItems(testStepsInstance);
        showingTestsFor = !showingTestsFor;
        addsShown.put(testStepsInstance.getId(), Boolean.valueOf(showingTestsFor));
    }

    public void unShow(TestStepsInstance testStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unShow");
        }
        setShowUrl(false);
        setShowComment(false);
        addsShown.remove(testStepsInstance.getId());
        addsUrl.remove(testStepsInstance.getId());
    }

    public boolean isShowingAddItems(TestStepsInstance testStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowingAddItems");
        }
        Boolean curValue = addsShown.get(testStepsInstance.getId());
        if (curValue == null) {
            return false;
        }
        return curValue;
    }

    public void showAddUrl(TestStepsInstance testStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showAddUrl");
        }
        setShowUrl(true);
        setShowComment(false);
        boolean showingTestsFor = isShowingAddUrl(testStepsInstance);
        showingTestsFor = !showingTestsFor;
        addsUrl.put(testStepsInstance.getId(), Boolean.valueOf(showingTestsFor));
    }

    public boolean isShowingAddUrl(TestStepsInstance testStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowingAddUrl");
        }
        Boolean curValue = addsUrl.get(testStepsInstance.getId());
        if (curValue == null) {
            return false;
        }
        return curValue;
    }

    public String getCommentToAdd() {
        if (commentToAdd != null) {
            commentToAdd = commentToAdd.trim();
        }
        return commentToAdd;
    }

    public void setCommentToAdd(String commentToAdd) {
        this.commentToAdd = commentToAdd;
    }

    public void addComment(TestStepsInstance testStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addComment");
        }
        TestStepsData tsd = null;
        tsd = new TestStepsData();
        tsd.setValue(getCommentToAdd());
        tsd.setComment(null);
        tsd.setDataType(DataType.getDataTypeByKeyword(DataType.COMMENT));

        if (tsd != null) {
            persistTestStepsData(tsd);

            TestStepsInstance tsi = HQLReloader.reloadDetached(testStepsInstance);
            tsi.getTestStepsDataList().add(tsd);
            EntityManagerService.provideEntityManager().merge(testStepsInstance);

            setCommentToAdd(null);
            addsShown.put(tsi.getId(), Boolean.FALSE);
        }
    }

    public void addComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addComment");
        }
        getSelectedTestInstanceFromPage();
        if (commentTobeAdded.trim().length() > 0) {
            String comment = StringEscapeUtils.escapeHtml(commentTobeAdded);
            comment = comment.replace("\r\n", "<br/>");
            comment = comment.replace("\n", "<br/>");
            comment = comment.replace("\r", "<br/>");

            selectedTestInstance.addComment(comment);

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedTestInstance = entityManager.merge(selectedTestInstance);
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Your comment is empty !");
        }
        commentTobeAdded = null;
        setblockPostComment(true);
    }

    public void validateComment(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateComment");
        }
        String comment = (String) value;
        setCommentToAdd(comment);

        if (comment.length() == 0) {
            Severity severity = FacesMessage.SEVERITY_WARN;
            FacesMessage message = new FacesMessage(severity,
                    "Your comment is empty", null);
            FacesMessages.instance().add(message);
            throw new ValidatorException(message);
        }

        String httpPattern = "http[s]?://.+";

        Pattern regex = Pattern.compile(httpPattern, Pattern.DOTALL);
        Matcher regexMatcher = regex.matcher(comment);
        if (regexMatcher.find()) {
            LOG.warn("User comment contains one or more URL.");
            Severity severity = FacesMessage.SEVERITY_WARN;
            FacesMessage message = new FacesMessage(severity,
                    "Your comment contains one or more URL. Please use the 'add url' button", null);
            FacesMessages.instance().add(message);
            throw new ValidatorException(message);
        }
        if (comment.length() > 2000) {
            LOG.warn("User comment contains more than 2000 characters.");
            Severity severity = FacesMessage.SEVERITY_WARN;
            FacesMessage message = new FacesMessage(severity,
                    "Your comment must be shorter than 2000 characters.", null);
            FacesMessages.instance().add(message);
            throw new ValidatorException(message);
        }
    }

    public void addUrl(TestStepsInstance testStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addUrl");
        }
        TestStepsData tsd = null;
        String url = getCommentToAdd();
        if (url.matches("http[s]?://.+")) {
            String reg = "(\\(.+\\))";
            Pattern regex = Pattern.compile(reg, Pattern.DOTALL);
            Matcher regexMatcher = regex.matcher(url);
            if (regexMatcher.find()) {
                regexMatcher.group();
                url = regexMatcher.replaceAll("");
            }
            tsd = createURLTestStepsData(url, null);
        }
        if (tsd != null) {
            persistTestStepsData(tsd);

            TestStepsInstance tsi = HQLReloader.reloadDetached(testStepsInstance);
            tsi.getTestStepsDataList().add(tsd);
            EntityManagerService.provideEntityManager().merge(testStepsInstance);

            setCommentToAdd(null);
            addsUrl.put(tsi.getId(), Boolean.FALSE);
        }
    }

    public void persistTestStepsData(TestStepsData tsd) {
        EntityManagerService.provideEntityManager().persist(tsd);
    }

    public void validateURL(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateURL");
        }
        String httpPattern = "http[s]?://.+";
        String whiteSpacePattern = "\\s";
        String url = (String) value;
        setCommentToAdd(url);
        url = url.trim();
        String gssName = ApplicationManager.instance().getTlsName();
        String appName = ApplicationPreferenceManager.instance().getApplicationUrlBaseName();

        Pattern regex = Pattern.compile(whiteSpacePattern);
        Matcher regexMatcher = regex.matcher(url);

        if (!url.matches(httpPattern) || regexMatcher.find()) {
            getErrorMessage(url, "URL provided by user is invalid : ", "You must enter a valid URL !");
        } else {
            httpPattern = "http[s]?://.+(/proxy/messages).+(seam.?id).+";
            if (url.contains("proxy") && !url.matches(httpPattern)) {
                getErrorMessage(url, "URL provided is not a PROXY permanent link ! ", "You must enter a valid PROXY permanent link !");
            }
            httpPattern = "http[s]?://.+(/EVSClient).+([&|\\?]oid).+";
            if (url.contains("EVSClient") && !url.matches(httpPattern)) {
                getErrorMessage(url, "URL provided is not an EVSClient permanent link ! ", "You must enter a valid EVSClient permanent link !");
            }
            httpPattern = "http[s]?://.+(/" + gssName
                    + "){1}((/testinstance/view)|(/details/connection)|(/questionnaire/viewer)|(/syslog/view)).+(seam.?id).+";
            if (gssName != null && !gssName.isEmpty() && url.contains(gssName) && !url.contains("EVSClient") && !url.matches(httpPattern)) {
                getErrorMessage(url, "URL provided is not a GSS permanent link ! ", "You must enter a valid GSS permanent link !");
            }
            httpPattern = "http[s]?://.+(/" + appName + ")(/objects/sample).+";
            if (url.contains(appName + "/objects") && !url.matches(httpPattern)) {
                getErrorMessage(url, "URL provided is not a Sample permanent link ! ", "You must enter a valid Sample permanent link !");
            }
        }
    }

    private void getErrorMessage(String url, String logMessage, String facesMessage) {
        LOG.warn(new StringBuilder().append(logMessage).append(url).toString());
        Severity severity = FacesMessage.SEVERITY_WARN;
        FacesMessage message = new FacesMessage(severity, facesMessage, null);
        FacesMessages.instance().add(message);
        throw new ValidatorException(message);
    }

    public List<TestStepDataWrapper> getDataListFor(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDataListFor");
        }
        if (o == null) {
            return new ArrayList<TestStepDataWrapper>();
        }
        if (o instanceof TestStepsInstance) {

            List<TestStepsData> result = reloadTestStepInstance((TestStepsInstance) o).getTestStepsDataList();

            List<TestStepDataWrapper> resultWrapped = new ArrayList<TestStepDataWrapper>();

            if (result == null) {
                result = new ArrayList<TestStepsData>();
            }

            for (TestStepsData testStepsData : result) {
                resultWrapped.add(new TestStepDataWrapper(testStepsData));
            }

            if (StringUtils.isNotEmpty(((TestStepsInstance) o).getComments())) {
                String comments = ((TestStepsInstance) o).getComments();
                TestStepsData tsd = new TestStepsData(null, DataType.getDataTypeByKeyword(DataType.COMMENT), null,
                        comments);
                tsd.setLastChanged(((TestStepsInstance) o).getLastChanged());
                tsd.setLastModifierId(((TestStepsInstance) o).getLastModifierId());

                resultWrapped.add(new TestStepDataWrapper(tsd));
            }

            Collections.sort(resultWrapped);
            return resultWrapped;
        } else {
            List<TestStepsData> result = reloadTestInstance((TestInstance) o).getTestStepsDataList();
            List<TestStepDataWrapper> resultWrapped = new ArrayList<TestStepDataWrapper>();

            if (result == null) {
                result = new ArrayList<TestStepsData>();
            }

            for (TestStepsData testStepsData : result) {
                resultWrapped.add(new TestStepDataWrapper(testStepsData));
            }

            Collections.sort(resultWrapped);
            return resultWrapped;
        }
    }

    public void initializeDownloadFileAndAddUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeDownloadFileAndAddUrl");
        }
        this.valueURL = null;
        this.commentURL = null;
        this.commentFile = null;
    }

    public boolean commentOfTSDIsNotNull(TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("commentOfTSDIsNotNull");
        }
        if (tsd != null) {
            if (tsd.getComment() != null) {
                if (!tsd.getComment().equals("")) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> gePossibleObjectTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("gePossibleObjectTypes");
        }
        List<String> res = new ArrayList<String>();
        List<ObjectFileType> loft = ObjectFileType.getListOfAllObjectFileType();
        for (ObjectFileType objectFileType : loft) {
            res.add(objectFileType.getKeyword());
        }
        Collections.sort(res);
        return res;
    }

    public void listenerTestStepsDataValue(FileUploadEvent event) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listenerTestStepsDataValue");
        }
        UploadedFile item = event.getUploadedFile();
        this.fich = new File(item);
        this.fichName = item.getName();
    }

    public String addReturnIfNeeded() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addReturnIfNeeded");
        }
        if (this.counter == 2) {
            this.counter = 0;
            return "<br />";
        } else {
            this.counter = this.counter + 1;
            return "";
        }
    }

    public String getImageForUrl(TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getImageForUrl");
        }
        if (tsd != null) {
            if (tsd.getDataType() != null) {
                if (tsd.getDataType().getKeyword().equals(DataType.OBJECT_DT)) {
                    return "icons22/sample.gif";
                } else if (tsd.getDataType().getKeyword().equals(DataType.PROXY_DT)) {
                    return "proxy.gif";
                } else if (tsd.getDataType().getKeyword().equals(DataType.XDS_DT)) {
                    return "xds.gif";
                } else {
                    return "icons22/world.gif";
                }
            }
        }
        return "";
    }

    public void addNewFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewFile");
        }
        if ((this.fich != null) && (this.fichName != null) && !this.fichName.equals("")) {
            TestInstanceUploadManagerLocal testInstanceUploadManagerLocal = (TestInstanceUploadManagerLocal) Component
                    .getInstance("testInstanceUploadManager");
            TestStepsData tsd = testInstanceUploadManagerLocal.persistTestStepsDataFile(this.fichName, commentFile);
            testInstanceUploadManagerLocal.saveFileUploaded(tsd, new ByteArrayInputStream(this.fich.getData()));
            addTestStepsData(tsd);
        }
        this.fich = null;
        this.fichName = "";
    }

    public void addNewURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewURL");
        }
        TestStepsData tsd = persistTestStepsDataUrl();
        addTestStepsData(tsd);
    }

    private void addTestStepsData(TestStepsData tsd) {
        if (tsd != null) {
            TestInstanceUploadManagerLocal testInstanceUploadManagerLocal = (TestInstanceUploadManagerLocal) Component
                    .getInstance("testInstanceUploadManager");
            if (urlRelatedToSelectedTestStepsInstance) {
                testInstanceUploadManagerLocal.addTestStepsDataToTestStepsInstance(tsd,
                        selectedTestStepsInstance.getId());
            } else {
                testInstanceUploadManagerLocal.addTestStepsDataToTestInstance(tsd, selectedTestInstance.getId());
            }
        }
    }

    protected void addTestStepsDataToTestInstance(TestStepsData tsd) {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        reloadTestInstance();
        this.selectedTestInstance.addTestStepsData(tsd);
        this.selectedTestInstance = entityManager.merge(this.selectedTestInstance);
        entityManager.flush();
    }

    protected void reloadTestStepInstance() {
        this.selectedTestStepsInstance = reloadTestStepInstance(this.selectedTestStepsInstance);
    }

    protected TestStepsInstance reloadTestStepInstance(TestStepsInstance instance) {
        TestStepsInstanceQuery query = new TestStepsInstanceQuery();
        query.id().eq(instance.getId());
        return query.getUniqueResult();
    }

    protected void reloadTestInstance() {
        this.selectedTestInstance = reloadTestInstance(this.selectedTestInstance);
    }

    protected TestInstance reloadTestInstance(TestInstance instance) {
        if (instance != null && instance.getId() != null) {
            TestInstanceQuery query = new TestInstanceQuery();
            query.id().eq(instance.getId());
            return query.getUniqueResult();
        }
        return null;
    }

    private TestStepsData persistTestStepsDataUrl() {
        if ((this.valueURL == null) || (this.valueURL.equals(""))) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "The url value is null");
            return null;
        }

        if (!this.valueURL.matches("http[s]?://.+")) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "The url value is not good, you have to use a URL like this : http://gazelle.ihe.net");
            return null;
        }

        String value = valueURL;
        String comment = commentURL;

        TestStepsData tsd = createURLTestStepsData(value, comment);

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        tsd = entityManager.merge(tsd);
        entityManager.flush();
        return tsd;
    }

    public TestStepsData createURLTestStepsData(String value, String comment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createURLTestStepsData");
        }
        TestStepsData tsd = new TestStepsData();
        DataType selectedDataType = DataType.getDataTypeByKeyword(DataType.URL);
        List<DataType> ldt = DataType.getAllDataType();
        for (DataType dt : ldt) {
            String urlPrefix = TestStepsData.createURL("", dt);
            if ((StringUtils.trimToNull(urlPrefix) != null) && value.startsWith(urlPrefix)) {
                selectedDataType = dt;
                if (value.contains(DataType.PROXY_DT) || value.contains(DataType.EVS_DT) || value.contains(DataType.GSS_DT)
                        || value.contains("objects")) {
                    value = value.replace(urlPrefix, "");
                    break;
                }
            }
        }
        tsd.setValue(value);
        tsd.setComment(comment);
        tsd.setDataType(selectedDataType);
        return tsd;
    }

    // end of test step data stuff

    public void persistSelectedTSIWithModifiedComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedTSIWithModifiedComment");
        }
        if ((this.selectedTsd.getId() == null) && (selectedTestStepsInstance != null)) {
            selectedTestStepsInstance = HQLReloader.reloadDetached(selectedTestStepsInstance);
            selectedTestStepsInstance.setComments(null);

            persistTestStepsData(this.selectedTsd);
            selectedTestStepsInstance.getTestStepsDataList().add(selectedTsd);

            EntityManagerService.provideEntityManager().merge(this.selectedTestStepsInstance);
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        this.selectedTsd = HQLReloader.reloadDetached(selectedTsd);
        if (this.selectedTsd.isComment()) {
            this.selectedTsd.setValue(this.selectedComment);
        } else {
            this.selectedTsd.setComment(this.selectedComment);
        }
        this.selectedTsd = entityManager.merge(this.selectedTsd);
        entityManager.flush();
    }

    public void initSelectedComment(Object tsi, TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initSelectedComment");
        }
        if (tsi != null) {
            this.selectedTsd = tsd;
            if (tsd.isComment()) {
                this.selectedComment = tsd.getValue();
            } else {
                this.selectedComment = tsd.getComment();
            }
            if (tsi instanceof TestStepsInstance) {
                this.selectedTestStepsInstance = (TestStepsInstance) tsi;
            } else {
                this.selectedTestStepsInstance = null;
            }
            this.selectedCommentWasModified = false;
        }
    }

    public boolean testingSessionForTestInstanceClosedForUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testingSessionForTestInstanceClosedForUser");
        }
        if ((this.selectedTestInstance != null) && (this.selectedTestInstance.getTestingSession().isClosed())) {
            User user = User.loggedInUser();
            if ((!user.getRoles().contains(Role.getADMINISTRATOR_ROLE()))
                    && (!user.getRoles().contains(Role.getMONITOR_ROLE()))) {
                return true;
            }
        }
        return false;
    }

    public void claimTestInstance(TestInstance selectedTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("claimTestInstance");
        }
        MonitorInSession monitorInSession = MonitorInSession.getActivatedMonitorInSessionForATestingSessionByUser(
                TestingSession.getSelectedTestingSession(), User.loggedInUser());
        if (monitorInSession != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedTestInstance.setMonitorInSession(monitorInSession);
            entityManager.merge(selectedTestInstance);
            entityManager.flush();
        } else {
            LOG.error("The logged in user is not monitor for this session :"
                    + (User.loggedInUser() != null ? User.loggedInUser().getUsername() : ""));
        }
    }

    public void releaseTestInstance(TestInstance selectedTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("releaseTestInstance");
        }
        MonitorInSession monitorInSession = MonitorInSession.getActivatedMonitorInSessionForATestingSessionByUser(
                TestingSession.getSelectedTestingSession(), User.loggedInUser());
        if (monitorInSession != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedTestInstance.setMonitorInSession(null);
            entityManager.merge(selectedTestInstance);
            entityManager.flush();
        }
    }

    // --------------------------------------------

    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    public void setTestStepsInstanceMonitorStatusToVerified(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstanceMonitorStatusToVerified");
        }
        if (inTestStepsInstance != null) {
            inTestStepsInstance.setStatus(Status.getSTATUS_VERIFIED());
            persistTestStepsInstance(inTestStepsInstance);
        }
    }

    public void setTestStepsInstanceMonitorStatusToFailed(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstanceMonitorStatusToFailed");
        }
        if (inTestStepsInstance != null) {
            inTestStepsInstance.setStatus(Status.getSTATUS_FAILED());
            persistTestStepsInstance(inTestStepsInstance);
        }
    }

    public void setTestStepsInstanceMonitorStatusToStarted(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstanceMonitorStatusToStarted");
        }
        if (inTestStepsInstance != null) {
            inTestStepsInstance.setStatus(Status.getSTATUS_STARTED());
            persistTestStepsInstance(inTestStepsInstance);
        }
    }

    public void setTestStepsInstanceStatusToFailed(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstanceStatusToFailed");
        }
        this.updateTestStepsInstanceToNewStatus(inTestStepsInstance, TestStepsInstanceStatus.getSTATUS_FAILED());
    }

    public void setTestStepsInstanceStatusToSkipped(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstanceStatusToSkipped");
        }
        this.updateTestStepsInstanceToNewStatus(inTestStepsInstance, TestStepsInstanceStatus.getSTATUS_SKIPPED());
    }

    public void setTestStepsInstanceStatusToDone(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstanceStatusToDone");
        }
        this.updateTestStepsInstanceToNewStatus(inTestStepsInstance, TestStepsInstanceStatus.getSTATUS_DONE());
    }

    public void setTestStepsInstanceStatusToActivated(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstanceStatusToActivated");
        }
        this.updateTestStepsInstanceToNewStatus(inTestStepsInstance, TestStepsInstanceStatus.getSTATUS_ACTIVATED());
    }

    private void updateTestStepsInstanceToNewStatus(TestStepsInstance inTestStepsInstance, TestStepsInstanceStatus tsis) {
        if ((inTestStepsInstance != null)
                && (TestInstance.getTestInstanceByTestStepsInstance(inTestStepsInstance).isOrchestrated())
                && (inTestStepsInstance.getTestStepsInstanceStatus().equals(TestStepsInstanceStatus
                .getSTATUS_ACTIVATED()))) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            boolean testInstanceVerified = LocalGPClient.continueProcess(inTestStepsInstance, entityManager);
            LOG.warn("Fix this code, it should not get here, there's nothing to do");
            if (testInstanceVerified) {
                LOG.warn("Fix this code, it should not get here");
            }
        }
        if (inTestStepsInstance != null) {
            inTestStepsInstance.setTestStepsInstanceStatus(tsis);
            inTestStepsInstance = persistTestStepsInstance(inTestStepsInstance);
        }
        if ((inTestStepsInstance != null) && (inTestStepsInstance.getId() != null)) {
            try {
                ProxyManager.markTestStep(inTestStepsInstance.getId());
            } catch (RemoteException e) {
                LOG.error("", e);
            }
        }
    }

    public TestStepsInstance persistTestStepsInstance(TestStepsInstance inTestStepsInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTestStepsInstance");
        }
        if (inTestStepsInstance != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            inTestStepsInstance = entityManager.merge(inTestStepsInstance);
            return inTestStepsInstance;
        }
        return null;
    }

    public void launchSimulatorForTestStepsInstance(TestStepsInstance tsi) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("launchSimulatorForTestStepsInstance");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        SystemInSession systemInSessionInitiator = tsi.getSystemInSessionInitiator();
        SystemInSession systemInSessionResponder = tsi.getSystemInSessionResponder();
        SimulatorInSession simulatorInSessionInitiator = SimulatorInSession.class.cast(systemInSessionInitiator);
        tsi.setTestStepsInstanceStatus(TestStepsInstanceStatus.getSTATUS_ACTIVATED());
        tsi = entityManager.merge(tsi);
        entityManager.flush();

        Boolean bb = this.sendMessageIfItIsSimulatorAsInitiator(tsi, simulatorInSessionInitiator,
                systemInSessionResponder);
        if (bb != null) {
            if (bb) {
                this.setTestStepsInstanceStatusToDone(tsi);
            } else {
                this.setTestStepsInstanceStatusToFailed(tsi);
            }
        }
    }

    private Boolean sendMessageIfItIsSimulatorAsInitiator(TestStepsInstance testStepsInstance,
                                                          SimulatorInSession simulatorInSessionInitiator, SystemInSession systemInSessionResponder) {
        Boolean result = null;
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestInstance testInstance = TestInstance.getTestInstanceByTestStepsInstance(testStepsInstance);
        TestInstanceParticipants testInstanceParticipants = TestInstanceParticipants
                .getTestInstanceParticipantsByTestInstanceBySiSByRoleInTest(testInstance, systemInSessionResponder,
                        testStepsInstance.getTestSteps().getTestRolesResponder().getRoleInTest());
        if ((testInstance != null) && (testInstanceParticipants != null)) {
            try {

                String urlSimulatorWSDL = Simulator.class.cast(simulatorInSessionInitiator.getSystem()).getUrl();
                String messageType = testStepsInstance.getTestSteps().getMessageType();
                String testInstanceId = testInstance.getTestInstanceIdForSimulator();
                String testInstanceParticipantsId = testInstanceParticipants.getId().toString();
                net.ihe.gazelle.simulator.ws.Transaction trans = Gazelle2SimulatorClassConverter
                        .convertTransaction(testStepsInstance.getTestSteps().getTransaction());

                // ---------
                AbstractConfiguration ac = null;

                boolean proxyUsed = false;
                if (testInstance.getProxyUsed() != null) {
                    proxyUsed = testInstance.getProxyUsed().booleanValue();
                }

                ConfigurationForWS confForWS = Gazelle2SimulatorClassConverter.convertAbstractConfiguration(ac,
                        proxyUsed);
                // ---------
                net.ihe.gazelle.simulator.ws.ContextualInformationInstance[] linputCI = Gazelle2SimulatorClassConverter
                        .convertListContextualInformationInstanceToArray(testStepsInstance
                                .getInputContextualInformationInstanceList());
                net.ihe.gazelle.simulator.ws.ContextualInformationInstance[] loutputCI = Gazelle2SimulatorClassConverter
                        .convertListContextualInformationInstanceToArray(testStepsInstance
                                .getOutputContextualInformationInstanceList());

                // ----------

                Pair<net.ihe.gazelle.simulator.ws.ContextualInformationInstance[], Boolean> lresultCI = null;
                try {
                    lresultCI = ClientCommonSimulator.sendMessageToSimulator(urlSimulatorWSDL, messageType,
                            testInstanceId, testInstanceParticipantsId, trans, confForWS, linputCI, loutputCI);
                } catch (Exception e) {
                    LOG.error("", e);
                }

                if ((lresultCI != null) && (lresultCI.getObject1() != null)) {
                    for (net.ihe.gazelle.simulator.ws.ContextualInformationInstance cii : lresultCI.getObject1()) {
                        net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance ciigaz = Simulator2GazelleClassConverter
                                .convertContextualInformationInstance(cii);
                        net.ihe.gazelle.tm.gazelletest.model.definition.ContextualInformation cigaz = ciigaz
                                .getContextualInformation();
                        net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance ciigazTosave = testStepsInstance
                                .getContextualInformationOutputByContextualInformation(cigaz);
                        if (ciigazTosave != null) {
                            ciigazTosave.setValue(ciigaz.getValue());
                            ciigazTosave = entityManager.merge(ciigazTosave);
                            entityManager.flush();
                        }
                        this.updateInputOfListTestSteps(testStepsInstance, ciigazTosave, entityManager);
                    }
                }
                if (lresultCI != null) {
                    result = lresultCI.getObject2();
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
        return result;
    }

    private void updateInputOfListTestSteps(TestStepsInstance tsi,
                                            net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance ciigazTosave, EntityManager
                                                    em) {
        TestInstance ti = TestInstance.getTestInstanceByTestStepsInstance(tsi);
        for (TestStepsInstance tsiTemp : ti.getTestStepsInstanceList()) {
            if (tsiTemp.getTestSteps().getStepIndex().intValue() > tsi.getTestSteps().getStepIndex().intValue()) {
                for (net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance cii : tsiTemp
                        .getInputContextualInformationInstanceList()) {
                    if (cii.getContextualInformation().equals(ciigazTosave.getContextualInformation())) {
                        cii.setValue(ciigazTosave.getValue());
                        cii = em.merge(cii);
                        em.flush();
                    }
                }
            }
        }
    }

    public List<AbstractConfiguration> getHl7InitiatorConfigurationsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHl7InitiatorConfigurationsList");
        }
        return hl7InitiatorConfigurationsList;
    }

    // Configurations for simulators

    public void setHl7InitiatorConfigurationsList(List<AbstractConfiguration> hl7InitiatorConfigurationsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setHl7InitiatorConfigurationsList");
        }
        this.hl7InitiatorConfigurationsList = hl7InitiatorConfigurationsList;
    }

    public List<AbstractConfiguration> getHl7ResponderConfigurationsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHl7ResponderConfigurationsList");
        }
        return hl7ResponderConfigurationsList;
    }

    public void setHl7ResponderConfigurationsList(List<AbstractConfiguration> hl7ResponderConfigurationsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setHl7ResponderConfigurationsList");
        }
        this.hl7ResponderConfigurationsList = hl7ResponderConfigurationsList;
    }

    public List<AbstractConfiguration> getHl7V3InitiatorConfigurationsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHl7V3InitiatorConfigurationsList");
        }
        return hl7V3InitiatorConfigurationsList;
    }

    public void setHl7V3InitiatorConfigurationsList(List<AbstractConfiguration> hl7v3InitiatorConfigurationsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setHl7V3InitiatorConfigurationsList");
        }
        hl7V3InitiatorConfigurationsList = hl7v3InitiatorConfigurationsList;
    }

    public List<AbstractConfiguration> getHl7V3ResponderConfigurationsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHl7V3ResponderConfigurationsList");
        }
        return hl7V3ResponderConfigurationsList;
    }

    public void setHl7V3ResponderConfigurationsList(List<AbstractConfiguration> hl7v3ResponderConfigurationsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setHl7V3ResponderConfigurationsList");
        }
        hl7V3ResponderConfigurationsList = hl7v3ResponderConfigurationsList;
    }

    public List<AbstractConfiguration> getDicomSCUConfigurationsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDicomSCUConfigurationsList");
        }
        return dicomSCUConfigurationsList;
    }

    public void setDicomSCUConfigurationsList(List<AbstractConfiguration> dicomSCUConfigurationsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDicomSCUConfigurationsList");
        }
        this.dicomSCUConfigurationsList = dicomSCUConfigurationsList;
    }

    public List<AbstractConfiguration> getDicomSCPConfigurationsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDicomSCPConfigurationsList");
        }
        return dicomSCPConfigurationsList;
    }

    public void setDicomSCPConfigurationsList(List<AbstractConfiguration> dicomSCPConfigurationsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDicomSCPConfigurationsList");
        }
        this.dicomSCPConfigurationsList = dicomSCPConfigurationsList;
    }

    public List<AbstractConfiguration> getWebServicesConfigurationsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWebServicesConfigurationsList");
        }
        return webServicesConfigurationsList;
    }

    public void setWebServicesConfigurationsList(List<AbstractConfiguration> webServicesConfigurationsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setWebServicesConfigurationsList");
        }
        this.webServicesConfigurationsList = webServicesConfigurationsList;
    }

    public List<AbstractConfiguration> getSyslogConfigurationsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSyslogConfigurationsList");
        }
        return syslogConfigurationsList;
    }

    public void setSyslogConfigurationsList(List<AbstractConfiguration> syslogConfigurationsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSyslogConfigurationsList");
        }
        this.syslogConfigurationsList = syslogConfigurationsList;
    }

    protected List<AbstractConfiguration> getConfigurationsForAType(Class<?> c, SystemInSession sis, Actor actor,
                                                                    WSTransactionUsage wstrans) {
        return AbstractConfiguration.getConfigurationsFiltered(c, sis, null, null, null, null, actor, null, wstrans,
                null, null, null);
    }

    public AbstractConfiguration getSelectedResponderConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedResponderConfiguration");
        }
        return selectedResponderConfiguration;
    }

    public void setSelectedResponderConfiguration(AbstractConfiguration selectedResponderConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedResponderConfiguration");
        }
        this.selectedResponderConfiguration = selectedResponderConfiguration;
    }

    protected AbstractConfiguration extractAbstractConfigurationFromTestStepsInstance(TestStepsInstance tsi) {
        if (tsi != null) {
            TestInstanceParticipants testInstanceParticipants = TestInstanceParticipants
                    .getTestInstanceParticipantsByTestInstanceBySiSByRoleInTest(
                            TestInstance.getTestInstanceByTestStepsInstance(tsi), tsi.getSystemInSessionResponder(),
                            tsi.getTestSteps().getTestRolesResponder().getRoleInTest());
            Actor act = null;

            if (testInstanceParticipants != null) {
                act = testInstanceParticipants.getActorIntegrationProfileOption().getActorIntegrationProfile()
                        .getActor();
            }
            List<AbstractConfiguration> hl7v2InitiatorConfigurations = this.getConfigurationsForAType(
                    HL7V2InitiatorConfiguration.class, tsi.getSystemInSessionResponder(), act, tsi.getTestSteps()
                            .getWstransactionUsage());
            if ((hl7v2InitiatorConfigurations != null) && (hl7v2InitiatorConfigurations.size() > 0)) {
                return hl7v2InitiatorConfigurations.get(0);
            }

            List<AbstractConfiguration> hl7v2ResponderConfigurations = this.getConfigurationsForAType(
                    HL7V2ResponderConfiguration.class, tsi.getSystemInSessionResponder(), act, tsi.getTestSteps()
                            .getWstransactionUsage());
            if ((hl7v2InitiatorConfigurations != null) && (hl7v2ResponderConfigurations.size() > 0)) {
                return hl7v2ResponderConfigurations.get(0);
            }

            List<AbstractConfiguration> hl7v3InitiatorConfigurations = this.getConfigurationsForAType(
                    HL7V3InitiatorConfiguration.class, tsi.getSystemInSessionResponder(), act, tsi.getTestSteps()
                            .getWstransactionUsage());
            if ((hl7v3InitiatorConfigurations != null) && (hl7v3InitiatorConfigurations.size() > 0)) {
                return hl7v3InitiatorConfigurations.get(0);
            }

            List<AbstractConfiguration> hl7v3ResponderConfigurations = this.getConfigurationsForAType(
                    HL7V3ResponderConfiguration.class, tsi.getSystemInSessionResponder(), act, tsi.getTestSteps()
                            .getWstransactionUsage());
            if ((hl7v3ResponderConfigurations != null) && (hl7v3ResponderConfigurations.size() > 0)) {
                return hl7v3ResponderConfigurations.get(0);
            }

            List<AbstractConfiguration> dicomscpConfigurations = this.getConfigurationsForAType(
                    DicomSCPConfiguration.class, tsi.getSystemInSessionResponder(), act, tsi.getTestSteps()
                            .getWstransactionUsage());
            if ((dicomscpConfigurations != null) && (dicomscpConfigurations.size() > 0)) {
                return dicomscpConfigurations.get(0);
            }

            List<AbstractConfiguration> dicomscuConfigurations = this.getConfigurationsForAType(
                    DicomSCUConfiguration.class, tsi.getSystemInSessionResponder(), act, tsi.getTestSteps()
                            .getWstransactionUsage());
            if ((dicomscuConfigurations != null) && (dicomscuConfigurations.size() > 0)) {
                return dicomscuConfigurations.get(0);
            }

            List<AbstractConfiguration> wsConfigurations = this.getConfigurationsForAType(
                    WebServiceConfiguration.class, tsi.getSystemInSessionResponder(), act, tsi.getTestSteps()
                            .getWstransactionUsage());
            if ((wsConfigurations != null) && (wsConfigurations.size() > 0)) {
                return wsConfigurations.get(0);
            }

            List<AbstractConfiguration> syslogConfigurations = this.getConfigurationsForAType(
                    SyslogConfiguration.class, tsi.getSystemInSessionResponder(), act, tsi.getTestSteps()
                            .getWstransactionUsage());
            if ((syslogConfigurations != null) && (syslogConfigurations.size() > 0)) {
                return syslogConfigurations.get(0);
            }
        }
        return null;
    }

    public boolean isPageLoaded() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isPageLoaded");
        }
        return pageLoaded;
    }

    public void setPageLoaded(boolean pageLoaded) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPageLoaded");
        }
        this.pageLoaded = pageLoaded;
    }

    public void reportJira() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reportJira");
        }
        jiraManager = new JiraManager(this);
        displayJiraPanel = true;
    }

    public void cancelJira() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelJira");
        }
        setDisplayJiraPanel(false);
    }

    public boolean isDisplayJiraPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayJiraPanel");
        }
        return displayJiraPanel;
    }

    public void setDisplayJiraPanel(boolean displayJiraPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayJiraPanel");
        }
        this.displayJiraPanel = displayJiraPanel;
    }

    @Override
    public void issueCreatedCallback() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("issueCreatedCallback");
        }
        setDisplayJiraPanel(false);
    }

    public JiraManager getJiraManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getJiraManager");
        }
        return jiraManager;
    }

    public void setJiraManager(JiraManager jiraManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setJiraManager");
        }
        this.jiraManager = jiraManager;
    }

    /**
     * true: if a TestStepsInstance in this TestInstance is active. false: if no TestStepsInstance in this TestInstance is active.
     *
     * @return isAnotherTestStepActive(TestInstance)
     */
    public boolean isAnotherTestStepActive() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAnotherTestStepActive");
        }

        return isAnotherTestStepActive(selectedTestInstance);
    }

    // START - CHANGES in support of NIST ITB

    /**
     * true: if a TestStepsInstance in this TestInstance is active. false: if no TestStepsInstance in this TestInstance is active.
     *
     * @return <b>true:</b> if a TestStepsInstance in this TestInstance is active.<br/>
     * <b>false:</b> if no TestStepsInstance in this TestInstance is active.
     */
    public boolean isAnotherTestStepActive(TestInstance selectedTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAnotherTestStepActive");
        }
        for (TestStepsInstance inst : selectedTestInstance.getTestStepsInstanceList()) {
            if (inst.getExecutionStatus() != null) {
                switch (inst.getExecutionStatus().getKey()) {
                    case WAITING:
                    case INITIATED:
                    case RESPONDED:
                    case PAUSED:

                        return true;
                    default:
                        continue;
                }
            }
        }

        return false;
    }

    /**
     * Changes the Execution Status of the selectedTestInstance
     */
    public void changeTiExecutionStatusToSelectedExecutionStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeTiExecutionStatusToSelectedExecutionStatus");
        }

        changeTiExecutionStatusTo(selectedExecutionStatus);
    }

    public void changeTiExecutionStatusTo(TestInstanceExecutionStatusEnum status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeTiExecutionStatusTo");
        }

        if (selectedTestInstance != null) {
            if ((status != null)
                    && !(status.getKeyword().equalsIgnoreCase(selectedTestInstance.getExecutionStatus().getKey()
                    .getKeyword()))) {

                switch (status) {
                    case COMPLETED:
                    case SKIPPED:
                    case ABORTED:
                    case PAUSED:
                        deactivateActiveTestStepsInstance();
                        break;
                    default:
                        ;
                }

                selectedTestInstance.setExecutionStatus(testInstanceExecutionStatusDAO
                        .getTestInstanceExecutionStatusByEnum(status));

                selectedTestInstance = testInstanceDAO.mergeTestInstance(selectedTestInstance);
            }
        } else {

        }
    }

    /**
     * Changes the execution status of the supplied TestStepsInstance tsi to the TestStepInstanceExecutionStatusEnum status supplied.
     *
     * @param tsi
     * @param status
     */
    public void changeTsiExecutionStatusTo(TestStepsInstance tsi, TestStepInstanceExecutionStatusEnum status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeTsiExecutionStatusTo");
        }

        if (tsi != null) {
            tsi.setExecutionStatus(new TestStepsInstanceExecutionStatusDAO().getTestStepExecutionStatusByEnum(status));
            EntityManager em = EntityManagerService.provideEntityManager();
            em.merge(tsi);
            em.flush();
        }
    }

    /**
     * Determines whether a particular TestStepsInstance Test Step is ready for activation
     *
     * @return
     */
    public boolean canTestStepBeActivated() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canTestStepBeActivated");
        }
        boolean testStepCanBeActivated = true;

        if (!(selectedTestInstance.getExecutionStatus().getKey().equals(TestInstanceExecutionStatusEnum.ACTIVE))) {
            return false;
        }

        for (TestStepsInstance inst : selectedTestInstance.getTestStepsInstanceList()) {
            if ((inst.getExecutionStatus() != null)
                    && inst.getExecutionStatus().getKey().getKeyword()
                    .equalsIgnoreCase(TestStepInstanceExecutionStatusEnum.WAITING.getKeyword())) {
                testStepCanBeActivated = false;
                break;
            }
        }

        return testStepCanBeActivated;
    }

    /**
     * Used by UI to refresh the list of TmStepInstanceMessage for a given tsi. Tareq: Not sure why we are retrieving this list and the list of
     * test step instances separately. May be because the
     * collections are not Set-based and because of known limitation on HQL pre-fetch with multiple List collections.
     * <p/>
     * Synchronizes the page scoped tsiMessagesMap and proxy channels status with the state of the database.
     */
    public void retrieveTestStatusAndMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveTestStatusAndMessages");
        }
        retrieveLatestTestInstanceExecutionStatus();
        if (getSelectedTestInstance() != null) {
            List<TestStepsInstance> testStepsInstances = getSelectedTestInstance().getTestStepsInstanceList();
            if (testStepsInstances != null) {
                for (TestStepsInstance testStepsInstance : testStepsInstances) {
                    List<TmStepInstanceMessage> listOfMsgs = new StepInstanceMsgDAO(
                            EntityManagerService.provideEntityManager())
                            .findTmStepInstanceMessagesByTsiIdOrderByDirAsc(testStepsInstance.getId());
                    tsiMessagesMap.put(testStepsInstance.getId(), listOfMsgs);
                }
            }
        }
        // If the values are cached, then manual page refresh will be required for updated Proxy Channel status to be displayed on UI.
        // Caching will save webservice call trips.
        if (!ApplicationPreferenceManager.instance().cacheProxyChannelStatus()) {
            retrieveProxyChannelsStatus();
        }
    }

    /**
     * Used by UI to refresh the list of TmStepInstanceMessage for a given tsi.
     *
     * @param tsi
     * @return
     */
    public List<TmStepInstanceMessage> fetchTmStepInstanceMessages(TestStepsInstance tsi) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchTmStepInstanceMessages");
        }
        if (tsi != null) {
            return tsiMessagesMap.get(tsi.getId());
        } else {
            return new ArrayList<TmStepInstanceMessage>();
        }
    }

    /**
     * Synchronizes the page-scoped activeProxyPorts with the Proxy
     */
    private void retrieveProxyChannelsStatus() {
        if (proxyIsActivated()) {
            try {
                activeProxyPorts.clear();
                List<Configuration> proxyChannels = ProxyManager.retrieveAllActiveChannels();
                for (Configuration configuration : proxyChannels) {
                    activeProxyPorts.add(configuration.getProxyPort());
                }
                LOG.debug(getClass().getSimpleName() + ".retrieveProxyChannelsStatus() activeProxyPorts="
                        + activeProxyPorts);
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.error("ProxyManager.retrieveProxyChannelsStatus() failed", e);
                }
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Cannot reach proxy, check your configuration in Administration");
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Proxy is not activated, check your configuration in Administration");
        }
    }

    public boolean proxyIsActivated() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("proxyIsActivated");
        }
        TestingSession inTestinSession = TestingSession.getSelectedTestingSession();
        TestingSessionQuery q = new TestingSessionQuery();
        q.id().eq(inTestinSession.getId());
        List<Boolean> res = q.isProxyUseEnabled().getListDistinct();
        return res.get(0);
    }

    /**
     * Determines whether or not the provided proxy port is currently active.
     *
     * @param proxyPort
     * @return true if the proxy port is active; false otherwise
     */
    public boolean proxyPortActive(int proxyPort) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("proxyPortActive");
        }
        return activeProxyPorts.contains(proxyPort);
    }

    public void retrieveLatestTestInstanceExecutionStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveLatestTestInstanceExecutionStatus");
        }
        if (getSelectedTestInstance() != null) {
            // Don't use em.find as result might be from cache
            TestInstance ti = (TestInstance) EntityManagerService
                    .provideEntityManager()
                    .createQuery(
                            "SELECT testInstance FROM TestInstance testInstance fetch all properties WHERE testInstance.id=:id")
                    .setParameter("id", getSelectedTestInstance().getId()).getSingleResult();
            setSelectedExecutionStatus(ti.getExecutionStatus() != null ? ti.getExecutionStatus().getKey()
                    : TestInstanceExecutionStatusEnum.ACTIVE);
        } else {
            LOG.warn("selectedTestInstance is null");
        }
    }

    public boolean showWarningOnInstituion(Institution currentInstitution) {
        if (selectedTestInstance.isContainsSameSystemsOrFromSameCompany()) {
            // those attributes are transient, if the selectedTestInstance has been reload, lists shall be computed again
            if (selectedTestInstance.getDuplicateCompanies() == null && selectedTestInstance.getDuplicateSystems() == null) {
                DuplicateFinder.findDuplicates(selectedTestInstance);
            }
            if (selectedTestInstance.getDuplicateCompanies() != null && selectedTestInstance.getDuplicateCompanies().contains(currentInstitution)) {
                return true;
            }
        }
        return false;
    }

    public boolean showWarningOnSystem(SystemInSession currentSIS) {
        if (selectedTestInstance.isContainsSameSystemsOrFromSameCompany()) {
            // those attributes are transient, if the selectedTestInstance has been reload, lists shall be computed again
            if (selectedTestInstance.getDuplicateCompanies() == null && selectedTestInstance.getDuplicateSystems() == null) {
                DuplicateFinder.findDuplicates(selectedTestInstance);
            }
            if (selectedTestInstance.getDuplicateSystems() != null && selectedTestInstance.getDuplicateSystems().contains(currentSIS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * UI Helper method. Checks to see if it is valid for ajax calls to continue refreshing the UI components.
     *
     * @return true: if it is okay, false: otherwise
     */
    public boolean isRefreshPermitted() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRefreshPermitted");
        }
        boolean result = false;

        if (selectedTestInstance != null) {
            if (!(selectedTestInstance.getTest().isTypeInteroperability())) {
                return false;
            }

            for (TestStepsInstance inst : selectedTestInstance.getTestStepsInstanceList()) {
                if (inst.getExecutionStatus() != null
                        && ((inst.getExecutionStatus().getKey().getKeyword()
                        .equalsIgnoreCase(TestStepInstanceExecutionStatusEnum.WAITING.getKeyword()))
                        || (inst.getExecutionStatus().getKey().getKeyword()
                        .equalsIgnoreCase(TestStepInstanceExecutionStatusEnum.INITIATED.getKeyword())) || (inst
                        .getExecutionStatus().getKey().getKeyword()
                        .equalsIgnoreCase(TestStepInstanceExecutionStatusEnum.RESPONDED.getKeyword())))) {
                    refreshedOnceAfterCompleted = false;
                    result = true;
                    break;
                }

                if (inst.getExecutionStatus() != null
                        && ((inst.getExecutionStatus().getKey().equals(TestStepInstanceExecutionStatusEnum.COMPLETED)))) {
                    if (refreshedOnceAfterCompleted) {
                        result = false;
                        break;
                    } else {
                        refreshedOnceAfterCompleted = true;
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Activate the supplied test steps instance and make other test steps instance inactive, if any is in active state
     *
     * @param instance
     */
    public void activateTestStepsInstance(TestStepsInstance instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateTestStepsInstance");
        }

        refreshedOnceAfterCompleted = false;

        deactivateActiveTestStepsInstance();

        if (instance != null) {

            changeTsiExecutionStatusTo(instance, TestStepInstanceExecutionStatusEnum.WAITING);
        }

        // refresh the test instance
        selectedTestInstance = testInstanceDAO.mergeTestInstance(selectedTestInstance);
    }

    /**
     * Change TestStepInstanceExecutionStatus of all TestStepsInstance to inactive.
     */
    public void deactivateActiveTestStepsInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deactivateActiveTestStepsInstance");
        }

        for (TestStepsInstance tsi : selectedTestInstance.getTestStepsInstanceList()) {

            if (tsi.getExecutionStatus() != null) {

                if (tsi.getExecutionStatus().getKey().equals(TestStepInstanceExecutionStatusEnum.WAITING)) {

                    changeTsiExecutionStatusTo(tsi, TestStepInstanceExecutionStatusEnum.INACTIVE);
                }
            } else {
                // set a inactive status if the status is null
                changeTsiExecutionStatusTo(tsi, TestStepInstanceExecutionStatusEnum.INACTIVE);
            }
        }
    }

    /**
     * Reactivates the selected TestStepsInstance part of the active Test Instance.
     */
    public void reActivateSelectedTsi() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reActivateSelectedTsi");
        }
        reActivateTsi(selectedTestStepsInstance);
    }

    /**
     * This reactivates a TestStepsInstance that is already in TestStepInstanceExecutionStatusEnum.COMPLETED state. As part of reactivation it
     * deletes any TmStepInstanceMessage records associated with
     * this TestStepsInstance.
     *
     * @param instance
     */
    public void reActivateTsi(TestStepsInstance instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reActivateTsi");
        }

        // if the test instance has been marked as completed for this test steps instance
        // than we need to move it back into active status
        if (selectedTestInstance != null) {

            switch (selectedTestInstance.getExecutionStatus().getKey()) {
                case COMPLETED:
                case ABORTED:
                case PAUSED:
                case SKIPPED:
                    changeTiExecutionStatusTo(TestInstanceExecutionStatusEnum.ACTIVE);
                    break;
                default:
                    ;
            }
        }

        if (instance != null) {

            activateTestStepsInstance(instance);

            deleteAllTsiMsgs(instance);

            // refresh the test instance
            selectedTestInstance = testInstanceDAO.mergeTestInstance(selectedTestInstance);
        } else {

        }
    }

    /**
     * Deletes all the TmStepInstanceMessage records associated with supplied TestStepsInstance instance.
     *
     * @param tsi
     */
    public void deleteAllTsiMsgs(TestStepsInstance tsi) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllTsiMsgs");
        }

        if (tsi != null) {
            new StepInstanceMsgDAO(EntityManagerService.provideEntityManager())
                    .deleteAllTmStepInstanceMessagesForTsi(tsi);
        }
    }

    /**
     * @param tsi
     */
    public void markTsiAsSelected(TestStepsInstance tsi) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("markTsiAsSelected");
        }

        this.selectedTestStepsInstance = tsi;
    }

    class StatusStats {
        private int nbSteps = 0;
        private int done = 0;
        private int failed = 0;
        private int skipped = 0;

        public StatusStats(int nbSteps) {
            this.nbSteps = nbSteps;
        }

        public void addFailed() {
            failed++;
        }

        public void addSkipped() {
            skipped++;
        }

        public void addDone() {
            done++;
        }

        public int getNbSteps() {
            return nbSteps;
        }

        public int getDone() {
            return done;
        }

        public int getFailed() {
            return failed;
        }

        public int getSkipped() {
            return skipped;
        }

        public int getDonePercentage() {
            return computePercentage(done);
        }

        public int getFailedPercentage() {
            int failedP = computePercentage(failed);
            return percentageCorrection(failedP);
        }

        private int percentageCorrection(int failedP) {
            int total = failedP + getDonePercentage() + getSkippedPercentage();
            if (total > 100) {
                return failedP - (total - 100);
            }
            return failedP;
        }

        public int getSkippedPercentage() {
            return computePercentage(skipped);
        }

        private int computePercentage(int val) {
            if (nbSteps != 0) {
                return Math.round((val * 100.0f) / nbSteps);
            } else {
                return 0;
            }
        }
    }
    // END - CHANGES in support of NIST ITB

    public boolean canChangeMonitor() {
        TestInstance ti = getSelectedTestInstance();
        Status tiStatus = ti.getLastStatus();
        return !tiStatus.equals(Status.getSTATUS_VERIFIED()) && !tiStatus.equals(Status.getSTATUS_FAILED());
    }

    public boolean canUpdateStepsValues(TestStepsData tsd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canUpdateStepsValues");
        }
        User user = User.loggedInUser();
        return user.getRoles().contains(Role.getADMINISTRATOR_ROLE()) && tsd.getDataType().getKeyword().equals(DataType.URL);
    }

    public String updateXdstoolsLink(String description) {
        if (description.contains("&amp;")) {
            description = description.replace("&amp;", "&");
        }
        if (description.contains("https://$$GWT$$/rest/xdstools?testInstanceId=$$testInstanceId$$&callingToolOid=$$callingToolOid$$")) {
            String gwtUrl = ApplicationPreferenceManager.getStringValue("gwt_url");
            String appOID = ApplicationPreferenceManager.getStringValue("app_instance_oid");
            if (gwtUrl != null && !gwtUrl.isEmpty() && appOID != null && !appOID.isEmpty()) {
                String url = gwtUrl + "rest/xdstools?testInstanceId=$$testInstanceId$$&callingToolOid=$$callingToolOid$$";
                URLParser parser = new URLParser();
                url = parser.replaceParams(url, getSelectedTestInstance().getId().toString(), appOID);
                return description.replace("https://$$GWT$$/rest/xdstools?testInstanceId=$$testInstanceId$$&callingToolOid=$$callingToolOid$$", new
                        HTMLFilter().filter(url));
            }
        }
        return description;
    }
}
