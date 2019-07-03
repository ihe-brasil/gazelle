package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.messaging.MessagePropertyChanged;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceEvent;
import net.ihe.gazelle.users.model.User;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

@MetaInfServices(MessageSourceFromPropertyChange.class)
public class TestInstanceCommentMessageSource extends MessageSourceUsersDB<TestInstance> implements
        MessageSourceFromPropertyChange<TestInstance, TestInstanceEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceCommentMessageSource.class);
    private Collection<String> pathsToUser;

    public TestInstanceCommentMessageSource() {
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
        return "gzl-icon-comment-o";
    }

    @Override
    public String getType(TestInstance instance, String[] messageParameters) {
        return "gazelle.message.testinstance.comment";
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
        return "comment";
    }

    @Override
    public void receivePropertyMessage(MessagePropertyChanged<TestInstance, TestInstanceEvent> messagePropertyChanged) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("receivePropertyMessage");
        }
        MessageManager.addMessage(this, messagePropertyChanged.getObject(), messagePropertyChanged.getObject().getId().toString(),
                messagePropertyChanged.getNewValue().getUsername());
    }

}
