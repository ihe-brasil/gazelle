package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.messaging.MessagePropertyChanged;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.users.model.User;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

@MetaInfServices(MessageSourceFromPropertyChange.class)
public class TestInstanceStatusMessageSource extends MessageSourceUsersDB<TestInstance> implements
        MessageSourceFromPropertyChange<TestInstance, Status> {

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceStatusMessageSource.class);
    private Collection<String> pathsToUser;

    public TestInstanceStatusMessageSource() {
        super();
        pathsToUser = new ArrayList<String>(2);
        pathsToUser.add("listTestInstanceEvent.username");
        pathsToUser
                .add("testInstanceParticipants.systemInSessionUser.systemInSession.system.institutionSystems.institution.users.username");
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
        return "gzl-icon-" + messageParameters[3].replace(' ', '-');
    }

    @Override
    public String getType(TestInstance instance, String[] messageParameters) {
        return "gazelle.message.testinstance.status";
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
        return "lastStatus";
    }

    @Override
    public void receivePropertyMessage(MessagePropertyChanged<TestInstance, Status> messagePropertyChanged) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("receivePropertyMessage");
        }
        if (messagePropertyChanged.getObject().getId() != null) {
            String lastDisplay = "None";
            if (messagePropertyChanged.getOldValue() != null) {
                lastDisplay = messagePropertyChanged.getOldValue().getLabelToDisplay();
            }
            String newDisplay = "None";
            String newKeyword = "None";
            if (messagePropertyChanged.getNewValue() != null) {
                newDisplay = messagePropertyChanged.getNewValue().getLabelToDisplay();
                newKeyword = messagePropertyChanged.getNewValue().getKeyword();
            }
            if (!lastDisplay.equals(newDisplay)) {
                MessageManager.addMessage(this, messagePropertyChanged.getObject(), newKeyword, newDisplay, lastDisplay, messagePropertyChanged
                        .getObject()
                        .getId().toString());
            }
        }
    }

}
