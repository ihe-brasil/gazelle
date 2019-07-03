/*
 * Copyright 2008 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ihe.gazelle.users.action;

import net.ihe.gazelle.menu.GazelleMenu;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.users.model.UserQuery;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * <b>Class Description : </b>Authentication Module<br>
 * <br>
 * This class authenticates a user with the Gazelle application.This module check that the login/password are stored in the database, and grant the
 * access depending of the user's role.
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @class Authenticator.java
 * @package net.ihe.gazelle.common.authentication
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */
@Name("authenticator")
public class Authenticator {
    // ~ Attributes ////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final Logger LOG = LoggerFactory.getLogger(Authenticator.class);

    /**
     * Identity to get informations about the user in the session context
     */
    @In(create = true)
    Identity identity;

    @In
    Credentials credentials;

    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;

    private User selectedUser;

    private List<User> users;

    /**
     * Activation Code sent by the user, clicking on the activation URL in the email sent after new account registration
     */
    @In(required = false)
    private String activationCode;

    /**
     * ChangePasswordCode sent by the user, clicking on the changePassword URL in the email sent after a password assistance (password lost)
     */
    private String changePasswordCode;

    // ~ Methods ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void resetSession(User user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void resetSession");
        }
        Contexts.getSessionContext().set("selectedUser", user);
    }

    /**
     * Get the user with the same activation code, waiting for an activation
     *
     * @return User : user account to activate
     */
    public static User findUserWithActivationCode(String code) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("User findUserWithActivationCode");
        }
        UserQuery query = new UserQuery();
        query.activationCode().eq(code);
        List<User> foundUsers = query.getList();

        if (foundUsers.size() != 1) {
            LOG.error("-----------------------------------------------------------------------------");
            LOG.error("None or several USERS found with the searched activation code ! ONE IS REQUIRED ! FATAL Error ! FOUND USERS = "
                    + foundUsers.size());

            for (int l = 0; l < foundUsers.size(); l++) {
                LOG.error(l + " - User = Id(" + foundUsers.get(l).getId() + ") " + foundUsers.get(l).getUsername());

            }

            LOG.error("-----------------------------------------------------------------------------");

            return null;
        }

        return foundUsers.get(0);
    }

    /**
     * Get the activation code - code in the email sent after a user account registration
     *
     * @return String activationCode
     */
    public String getActivationCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActivationCode");
        }
        return activationCode;
    }

    /**
     * Set the activation code - code in the email sent after a user account registration
     *
     * @param String activationCode
     */
    public void setActivationCode(String activationCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActivationCode");
        }
        this.activationCode = activationCode;
    }

    /**
     * Get the changePassword code - code in the email sent after a password assistance (password lost)
     *
     * @return String changePasswordCode
     */
    public String getChangePasswordCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChangePasswordCode");
        }
        return changePasswordCode;
    }

    /**
     * Set the changePassword code - code in the email sent after a password assistance (password lost)
     *
     * @param String changePasswordCode
     */
    public void setChangePasswordCode(String changePasswordCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setChangePasswordCode");
        }
        this.changePasswordCode = changePasswordCode;
    }

    /**
     * Global Authentication method to login to the Gazelle application We check if the user is allowed to connect Then, we set the roles
     *
     * @return false if authentication is wrong, else it returns true (then the user will be logged in)
     */
    public boolean authenticate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("authenticate");
        }

        // write your authentication logic here,
        // return true if the authentication was
        // successful, false otherwise

        // We check if the user exists

        // final User
        selectedUser = authenticateUser(credentials.getUsername().trim(), credentials.getPassword());

        if (selectedUser == null) {

            return false;
        }

        // We check the role of this user

        setRolesAndAccessLevels(selectedUser);

        LOG.warn("Login : " + selectedUser.getFirstname() + " " + selectedUser.getLastname() + " from "
                + selectedUser.getInstitution().getName() + " is logged in !    (" + selectedUser.getUsername() + ", "
                + selectedUser.getEmail() + ", logins = " + selectedUser.getLoginsCounter() + ")");

        // We update lastLogin information

        selectedUser.setLastLogin(new Date());
        selectedUser.setLoginsCounter(selectedUser.getLoginsCounter() + 1);
        selectedUser.setFailedLoginAttemptsCounter(0);
        if (selectedUser.getActivationCode() != null) {
            selectedUser.setActivationCode(null);
        }
        selectedUser = entityManager.merge(selectedUser);

        resetSession(selectedUser);
        GazelleMenu.refreshMenu();

        return true;
    }

    public String authenticateWithoutCheckingPassword(User inUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("authenticateWithoutCheckingPassword");
        }

        User oldLoggedUser = User.loggedInUser();
        for (Role r : oldLoggedUser.getRoles()) {
            identity.removeRole(r.getName());
        }

        if (inUser == null) {

            return null;
        }

        Identity.instance().getCredentials().setUsername(inUser.getUsername());
        // We check the role of this user

        setRolesAndAccessLevels(inUser);

        // We update lastLogin information

        inUser.setLastLogin(new Date());
        inUser.setLoginsCounter(inUser.getLoginsCounter() + 1);
        inUser.setFailedLoginAttemptsCounter(0);
        if (inUser.getActivationCode() != null) {
            inUser.setActivationCode(null);
        }
        inUser = entityManager.merge(inUser);

        resetSession(inUser);

        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.users.user.YouAreNowLoggedInAs", inUser.getUsername());
        return "/home.seam";
    }

    /**
     * Authentication method to login to the Gazelle application
     *
     * @param username Username (login) of this user
     * @param password Password of this user
     * @return null if authentication is wrong, else it returns the user
     */
    private User authenticateUser(final String username, final String password) {
        String hashPassword;

        User u = User.FindUserWithUsername(username);
        if (u == null) {
            LOG.warn("Invalid authentication credentials for username '" + username + " . This user does not exist...'");
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.authenticator.WrongUsername", username);

            return null;
        }

        // Password stored in database is null or the password is empty
        if ((u.getPassword() == null) || (u.getPassword().equals(""))) {
            LOG.warn("Invalid authentication credentials for username " + username
                    + " . The password is null in database...");

            return null;
        }

        // Password given is null - This case has to be check by the JSF
        if ((password == null) || (password.equals(""))) {
            // This case has to be checked by the JSF
            LOG.error("Invalid authentication credentials for username - PASSWORD NULL - This case has to be checked by the JSF ");

            return null;
        }

        // Hashcode for password

        hashPassword = password;

        // Password given is wrong
        if (!u.getPassword().equals(hashPassword)) {

            Integer FailedLoginAttemptsCount = u.getFailedLoginAttemptsCounter() + 1;
            u.setFailedLoginAttemptsCounter(FailedLoginAttemptsCount);

            Integer iMaxLoginAttemptsAuthorized = 5;
            if (FailedLoginAttemptsCount >= iMaxLoginAttemptsAuthorized) {
                LOG.warn("User " + u.getUsername() + " is now blocked ");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.WARN,
                        "gazelle.users.authenticator.AccountBlockedDueToMoreThanXLoginFailures",
                        iMaxLoginAttemptsAuthorized);

                u.setBlocked(true);
            } else {
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.authenticator.WrongPassword",
                        iMaxLoginAttemptsAuthorized - FailedLoginAttemptsCount);

            }

            entityManager.merge(u);

            return null;
        }

        // We have to check if the user account is blocked
        if (u.getBlocked()) {

            LOG.error("This user account is BLOCKED : " + username);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.authenticator.AccountIsBlocked");
            return null;
        }

        // We have to check if the user account is activated
        if ((!u.getActivated()) && (u.getChangePasswordCode() == null)) {

            LOG.error("This user account is NOT ACTIVATED : " + username);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.authenticator.AccountIsNotActivated");
            return null;
        }

        return u;
    }

    /**
     * Set Roles and access levels to the given User
     *
     * @param user the current user
     */
    private void setRolesAndAccessLevels(final User user) {
        List<Role> foundRoles;

        // We retrieve the roles corresponding to this user
        foundRoles = user.getRoles();

        for (int i = 0; i < foundRoles.size(); i++) {

            identity.addRole(foundRoles.get(i).getName());
        }
    }

    /**
     * Active a new user account (after the email validation) for the Gazelle application We check if this code is waiting for an activation, and
     * we active the associated account
     *
     * @return String : determinate where to redirect the user (check your pages.xml file)
     */
    public String activate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activate");
        }

        User user = findUserWithActivationCode(activationCode);

        if (user != null) {
            user.setActivated(true);

            entityManager.merge(user);
            entityManager.flush();

            Institution activatedInstitution = entityManager.find(Institution.class, user.getInstitution().getId());
            activatedInstitution.setActivated(true);
            entityManager.persist(activatedInstitution);

            users = User.getUsersFiltered(activatedInstitution, null);

            return "activated";

        } else {
            return "notFound";
        }
    }

    /**
     * @TODO JBM Sould be executed unlogged
     */
    public String editUserWithActivationCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editUserWithActivationCode");
        }

        selectedUser = findUserWithActivationCode(activationCode);

        if (selectedUser != null) {
            Contexts.getSessionContext().set("selectedUser", selectedUser);
            return "changePassword";

        } else {

            return "notFound";
        }

    }

    /**
     * Password assistance - user forgot his password and received an email with a link containing a changePasswordCode If this code is existing
     * than we redirect the user to a page, where he will
     * change his password. If this code does not exist, user is redirect to the homepage
     *
     * @return String : determinate where to redirect the user (check your pages.xml file)
     */
    public String changeLostPassword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeLostPassword");
        }

        selectedUser = findUserWithChangePasswordCode(changePasswordCode);

        if ((selectedUser != null) && (!identity.isLoggedIn())) {
            Contexts.getSessionContext().set("selectedUser", selectedUser);

            return "changePassword";

        } else {
            return "notFound";
        }
    }

    /**
     * Get the user with the same changePasswordCode code, waiting for a password assistance
     *
     * @return User : user account to assist (change password)
     */
    @SuppressWarnings("unchecked")
    public User findUserWithChangePasswordCode(String code) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findUserWithChangePasswordCode");
        }
        List<User> foundUsers;

        Query query = entityManager.createQuery("SELECT u FROM User u where u.changePasswordCode = :code ");
        query.setParameter("code", code);
        foundUsers = query.getResultList();

        if (foundUsers.size() != 1) {
            LOG.error("-----------------------------------------------------------------------------");
            LOG.error("FATAL : [Password assistance - password lost]" + foundUsers.size()
                    + " user(s) found with the changePasswordCode ! ");

            for (int l = 0; l < foundUsers.size(); l++) {
                LOG.error(l + " - User = Id(" + foundUsers.get(l).getId() + ") " + foundUsers.get(l).getUsername());

            }

            LOG.error("-----------------------------------------------------------------------------");

            return null;
        }

        return foundUsers.get(0);
    }

    /**
     * Logout method - used to reset roles
     *
     * @return String : loggedOut
     */

    public String logout() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("logout");
        }
        Identity.instance().logout();

        return "loggedOut";
    }
}
