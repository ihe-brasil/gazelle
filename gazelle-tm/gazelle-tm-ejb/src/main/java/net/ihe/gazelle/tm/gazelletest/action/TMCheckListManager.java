package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.tree.GazelleTreeNodeImpl;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterion;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.action.ActorIntegrationProfileOptionFilter;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.datamodel.AIPOForCheckDataModel;
import net.ihe.gazelle.tm.datamodel.TestInstanceDataModelForCheck;
import net.ihe.gazelle.tm.gazelletest.checklist.*;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.gazelletest.model.reversed.AIPOQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.ajax4jsf.model.DataVisitResult;
import org.ajax4jsf.model.DataVisitor;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.richfaces.component.UITree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.jboss.seam.ScopeType.SESSION;

/**
 * @author abderrazek boufahja
 */

@Name("tmCheckListManager")
@Scope(SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("TMCheckListManagerLocal")
public class TMCheckListManager implements TMCheckListManagerLocal, Serializable {

    private static final long serialVersionUID = -8472964114322655168L;
    private static final Logger LOG = LoggerFactory.getLogger(TMCheckListManager.class);

    private GazelleTreeNodeImpl<QuestionCheckList> treeNodeQuestionList;

    private String selectedQuestion;

    private List<TestSteps> testStepsWithNoTests;

    private List<TestStepsInstance> testStepsInstanceWithNoTestInstance;

    private List<Test> testCATReadyWithNoTestSteps;

    private List<MetaTest> metaTestsWithNoTests;

    private List<RoleInTest> roleInTestsWithNoTests;

    private List<RoleInTest> roleInTestsWithNoTestParticipants;

    private List<ContextualInformation> listCIWithNoTestSteps;

    private List<ContextualInformationInstance> listCIIWithNoTSI;

    private Filter filter;

    private TestInstanceDataModelForCheck listTIWithNoTS;

    private AIPOForCheckDataModel listAIPOWithNoTest;

    private RoleInTest selectedRoleInTest;

    private Domain selectedDomain;

    private ActorIntegrationProfileOptionFilter actorIntegrationProfileOptionFilter;

    private FilterDataModel<ActorIntegrationProfileOption> actorIntegrationProfileOption;

    @Override
    public FilterDataModel<ActorIntegrationProfileOption> getActorIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfileOption");
        }
        if (actorIntegrationProfileOption == null) {
            actorIntegrationProfileOption = new FilterDataModel<ActorIntegrationProfileOption>(
                    getActorIntegrationProfileOptionFilter()) {
                @Override
                protected Object getId(ActorIntegrationProfileOption t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        if (selectedQuestion.equals("listaipowithoutconnectathontest")) {
            List<Integer> idAipoWithTest = getAipoWithConnectathonTests();
            actorIntegrationProfileOptionFilter.setExcludedAIPOList(idAipoWithTest);
        } else if (selectedQuestion.equals("listaipowithoutpreconnectathontest")) {
            List<Integer> idAipoWithPreConnectathonTest = getAipoWithPreConnectathonTests();
            actorIntegrationProfileOptionFilter.setExcludedAIPOList(idAipoWithPreConnectathonTest);
        } else {
            List<Integer> idAipoWithTest = getAipoWithTests();
            actorIntegrationProfileOptionFilter.setExcludedAIPOList(idAipoWithTest);
        }
        return actorIntegrationProfileOption;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActorIntegrationProfileOptionFilter getActorIntegrationProfileOptionFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfileOptionFilter");
        }
        if (actorIntegrationProfileOptionFilter == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            final Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            actorIntegrationProfileOptionFilter = new ActorIntegrationProfileOptionFilter(requestParameterMap);
        }
        return actorIntegrationProfileOptionFilter;
    }

    public List<Integer> getAipoWithTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoWithTests");
        }
        AIPOQuery aipoQ = new AIPOQuery();
        getAIPOWithReadyTests(aipoQ);
        aipoQ.testParticipants().roleInTest().testRoles().isNotEmpty();
        List<Integer> idAipoWithTest = aipoQ.id().getListDistinct();
        return idAipoWithTest;
    }

    public List<Integer> getAipoWithConnectathonTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoWithConnectathonTests");
        }
        AIPOQuery aipoQ = new AIPOQuery();
        getAIPOWithReadyTests(aipoQ);
        aipoQ.testParticipants().roleInTest().testRoles().test().testType().eq(TestType.getTYPE_CONNECTATHON());
        List<Integer> idAipoWithConnectathonTest = aipoQ.id().getListDistinct();
        return idAipoWithConnectathonTest;
    }

    private void getAIPOWithReadyTests(AIPOQuery aipoQ) {
        aipoQ.testParticipants().roleInTest().testRoles().test().testStatus().eq(TestStatus.getSTATUS_READY());
    }

    public List<Integer> getAipoWithPreConnectathonTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoWithPreConnectathonTests");
        }
        AIPOQuery aipoQ = new AIPOQuery();
        getAIPOWithReadyTests(aipoQ);
        aipoQ.testParticipants().roleInTest().testRoles().test().testType().eq(TestType.getTYPE_MESA());
        List<Integer> idAipoWithPreConnectathonTest = aipoQ.id().getListDistinct();
        return idAipoWithPreConnectathonTest;
    }

    @Override
    public Domain getSelectedDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomain");
        }
        return selectedDomain;
    }

    @Override
    public void setSelectedDomain(Domain selectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomain");
        }
        this.selectedDomain = selectedDomain;
        initAIPOWithNoTests();
    }

    @Override
    public TestInstanceDataModelForCheck getListTIWithNoTS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListTIWithNoTS");
        }
        return listTIWithNoTS;
    }

    @Override
    public void setListTIWithNoTS(TestInstanceDataModelForCheck listTIWithNoTS) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListTIWithNoTS");
        }
        this.listTIWithNoTS = listTIWithNoTS;
    }

    @Override
    public AIPOForCheckDataModel getListAIPOWithNoTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListAIPOWithNoTest");
        }
        initAIPOWithNoTests();
        return listAIPOWithNoTest;
    }

    @Override
    public void setListAIPOWithNoTest(AIPOForCheckDataModel listAIPOWithNoTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListAIPOWithNoTest");
        }
        this.listAIPOWithNoTest = listAIPOWithNoTest;
    }

    @Override
    public RoleInTest getSelectedRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedRoleInTest");
        }
        return selectedRoleInTest;
    }

    @Override
    public void setSelectedRoleInTest(RoleInTest selectedRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedRoleInTest");
        }
        this.selectedRoleInTest = selectedRoleInTest;
    }

    @Override
    public List<ContextualInformationInstance> getListCIIWithNoTSI() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListCIIWithNoTSI");
        }
        return listCIIWithNoTSI;
    }

    @Override
    public void setListCIIWithNoTSI(List<ContextualInformationInstance> listCIIWithNoTSI) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListCIIWithNoTSI");
        }
        this.listCIIWithNoTSI = listCIIWithNoTSI;
    }

    @Override
    public List<RoleInTest> getRoleInTestsWithNoTestParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTestsWithNoTestParticipants");
        }
        return roleInTestsWithNoTestParticipants;
    }

    @Override
    public void setRoleInTestsWithNoTestParticipants(List<RoleInTest> roleInTestsWithNoTestParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTestsWithNoTestParticipants");
        }
        this.roleInTestsWithNoTestParticipants = roleInTestsWithNoTestParticipants;
    }

    @Override
    public List<RoleInTest> getRoleInTestsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTestsWithNoTests");
        }
        return roleInTestsWithNoTests;
    }

    @Override
    public void setRoleInTestsWithNoTests(List<RoleInTest> roleInTestsWithNoTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTestsWithNoTests");
        }
        this.roleInTestsWithNoTests = roleInTestsWithNoTests;
    }

    @Override
    public List<ContextualInformation> getListCIWithNoTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListCIWithNoTestSteps");
        }
        return listCIWithNoTestSteps;
    }

    @Override
    public void setListCIWithNoTestSteps(List<ContextualInformation> listCIWithNoTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListCIWithNoTestSteps");
        }
        this.listCIWithNoTestSteps = listCIWithNoTestSteps;
    }

    @Override
    public List<MetaTest> getMetaTestsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaTestsWithNoTests");
        }
        return metaTestsWithNoTests;
    }

    @Override
    public void setMetaTestsWithNoTests(List<MetaTest> metaTestsWithNoTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMetaTestsWithNoTests");
        }
        this.metaTestsWithNoTests = metaTestsWithNoTests;
    }

    @Override
    public List<Test> getTestCATReadyWithNoTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestCATReadyWithNoTestSteps");
        }
        return testCATReadyWithNoTestSteps;
    }

    @Override
    public void setTestCATReadyWithNoTestSteps(List<Test> testCATReadyWithNoTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestCATReadyWithNoTestSteps");
        }
        this.testCATReadyWithNoTestSteps = testCATReadyWithNoTestSteps;
    }

    @Override
    public List<TestStepsInstance> getTestStepsInstanceWithNoTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStepsInstanceWithNoTestInstance");
        }
        return testStepsInstanceWithNoTestInstance;
    }

    @Override
    public void setTestStepsInstanceWithNoTestInstance(List<TestStepsInstance> testStepsInstanceWithNoTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstanceWithNoTestInstance");
        }
        this.testStepsInstanceWithNoTestInstance = testStepsInstanceWithNoTestInstance;
    }

    @Override
    public List<TestSteps> getTestStepsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStepsWithNoTests");
        }
        return testStepsWithNoTests;
    }

    @Override
    public void setTestStepsWithNoTests(List<TestSteps> testStepsWithNoTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsWithNoTests");
        }
        this.testStepsWithNoTests = testStepsWithNoTests;
    }

    @Override
    public String getSelectedQuestion() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedQuestion");
        }
        return selectedQuestion;
    }

    @Override
    public void setSelectedQuestion(String selectedQuestion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedQuestion");
        }
        this.selectedQuestion = selectedQuestion;
    }

    @Override
    public GazelleTreeNodeImpl<QuestionCheckList> getTreeNodeQuestionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTreeNodeQuestionList");
        }
        return treeNodeQuestionList;
    }

    @Override
    public void setTreeNodeQuestionList(GazelleTreeNodeImpl<QuestionCheckList> treeNodeQuestionList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTreeNodeQuestionList");
        }
        this.treeNodeQuestionList = treeNodeQuestionList;
    }

    @Override
    public void initTIwithNoTSs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTIwithNoTSs");
        }
        filter = new Filter(TestInstance.class, new ArrayList<HQLCriterion<TestInstance, ?>>());
        listTIWithNoTS = new TestInstanceDataModelForCheck(filter);
        listTIWithNoTS.setTestingSessionIsNull(true);
        listTIWithNoTS.resetCache();
    }

    @Override
    public int getNumberOfTIWithNoTSs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfTIWithNoTSs");
        }
        if (this.listTIWithNoTS == null) {
            return 0;
        }
        return this.listTIWithNoTS.size();
    }

    @Override
    public void deleteAllTIWithNoTSs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllTIWithNoTSs");
        }
        if ((this.listTIWithNoTS != null) && (this.listTIWithNoTS.size() > 0)) {
            //try { //TODO fix this deleteAllTIWithNoTSs
            this.listTIWithNoTS.walk(null, new DataVisitor() {

                @Override
                public DataVisitResult process(FacesContext arg0, Object arg1, Object arg2) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("process");
                    }
                    TestInstance ti = (TestInstance) arg1;
                    try {
                        ti.setTestingSession(TestingSession.getSelectedTestingSession());
                        EntityManager entityManager = EntityManagerService.provideEntityManager();
                        ti = entityManager.merge(ti);
                        entityManager.flush();
                        TestInstance.deleteConnectathonTestInstanceWithFind(ti);
                    } catch (Exception e) {
                        LOG.error("", e);
                    }
                    return DataVisitResult.CONTINUE;
                }
            }, null, null);
            FacesMessages.instance().add(StatusMessage.Severity.INFO,
                    "All test instances which are not related to a testing session are deleted.");
            //} catch (IOException e) {
            //	LOG.error("", e);
            //}
        }
    }

    @Override
    public void initTreeNodeReaderList() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTreeNodeReaderList");
        }
        treeNodeQuestionList = new GazelleTreeNodeImpl<QuestionCheckList>();
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("/checklist.xml");

            Nodes nodes = NodeTransformer.load(is);
            int i = 1;
            for (Node node : nodes.getNode()) {
                i = generateTreeNodes(i, node, treeNodeQuestionList);
            }

            is.close();
        } catch (JAXBException e) {
            LOG.error("", e);
        }
    }

    private int generateTreeNodes(int i, Node node, GazelleTreeNodeImpl<QuestionCheckList> treeNodeParent) {
        boolean isMasterModel = ApplicationManager.instance().isMasterModel();
        boolean isTM = ApplicationManager.instance().isTestManagement();
        if (node != null) {
            if (!isTM && node.getApplication().equals("TM")) {
                return i;
            }
            if (!isMasterModel && node.getApplication().equals("GMM")) {
                return i;
            }
            int j = 1;
            GazelleTreeNodeImpl<QuestionCheckList> nodeTreeNode = new GazelleTreeNodeImpl<QuestionCheckList>();
            nodeTreeNode.setData(QuestionCheckList.valueOf(node.getValue()));
            treeNodeParent.addChild(i, nodeTreeNode);
            for (Object obj : node.getNodeOrQuestion()) {
                if (obj instanceof Question) {
                    Question q = (Question) obj;
                    GazelleTreeNodeImpl<QuestionCheckList> questionNode = new GazelleTreeNodeImpl<QuestionCheckList>();
                    questionNode.setData(QuestionCheckList.valueOf(q.getValue()));
                    nodeTreeNode.addChild(j, questionNode);
                    //TODO check this error, the line appeared after migrating to jboss7
                    //	questionNode.setParent(nodeTreeNode);
                    j = j + 1;
                } else if (obj instanceof Node) {
                    Node nd = (Node) obj;
                    j = this.generateTreeNodes(j, nd, nodeTreeNode);
                }
            }
            i = i + 1;
        }
        return i;
    }

    @Override
    public String getQuestionFromQuestionCheckList(QuestionCheckList qcl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getQuestionFromQuestionCheckList");
        }
        if (qcl == null) {
            return null;
        }
        return ResourceBundle.instance().getString(qcl.getQuestion());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initAIPOWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initAIPOWithNoTests");
        }
        this.listAIPOWithNoTest = new AIPOForCheckDataModel();
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        String querr1 = "SELECT DISTINCT participants.actorIntegrationProfileOption.id FROM TestRoles tr JOIN tr.roleInTest.testParticipantsList " +
                "participants ";
        Query query = entityManager.createQuery(querr1);
        List<Integer> limplId = query.getResultList();
        this.listAIPOWithNoTest.setListRestriction(limplId);
        this.listAIPOWithNoTest.setDomainRestriction(selectedDomain);
        this.listAIPOWithNoTest.resetCache();
    }

    @Override
    public int getNumberAIPOWithNoTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberAIPOWithNoTest");
        }
        if (this.listAIPOWithNoTest == null) {
            return 0;
        }
        return this.listAIPOWithNoTest.size();
    }

    @Override
    public String checkForDomainOf(ActorIntegrationProfileOption aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkForDomainOf");
        }
        if (aipo != null) {
            HQLQueryBuilder<Domain> hql = new HQLQueryBuilder<Domain>(Domain.class);
            hql.addEq("integrationProfilesForDP.id", aipo.getActorIntegrationProfile().getIntegrationProfile().getId());
            List<Domain> ff = hql.getList();
            if ((ff != null) && (ff.size() > 0)) {
                return ff.get(0).getKeyword();
            }
        }
        return null;
    }

    @Override
    public void initTestStepsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTestStepsWithNoTests");
        }
        HQLQueryBuilder<TestSteps> querybuilder = new HQLQueryBuilder<TestSteps>(TestSteps.class);
        querybuilder.addRestriction(HQLRestrictions.eq("testParent", null));
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testStepsInstances"));
        this.testStepsWithNoTests = querybuilder.getList();
    }

    @Override
    public int getNumberTestStepsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberTestStepsWithNoTests");
        }
        if (this.testStepsWithNoTests == null) {
            return 0;
        }
        return this.testStepsWithNoTests.size();
    }

    @Override
    public void deleteAllTestStepsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllTestStepsWithNoTests");
        }
        if (this.testStepsWithNoTests != null) {
            for (TestSteps ts : this.testStepsWithNoTests) {
                try {
                    TestSteps.deleteTestStepsWithFind(ts);
                } catch (Exception e) {
                    LOG.error("", e);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
                }
            }
        }
    }

    @Override
    public void initTestStepsInstanceWithNoTestInstances() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTestStepsInstanceWithNoTestInstances");
        }
        HQLQueryBuilder<TestStepsInstance> querybuilder = new HQLQueryBuilder<TestStepsInstance>(
                TestStepsInstance.class);
        querybuilder.addRestriction(HQLRestrictions.eq("testInstance", null));
        this.testStepsInstanceWithNoTestInstance = querybuilder.getList();
    }

    @Override
    public int getNumberTestStepsInstanceWithNoTestInstances() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberTestStepsInstanceWithNoTestInstances");
        }
        if (this.testStepsInstanceWithNoTestInstance == null) {
            return 0;
        }
        return this.testStepsInstanceWithNoTestInstance.size();
    }

    @Override
    public void deleteAllTestStepsInstanceWithNoTestsInstances() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllTestStepsInstanceWithNoTestsInstances");
        }
        if (this.testStepsInstanceWithNoTestInstance != null) {
            for (TestStepsInstance tsi : this.testStepsInstanceWithNoTestInstance) {
                try {
                    TestStepsInstance.deleteTestStepsInstanceWithFind(tsi);
                } catch (Exception e) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to delete test step with id " + tsi.getId());
                    LOG.error("Failed to delete test step with id " + tsi.getId(), e);
                }
            }
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "TestStepsInstance deleted.");
        }
    }

    @Override
    public void initTestCATReadyWithNoTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTestCATReadyWithNoTestSteps");
        }
        HQLQueryBuilder<Test> querybuilder = new HQLQueryBuilder<Test>(Test.class);
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testStepsList"));
        querybuilder.addRestriction(HQLRestrictions.eq("testStatus.keyword", "ready"));
        querybuilder.addRestriction(HQLRestrictions.in("testType", TestType.getTestTypesWithoutMESA()));
        this.testCATReadyWithNoTestSteps = querybuilder.getList();
        Collections.sort(this.testCATReadyWithNoTestSteps);
    }

    @Override
    public int getNumberTestCATReadyWithNoTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberTestCATReadyWithNoTestSteps");
        }
        if (this.testCATReadyWithNoTestSteps == null) {
            return 0;
        }
        return this.testCATReadyWithNoTestSteps.size();
    }

    @Override
    public void initMetaTestsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initMetaTestsWithNoTests");
        }
        HQLQueryBuilder<MetaTest> querybuilder = new HQLQueryBuilder<MetaTest>(MetaTest.class);
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testRolesList"));
        this.metaTestsWithNoTests = querybuilder.getList();
    }

    @Override
    public int getNumberMetaTestsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberMetaTestsWithNoTests");
        }
        if (this.metaTestsWithNoTests == null) {
            return 0;
        }
        return this.metaTestsWithNoTests.size();
    }

    @Override
    public Boolean adviseNodeOpened(UITree tree) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("adviseNodeOpened");
        }
        return Boolean.TRUE;
    }

    @Override
    public void initRoleInTestsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initRoleInTestsWithNoTests");
        }
        HQLQueryBuilder<RoleInTest> querybuilder = new HQLQueryBuilder<RoleInTest>(RoleInTest.class);
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testRoles"));
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testInstanceParticipants"));
        this.roleInTestsWithNoTests = querybuilder.getList();
        Collections.sort(this.roleInTestsWithNoTests);
    }

    @Override
    public int getNumberRoleInTestsWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberRoleInTestsWithNoTests");
        }
        if (this.roleInTestsWithNoTests == null) {
            return 0;
        }
        return this.roleInTestsWithNoTests.size();
    }

    @Override
    public void deleteSelectedRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedRoleInTest");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        try {
            RoleInTest.deleteRoleInTest(selectedRoleInTest, entityManager);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Selected RoleInTest is removed.");
        } catch (Exception e) {
            LOG.error("", e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "Selected RoleInTest can not be removed because it is maybe referenced by testRoles.");
        }
    }

    @Override
    public void deleteAllRoleInTestWithNoTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllRoleInTestWithNoTests");
        }
        if (this.roleInTestsWithNoTests != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            for (RoleInTest rit : this.roleInTestsWithNoTests) {
                RoleInTest.deleteRoleInTest(rit, entityManager);
            }
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "All role in test with no tests are removed.");
        }
    }

    @Override
    public void initRoleInTestsWithNoTestParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initRoleInTestsWithNoTestParticipants");
        }
        HQLQueryBuilder<RoleInTest> querybuilder = new HQLQueryBuilder<RoleInTest>(RoleInTest.class);
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testParticipantsList"));
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testInstanceParticipants"));
        this.roleInTestsWithNoTestParticipants = querybuilder.getList();
        Collections.sort(this.roleInTestsWithNoTestParticipants);
    }

    @Override
    public int getNumberRoleInTestsWithNoTestParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberRoleInTestsWithNoTestParticipants");
        }
        if (this.roleInTestsWithNoTestParticipants == null) {
            return 0;
        }
        return this.roleInTestsWithNoTestParticipants.size();
    }

    @Override
    public void deleteAllRoleInTestWithNoTestParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllRoleInTestWithNoTestParticipants");
        }
        if (this.roleInTestsWithNoTestParticipants != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            for (RoleInTest rit : this.roleInTestsWithNoTestParticipants) {
                RoleInTest.deleteRoleInTest(rit, entityManager);
            }
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "All role in test with no testParticipants are removed.");
        }
    }

    // ------

    @Override
    public void initCIWithNoTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initCIWithNoTestSteps");
        }
        HQLQueryBuilder<ContextualInformation> querybuilder = new HQLQueryBuilder<ContextualInformation>(
                ContextualInformation.class);
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testStepsAsInput"));
        querybuilder.addRestriction(HQLRestrictions.isNull("testStepsAsOutput"));
        querybuilder.addRestriction(HQLRestrictions.isEmpty("contextualInformationInstances"));
        this.listCIWithNoTestSteps = querybuilder.getList();
    }

    @Override
    public int getNumberCIWithNoTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberCIWithNoTestSteps");
        }
        if (this.listCIWithNoTestSteps != null) {
            return this.listCIWithNoTestSteps.size();
        }
        return 0;
    }

    @Override
    public void deleteAllCIWithNoTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllCIWithNoTestSteps");
        }
        if (this.listCIWithNoTestSteps != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            for (ContextualInformation ci : this.listCIWithNoTestSteps) {
                if (ci.getPath() == null) {
                    HQLQueryBuilder<Path> hh = new HQLQueryBuilder<Path>(Path.class);
                    List<Path> lp = hh.getList();
                    if ((lp != null) && (lp.size() > 0)) {
                        ci.setPath(lp.get(0));
                        ci = entityManager.merge(ci);
                        entityManager.flush();
                    } else {
                        FacesMessages
                                .instance()
                                .add(StatusMessage.Severity.ERROR, "No way to delete contextual information not related to a test steps. You have " +
                                        "to add at least one PATH element to your database.");
                        return;
                    }
                }
                ContextualInformation.deleteContextualInformation(entityManager, ci);
            }
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "All contextual information with no testSteps are deleted.");
        }
    }

    @Override
    public void initCIIWithNoTestStepsInstances() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initCIIWithNoTestStepsInstances");
        }
        HQLQueryBuilder<ContextualInformationInstance> querybuilder = new HQLQueryBuilder<ContextualInformationInstance>(
                ContextualInformationInstance.class);
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testStepsInstancesAsInput"));
        querybuilder.addRestriction(HQLRestrictions.isEmpty("testStepsInstancesAsOutput"));
        this.listCIIWithNoTSI = querybuilder.getList();
    }

    @Override
    public int getNumberCIIWithNoTSI() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberCIIWithNoTSI");
        }
        if (this.listCIIWithNoTSI != null) {
            return this.listCIIWithNoTSI.size();
        }
        return 0;
    }

    @Override
    public void deleteAllCIIWithNoTSIs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllCIIWithNoTSIs");
        }
        if (this.listCIIWithNoTSI != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            for (ContextualInformationInstance cii : this.listCIIWithNoTSI) {
                cii = entityManager.find(ContextualInformationInstance.class, cii.getId());
                ContextualInformationInstance.removeContextualInformationinstance(cii, entityManager);
            }
            FacesMessages.instance()
                    .add(StatusMessage.Severity.INFO, "All contextual information instances with no testStepsInstances are deleted.");
        }
    }

    // -----------------------------------------------

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
