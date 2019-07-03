package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.messaging.MessagePropertyChanged;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.users.model.User;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

@MetaInfServices(MessageSourceFromPropertyChange.class)
public class MesaTestInstanceStatusMessageSource extends MessageSourceUsersDB<TestInstanceParticipants> implements
        MessageSourceFromPropertyChange<TestInstanceParticipants, Status> {

    private static final Logger LOG = LoggerFactory.getLogger(MesaTestInstanceStatusMessageSource.class);
    private Collection<String> pathsToUser;

    public MesaTestInstanceStatusMessageSource() {
        super();
        pathsToUser = new ArrayList<String>(2);
        pathsToUser.add("testInstance.listTestInstanceEvent.username");
        pathsToUser
                .add("systemInSessionUser.systemInSession.system.institutionSystems.institution.users.username");
    }

    @Override
    public Class<TestInstanceParticipants> getPropertyObjectClass() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPropertyObjectClass");
        }
        return TestInstanceParticipants.class;
    }

    @Override
    public String getPropertyName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPropertyName");
        }
        return "preCatLastStatus";
    }

    @Override
    public Integer getId(TestInstanceParticipants sourceObject, String[] messageParameters) {
        return sourceObject.getId();
    }

    @Override
    public Collection<String> getPathsToUsername() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPathsToUsername");
        }
        return pathsToUser;
    }

    @Override
    public Class<TestInstanceParticipants> getSourceClass() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSourceClass");
        }
        return TestInstanceParticipants.class;
    }

    @Override
    public void filterUsers(HQLQueryBuilder<User> queryBuilder, String[] messageParameters) {
        //ALL
    }

    @Override
    public String getType(TestInstanceParticipants instance, String[] messageParameters) {
        return "gazelle.message.testinstance.status";
    }

    @Override
    public String getLink(TestInstanceParticipants instance, String[] messageParameters) {
        return "mesaTestInstance.seam?id=" + instance.getId();
    }

    @Override
    public String getImage(TestInstanceParticipants sourceObject, String[] messageParameters) {
        return "gzl-icon-" + messageParameters[3].replace(' ', '-');
    }

    @Override
    public void receivePropertyMessage(MessagePropertyChanged<TestInstanceParticipants, Status> messagePropertyChanged) {
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
