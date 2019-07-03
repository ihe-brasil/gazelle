package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.users.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MessageSourceUsersDB<T> implements MessageSource<T> {

    @Override
    public List<User> getUsers(User loggedInUser, T sourceObject, String[] messageParameters) {
        EntityManagerService.provideEntityManager();

        Collection<String> pathsToUser = getPathsToUsername();
        Set<String> usernames = new HashSet<String>();
        for (String pathToUser : pathsToUser) {
            HQLQueryBuilder<T> queryBuilder = new HQLQueryBuilder<T>(getSourceClass());
            queryBuilder.addEq("id", getId(sourceObject, messageParameters));

            List<?> values = queryBuilder.getListDistinct(pathToUser);
            for (Object value : values) {
                if (value instanceof String) {
                    usernames.add((String) value);
                }
            }
        }

        if (loggedInUser != null) {
            usernames.remove(loggedInUser.getUsername());
        }

        HQLQueryBuilder<User> queryBuilder = new HQLQueryBuilder<User>(User.class);
        queryBuilder.addIn("username", usernames);
        filterUsers(queryBuilder, messageParameters);
        return queryBuilder.getList();
    }

    /**
     * @param sourceObject
     * @return the id of sourceObject
     */
    public abstract Integer getId(T sourceObject, String[] messageParameters);

    /**
     * Paths from T to the usernames
     */
    public abstract Collection<String> getPathsToUsername();

    /**
     * @return T class
     */
    public abstract Class<T> getSourceClass();

    /**
     * Filter users if the message source should not send messages to all users (only vendors for instance)
     *
     * @param queryBuilder
     */
    public abstract void filterUsers(HQLQueryBuilder<User> queryBuilder, String[] messageParameters);

}
