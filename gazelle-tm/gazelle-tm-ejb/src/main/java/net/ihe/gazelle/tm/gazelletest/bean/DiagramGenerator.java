package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.filecache.FileCacheRenderer;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestSteps;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceQuery;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.sf.sdedit.config.Configuration;
import net.sf.sdedit.config.ConfigurationManager;
import net.sf.sdedit.diagram.Diagram;
import net.sf.sdedit.error.SemanticError;
import net.sf.sdedit.error.SyntaxError;
import net.sf.sdedit.server.Exporter;
import net.sf.sdedit.text.TextHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.*;

public class DiagramGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(DiagramGenerator.class);

    private static String safeDiagramString(String input) {
        String output = input;
        output = output.replace(".", "\\.");
        output = output.replace(":", "\\:");
        output = output.replace("=", "\\=");
        return output;
    }

    private static void addStart(StringBuilder sb, String keyword) {
        sb.append("#![").append(keyword).append("]\nuser:Actor\n");
    }

    private static void addActor(StringBuilder sb, String keyword, Set<String> actors) {
        sb.append(safeDiagramString(keyword));
        sb.append(":");
        sb.append(safeDiagramString(StringUtils.join(actors, '/')));
        sb.append("\n");
    }

    private static void addTestStep(StringBuilder sb, TestSteps testStep, String initiator, String responder) {
        if (testStep.getDescription().length() > 0) {
            sb.append("user:");
            sb.append(safeDiagramString(initiator));
            sb.append(".");

            String description = testStep.getDescription();
            String[] lines = description.split("\r\n|\r|\n");
            boolean first = true;
            for (String line : lines) {
                if (first) {
                    first = false;
                } else {
                    sb.append("\\\\n");
                }
                String newLine = WordUtils.wrap(line, 40);
                newLine = newLine.replace("\r\n", "\\\\n");
                newLine = newLine.replace("\r", "\\\\n");
                newLine = newLine.replace("\n", "\\\\n");
                sb.append(safeDiagramString(newLine));
            }
            sb.append("\n");
        }
        sb.append(safeDiagramString(initiator));
        sb.append(": =");
        sb.append(safeDiagramString(responder));
        sb.append(".");
        if (testStep.getTransaction() != null) {
            sb.append(testStep.getTransaction().getKeyword());
            sb.append(" : ");
            sb.append(testStep.getMessageType());
        }
        if (testStep.getSecured()) {
            sb.append("{colour=red}");
        }

        sb.append("\n");
        sb.append("\n");
    }

    public static StringBuilder getTestInstanceSequenceDiagramSD(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StringBuilder getTestInstanceSequenceDiagramSD");
        }
        StringBuilder sb = new StringBuilder();
        addStart(sb, testInstance.getTest().getKeyword());

        TestInstanceQuery testInstanceQuery = new TestInstanceQuery();
        testInstanceQuery.id().eq(testInstance.getId());
        List<SystemInSession> sisList = testInstanceQuery.testInstanceParticipants().systemInSessionUser()
                .systemInSession().getListDistinct();
        if (sisList != null) {
            for (SystemInSession sis : sisList) {
                List<TestInstanceParticipants> tipList = TestInstanceParticipants
                        .getTestInstanceParticipantsByTestInstanceBySystemInSession(testInstance, sis);
                Set<String> actors = new TreeSet<String>();
                for (TestInstanceParticipants tip : tipList) {
                    if (tip.getActorIntegrationProfileOption() != null) {
                        actors.add(tip.getActorIntegrationProfileOption().getActorIntegrationProfile().getActor()
                                .getKeyword());
                    }
                }
                addActor(sb, sis.getSystem().getKeyword(), actors);
            }
        }
        sb.append("\n");

        List<TestStepsInstance> tsl = testInstance.getTestStepsInstanceList();
        Collections.sort(tsl);
        for (TestStepsInstance ts : tsl) {
            TestSteps testStep = ts.getTestSteps();
            String initiator = ts.getSystemInSessionInitiator().getSystem().getKeyword();
            String responder = ts.getSystemInSessionResponder().getSystem().getKeyword();

            addTestStep(sb, testStep, initiator, responder);
        }
        return sb;
    }

    public static StringBuilder getTestSequenceDiagramSD(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StringBuilder getTestSequenceDiagramSD");
        }
        StringBuilder sb = new StringBuilder();
        addStart(sb, test.getKeyword());

        List<TestRoles> testRolesList = TestRoles.getTestRolesListForATest(test);
        if (testRolesList != null) {
            for (TestRoles tr : testRolesList) {
                TreeSet<String> actors = new TreeSet<String>();

                if (tr.getRoleInTest().getTestParticipantsList() != null) {
                    for (TestParticipants tp : tr.getRoleInTest().getTestParticipantsList()) {
                        if (tp.getTested()) {
                            actors.add(tp.getActorIntegrationProfileOption().getActorIntegrationProfile().getActor()
                                    .getKeyword());
                        }
                    }
                }

                if (actors.size() == 0) {
                    actors.add(tr.getRoleInTest().getKeyword());
                }
                addActor(sb, tr.getRoleInTest().getKeyword(), actors);
            }
        }
        sb.append("\n");

        List<TestSteps> tsl = new ArrayList<TestSteps>(test.getTestStepsList());
        Collections.sort(tsl);
        for (TestSteps testStep : tsl) {
            String initiator = testStep.getTestRolesInitiator().getRoleInTest().getKeyword();
            String responder = testStep.getTestRolesResponder().getRoleInTest().getKeyword();

            addTestStep(sb, testStep, initiator, responder);
        }
        return sb;
    }

    public static void getTestSequenceDiagram(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void getTestSequenceDiagram");
        }
        StringBuilder sb = getTestSequenceDiagramSD(test);
        generateDiagram(sb.toString(), "SequenceDiagram_" + test.getKeyword());
    }

    public static void updateSequenceDiagram(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void getTestSequenceDiagram");
        }
        StringBuilder sb = getTestSequenceDiagramSD(test);
        updateDiagram(sb.toString(), "SequenceDiagram_" + test.getKeyword());
    }

    public static void getTestInstanceSequenceDiagram(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void getTestInstanceSequenceDiagram");
        }
        StringBuilder sb = getTestInstanceSequenceDiagramSD(testInstance);
        generateDiagram(sb.toString(), "SequenceDiagram_TI_" + testInstance.getId());
    }

    private static void generateDiagram(String sequenceDiagramString, String cacheKey) {

        try {
            try {
                FileCache.getFile(cacheKey, sequenceDiagramString, new FileCacheRenderer() {
                    @Override
                    public void render(OutputStream out, String value) throws Exception {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("render");
                        }
                        generatePNG(out, value);
                    }

                    @Override
                    public String getContentType() {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("getContentType");
                        }
                        return "image/png";
                    }
                });
            } catch (SyntaxError se) {
                TextHandler th = (TextHandler) se.getProvider();
                String msg = "ERROR:syntax error in line " + th.getLineNumber() + ": " + se.getMessage() + "\n" + sequenceDiagramString;
                LOG.error(msg, se);
            } catch (SemanticError se) {
                TextHandler th = (TextHandler) se.getProvider();
                String msg = "ERROR:semantic error in line " + th.getLineNumber() + ": " + se.getMessage() + "\n" + sequenceDiagramString;
                LOG.error(msg, se);
            } catch (Throwable t) {
                LOG.error("ERROR:fatal error: " + t.getMessage(), t);
            }
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void updateDiagram(String sequenceDiagramString, String cacheKey) {

        try {
            try {
                FileCache.updateFile(cacheKey, sequenceDiagramString, new FileCacheRenderer() {
                    @Override
                    public void render(OutputStream out, String value) throws Exception {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("render");
                        }
                        generatePNG(out, value);
                    }

                    @Override
                    public String getContentType() {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("getContentType");
                        }
                        return "image/png";
                    }
                });
            } catch (SyntaxError se) {
                TextHandler th = (TextHandler) se.getProvider();
                String msg = "ERROR:syntax error in line " + th.getLineNumber() + ": " + se.getMessage() + "\n" + sequenceDiagramString;
                LOG.error(msg, se);
            } catch (SemanticError se) {
                TextHandler th = (TextHandler) se.getProvider();
                String msg = "ERROR:semantic error in line " + th.getLineNumber() + ": " + se.getMessage() + "\n" + sequenceDiagramString;
                LOG.error(msg, se);
            } catch (Throwable t) {
                LOG.error("ERROR:fatal error: " + t.getMessage(), t);
            }
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    public static void generatePNG(OutputStream out, String value) throws SemanticError, SyntaxError {
        Exporter exporter = Exporter.getExporter("png", null, "A4", out);
        if (exporter == null) {
            throw new RuntimeException("FreeHEP library missing.");
        }
        Configuration conf = ConfigurationManager.createNewDefaultConfiguration().getDataObject();
        conf.setThreaded(false);
        Diagram diagram = new Diagram(conf, new TextHandler(value), exporter);
        diagram.generate();
        exporter.export();
    }

}
