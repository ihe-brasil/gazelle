package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * used on the comments of preCAT testInstances
 *
 * @author aboufahj
 */
public class TestInstanceParticipantCommentMessageSource extends MessageSourceUsersDB<TestInstanceParticipants> {

    public static final TestInstanceParticipantCommentMessageSource INSTANCE = new TestInstanceParticipantCommentMessageSource();

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceParticipantCommentMessageSource.class);

    private Collection<String> pathsToUser;

    private TestInstanceParticipantCommentMessageSource() {
        super();
        pathsToUser = new ArrayList<String>(2);
        pathsToUser.add("systemInSessionUser.user.username");
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
    public String getLink(TestInstanceParticipants instance, String[] messageParameters) {
        return "/mesaTestInstance.seam?id=" + instance.getId();
    }

    @Override
    public Integer getId(TestInstanceParticipants sourceObject, String[] messageParameters) {
        return sourceObject.getId();
    }

    @Override
    public String getImage(TestInstanceParticipants sourceObject, String[] messageParameters) {
        return "gzl-icon-comment-o";
    }

    @Override
    public String getType(TestInstanceParticipants instance, String[] messageParameters) {
        return "gazelle.message.testinstance.precomment";
    }

    @Override
    public void filterUsers(HQLQueryBuilder<User> queryBuilder, String[] messageParameters) {
        // ALL
    }

}
