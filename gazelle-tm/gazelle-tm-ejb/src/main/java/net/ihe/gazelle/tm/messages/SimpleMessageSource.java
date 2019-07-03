package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.users.model.User;

import java.util.Arrays;
import java.util.List;

public class SimpleMessageSource implements MessageSource<User> {

    public static final SimpleMessageSource INSTANCE = new SimpleMessageSource();

    private SimpleMessageSource() {
        super();
    }

    @Override
    public String getLink(User instance, String[] messageParameters) {
        if ((messageParameters != null) && (messageParameters.length > 2)) {
            return messageParameters[2];
        } else {
            return "";
        }
    }

    @Override
    public String getImage(User sourceObject, String[] messageParameters) {
        return "gzl-icon-comment-o";
    }

    @Override
    public String getType(User instance, String[] messageParameters) {
        return "gazelle.message.simple";
    }

    @Override
    public List<User> getUsers(User loggedInUser, User sourceObject, String[] messageParameters) {
        String[] usersIds = messageParameters[0].split(" ");
        HQLQueryBuilder<User> builder = new HQLQueryBuilder<User>(User.class);
        builder.addIn("username", Arrays.asList(usersIds));
        return builder.getList();
    }

}
