package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.bean.TestTimeline;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.util.Base64;
import org.kohsuke.graphviz.Edge;
import org.kohsuke.graphviz.Graph;
import org.kohsuke.graphviz.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

@Name("kpiTest")
@Scope(ScopeType.PAGE)
public class KpiTest implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(KpiTest.class);
    private static final String LABEL = "label";

    private static final String COLOR = "color";

    private static final String SEPARATOR = " - ";

    private static final long serialVersionUID = 1415332571702354562L;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @In
    private EntityManager entityManager;

    // Properties of the test displayed
    private Test test;
    private TestingSession testingSession;
    private String timelineXML;
    private String timelineStart;
    private String timelineEnd;

    public static Graph generateGraphForTest(EntityManager entityManager, Test test, TestingSession session) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Graph generateGraphForTest");
        }
        Graph g = new Graph();

        String queryStr = "SELECT tip.testInstance, tip.systemInSessionUser.systemInSession, tip.roleInTest.keyword FROM TestInstanceParticipants " +
                "tip "
                + "where tip.testInstance.test = :test";
        if (session != null) {
            queryStr = queryStr + " and tip.testInstance.testingSession=:inTestingSession";
        }
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("test", test);
        if (session != null) {
            query.setParameter("inTestingSession", session);
        }

        List<Object[]> result = query.getResultList();

        Map<String, Node> systemRoleNodes = new HashMap<String, Node>();
        Map<TestInstance, Set<Node>> participants = new HashMap<TestInstance, Set<Node>>();

        for (Object[] array : result) {
            TestInstance testInstance = (TestInstance) array[0];
            SystemInSession systemInSession = (SystemInSession) array[1];
            String roleInTest = (String) array[2];

            String systemLabel = systemInSession.getSystem().getKeyword() + SEPARATOR + roleInTest;
            Node system = systemRoleNodes.get(systemLabel);
            if (system == null) {
                system = new Node();
                system.attr("shape", "box");
                system.attr(LABEL, systemLabel);
                g.node(system);
                systemRoleNodes.put(systemLabel, system);
            }
            Set<Node> set = participants.get(testInstance);
            if (set == null) {
                set = new HashSet<Node>();
                participants.put(testInstance, set);
            }
            set.add(system);
        }

        Set<Entry<TestInstance, Set<Node>>> entrySet = participants.entrySet();
        for (Entry<TestInstance, Set<Node>> entry : entrySet) {
            Set<Node> set = entry.getValue();
            Node[] systemNodes = set.toArray(new Node[set.size()]);
            TestInstance testInstance = entry.getKey();
            if (set.size() == 2) {
                Edge e = new Edge(systemNodes[0], systemNodes[1]);
                e.attr(LABEL, Integer.toString(testInstance.getId()));
                e.attr(COLOR, Status.getGraphColor(testInstance.getLastStatus()));
                e.attr("arrowhead", "none");
                g.edge(e);
            } else if (set.size() > 2) {
                Node ti = new Node();
                ti.attr("shape", "box");
                ti.attr(COLOR, Status.getGraphColor(testInstance.getLastStatus()));
                ti.attr(LABEL, Integer.toString(testInstance.getId()));
                g.node(ti);
                for (int i = 0; i < systemNodes.length; i++) {
                    Edge e = new Edge(systemNodes[i], ti);
                    e.attr("arrowhead", "none");
                    e.attr(COLOR, Status.getGraphColor(testInstance.getLastStatus()));
                    g.edge(e);
                }
            }
        }
        g.attr("overlap", "false");
        return g;
    }

    public static Graph generateGraphForTestOld(EntityManager entityManager, Test test, TestingSession session) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Graph generateGraphForTestOld");
        }
        Graph g = new Graph();

        Query query = entityManager
                .createQuery("SELECT tip.testInstance, tip.systemInSessionUser.systemInSession, tip.roleInTest.keyword FROM " +
                        "TestInstanceParticipants tip "
                        + "where tip.testInstance.test = :test and "
                        + "tip.testInstance.testingSession=:inTestingSession");
        query.setParameter("test", test);
        query.setParameter("inTestingSession", session);

        Map<SystemInSession, Node> systemNodes = new HashMap<SystemInSession, Node>();
        Map<TestInstance, Map<String, SystemInSession>> testInstanceNodes = new HashMap<TestInstance, Map<String, SystemInSession>>();

        List<Object[]> result = query.getResultList();
        for (Object[] array : result) {
            TestInstance testInstance = (TestInstance) array[0];
            SystemInSession systemInSession = (SystemInSession) array[1];
            String roleInTest = (String) array[2];

            generateGraphForTestCreateSystem(g, systemNodes, systemInSession);
            Map<String, SystemInSession> systems = testInstanceNodes.get(testInstance);
            if (systems == null) {
                systems = new HashMap<String, SystemInSession>();
                testInstanceNodes.put(testInstance, systems);
            }
            systems.put(roleInTest, systemInSession);
        }

        Set<Entry<TestInstance, Map<String, SystemInSession>>> entrySet = testInstanceNodes.entrySet();
        for (Entry<TestInstance, Map<String, SystemInSession>> entry : entrySet) {
            Map<String, SystemInSession> systems = entry.getValue();
            TestInstance testInstance = entry.getKey();
            if (systems.size() == 2) {
                Object[] systemsArray = systems.values().toArray();
                Node node1 = systemNodes.get(systemsArray[0]);
                Node node2 = systemNodes.get(systemsArray[1]);
                Edge edge = new Edge(node1, node2);
                edge.attr(COLOR, Status.getGraphColor(testInstance.getLastStatus()));
                edge.attr(LABEL, Integer.toString(testInstance.getId()) + SEPARATOR
                        + generateGraphForTestMonitorUsername(testInstance) + SEPARATOR
                        + testInstance.getLastStatus().getKeyword());
                g.edge(edge);
            } else {
                Node ti = new Node();
                ti.attr(COLOR, Status.getGraphColor(testInstance.getLastStatus()));
                ti.attr(LABEL, Integer.toString(testInstance.getId()) + SEPARATOR
                        + generateGraphForTestMonitorUsername(testInstance) + SEPARATOR
                        + testInstance.getLastStatus().getKeyword());
                g.node(ti);

                Set<Entry<String, SystemInSession>> entrySet2 = systems.entrySet();
                for (Entry<String, SystemInSession> entry2 : entrySet2) {
                    Node sysNode = systemNodes.get(entry2.getValue());

                    Edge edge = new Edge(ti, sysNode);
                    edge.attr(LABEL, entry2.getKey());
                    g.edge(edge);
                }
            }
        }

        return g;
    }

    private static String generateGraphForTestMonitorUsername(TestInstance testInstance) {
        if ((testInstance != null) && (testInstance.getMonitorInSession() != null)
                && (testInstance.getMonitorInSession().getUser() != null)) {
            return testInstance.getMonitorInSession().getUser().getUsername();
        } else {
            return null;
        }
    }

    private static Node generateGraphForTestCreateSystem(Graph g, Map<SystemInSession, Node> systems,
                                                         SystemInSession systemInSession) {
        Node system = systems.get(systemInSession);
        if (system == null) {
            system = new Node();
            system.attr("shape", "box");
            if ((systemInSession != null) && (systemInSession.getSystem() != null)
                    && (systemInSession.getSystem().getKeyword() != null)) {
                system.attr(LABEL, systemInSession.getSystem().getKeyword());
            } else {
                system.attr(LABEL, "???");
            }
            g.node(system);
            systems.put(systemInSession, system);
        }
        return system;
    }

    private static Node generateGraphForTestCreateTestInstance(Graph g, Map<TestInstance, Node> testInstances,
                                                               TestInstance testInstance) {
        Node ti = testInstances.get(testInstance);
        if (ti == null) {
            ti = new Node();
            ti.attr(COLOR, Status.getGraphColor(testInstance.getLastStatus()));
            ti.attr(LABEL, Integer.toString(testInstance.getId()) + SEPARATOR
                    + generateGraphForTestMonitorUsername(testInstance) + SEPARATOR
                    + testInstance.getLastStatus().getKeyword());
            g.node(ti);
            testInstances.put(testInstance, ti);
        }
        return ti;
    }

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public Test getTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTest");
        }
        return test;
    }

    public void loadTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTest");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String testId = fc.getExternalContext().getRequestParameterMap().get("id");
        if (testId != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            test = em.find(Test.class, Integer.valueOf(testId));
        }
        String testingSessionId = fc.getExternalContext().getRequestParameterMap().get("testingSessionId");
        if ((testingSessionId != null) && !"".equals(testingSessionId)) {
            EntityManager em = EntityManagerService.provideEntityManager();
            testingSession = em.find(TestingSession.class, Integer.valueOf(testingSessionId));
        }

    }

    public String getXDotBase64() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getXDotBase64");
        }
        Graph g = generateGraphForTest(entityManager, test, testingSession);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        List<String> commands = new ArrayList<String>();

        commands.add("neato");
        commands.add("-Txdot");

        String result = "";
        try {
            g.generateTo(commands, bos);
            bos.close();
            result = Base64.encodeBytes(bos.toByteArray(), Base64.DONT_BREAK_LINES);
        } catch (Exception e) {
            // FIXME
        }
        return result;
    }

    public void timelinePrepare() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("timelinePrepare");
        }
        TestTimeline timeline = new TestTimeline();

        HQLQueryBuilder<TestInstance> queryBuilderTI = new HQLQueryBuilder<TestInstance>(TestInstance.class);
        queryBuilderTI.addEq("test.id", test.getId());
        if (testingSession != null) {
            queryBuilderTI.addEq("testingSession.id", testingSession.getId());
        }
        List<TestInstance> list = queryBuilderTI.getList();

        List<Status> statuses = Arrays.asList(Status.values());

        for (TestInstance testInstance : list) {
            timeline.addTestInstance(testInstance, statuses);
        }

        timelineXML = timeline.getTimeLineXML();
        timelineStart = timeline.getStart();
        timelineEnd = timeline.getEnd();
    }

    public String redirectToKpiTest(Integer testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("redirectToKpiTest");
        }
        return "/testing/kpi/kpiTest.seam?id=" + testId;//DONE
    }

    public String redirectToKpiGraph(Integer testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("redirectToKpiGraph");
        }
        return "/testing/kpi/kpiTestCanviz.seam?id=" + testId;//DONE
    }

    public String redirectToKpiTimeline(Integer testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("redirectToKpiTimeline");
        }
        return "/testing/kpi/timeline/kpiTestTimeline.seam?id=" + testId;//DONE
    }

    public String timelineStartDate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("timelineStartDate");
        }
        return timelineStart;
    }

    public String timelineEndDate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("timelineEndDate");
        }
        return timelineEnd;
    }

    public String timelineXmlBase64() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("timelineXmlBase64");
        }
        return Base64.encodeBytes(timelineXML.getBytes(UTF_8), Base64.DONT_BREAK_LINES);
    }

}
