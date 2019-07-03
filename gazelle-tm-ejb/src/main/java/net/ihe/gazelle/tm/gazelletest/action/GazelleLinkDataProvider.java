package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.LinkDataProvider;
import net.ihe.gazelle.objects.model.ObjectInstance;
import net.ihe.gazelle.tf.model.Document;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.international.StatusMessage;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@MetaInfServices(LinkDataProvider.class)
public class GazelleLinkDataProvider implements LinkDataProvider {
    public static final GazelleLinkDataProvider INSTANCE = new GazelleLinkDataProvider();
    private static final Logger LOG = LoggerFactory.getLogger(GazelleLinkDataProvider.class);
    private static List<Class<?>> supportedClasses;

    static {
        supportedClasses = new ArrayList<Class<?>>();
        supportedClasses.add(TestInstance.class);
        supportedClasses.add(MonitorInSession.class);
        supportedClasses.add(Test.class);
        supportedClasses.add(User.class);

        supportedClasses.add(Institution.class);
        supportedClasses.add(System.class);
        supportedClasses.add(SystemInSession.class);

        supportedClasses.add(ObjectInstance.class);
        supportedClasses.add(Document.class);
    }

    @Override
    public List<Class<?>> getSupportedClasses() {
        return supportedClasses;
    }

    @Override
    public String getLabel(Object o, boolean detailed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        if (o instanceof TestInstance) {
            TestInstance testInstance = (TestInstance) o;
            if (detailed) {
                return testInstance.getId().toString() + " (" + testInstance.getTest().getKeyword() + ")";
            } else {
                return testInstance.getId().toString();
            }
        } else if (o instanceof MonitorInSession) {
            MonitorInSession monitorInSession = (MonitorInSession) o;
            if (detailed) {
                return monitorInSession.getUser().getFirstname() + " " + monitorInSession.getUser().getLastname()
                        + " (" + monitorInSession.getUser().getUsername() + ")";
            } else {
                return monitorInSession.getUser().getUsername();
            }
        } else if (o instanceof Test) {
            Test test = (Test) o;
            if (detailed) {
                return test.getKeyword();
            } else {
                return test.getKeyword();
            }
        } else if (o instanceof User) {
            User user = (User) o;
            if (detailed) {
                return user.getFirstname() + " " + user.getLastname() + " (" + user.getUsername() + ")";
            } else {
                return user.getUsername();
            }
        } else if (o instanceof Institution) {
            Institution institution = (Institution) o;
            if (detailed) {
                return institution.getKeyword() + " - " + institution.getName();
            } else {
                return institution.getKeyword();
            }
        } else if (o instanceof System) {
            System system = (System) o;
            return getSystemLabel(detailed, system);
        } else if (o instanceof SystemInSession) {
            SystemInSession systemInSession = (SystemInSession) o;
            System system = systemInSession.getSystem();
            return getSystemLabel(detailed, system);
        } else if (o instanceof ObjectInstance) {
            ObjectInstance objectInstance = (ObjectInstance) o;
            if (detailed) {
                return objectInstance.getName() + " - " + objectInstance.getObject().getDescription();
            } else {
                return objectInstance.getName();
            }
        } else if (o instanceof Document) {
            Document document = (Document) o;
            if (detailed) {
                return document.getName() + " - " + document.getLifecyclestatus();
            } else {
                return document.getName();
            }
        }
        return "";
    }

    protected String getSystemLabel(boolean detailed, System system) {
        if (detailed) {
            return system.getKeywordVersion() + " - " + system.getName();
        } else {
            return system.getKeyword();
        }
    }

    @Override
    public String getLink(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLink");
        }
        if (o instanceof TestInstance) {
            TestInstance testInstance = (TestInstance) o;
            return "testInstance.seam?id=" + testInstance.getId().toString();
        }
        if (o instanceof MonitorInSession) {
            MonitorInSession monitorInSession = (MonitorInSession) o;
            return "monitors/monitor.seam?id=" + monitorInSession.getId().toString();
        }
        if (o instanceof Test) {
            Test test = (Test) o;
            return "test.seam?id=" + test.getId().toString();
        }
        if (o instanceof User) {
            User user = (User) o;
            return "users/user.seam?id=" + user.getId().toString();
        }
        if (o instanceof Institution) {
            Institution institution = (Institution) o;
            return "users/institution/showInstitution.seam?id=" + institution.getKeyword();
        }
        if (o instanceof System) {
            System system = (System) o;
            return "systemInSession.seam?systemId=" + system.getId();
        }
        if (o instanceof Document) {
            Document document = (Document) o;
            return "documents/showDocument.seam?doc=" + document.getId();
        }
        if (o instanceof SystemInSession) {
            SystemInSession systemInSession = (SystemInSession) o;
            return "systemInSession.seam?systemInSessionId=" + systemInSession.getId();
        } else if (o instanceof ObjectInstance) {
            ObjectInstance objectInstance = (ObjectInstance) o;
            return "/objects/sample.seam?id=" + objectInstance.getId();
        }
        return "";
    }

    @Override
    public String getTooltip(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTooltip");
        }
        if (o instanceof TestInstance) {
            TestInstance testInstance = (TestInstance) o;
            String labelToDisplay = testInstance.getLastStatus().getLabelToDisplay();

            StringBuilder partners = new StringBuilder(" Partner(s) : ");
            List<TestInstanceParticipants> tipList = testInstance.getTestInstanceParticipants();
            for (TestInstanceParticipants tip : tipList) {
                partners.append(tip.getSystemInSessionUser().getSystemInSession().getSystem().getKeyword()).append(" ");
            }


            StringBuilder username = new StringBuilder("");
            MonitorInSession monitorInSession = testInstance.getMonitorInSession();
            if (monitorInSession != null) {
                User user = monitorInSession.getUser();
                if (user != null) {
                    username.append(" - ").append(user.getUsername());
                }
            }
            return StatusMessage.getBundleMessage(labelToDisplay, labelToDisplay) + username.toString() + partners.toString();
        }
        return "";
    }

}
