package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.users.model.User;

import java.util.List;

/**
 * A message source (like TestInstance, ...)<br />
 * Used to add a message
 *
 * @see MessageManager#addMessage(MessageSource, Object, String, String, String...)
 */
public interface MessageSource<T> {

    /**
     * This type is used for internationalization. The type should be in messages, as a title. The type + ".message" should be the message
     * parameterized for display.
     *
     * @return the message type
     */
    String getType(T instance, String[] messageParameters);

    /**
     * Return the list of users that should receive that message
     *
     * @param loggedInUser
     * @param sourceObject
     * @param messageParameters
     * @return May be null, using getPathsToUsername then
     */
    List<User> getUsers(User loggedInUser, T sourceObject, String[] messageParameters);

    /**
     * @param instance
     * @return the link of the page showing an instance of T, relative to context (test.seam?id=12)
     */
    String getLink(T instance, String[] messageParameters);

    /**
     * @param sourceObject      an instance of T
     * @param messageParameters message parameters of the message
     * @return an image path relative to context (img/failed.gif)
     */
    String getImage(T sourceObject, String[] messageParameters);

}
