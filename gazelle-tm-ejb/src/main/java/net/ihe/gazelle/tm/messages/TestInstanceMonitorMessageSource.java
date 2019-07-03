package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.messaging.MessagePropertyChanged;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.users.model.User;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

@MetaInfServices(MessageSourceFromPropertyChange.class)
public class TestInstanceMonitorMessageSource extends MessageSourceUsersDB<TestInstance> implements
        MessageSourceFromPropertyChange<TestInstance, MonitorInSession> {
    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceMonitorMessageSource.class);
    private Collection<String> pathsToUser;

    public TestInstanceMonitorMessageSource() {
        super();
        pathsToUser = new ArrayList<String>(2);
        pathsToUser.add("listTestInstanceEvent.username");
    }

    @Override
    public Collection<String> getPathsToUsername() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPathsToUsername");
        }
        return pathsToUser;
    }

    @Override
    public Class<TestInstance> getSourceClass() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSourceClass");
        }
        return TestInstance.class;
    }

    @Override
    public String getLink(TestInstance instance, String[] messageParameters) {
        return "testInstance.seam?id=" + instance.getId();
    }

    @Override
    public Integer getId(TestInstance sourceObject, String[] messageParameters) {
        return sourceObject.getId();
    }

    @Override
    public String getImage(TestInstance sourceObject, String[] messageParameters) {
        return "gzl-icon-release";
    }

    @Override
    public String getType(TestInstance instance, String[] messageParameters) {
        return "gazelle.message.testinstance.monitor";
    }

    @Override
    public void filterUsers(HQLQueryBuilder<User> queryBuilder, String[] messageParameters) {
        // ALL
    }

    @Override
    public Class<TestInstance> getPropertyObjectClass() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPropertyObjectClass");
        }
        return TestInstance.class;
    }

    @Override
    public String getPropertyName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPropertyName");
        }
        return "monitorInSession";
    }

    @Override
    public void receivePropertyMessage(MessagePropertyChanged<TestInstance, MonitorInSession> messagePropertyChanged) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("receivePropertyMessage");
        }
        String lastMonitor = "None";
        if ((messagePropertyChanged.getOldValue() != null) && (messagePropertyChanged.getOldValue().getUser() != null)) {
            lastMonitor = messagePropertyChanged.getOldValue().getUser().getUsername();
        }
        String newMonitor = "None";
        if ((messagePropertyChanged.getNewValue() != null) && (messagePropertyChanged.getNewValue().getUser() != null)) {
            newMonitor = messagePropertyChanged.getNewValue().getUser().getUsername();
        }
        if (!lastMonitor.equals(newMonitor)) {
            MessageManager.addMessage(this, messagePropertyChanged.getObject(), newMonitor, lastMonitor, messagePropertyChanged.getObject()
                    .getId().toString());
        }
    }

}
