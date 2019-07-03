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

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.statusMessage.StatusMessageProvider;
import net.ihe.gazelle.tm.filter.valueprovider.InstitutionFixer;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.users.model.UserPreferences;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.users.model.UserQuery;
import net.ihe.gazelle.util.Md5Encryption;
import net.ihe.gazelle.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;

/**
 * <b>Class Description : </b>UserManager<br>
 * <br>
 * This class manage the User (login) object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add a
 * user (login)</li> <li>Delete a user</li> <li>Show a
 * user</li> <li>Edit a user</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */

@Scope(ScopeType.SESSION)
@Name("userManager")
@Synchronized(timeout = 10000)
@GenerateInterface("UserManagerLocal")
public class UserManager implements Serializable, UserManagerLocal, QueryModifier<User> {

    /**
     * Field DEFAULT_PASSWORD. (value is ""toChange"")
     */
    public static final String DEFAULT_PASSWORD = "toChange";
    /**
     * Field UNCHECKED. (value is ""unchecked"")
     */
    private static final String UNCHECKED = "unchecked";
    /**
     * Field PASSWORD_HAS_CHANGED. (value is ""Password has changed. Updating and hashing NEW password"")
     */
    private static final String PASSWORD_HAS_CHANGED = "Password has changed. Updating and hashing NEW password";
    /**
     * Field NULL. (value is ""NULL"")
     */
    private static final String NULL = "NULL";
    /**
     * Field USER_SUCCESSFULLY_UPDATED. (value is ""gazelle.users.user.faces.UserSuccessfullyUpdated"")
     */
    private static final String USER_SUCCESSFULLY_UPDATED = "gazelle.users.user.faces.UserSuccessfullyUpdated";
    /**
     * Field USER_SUCCESSFULLY_CREATED. (value is ""gazelle.users.user.faces.UserSuccessfullyCreated"")
     */
    private static final String USER_SUCCESSFULLY_CREATED = "gazelle.users.user.faces.UserSuccessfullyCreated";
    /**
     * Field USER_SUCCESSFULLY_UPDATED_BY_ADMIN. (value is ""gazelle.users.user.faces.UserSuccessfullyCreatedByAdmin"")
     */
    private static final String USER_SUCCESSFULLY_UPDATED_BY_ADMIN = "gazelle.users.user.faces.UserSuccessfullyCreatedByAdmin";
    /**
     * Field EMAIL_SENT_TO_ADMIN_AFTER_USER_CREATION. (value is ""gazelle.users.user.faces.EmailSentToAdminAfterUserCreation"")
     */
    private static final String EMAIL_SENT_TO_ADMIN_AFTER_USER_CREATION = "gazelle.users.user.faces.EmailSentToAdminAfterUserCreation";
    /**
     * Field PASSWORDS_DO_NOT_MATCH. (value is ""gazelle.users.registration.faces.PasswordsDoNotMatch"")
     */
    private static final String PASSWORDS_DO_NOT_MATCH = "gazelle.users.registration.faces.PasswordsDoNotMatch";
    /**
     * Field USERS_USER_REGISTER_SEAM. (value is ""/users/user/register.seam"")
     */
    private static final String USERS_USER_REGISTER_SEAM = "/users/user/register.seam";
    /**
     * Field USERS_USER_LIST_USERS_INSTITUTION_SEAM. (value is ""/users/user/listUsersInstitution.seam"")
     */
    private static final String USERS_USER_LIST_USERS_INSTITUTION_SEAM = "/users/user/listUsersInstitution.seam";
    /**
     * Field USERS_USER_CHANGE_USER_PASSWORD_SEAM. (value is ""/users/user/changeUserPassword.seam"")
     */
    private static final String USERS_USER_CHANGE_USER_PASSWORD_SEAM = "/users/user/changeUserPassword.seam";
    /**
     * Field USERS_LOGIN_LOGIN_SEAM. (value is ""/users/login/login.seam"")
     */
    private static final String USERS_LOGIN_LOGIN_SEAM = "/users/login/login.seam";
    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450911336361283760L;
    /**
     * Field Log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserManager.class);
    /**
     * A new user does not belong to an institution. We set a default institution value (No institution) during the account registration
     */
    private final static String DEFAULTINSTITUTIONKEYWORD = NULL;

    // ~ Statics variables and Class initializer
    // ///////////////////////////////////////////////////////////////////////
    public String currentEmail;
    /**
     * Renderer converts the internal representation of UIComponents into the output stream (or writer) associated with the response we are
     * creating for a particular request.!
     */
    @In
    private Renderer renderer;

    // ~ IN/OUT Attributes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Selected institution choosen by the admin in a management page (users/systems/contacts..) - Institution object managed by this bean ,
     * variable used between business and presentation layer
     */
    @In(required = false)
    @Out(required = false)
    private Institution choosenInstitutionForAdmin;
    /**
     * Activation Code sent by the user, clicking on the activation URL in the email sent after new account registration
     */
    @In(required = false)
    private String activationCode;
    /**
     * Field emailManager.
     */
    @In
    private EmailManagerLocal emailManager;
    /**
     * Field sct.
     */
    @Resource
    private SessionContext sct;
    /**
     * User object managed by this manager bean
     */
    private User selectedUser;

    // ~ Attributes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Field userToUse.
     */
    private User userToUse;
    /**
     * Field selectedInstitution.
     */
    private Institution selectedInstitution;
    /**
     * List of all user objects to be managed by this manager bean
     */

    private List<User> users;
    /**
     * Field rolesForUser.
     */
    private List<Pair<Boolean, Role>> rolesForUser;
    /**
     * Field displayEditPanel.
     */
    private Boolean displayEditPanel = false;
    /**
     * Field displayViewPanel.
     */
    private Boolean displayViewPanel = false;
    /**
     * Field displayListPanel.
     */
    private Boolean displayListPanel = true;
    /**
     * Field selectedRoles.
     */
    private List<String> selectedRoles;
    private FilterDataModel<User> datamodel = null;
    private Filter<User> filter = null;

    public String getCurrentEmail() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentEmail");
        }
        return currentEmail;
    }

    public void setCurrentEmail(String currentEmail) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCurrentEmail");
        }
        this.currentEmail = currentEmail;
    }

    public FilterDataModel<User> getDatamodel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDatamodel");
        }
        if (datamodel == null) {
            datamodel = new FilterDataModel<User>(getFilter()) {
                @Override
                protected Object getId(User t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        if (choosenInstitutionForAdmin != null) {
            datamodel = new FilterDataModel<User>(getFilter(choosenInstitutionForAdmin)) {
                @Override
                protected Object getId(User t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
            choosenInstitutionForAdmin = null;
        }
        return datamodel;
    }

    // ~ Methods
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void resetCache() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetCache");
        }
        if (datamodel != null) {
            datamodel.resetCache();
        }
    }

    public void clearFilters() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearFilters");
        }
        getFilter().clear();
    }

    public Filter<User> getFilter(Institution institutionDefaultValue) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        UserQuery query = new UserQuery();

        HQLCriterionsForFilter<User> hqlCriterions = query.getHQLCriterionsForFilter();

        if (institutionDefaultValue != null) {
            hqlCriterions.addPath("institution", query.institution(), institutionDefaultValue,
                    InstitutionFixer.INSTANCE);
        } else {
            hqlCriterions.addPath("institution", query.institution(), InstitutionFixer.INSTANCE,
                    InstitutionFixer.INSTANCE);
        }

        hqlCriterions.addPath("username", query.username());
        hqlCriterions.addPath("firstname", query.firstname());
        hqlCriterions.addPath("lastname", query.lastname());
        hqlCriterions.addPath("activated", query.activated());
        hqlCriterions.addPath("blocked", query.blocked());
        hqlCriterions.addPath("role", query.roles().name());

        hqlCriterions.addQueryModifier(this);

        filter = new Filter<User>(hqlCriterions, requestParameterMap);
        filter.getFilterValues();
        return filter;
    }

    public Filter<User> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            return getFilter(null);
        } else {
            return filter;
        }
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<User> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }

        // Duplicates with: hqlCriterions.addPath("institution", query.institution(), InstitutionFixer.INSTANCE);
        // UserQuery query = new UserQuery(queryBuilder);
        // if (!User.loggedInUser().hasRole(Role.getADMINISTRATOR_ROLE())) {
        // query.addRestriction(query.institution().eqRestriction(Institution.getLoggedInInstitution()));
        // }
    }

    /**
     * Method getSelectedRoles.
     *
     * @return List<String>
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getSelectedRoles()
     */
    @Override
    public List<String> getSelectedRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedRoles");
        }
        return selectedRoles;
    }

    /**
     * Method setSelectedRoles.
     *
     * @param selectedRoles List<String>
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setSelectedRoles(List<String>)
     */
    @Override
    public void setSelectedRoles(List<String> selectedRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedRoles");
        }
        this.selectedRoles = selectedRoles;
    }

    /**
     * Method displayUsersForCompany.
     *
     * @param i Institution
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#displayUsersForCompany(Institution)
     */
    @Override
    public String displayUsersForCompany(Institution i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayUsersForCompany");
        }
        choosenInstitutionForAdmin = i;
        Contexts.getSessionContext().set("choosenInstitutionForAdmin", choosenInstitutionForAdmin);
        return "/users/user/listUsersInstitution.xhtml";
    }

    /**
     * Method getLoggedInUser.
     *
     * @return User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getLoggedInUser()
     */
    @Override
    public User getLoggedInUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLoggedInUser");
        }
        return User.loggedInUser();
    }

    /**
     * Method used to retrieve the user to display when the permanent link /users/user.seam?id=... is accessed by user
     *
     * @return User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getUserById()
     */
    @Override
    public User getUserById() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserById");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String userId = params.get("id");
        if ((userId != null) && !userId.isEmpty()) {
            try {
                Integer id = Integer.decode(userId);
                EntityManager em = EntityManagerService.provideEntityManager();
                setSelectedUser(em.find(User.class, id));
                return em.find(User.class, id);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Action done when the button Search is clicked. We set the previous search to null.
     *
     * @return JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#doFindCompanies()
     */
    @Override
    public String doFindCompanies() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doFindCompanies");
        }
        return USERS_USER_LIST_USERS_INSTITUTION_SEAM;
    }

    /**
     * Method getUsers.
     *
     * @return List<User>
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getUsers()
     */
    @Override
    public List<User> getUsers() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsers");
        }
        return users;
    }

    /**
     * Method setUsers.
     *
     * @param users List<User>
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setUsers(List<User>)
     */
    @Override
    public void setUsers(List<User> users) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setUsers");
        }
        this.users = users;
    }

    /**
     * Method findUsersForCompany.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#findUsersForCompany()
     */
    @Override
    public void findUsersForCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findUsersForCompany");
        }
        if ((choosenInstitutionForAdmin != null)) {
            this.users = User.getUsersFiltered(choosenInstitutionForAdmin, null);
        } else {

            this.users = User.listAllUsers();
        }
    }

    /**
     * Method addNewUserButton.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#addNewUserButton()
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'addNewUserButton', null)}")
    public void addNewUserButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewUserButton");
        }

        selectedUser = new User();

        if (Role.isLoggedUserAdmin()) {
            rolesForUser = Role.getRoleAttributableIfWithBoolean(Role.ADMINISTRATOR_ROLE_STRING);
        } else if (Role.isLoggedUserVendorAdmin()) {

            rolesForUser = Role.getRoleAttributableIfWithBoolean(Role.VENDOR_ADMIN_ROLE_STRING);
        }
        openEditPanel();
    }

    @Override
    public boolean isNewUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isNewUser");
        }
        if (isCreatingAUser()) {
            return true;
        }
        return false;
    }

    /**
     * Add (create/update) a user to the database This operation is allowed for some granted users (check the security.drl) Used to register a new
     * user with a vendor_admin role. Used in register.xhtml
     *
     * @return String : JSF page to render * @throws Exception * @see net.ihe.gazelle.users.action.UserManagerLocal#addUser()
     * @throws Exception
     */
    @Override
    @Deprecated
    // look like this is not used!
    @Restrict("#{s:hasPermission('UserManager', 'addUser', null)}")
    public String addUser() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addUser");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        // If the User does not exist, we create it
        if (!isUserNameAvailable()) {
            return USERS_USER_REGISTER_SEAM;
        }
        if (!isUserEmailAvailable()) {
            return USERS_USER_REGISTER_SEAM;
        }

        if (isCreatingAUser()) {

            // We check the password and the password confirmation are the same
            // string.
            if (!selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation())) {
                LOG.warn("Passwords do not match. Please re-enter your password.");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, PASSWORDS_DO_NOT_MATCH);
                return USERS_USER_REGISTER_SEAM;
            }
            // We hash and update the new password
            String hashPassword = Md5Encryption.hashPassword(selectedUser.getPassword());
            selectedUser.setPassword(hashPassword);

            // User has no institution. He will update his institution after his
            // account activation (after email confirmation)
            // We set the null institution before his activation
            Institution nullInstitution = Institution.findInstitutionWithKeyword(NULL);
            selectedUser.setInstitution(nullInstitution);

            // At that time user, has no role, there are defined after the
            // activation.

            // We set the activated attribute to false
            selectedUser.setActivated(false);

            // If the procedure is a lost password, we set the activated
            // attribute to true and activation code to null
            if (selectedUser.getChangePasswordCode() != null) {
                selectedUser.setActivated(true);
                selectedUser.setActivationCode(null);

                // We set the change password code to null
                selectedUser.setChangePasswordCode(null);
            }

            // We set the creation date
            selectedUser.setCreationDate(new Date());

            // We set the login counters
            selectedUser.setLoginsCounter(0);
            selectedUser.setFailedLoginAttemptsCounter(0);

            // Reset Login Attempts.
            selectedUser.setFailedLoginAttemptsCounter(0);

            // We set the activation code (unique user in time)
            String seed = selectedUser.getUsername() + System.currentTimeMillis() + selectedUser.getPassword()
                    + selectedUser.getUsername();
            selectedUser.setActivationCode(Md5Encryption.hashPassword(seed));

            // We set the institution for this user

            if (!isNeededToCreateANewInstitution()) {
                // We set the account on blocked, the user will activate his
                // account, clicking on the link in the activation request
                // email.
                // When the account will be activated by the user, a validation
                // request will be sent to the company's administrator, this
                // admin will have to validate the new user account

                // We set the blocked attribute to true - user is blocked till
                // the administrator validation
                selectedUser.setBlocked(true);

            } else {

                // We set the blocked attribute to false - user is not blocked
                // (a user is blocked if he failed to connect after X attempts,
                // or waiting the user registration validation by the company's
                // admin)
                selectedUser.setBlocked(false);

            }

            selectedUser = entityManager.merge(selectedUser);

            // We send an email to the new user - waiting for his activation
            emailManager.setConcerned(selectedUser);
            emailManager.setRecipient(selectedUser);
            emailManager.sendEmail(EmailType.TO_USER_ON_USER_REGISTERING_WITHOUT_INSTITUTION);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_CREATED, selectedUser.getFirstname(),
                    selectedUser.getLastname());

            // Create user preferences
            UserPreferences userPreferences = new UserPreferences();
            userPreferences.setShowSequenceDiagram(true);
            userPreferences.setUser(selectedUser);
            userPreferences.setDisplayEmail(false);
            userPreferences.setNumberOfResultsPerPage(UserPreferences.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
            entityManager.merge(userPreferences);
            entityManager.flush();

            selectedUser = null;
            selectedInstitution = null;

            entityManager.flush();
            return USERS_LOGIN_LOGIN_SEAM;

        } else
        // If the User exists, we update it
        {

            User existing = entityManager.find(User.class, selectedUser.getId());
            selectedUser.setId(existing.getId());
            // username would not be allowed to changed in editUser.xhtml.
            selectedUser.setUsername(existing.getUsername());

            // If password changed
            if (!selectedUser.getPassword().equals("")) {

                // We check the password and the password confirmation are the
                // same string.
                if (!selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation())) {
                    LOG.warn("Passwords do not match. Please re-enter your password..");
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, PASSWORDS_DO_NOT_MATCH);
                    return USERS_USER_REGISTER_SEAM;
                }

                // If password changed, we hash and update the new password
                if (!selectedUser.getPassword().equals(existing.getPassword())) {

                    String hashPassword = Md5Encryption.hashPassword(selectedUser.getPassword());
                    selectedUser.setPassword(hashPassword);
                }
            } else
            // If password did not change, we keep the old one
            {
                // We retrieve and keep the old encrypted password
                selectedUser.setPassword(existing.getPassword());

            }

            // We retrieve and keep the unchanged institution
            selectedUser.setInstitution(existing.getInstitution());

            // We merge the updated object in database
            selectedUser = entityManager.merge(selectedUser);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED, selectedUser.getFirstname(),
                    selectedUser.getLastname(), selectedUser.getUsername());

            return "/users/user/showUser.seam";

        }

    }

    /**
     * Method registerAccountButton.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#registerAccountButton()
     */
    @Override
    public void registerAccountButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerAccountButton");
        }
        selectedInstitution = new Institution();

        selectedUser = new User();
        selectedUser.setInstitution(new Institution());
    }

    /**
     * Add (create/update) a user to the database This operation is allowed for some granted users (check the security.drl) Used to register a new
     * user with a vendor_admin role. Used in register.xhtml
     *
     * @return String : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#registerUser()
     */
    @Override
    public String registerUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerUser");
        }
        return registerUser(ApplicationPreferenceManager.instance(), this.emailManager);

    }

    /**
     * Method registerUser.
     *
     * @param applicationPreferenceManager ApplicationPreferenceManager
     * @param emailManager                 EmailManagerLocal
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#registerUser(ApplicationPreferenceManager, EmailManagerLocal)
     */
    public String registerUser(ApplicationPreferenceManager applicationPreferenceManager, EmailManagerLocal emailManager) {
        boolean userIsAdmin = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerUser");
        }

        if (selectedUser == null) {
            return null;
        }

        if (isInstitutionSelected()) {

            StatusMessageProvider.instance().addFromResourceBundle(
                    "gazelle.validator.users.institution.company.PleaseSelect");

            return USERS_USER_REGISTER_SEAM;
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        try {

            Boolean isJustAVendorUser = false;

            if (!isUserNameAvailable()) {
                return USERS_USER_REGISTER_SEAM;
            }
            if (!isUserEmailAvailable()) {
                return USERS_USER_REGISTER_SEAM;
            }
            // We check the password and the password confirmation are the same
            // string.
            if (isPasswordCheckOk()) {
                LOG.warn("Passwords do not match. Please re-enter your password.");
                StatusMessageProvider.instance().addFromResourceBundle(PASSWORDS_DO_NOT_MATCH);
                return USERS_USER_REGISTER_SEAM;
            }
            // We hash and update the new password
            String hashPassword = selectedUser.getPassword();
            selectedUser.setPassword(hashPassword);

            // We set the institution for this user

            if (isNeededToCreateANewInstitution()) {
                // User has no institution. We create an institution (not activated)

                if (isNewInstitutionNameIsAlreadyUsed()) {
                    sendWebMessage("gazelle.validator.users.institution.name.alreadyExists");
                    return USERS_USER_REGISTER_SEAM;
                }

                if (isNewInstitutionKeywordIsAlreadyUsed()) {
                    sendWebMessage("gazelle.validator.users.institution.keyword.alreadyExists");
                    return USERS_USER_REGISTER_SEAM;
                }

                selectedUser.getInstitution().setActivated(false);
                Institution institution = selectedUser.getInstitution();
                Institution mergedInstitution = entityManager.merge(institution);
                selectedUser.setInstitution(mergedInstitution);

                // We set the blocked attribute to false - user is not blocked
                // (a user is blocked if he failed to connect after X attempts,
                // or waiting the user registration validation by the company's
                // admin)
                selectedUser.setBlocked(false);

                // We set the roles/rights for this user
                List<Role> roles = new Vector<Role>();
                // Role vendor_role
                Role vendorRole = Role.getVENDOR_ROLE();
                roles.add(vendorRole);
                // Role vendor_admin_role
                Role vendorAdminRole = Role.getVENDOR_ADMIN_ROLE();
                roles.add(vendorAdminRole);
                selectedUser.setRoles(roles);

            } else {
                // User has already an institution. We set the institution to that
                // user

                selectedUser.setInstitution(selectedInstitution);
                // We set the account on blocked, the user will activate his
                // account, clicking on the link in the activation request
                // email.
                // When the account will be activated by the user, a validation
                // request will be sent to the company's administrator, this
                // admin will have to validate the new user account

                // We set the blocked attribute to true - user is blocked till
                // the administrator validation
                selectedUser.setBlocked(true);

                // We set the roles/rights for this user
                List<Role> roles = new Vector<Role>();
                // Role vendor_role
                Role vendorRole = Role.getVENDOR_ROLE();
                roles.add(vendorRole);
                isJustAVendorUser = true;

                selectedUser.setRoles(roles);

            }

            // At that time user, has no role, there are defined after the
            // activation.

            // We set the activated attribute to false
            selectedUser.setActivated(false);
            selectedUser.setCreationDate(new Date());

            selectedUser.setLoginsCounter(0);
            selectedUser.setFailedLoginAttemptsCounter(0);

            // We set the activation code (unique user in time)
            String seed = selectedUser.getLastname() + System.currentTimeMillis() + selectedUser.getPassword()
                    + selectedUser.getLastname();
            selectedUser.setActivationCode(Md5Encryption.hashPassword(seed));

            selectedUser = entityManager.merge(selectedUser);

            // If new user belongs to an existing company :
            // We send an email to the new user - waiting for his activation
            if (!isJustAVendorUser) {
                emailManager.setConcerned(selectedUser);
                emailManager.setRecipient(selectedUser);

                emailManager.sendEmail(EmailType.TO_USER_ON_USER_REGISTERING_WITHOUT_INSTITUTION);

                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_CREATED, selectedUser.getFirstname(),
                        selectedUser.getLastname());

            }
            // If new user belongs to an existing company - Email is sent
            // informing that admin will grant him
            // If the new user belongs to an existing company, we send an email
            // to the company administrator. This one will grant the new user to
            // access to application as a company user.
            else {
                List<User> companyAdmins = User.getUsersFiltered(selectedInstitution, Role.getVENDOR_ADMIN_ROLE());

                if (companyAdmins.size() == 0) {

                    String email = applicationPreferenceManager.getApplicationAdminEmail();
                    UserQuery query = new UserQuery();
                    query.email().eq(email);
                    User admin = query.getUniqueResult();

                    if (admin != null) {
                        companyAdmins = new ArrayList<User>();
                        companyAdmins.add(admin);
                    } else {
                        companyAdmins = User.getUsersFiltered(null, Role.getADMINISTRATOR_ROLE());
                    }
                    userIsAdmin = true;
                }

                List<String> adminMails = getRealUserList(companyAdmins);

                String adminMailsString = StringUtils.join(adminMails, " , ");

                emailManager.setInstitutionAdmins(companyAdmins);
                emailManager.setInstitution(selectedInstitution);

                // We send an email to the new user - waiting for his activation
                emailManager.setConcerned(selectedUser);
                emailManager.setRecipient(selectedUser);

                if (!userIsAdmin) {
                    emailManager.sendEmail(EmailType.TO_USER_ON_USER_REGISTERING_WITH_INSTITUTION);
                } else {
                    emailManager.sendEmail(EmailType.TO_USER_ON_USER_REGISTERING_WITH_INSTITUTION_WITHOUT_ADMIN);
                }

                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_CREATED, selectedUser.getFirstname(),
                        selectedUser.getLastname());

                emailManager.setConcerned(selectedUser);
                if (!userIsAdmin) {
                    emailManager.sendEmail(EmailType.TO_INSTITUTION_ADMIN_ON_USER_REGISTERING);
                } else {
                    emailManager.sendEmail(EmailType.TO_ADMIN_ON_USER_REGISTERING);
                }
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, EMAIL_SENT_TO_ADMIN_AFTER_USER_CREATION,
                        selectedInstitution.getName() + " - " + adminMailsString);
            }

            // Create user preferencesweb
            UserPreferences userPreferences = new UserPreferences();
            userPreferences.setShowSequenceDiagram(true);
            userPreferences.setUser(selectedUser);
            userPreferences.setDisplayEmail(false);
            userPreferences.setNumberOfResultsPerPage(UserPreferences.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
            TestingSession defaultTestingSession = TestingSession.getDefault(entityManager);
            if (defaultTestingSession != null) {
                userPreferences.setSelectedTestingSession(defaultTestingSession);
            }
            userPreferences = entityManager.merge(userPreferences);
            entityManager.flush();

            selectedUser = null;
            userToUse = null;

            entityManager.flush();

            return USERS_LOGIN_LOGIN_SEAM;
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Sorry your account cannot be created due to server issue");
            entityManager.clear();
            return USERS_LOGIN_LOGIN_SEAM;
        }

    }

    /**
     * Method isNewInstitutionKeywordIsAlreadyUsed.
     *
     * @return boolean
     */
    private boolean isNewInstitutionKeywordIsAlreadyUsed() {
        return !InstitutionManager.validateInstitutionKeyword(getNewInstituionKeyword());
    }

    /**
     * Method isNewInstitutionNameIsAlreadyUsed.
     *
     * @return boolean
     */
    private boolean isNewInstitutionNameIsAlreadyUsed() {
        return !InstitutionManager.validateInstitutionName(getNewInstitutionName());
    }

    /**
     * Method sendWebMessage.
     *
     * @param message String
     */
    private void sendWebMessage(String message) {
        StatusMessageProvider.instance().addFromResourceBundle(message);
    }

    /**
     * Method getNewInstituionKeyword.
     *
     * @return String
     */
    private String getNewInstituionKeyword() {
        return selectedUser.getInstitution().getKeyword();
    }

    /**
     * Method getNewInstitutionName.
     *
     * @return String
     */
    private String getNewInstitutionName() {
        return selectedUser.getInstitution().getName();
    }

    /**
     * Method isNeededToCreateANewInstitution.
     *
     * @return boolean
     */
    private boolean isNeededToCreateANewInstitution() {
        return selectedInstitution.getKeyword().equals(NULL);
    }

    /**
     * Method isPasswordCheckOk.
     *
     * @return boolean
     */
    private boolean isPasswordCheckOk() {
        return (selectedUser.getPassword() == null) || (selectedUser.getPasswordConfirmation() == null)
                || !selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation());
    }

    /**
     * Method isInstitutionSelected.
     *
     * @return boolean
     */
    private boolean isInstitutionSelected() {
        return (selectedInstitution == null) || (selectedInstitution.getId() == null);
    }

    /**
     * Method getRealUserList.
     *
     * @param companyAdmins List<User>
     * @return List<String>
     */
    protected List<String> getRealUserList(List<User> companyAdmins) {
        List<String> adminMails = new ArrayList<String>();
        if (companyAdmins != null) {
            for (User user : companyAdmins) {
                if ((user.getActivated() != null) && user.getActivated()) {
                    adminMails.add(user.getEmail());
                }
            }
        }
        return adminMails;
    }

    /**
     * Active a new user account by a vendor admin action (clicking on the email link) for the Gazelle application We check if this code is waiting
     * for an activation, and we active the associated
     * account
     *
     * @return String : determinate where to redirect the user (check your pages.xml file) * @throws Exception * @see net.ihe.gazelle.users.action
     * .UserManagerLocal#activateByVendorAdmin()
     * @throws Exception
     */
    @Override
    public String activateByVendorAdmin() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateByVendorAdmin");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        selectedUser = Authenticator.findUserWithActivationCode(activationCode);

        if (selectedUser != null) {
            selectedUser.setActivated(true);
            selectedUser.setBlocked(false);
            selectedUser.setActivationCode(null);

            selectedUser = entityManager.merge(selectedUser);

            selectedInstitution = selectedUser.getInstitution();

            // We send an email to the new user - explaining that admin has
            // validated his account

            userToUse = new User();

            emailManager.setConcerned(selectedUser);
            emailManager.setRecipient(selectedUser);
            emailManager.sendEmail(EmailType.TO_USER_ON_USER_ACTIVATED_BY_INSTITUTION_ADMIN);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.users.user.faces.UserSuccessfullyGrantedByAdmin",
                    selectedUser.getUsername());

            return "activated";

        } else {
            return "notFound";
        }
    }

    /**
     * Add (create/update) a user to the database by a company admin This operation is allowed for some granted users (check the security.drl) This
     * method is similar to the previous addUser method but
     * has a different behavior. It create a user without assigning any password, send an email to the new user to create a new password.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#addUserByAdmin()
     */

    @Override
    @Restrict("#{s:hasPermission('UserManager', 'addUserByAdmin', null)}")
    public void addUserByAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addUserByAdmin");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        if (selectedUser == null) {
            LOG.error("selectedUser is null");
            return;
        }
        boolean sendEmailToOldEmail = false;
        try {
            // Here we are acting as User Admin (and NOT vendor admin)

            if (Role.isLoggedUserAdmin()) {
                if (!isUserNameAvailable()) {
                    return;
                }
                if (!isUserEmailAvailable()) {
                    return;
                }

                if (rolesForUser.size() < 1) {
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "gazelle.users.user.faces.PleaseSelectARoleForThisUser");
                    return;
                }

                if (rolesForUser.isEmpty()) {
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "gazelle.users.user.faces.PleaseSelectARoleForThisUser2");
                    return;
                }

                if (selectedInstitution == null) {
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.UnknownInstitution");
                    return;
                }

                selectedUser.setInstitution(selectedInstitution);

                // We set the creation date
                selectedUser.setCreationDate(new Date());

                List<Role> roles = new Vector<Role>();

                // Role vendor_role
                for (Pair<Boolean, Role> role : rolesForUser) {
                    if (role.getObject1()) {
                        roles.add(role.getObject2());
                        if (role.getObject2().equals(Role.getTEST_SESSION_ADMIN())) {
                            if (!roles.contains(Role.getMONITOR_ROLE())) {
                                roles.add(Role.getMONITOR_ROLE());
                            }
                        }
                    }
                }
                if (roles.size() == 0) {
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "gazelle.users.user.faces.PleaseSelectARoleForThisUser");
                    return;
                }

                selectedUser.setRoles(roles);

                // This is the case where we are creating a user and not
                // updating one

                if (isCreatingAUser()) {

                    String hashPassword = Md5Encryption.hashPassword(UserManager.DEFAULT_PASSWORD);

                    selectedUser.setPassword(hashPassword);
                    selectedUser.setLoginsCounter(0);
                    selectedUser.setFailedLoginAttemptsCounter(0);

                    selectedUser.setActivated(true);

                    String seed = selectedUser.getUsername() + System.currentTimeMillis() + selectedUser.getPassword()
                            + selectedUser.getUsername();
                    String hashed = Md5Encryption.hashPassword(seed);
                    selectedUser.setActivationCode(hashed);

                    selectedUser = entityManager.merge(selectedUser);

                    // HITTI-91: Deactivate Email notification for Connectathon.

                    // We send an email to the new user - waiting for his
                    // activation
                    emailManager.setConcerned(selectedUser);
                    emailManager.setRecipient(selectedUser);
                    emailManager.sendEmail(EmailType.TO_USER_ON_USER_REGISTERED_BY_INSTITUTION_ADMIN);
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED_BY_ADMIN,
                            selectedUser.getFirstname(), selectedUser.getLastname(), selectedUser.getUsername(),
                            selectedUser.getEmail());
                } else {
                    // This is now the case where we are updating a user.
                    // If user email as change, send email to the old email
                    userCurrentEmail();
                    String newEmail = selectedUser.getEmail();
                    if (!newEmail.equals(currentEmail)) {
                        sendEmailToOldEmail = true;
                        // Set the new email
                        selectedUser.setEmail(newEmail);
                    }
                    selectedUser = entityManager.merge(selectedUser);
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED,
                            selectedUser.getFirstname(), selectedUser.getLastname(), selectedUser.getUsername());
                }

                UserPreferences userPrefs = UserPreferences.getPreferencesForUser(selectedUser);
                if (userPrefs == null) {
                    // Create user preferences
                    UserPreferences userPreferences = new UserPreferences();
                    userPreferences.setShowSequenceDiagram(true);
                    userPreferences.setUser(selectedUser);
                    userPreferences.setDisplayEmail(false);
                    userPreferences.setNumberOfResultsPerPage(UserPreferences.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
                    TestingSession defaultTestingSession = TestingSession.getDefault(entityManager);
                    if (defaultTestingSession != null) {
                        userPreferences.setSelectedTestingSession(defaultTestingSession);
                    }
                    userPreferences = entityManager.merge(userPreferences);
                    entityManager.flush();
                }
                closeViewOrEditPanelsAndOpenList();
            }
            // User is not admin but a vendor admin
            else if (Role.isLoggedUserVendorAdmin() || Role.isLoggedUserVendorUser()) {
                List<Role> roles = new Vector<Role>();

                for (Pair<Boolean, Role> role : rolesForUser) {
                    if (role.getObject1()) {
                        roles.add(role.getObject2());
                        if (role.getObject2().equals(Role.getTEST_SESSION_ADMIN())) {
                            if (!roles.contains(Role.getMONITOR_ROLE())) {
                                roles.add(Role.getMONITOR_ROLE());
                            }
                        }
                    }
                }

                if (roles.size() == 0) {

                    if (Role.isLoggedUserVendorAdmin()) {
                        roles.add(Role.getVENDOR_ADMIN_ROLE());
                    } else if (Role.isLoggedUserVendorUser()) {
                        roles.add(Role.getVENDOR_ROLE());
                    } else {

                        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.users.user.faces.PleaseSelectARoleForThisUser");
                        return;
                    }
                }

                selectedUser.setRoles(roles);

                if (isCreatingAUser()) {

                    Institution inst = User.loggedInUser().getInstitution();
                    selectedUser.setInstitution(inst);

                    // At that time user, has no role, there are defined after
                    // the activation.

                    // When created by vendor admin, automatically activated
                    selectedUser.setActivated(true);
                    selectedUser.setCreationDate(new Date());
                    // We set the blocked attribute to false - user is not
                    // blocked (an newUser is blocked if he failed to connect
                    // after X attempts)
                    selectedUser.setBlocked(false);

                    String hashPassword = Md5Encryption.hashPassword(UserManager.DEFAULT_PASSWORD);
                    selectedUser.setPassword(hashPassword);

                    // We set the login counters
                    selectedUser.setLoginsCounter(0);
                    selectedUser.setFailedLoginAttemptsCounter(0);

                    // We set the activation code (unique user in time)
                    String seed = selectedUser.getUsername() + System.currentTimeMillis() + selectedUser.getPassword()
                            + selectedUser.getUsername();
                    String hashed = Md5Encryption.hashPassword(seed);
                    selectedUser.setActivationCode(hashed);

                    selectedUser = entityManager.merge(selectedUser);
                    // We send an email to the new user - waiting for his
                    // activation
                    emailManager.setConcerned(selectedUser);
                    emailManager.setRecipient(selectedUser);
                    emailManager.sendEmail(EmailType.TO_USER_ON_USER_REGISTERED_BY_INSTITUTION_ADMIN);
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED_BY_ADMIN,
                            selectedUser.getFirstname(), selectedUser.getLastname(), selectedUser.getUsername(),
                            selectedUser.getEmail());

                } else {

                    selectedUser = entityManager.merge(selectedUser);
                    StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED,
                            selectedUser.getFirstname(), selectedUser.getLastname(), selectedUser.getUsername());
                }

                UserPreferences userPrefs = UserPreferences.getPreferencesForUser(selectedUser);
                if (userPrefs == null) {
                    // Create user preferences
                    UserPreferences userPreferences = new UserPreferences();
                    userPreferences.setShowSequenceDiagram(true);
                    userPreferences.setUser(selectedUser);
                    userPreferences.setDisplayEmail(false);
                    userPreferences.setNumberOfResultsPerPage(UserPreferences.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
                    TestingSession defaultTestingSession = TestingSession.getDefault(entityManager);
                    if (defaultTestingSession != null) {
                        userPreferences.setSelectedTestingSession(defaultTestingSession);
                    }
                    userPreferences = entityManager.merge(userPreferences);
                    entityManager.flush();
                }
                // We reset the current institution to the one of the userlogged

                getUsersListDependingInstitution();
                closeViewOrEditPanelsAndOpenList();
            } else if (Role.isLoggedUserUser()) {
                // Go back to home why are you here ?
                // a user cannot create a new user

                return;
            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "Problem adding a user : " + e.getMessage());
            return;
        }
        if (sendEmailToOldEmail) {
            sendEmailToOldEmail();
        }
        resetCache();
    }

    private void sendEmailToOldEmail() {
        User user = new User(selectedUser);
        emailManager.setConcerned(user);
        emailManager.setRecipient(user);

        // For send email to the old email
        emailManager.getRecipient().setEmail(currentEmail);
        try {
            emailManager.sendEmail(EmailType.TO_USER_TO_CHANGE_EMAIL);
            String mess = ResourceBundle.instance().getString("gazelle.users.user.faces.EmailSendToTheUser");
            StatusMessages.instance().add(StatusMessage.Severity.INFO,
                    mess + user.getFirstname() + " " + user.getLastname() + currentEmail);
        } catch (Exception e) {
            String mess = ResourceBundle.instance().getString("gazelle.users.user.faces.FailedToSendEmailToTheUser");
            StatusMessages.instance().add(StatusMessage.Severity.ERROR, mess + " " + user.getFirstname() + " " + user.getLastname()
                    + " " + currentEmail);
        }
    }

    private boolean isCreatingAUser() {
        return selectedUser.getId() == null;
    }

    /**
     * Called method when a user needs to update his/her password
     *
     * @return String : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#updatePassword()
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'updatePassword', null)}")
    public String updatePassword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updatePassword");
        }
        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager()) {

            // If password changed, we hash and update the new password
            if (selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation())) {

                EntityManager entityManager = EntityManagerService.provideEntityManager();
                String hashPassword = Md5Encryption.hashPassword(selectedUser.getPassword());
                selectedUser.setPassword(hashPassword);
                // We retrieve and keep the unchanged institution
                selectedUser.setActivationCode(null);
                selectedUser = entityManager.merge(selectedUser);
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED, selectedUser.getFirstname(),
                        selectedUser.getLastname(), selectedUser.getUsername());
            } else {
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.PasswordsDoNotMatch");
                return USERS_USER_CHANGE_USER_PASSWORD_SEAM;
            }

        }

        Identity.instance().getCredentials().invalidate();
        return "/home.seam";
    }

    /**
     * Used to change the password when the user forgot it. Called by the page changeUserPassword.xhtml
     *
     * @return : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#updatePasswordNotLogged()
     */
    @Override
    public String updatePasswordNotLogged() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updatePasswordNotLogged");
        }

        // If password changed, we hash and update the new password
        if (selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation())) {

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedUser.setChangePasswordCode(null);
            selectedUser.setActivationCode(null);
            selectedUser = entityManager.merge(selectedUser);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED, selectedUser.getFirstname(),
                    selectedUser.getLastname(), selectedUser.getUsername());
        } else {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, PASSWORDS_DO_NOT_MATCH);
            return USERS_USER_CHANGE_USER_PASSWORD_SEAM;
        }

        return "/home.seam";
    }

    /**
     * Used by an admin (admin or vendor_admin) to change the password when the user forgot it. Called by the page changeUserPassword.xhtml
     *
     * @return : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#updateUserPasswordForAdmin()
     */
    @Override
    public String updateUserPasswordForAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateUserPasswordForAdmin");
        }

        // If password changed, we hash and update the new password
        if (selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation())) {

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedUser.setChangePasswordCode(null);
            selectedUser.setActivationCode(null);
            selectedUser = entityManager.merge(selectedUser);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED, selectedUser.getFirstname(),
                    selectedUser.getLastname(), selectedUser.getUsername());
            try {
                sendEmailToChangePasswordByAdmin();
            } catch (Exception e) {
                LOG.error("Error sending mail, error =" + e.getMessage());
            }
            return USERS_USER_LIST_USERS_INSTITUTION_SEAM;
        } else {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, PASSWORDS_DO_NOT_MATCH);
            return USERS_USER_CHANGE_USER_PASSWORD_SEAM;
        }
    }

    /**
     * Used by a user, when he is registered by an admin from his company. He can change the password he clicks on this link. Called by the page
     * changeUserPasswordAfterRegistration.xhtml
     *
     * @return : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#updateUserPasswordAfterRegistration()
     */
    @Override
    public String updateUserPasswordAfterRegistration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateUserPasswordAfterRegistration");
        }

        // If password changed, we hash and update the new password
        if (selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation())) {

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedUser.setChangePasswordCode(null);
            selectedUser.setActivationCode(null);
            selectedUser = entityManager.merge(selectedUser);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, USER_SUCCESSFULLY_UPDATED, selectedUser.getFirstname(),
                    selectedUser.getLastname(), selectedUser.getUsername());
        } else {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, PASSWORDS_DO_NOT_MATCH);
            return "/users/user/changeUserPasswordAfterRegistration.seam";
        }

        return "/home.seam";
    }

    /**
     * Update the user's informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param u : User to be updated
     * @see net.ihe.gazelle.users.action.UserManagerLocal#updateUser(User)
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'updateUser', null)}")
    public void updateUser(final User u) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateUser");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.merge(u);
    }

    /**
     * Delete a User and persist the operation
     *
     * @param inSelectedUser User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#deleteUser(User)
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'deleteUser', null)}")
    public void deleteUser(final User inSelectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteUser");
        }

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserVendorAdmin()) {
            if (inSelectedUser != null) {

                try {
                    User.deleteUserWithFind(inSelectedUser);
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, inSelectedUser.getUsername() + "has been blocked !");

                } catch (ConstraintViolationException cve) {
                    ExceptionLogging.logException(cve, LOG);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Couldn't delete user " + inSelectedUser.getUsername()
                                    + " : the deletion violates database constraint " + cve.getConstraintName());
                } catch (Exception npe) {
                    ExceptionLogging.logException(npe, LOG);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem deleting user ");
                }

            } else {
                LOG.error("method deleteUser() : selectedUser passed as parameter is null ! ");
            }

        }

        if (Role.isLoggedUserAdmin()) {
            findUsersForCompany();
        } else if (Role.isLoggedUserVendorAdmin()) {
            getUsersListDependingInstitution();
        }
    }

    /**
     * Block a User and persist the operation
     *
     * @param inSelectedUser User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#blockUser(User)
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'deleteUser', null)}")
    public void blockUser(final User inSelectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("blockUser");
        }

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserVendorAdmin()) {
            if (inSelectedUser != null) {

                try {
                    User.blockUserWithFind(inSelectedUser);
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, inSelectedUser.getUsername() + "has been blocked !");

                } catch (ConstraintViolationException cve) {
                    ExceptionLogging.logException(cve, LOG);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Couldn't block user " + inSelectedUser.getUsername()
                                    + " : the block violates database constraint " + cve.getConstraintName());
                } catch (Exception npe) {
                    ExceptionLogging.logException(npe, LOG);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem blocking user ");
                }

            } else {
                LOG.error("method blockUser() : selectedUser passed as parameter is null ! ");
            }

        }
    }

    /**
     * Unlock a User and persist the operation
     *
     * @param inSelectedUser User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#unblockUser(User)
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'deleteUser', null)}")
    public void unblockUser(final User inSelectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("blockUser");
        }

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager() || Role.isLoggedUserVendorAdmin()) {
            if (inSelectedUser != null) {

                try {
                    User.unblockUserWithFind(inSelectedUser);
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, inSelectedUser.getUsername() + "has been unblocked !");

                } catch (ConstraintViolationException cve) {
                    ExceptionLogging.logException(cve, LOG);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Couldn't unblock user " + inSelectedUser.getUsername()
                                    + " : the block violates database constraint " + cve.getConstraintName());
                } catch (Exception npe) {
                    ExceptionLogging.logException(npe, LOG);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem unblocking user ");
                }

            } else {
                LOG.error("method unblockUser() : selectedUser passed as parameter is null ! ");
            }
        }
    }

    /**
     * Delete the selected user (called from the confirmation modal panel) This operation is allowed for some granted users (check the security.drl)
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#deleteSelectedUser()
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'deleteUser', null)}")
    public void deleteSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedUser");
        }

        // Retrieve variable from current Session ( variable setted by
        // userManagerExtra )
        selectedUser = (User) Component.getInstance("selectedUser");

        if (selectedUser == null) {
            LOG.error(" cannot delete user : user is null ");
            return;
        }

        deleteUser(selectedUser);
        resetCache();
    }

    /**
     * Block the selected user (called from the confirmation modal panel) This operation is allowed for some granted users (check the security.drl)
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#blockSelectedUser()
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'deleteUser', null)}")
    public void blockSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("blockSelectedUser");
        }

        // Retrieve variable from current Session ( variable setted by
        // userManagerExtra )
        selectedUser = (User) Component.getInstance("selectedUser");

        if (selectedUser == null) {
            LOG.error(" cannot block user : user is null ");
            return;
        }

        blockUser(selectedUser);
        resetCache();
    }

    /**
     * This method is called when a user clicks on 'create a new account'. It initializes needed variables.
     *
     * @return String : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#registerAction()
     */
    @Override
    public String registerAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("registerAction");
        }
        selectedInstitution = new Institution();
        selectedUser = new User();

        return USERS_USER_REGISTER_SEAM;
    }

    /**
     * This method is called when a user clicks on 'Sign in' button. It initializes needed variables.
     *
     * @return String : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#loginAction()
     */
    @Override
    public String loginAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loginAction");
        }

        return USERS_LOGIN_LOGIN_SEAM;

    }

    /**
     * Return a bool to display or not the delete button
     *
     * @param inUser User
     * @return boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#displayDelete(User)
     */
    @Override
    public boolean displayDelete(User inUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayDelete");
        }

        if (inUser == null) {
            return false;
        }

        Identity identity = Identity.instance();

        if (identity.isLoggedIn() && !identity.getCredentials().getUsername().equals(inUser.getUsername())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Render the selected user This operation is allowed for some granted users (check the security.drl)
     *
     * @param u User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#viewUser(User)
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'viewUser', null)}")
    public void viewUser(final User u) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewUser");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        selectedUser = entityManager.find(User.class, u.getId());

        openViewPanel();
    }

    /**
     * Method editUserPreferences.
     *
     * @param inSelectedUser User
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#editUserPreferences(User)
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'editUser', null)}")
    public String editUserPreferences(User inSelectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editUserPreferences");
        }
        Contexts.getSessionContext().set("userForPreferences", inSelectedUser);
        return "/users/user/editOtherUserPreferences.seam";
    }

    /**
     * Edit the selected user's informations This operation is allowed for some granted users (check the security.drl)
     *
     * @param inSelectedUser User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#editUser(User)
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'editUser', null)}")
    public void editUser(User inSelectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editUser");
        }
        if (inSelectedUser == null) {
            LOG.error("inSelectedUser is null");
            return;
        }

        try {

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedUser = entityManager.find(User.class, inSelectedUser.getId());
            selectedInstitution = selectedUser.getInstitution();

            if (Role.isLoggedUserAdmin() || Role.isLoggedUserTestingSessionAdmin()) {
                rolesForUser = Role.getRoleAttributableIfWithBoolean(Role.ADMINISTRATOR_ROLE_STRING);
            } else if (Role.isLoggedUserVendorAdmin()) {
                rolesForUser = Role.getRoleAttributableIfWithBoolean(Role.VENDOR_ADMIN_ROLE_STRING);
            } else if (Role.isLoggedUserVendorUser()) {
                rolesForUser = Role.getRoleAttributableIfWithBoolean(Role.VENDOR_ROLE_STRING);
            } else {
                throw new Exception("rolesForUser to attribute is null");
            }

            for (int roleI = 0; roleI < rolesForUser.size(); roleI++) {
                for (int roleOwnedByUser = 0; roleOwnedByUser < selectedUser.getRoles().size(); roleOwnedByUser++) {
                    if ((rolesForUser.get(roleI) != null)
                            && (rolesForUser.get(roleI).getObject2() != null)
                            && rolesForUser.get(roleI).getObject2().getName()
                            .equals(selectedUser.getRoles().get(roleOwnedByUser).getName())) {
                        rolesForUser.get(roleI).setObject1(true);
                        break;

                    }

                }
            }

            openEditPanel();

        } catch (Exception e) {

            ExceptionLogging.logException(e, LOG);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.ProblemEditingUser" + e
                    .getMessage());

        }
    }

    /**
     * Method editUser.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#editUser()
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'editUser', null)}")
    public void editUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editUser");
        }
        editUser(selectedUser);
    }

    /**
     * Method editUserAction.
     *
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#editUserAction()
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'editUser', null)}")
    public String editUserAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editUserAction");
        }

        editUser(selectedUser);
        displayEditPanel = true;
        displayListPanel = false;
        displayViewPanel = false;

        return USERS_USER_LIST_USERS_INSTITUTION_SEAM;
    }

    /**
     * Create a new user. This method is used by a Java client This operation is allowed for some granted users (check the security.drl)
     *
     * @param u : user to create
     * @see net.ihe.gazelle.users.action.UserManagerLocal#createUser(User)
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'createUser', null)}")
    public void createUser(final User u) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createUser");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        entityManager.persist(u);
        entityManager.flush();
    }

    /**
     * This method is used to hash a password using the MD5 encryption.
     *
     * @param password : Password to encrypt
     * @return String : Encrypted password * @see net.ihe.gazelle.users.action.UserManagerLocal#hashPassword(String)
     */
    @Override
    public String hashPassword(final String password) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashPassword");
        }
        // We hash the password
        String hashPassword = Md5Encryption.hashPassword(password);

        return hashPassword;
    }

    /**
     * Get the institution of the current logged in user This operation is allowed for some granted users (check the security.drl)
     *
     * @return Institution of the logged in user * @see net.ihe.gazelle.users.action.UserManagerLocal#getInstitution()
     */
    @Override
    @Restrict("#{s:hasPermission('UserManager', 'getInstitution', null)}")
    public Institution getInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitution");
        }

        Institution institutionLoggedIn = Institution.getLoggedInInstitution();

        return institutionLoggedIn;

    }

    /**
     * Check the institution existence for the logged in user.
     *
     * @return String :String code : returns either "unknown" if institution does not exist, or "existing" string code. * @see net.ihe.gazelle
     * .users.action.UserManagerLocal#isExistingInstitution()
     */
    @Override
    public String isExistingInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isExistingInstitution");
        }

        User foundUser = User.loggedInUser();
        // We retrieve the user from the database with the same keyword

        if (DEFAULTINSTITUTIONKEYWORD.equals(foundUser.getInstitution().getKeyword())) {

            return "unknown";
        } else {

            return "existing";
        }

    }

    /**
     * Get the Users list to display (in manage Users, from the Company menu, for admins), depending on the logged in user This operation is
     * allowed for some granted users (check the security.drl)
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getUsersListDependingInstitution()
     */

    @Override
    @Restrict("#{s:hasPermission('UserManager', 'getUsersListDependingInstitution', null)}")
    public void getUsersListDependingInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsersListDependingInstitution");
        }

        if (Role.isLoggedUserAdmin()) {

            findUsersForCompany();
        } else if (Role.isLoggedUserVendorAdmin()) {
            if (users != null) {
                users.clear();
            }
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.flush();

            Institution institution = Institution.getLoggedInInstitution();

            users = User.getUsersFiltered(institution, null);

        } else {
        }
    }

    /**
     * Get the All Users list to display (for admins) This operation is allowed for some granted users (check the security.drl)
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getAllUsersListForAdmin()
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    @Restrict("#{s:hasPermission('UserManager', 'getAllUsersListForAdmin', null)}")
    public void getAllUsersListForAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllUsersListForAdmin");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        Session s = (Session) entityManager.getDelegate();
        Integer results = 0;

        Criteria c = s.createCriteria(User.class);
        users = c.list();
        results = users.size();

        if (results == 0) {
            LOG.error("Error getting all users list" + results);
        }
    }

    /**
     * Get the All Users list to display in the datatable
     *
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#listUsersAction()
     */

    @Override
    public String listUsersAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listUsersAction");
        }

        getUsersListDependingInstitution();

        return USERS_USER_LIST_USERS_INSTITUTION_SEAM;
    }

    @Override
    public void sendEmailToChangePasswordByAdmin() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmailToChangePasswordByAdmin");
        }
        if (selectedUser == null) {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.UserNotFound");
        }
        User foundUser = getUser();
        if (foundUser == null) {
            // try to find the user with a username
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.UserNotFound");
        } else {
            selectedUser = foundUser;
            sendEmailPasswordUpdatedByAdmin();
        }
    }

    private void sendEmailPasswordUpdatedByAdmin() throws Exception {
        if (selectedUser != null) {
            // We send the email to the user - including the link to change the
            // password (with the changePasswordCode)
            emailManager.setConcerned(selectedUser);
            emailManager.setRecipient(selectedUser);
            emailManager.sendEmail(EmailType.TO_USER_UPDATED_PASSWORD_BY_ADMIN);
            String mess = ResourceBundle.instance().getString("gazelle.users.user.faces.EmailSendToTheUser");
            StatusMessages.instance().add(StatusMessage.Severity.INFO, mess +
                    selectedUser.getFirstname() + " " + selectedUser.getLastname());
        }
    }

    /**
     * Send an email to the user with a link to change his password (check your pages.xml file) This is the password assistance, used when a user
     * forgot his password. This operation is allowed for
     * anybody (nothing to declare in security.drl)
     *
     * @return String : determinate where to redirect the user (check your pages.xml file) * @throws Exception * @see net.ihe.gazelle.users.action
     * .UserManagerLocal#sendEmailToChangePassword()
     * @throws Exception
     */
    @Override
    public String sendEmailToChangePassword() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmailToChangePassword");
        }
        if (selectedUser == null) {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.UserNotFound");
            return USERS_LOGIN_LOGIN_SEAM;
        }
        User foundUser = getUser();
        if (foundUser == null) {
            // try to find the user with a username
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.UserNotFound");
            return USERS_LOGIN_LOGIN_SEAM;
        } else {
            selectedUser = foundUser;
            return sendEmail();
        }
    }

    @Override
    public void sendEmailToChangePasswordAsAdmin() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmailToChangePasswordAsAdmin");
        }
        if (selectedUser == null) {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.UserNotFound");
        }
        User foundUser = getUser();
        if (foundUser == null) {
            // try to find the user with a username
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.user.faces.UserNotFound");
        } else {
            selectedUser = foundUser;
            sendEmailToSelectedUser();
        }
    }


    private User getUser() {
        User foundUser = null;
        if ((selectedUser.getEmail() != null) && (selectedUser.getEmail().trim().length() > 0)) {
            UserQuery query = new UserQuery();
            query.email().eq(selectedUser.getEmail());
            List<User> listOfUser = query.getList();
            if (listOfUser.size() > 0) {
                foundUser = listOfUser.get(0);
            }
        } else if ((selectedUser.getUsername() != null) && (selectedUser.getUsername().trim().length() > 0)) {
            UserQuery query = new UserQuery();
            query.username().eq(selectedUser.getUsername());
            List<User> listOfUser = query.getList();
            if (listOfUser.size() > 0) {
                foundUser = listOfUser.get(0);
            }
        }
        return foundUser;
    }

    /**
     * Method sendEmail.
     *
     * @return String
     * @throws Exception
     */
    private String sendEmail() throws Exception {

        if (sendEmailToSelectedUser()) {
            return null;
        }
        return USERS_LOGIN_LOGIN_SEAM;
    }

    public boolean sendEmailToSelectedUser() throws Exception {
        if (selectedUser == null) {
            return true;
        }
        if (selectedUser.getActivated()) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            // We set the change password code (unique user in time)
            String seed = selectedUser.getUsername() + System.currentTimeMillis() + selectedUser.getPassword()
                    + selectedUser.getUsername();
            selectedUser.setChangePasswordCode(Md5Encryption.hashPassword(seed));

            selectedUser.setChangePasswordCode(Md5Encryption.hashPassword(seed));

            // We use the user object because this is the one used in
            // /email/forgotPasswordEmail.xhtml

            selectedUser = entityManager.merge(selectedUser);
            entityManager.flush();

            // We send the email to the user - including the link to change the
            // password (with the changePasswordCode)
            emailManager.setConcerned(selectedUser);
            emailManager.setRecipient(selectedUser);
            emailManager.sendEmail(EmailType.TO_USER_FORGOT_PASSWORD);

            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.users.authenticator.WeWillBeSendingYou" +
                    "change at " + selectedUser.getEmail());
        } else {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "gazelle.users.authenticator.PleaseActivateYourAccountBefore");
            LOG.error("User (" + selectedUser.getUsername() + ", " + selectedUser.getFirstname() + " "
                    + selectedUser.getLastname()
                    + ") wants to change its password but his account is not yet activated...");
        }
        return false;
    }

    /**
     * Method displayUsernameField.
     *
     * @return boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#displayUsernameField()
     */
    @Override
    public boolean displayUsernameField() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayUsernameField");
        }
        return ((selectedUser == null) || (isCreatingAUser()));
    }

    /**
     * Method displayPassword.
     *
     * @return boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#displayPassword()
     */
    @Override
    public boolean displayPassword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayPassword");
        }
        return (!Role.isLoggedUserAdmin() && !Role.isLoggedUserVendorAdmin());
    }

    /**
     * Validate the user name filled in the JSF (length, unique) This operation is allowed for anybody (nothing to declare in security.drl)
     *
     * @return boolean : if the validation is done without error (true) * @see net.ihe.gazelle.users.action.UserManagerLocal#validateUserName()
     */
    @Override
    public boolean isUserNameAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isUserNameAvailable");
        }

        UserQuery query = new UserQuery();
        if (selectedUser.getId() != null) {
            query.id().neq(selectedUser.getId());
        }
        query.username().eq(selectedUser.getUsername());
        List<User> listOfUsers = query.getList();

        if (listOfUsers.size() == 0) {

            return true;

        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("userUsername",
                    StatusMessage.Severity.WARN, "gazelle.validator.users.user.name.alreadyExists",
                    "A user with that name already exists. Please choose another username...");
            return false;
        }
    }

    /**
     * Validate the user email filled in the JSF (length, unique) This operation is allowed for anybody (nothing to declare in security.drl)
     *
     * @return boolean : if the validation is done without error (true) * @see net.ihe.gazelle.users.action.UserManagerLocal#validateUserEmail()
     */
    @Override
    public boolean isUserEmailAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isUserEmailAvailable");
        }

        UserQuery query = new UserQuery();
        if (selectedUser.getId() != null) {
            query.id().neq(selectedUser.getId());
        }
        query.email().eq(selectedUser.getEmail());

        if (query.getCount() == 0) {

            return true;

        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("userEmail", StatusMessage.Severity.WARN,
                    "gazelle.validator.users.user.email.alreadyExists",
                    "A user with that email address already exists. Please choose another email address...");
            return false;
        }

    }

    public void userCurrentEmail() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("userCurrentEmail");
        }
        UserQuery query = new UserQuery();
        query.username().eq(selectedUser.getUsername());
        setCurrentEmail(query.email().getListDistinct().get(0));
    }

    /**
     * Validate the user password filled in the JSF (length, unique) This operation is allowed for anybody (nothing to declare in security.drl)
     *
     * @param currentPasswordConfirmation : String corresponding to the user password to validate
     * @param currentPassword             String
     * @return boolean : if the validation is done without error (true) * @see net.ihe.gazelle.users.action
     * .UserManagerLocal#validatePasswordConfirmation(String, String)
     */
    @Override
    public String validatePasswordConfirmation(String currentPassword, String currentPasswordConfirmation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validatePasswordConfirmation");
        }

        if (currentPassword.equals(currentPasswordConfirmation)) {
            return currentPasswordConfirmation;
        } else {
            LOG.warn("Passwords do not match. Please re-enter your password...");
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("userPasswordConfirmation",
                    StatusMessage.Severity.WARN, PASSWORDS_DO_NOT_MATCH, "Passwords do not match");
            return currentPasswordConfirmation;
        }

    }

    /**
     * This method is called when the user clicks on the 'Change Password' button (in edit user page) It returns the JSF page name where it s
     * possible to change the user's password.
     *
     * @return String : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#displayChangePassword()
     */
    @Override
    public String displayChangePassword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayChangePassword");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        selectedUser = entityManager.find(User.class, selectedUser.getId());

        return USERS_USER_CHANGE_USER_PASSWORD_SEAM;
    }

    /**
     * This method is called when the user clicks on the 'Change Password' button (in view user page) It returns the JSF page name where it s
     * possible to change the user's password.
     *
     * @return String : JSF page to render * @see net.ihe.gazelle.users.action.UserManagerLocal#displayChangePasswordFromView()
     */
    @Override
    public String displayChangePasswordFromView() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayChangePasswordFromView");
        }
        if (selectedUser == null) {
            return null;
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        selectedUser = entityManager.find(User.class, selectedUser.getId());

        return USERS_USER_CHANGE_USER_PASSWORD_SEAM;
    }

    /**
     * Method editAccount.
     *
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#editAccount()
     */
    @Override
    public String editAccount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editAccount");
        }
        selectedUser = User.loggedInUser();
        return "/users/user/userPreferences.seam";
    }

    /**
     * Method changePasswordAction.
     *
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#changePasswordAction()
     */
    @Override
    public String changePasswordAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changePasswordAction");
        }

        return "/users/user/changePasswordCurrentLoggedUser.seam";
    }

    /**
     * Method changePassword.
     *
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#changePassword()
     */
    @Override
    public String changePassword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changePassword");
        }

        User userToModify = User.loggedInUser();
        if ((userToModify == null) || (selectedUser == null)) {
            return null;
        }

        if (userToModify.getPassword().equals(selectedUser.getOldPassword())) {
            if (selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation())) {

                EntityManager entityManager = EntityManagerService.provideEntityManager();
                selectedUser = entityManager.merge(selectedUser);
                return "/users/user/userPreferences.seam";
            } else {
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.authenticator.PasswordsDoNotMatch");
                return "/users/user/changePasswordCurrentLoggedUser.seam";
            }

        } else {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.authenticator.InvalidPassword");
            return "/users/user/changePasswordCurrentLoggedUser.seam";
        }
    }

    /**
     * Method changeInstitution.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#changeInstitution()
     */
    @Override
    public void changeInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeInstitution");
        }
        if ((selectedInstitution != null) && isNeededToCreateANewInstitution()) {
            if (selectedUser != null) {
                selectedUser.setInstitution(new Institution());
            }
        }
    }

    /**
     * Method getSelectedUser.
     *
     * @return User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getSelectedUser()
     */
    @Override
    public User getSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedUser");
        }
        if (selectedUser == null) {
            selectedUser = (User) Component.getInstance("selectedUser");
        }
        return selectedUser;
    }

    /**
     * Method setSelectedUser.
     *
     * @param selectedUser User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setSelectedUser(User)
     */
    @Override
    public void setSelectedUser(User selectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedUser");
        }

        this.selectedUser = selectedUser;
    }

    /**
     * Get the activation code - code in the email sent after a user account registration
     *
     * @return String activationCode * @see net.ihe.gazelle.users.action.UserManagerLocal#getActivationCode()
     */
    @Override
    public String getActivationCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActivationCode");
        }

        return activationCode;
    }

    /**
     * Set the activation code - code in the email sent after a user account registration
     *
     * @param activationCode String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setActivationCode(String)
     */
    @Override
    public void setActivationCode(String activationCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActivationCode");
        }

        this.activationCode = activationCode;
    }

    /**
     * Method getUserToUse.
     *
     * @return User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getUserToUse()
     */
    @Override
    public User getUserToUse() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserToUse");
        }
        return userToUse;
    }

    /**
     * Method setUserToUse.
     *
     * @param userToUse User
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setUserToUse(User)
     */
    @Override
    public void setUserToUse(User userToUse) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setUserToUse");
        }
        this.userToUse = userToUse;
    }

    /**
     * Method getSelectedInstitution.
     *
     * @return Institution
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getSelectedInstitution()
     */
    @Override
    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }
        return selectedInstitution;
    }

    /**
     * Method setSelectedInstitution.
     *
     * @param selectedInstitution Institution
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setSelectedInstitution(Institution)
     */
    @Override
    public void setSelectedInstitution(Institution selectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitution");
        }
        this.selectedInstitution = selectedInstitution;
    }

    /**
     * Method getChoosenInstitutionForAdmin.
     *
     * @return Institution
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getChoosenInstitutionForAdmin()
     */
    @Override
    public Institution getChoosenInstitutionForAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChoosenInstitutionForAdmin");
        }
        return choosenInstitutionForAdmin;
    }

    /**
     * Method setChoosenInstitutionForAdmin.
     *
     * @param choosenInstitutionForAdmin Institution
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setChoosenInstitutionForAdmin(Institution)
     */
    @Override
    public void setChoosenInstitutionForAdmin(Institution choosenInstitutionForAdmin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setChoosenInstitutionForAdmin");
        }
        this.choosenInstitutionForAdmin = choosenInstitutionForAdmin;
    }

    /**
     * Method getRolesForUser.
     *
     * @return List<Pair
           * <
                   *   Boolean
                              *   ,
                       *       Role>>
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getRolesForUser()
     */
    @Override
    public List<Pair<Boolean, Role>> getRolesForUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRolesForUser");
        }
        return rolesForUser;
    }

    /**
     * Method setRolesForUser.
     *
     * @param rolesForUser List<Pair<Boolean,Role>>
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setRolesForUser(List<Pair<Boolean,Role>>)
     */
    @Override
    public void setRolesForUser(List<Pair<Boolean, Role>> rolesForUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRolesForUser");
        }
        this.rolesForUser = rolesForUser;
    }

    /**
     * Method getDisplayEditPanel.
     *
     * @return Boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getDisplayEditPanel()
     */
    @Override
    public Boolean getDisplayEditPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayEditPanel");
        }
        return displayEditPanel;
    }

    /**
     * Method setDisplayEditPanel.
     *
     * @param displayEditPanel Boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setDisplayEditPanel(Boolean)
     */
    @Override
    public void setDisplayEditPanel(Boolean displayEditPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayEditPanel");
        }
        this.displayEditPanel = displayEditPanel;
    }

    /**
     * Method getDisplayViewPanel.
     *
     * @return Boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getDisplayViewPanel()
     */
    @Override
    public Boolean getDisplayViewPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayViewPanel");
        }
        return displayViewPanel;
    }

    /**
     * Method setDisplayViewPanel.
     *
     * @param displayViewPanel Boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setDisplayViewPanel(Boolean)
     */
    @Override
    public void setDisplayViewPanel(Boolean displayViewPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayViewPanel");
        }
        this.displayViewPanel = displayViewPanel;
    }

    /**
     * Method getDisplayListPanel.
     *
     * @return Boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getDisplayListPanel()
     */
    @Override
    public Boolean getDisplayListPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayListPanel");
        }
        return displayListPanel;
    }

    /**
     * Method setDisplayListPanel.
     *
     * @param displayListPanel Boolean
     * @see net.ihe.gazelle.users.action.UserManagerLocal#setDisplayListPanel(Boolean)
     */
    @Override
    public void setDisplayListPanel(Boolean displayListPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayListPanel");
        }
        this.displayListPanel = displayListPanel;
    }

    /**
     * Method initializePanels.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#initializePanelsAndFilter()
     */
    @Override
    public void initializePanelsAndFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializePanels");
        }
        displayListPanel = true;
        displayViewPanel = false;
        displayEditPanel = false;

        filter = null;
        datamodel = null;
    }

    /**
     * Method closeViewOrEditPanelsAndOpenList.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#closeViewOrEditPanelsAndOpenList()
     */
    @Override
    public void closeViewOrEditPanelsAndOpenList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeViewOrEditPanelsAndOpenList");
        }
        initializePanelsAndFilter();
    }

    /**
     * Method openEditPanel.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#openEditPanel()
     */
    @Override
    public void openEditPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openEditPanel");
        }
        displayListPanel = false;
        displayViewPanel = false;
        displayEditPanel = true;
    }

    /**
     * Method openViewPanel.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#openViewPanel()
     */
    @Override
    public void openViewPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openViewPanel");
        }
        displayListPanel = false;
        displayViewPanel = true;
        displayEditPanel = false;

    }

    /**
     * Method intializeSelectedUser.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#intializeSelectedUser()
     */
    @Override
    public void intializeSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("intializeSelectedUser");
        }
        selectedUser = new User();
    }

    /**
     * This method gets users list for a reassignment (you cannot delete a user if this user is owner of systems)
     *
     * @param inUserToDelete User
     * @return List of users for reassignment * @see net.ihe.gazelle.users.action.UserManagerLocal#getUsersForReassignement(User)
     */
    @Override
    public List<User> getUsersForReassignement(User inUserToDelete) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsersForReassignement");
        }

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager()) {
            List<User> availableUsers = User.listAllUsersFromCompanyExceptUser(inUserToDelete.getInstitution(),
                    inUserToDelete);
            List<User> administrators = User.getUsersFiltered(null, Role.getADMINISTRATOR_ROLE());
            if (availableUsers != null) {
                availableUsers.addAll(administrators);
            } else {
                availableUsers = administrators;
            }
            return availableUsers;
        } else {
            return User.listAllUsersFromCompanyExceptUser(inUserToDelete.getInstitution(), inUserToDelete);
        }

    }

    @Override
    public GazelleListDataModel<User> getUsersForReassignementDM(User inUserToDelete) {
        return new GazelleListDataModel<User>(getUsersForReassignement(inUserToDelete));
    }

    /**
     * Method getListRolesForSelectedUser.
     *
     * @return String
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getListRolesForSelectedUser()
     */
    @Override
    public String getListRolesForSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListRolesForSelectedUser");
        }
        StringBuilder res = new StringBuilder();
        if (this.selectedUser != null) {
            if (this.selectedUser.getRoles() != null) {
                int i = 0;
                for (Role role : this.selectedUser.getRoles()) {
                    if (i > 0) {
                        res.append(" / ");
                    }
                    res.append(role.getName());
                    i = i + 1;
                }
            }
        }
        return res.toString();
    }

    @Override
    public List<User> getUsersList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsersList");
        }
        if (datamodel != null) {
            users = (List<User>) datamodel.getAllItems(FacesContext.getCurrentInstance());
        } else {
            users = User.listAllUsers();
        }
        return users;
    }

    /**
     * Method getUsersListDependingSelectedCriterias.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getUsersListDependingSelectedCriterias()
     */
    @Override
    public void getUsersListDependingSelectedCriterias() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsersListDependingSelectedCriterias");
        }

        if (Role.isLoggedUserAdmin()) {

            findUsersForCompany();
        } else if (Role.isLoggedUserVendorAdmin()) {
            if (users != null) {
                users.clear();
            }
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.flush();

            Institution institution = Institution.getLoggedInInstitution();

            users = User.getAllUsersFromInstituion(institution, null);

        } else {
        }
        if ((this.selectedRoles != null) && (this.selectedRoles.size() > 0)) {
            List<User> tmpuser = new ArrayList<User>();
            if (this.users != null) {
                for (User uss : this.users) {
                    boolean toadd = true;
                    for (String roll : this.selectedRoles) {
                        if (!uss.getRoles().contains(Role.getRoleWithName(roll))) {
                            toadd = false;
                        }
                    }
                    if (toadd) {
                        tmpuser.add(uss);
                    }
                }
                this.users = tmpuser;
            }
        }
    }

    /**
     * Method resetVariables.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#resetVariables()
     */
    @Override
    public void resetVariables() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetVariables");
        }
        this.selectedRoles = new ArrayList<String>();
        this.choosenInstitutionForAdmin = null;
    }

    /**
     * returns the list of users for a given company with role != vendor_admin_role
     *
     * @param institution Institution
     * @return List<User>
     * @see net.ihe.gazelle.users.action.UserManagerLocal#getNoVendorAdminUsersForInstitution(Institution)
     */
    @Override
    public List<User> getNoVendorAdminUsersForInstitution(Institution institution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNoVendorAdminUsersForInstitution");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Session session = (Session) em.getDelegate();
        Criteria c = session.createCriteria(User.class);
        c.add(Restrictions.eq("institution", institution));
        List<User> users = c.list();
        List<User> usersToReturn = new ArrayList<User>();
        if ((users != null) && !users.isEmpty()) {
            for (User user : users) {
                if (!user.getRoles().contains(Role.getVENDOR_ADMIN_ROLE())) {
                    usersToReturn.add(user);
                } else {
                    continue;
                }
            }
            return usersToReturn;
        } else {
            return null;
        }
    }

    @Override
    public GazelleListDataModel<User> getNoVendorAdminUsersForInstitutionDM(Institution institution) {
        return new GazelleListDataModel<User>(getNoVendorAdminUsersForInstitution(institution));
    }

    public List<String> getFilteredUsersEmails() {
        List<User> user = getUsersList();
        List<String> usersEmails = new ArrayList<>();
        for (User u : user) {
            usersEmails.add(u.getEmail());
        }
        return usersEmails;
    }

    public String getFilteredUsersEmailsToString() {
        List<String> usersEmails = getFilteredUsersEmails();
        StringBuilder message = new StringBuilder("");
        for (String email : usersEmails) {
            message.append(email);
            message.append(",");
        }
        message.deleteCharAt(message.length() - 1);
        return message.toString();
    }

    public int getNumberOfFilteredUsers() {
        return getUsersList().size();
    }

    /**
     * Destroy the Manager bean when the session is over.
     *
     * @see net.ihe.gazelle.users.action.UserManagerLocal#destroy()
     */
    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public boolean canViewEmailAddress(User monitor) {
        if (Authorizations.VENDOR_ADMIN.isGranted()) {
            Institution loggedInUser = User.loggedInUser().getInstitution();

            Institution institution = monitor.getInstitution();
            if (institution.getId().equals(loggedInUser.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     */
    public static enum EmailType implements
            EmailTemplate {

        /**
         * Field TO_USER_ON_USER_REGISTERING_WITHOUT_INSTITUTION.
         */
        TO_USER_ON_USER_REGISTERING_WITHOUT_INSTITUTION("/email/toUserOnUserRegisteringWithoutInstitution.xhtml"),

        /**
         * Field TO_INSTITUTION_ADMIN_ON_USER_REGISTERING.
         */
        TO_INSTITUTION_ADMIN_ON_USER_REGISTERING("/email/toInstitutionAdminOnUserRegistering.xhtml"),

        /**
         * Field TO_ADMIN_ON_USER_REGISTERING.
         */
        TO_ADMIN_ON_USER_REGISTERING("/email/toAdminOnUserRegistering.xhtml"),

        /**
         * Field TO_USER_ON_USER_REGISTERING_WITH_INSTITUTION.
         */
        TO_USER_ON_USER_REGISTERING_WITH_INSTITUTION("/email/toUserOnUserRegisteringWithInstitution.xhtml"),

        /**
         * Field TO_USER_ON_USER_REGISTERING_WITH_INSTITUTION_WITHOUT_ADMIN.
         */
        TO_USER_ON_USER_REGISTERING_WITH_INSTITUTION_WITHOUT_ADMIN("/email/toUserOnUserRegisteringWithInstitutionWithoutAdmin.xhtml"),

        /**
         * Field TO_USER_ON_USER_ACTIVATED_BY_INSTITUTION_ADMIN.
         */
        TO_USER_ON_USER_ACTIVATED_BY_INSTITUTION_ADMIN("/email/toUserOnUserActivatedByInstitutionAdmin.xhtml"),

        /**
         * Field TO_USER_ON_USER_REGISTERED_BY_INSTITUTION_ADMIN.
         */
        TO_USER_ON_USER_REGISTERED_BY_INSTITUTION_ADMIN("/email/toUserOnUserRegisteredByInstitutionAdmin.xhtml"),

        /**
         * Field TO_USER_FORGOT_PASSWORD.
         */
        TO_USER_FORGOT_PASSWORD("/email/toUserForgotPassword.xhtml"),

        /**
         * Field TO_USER_UPDATED_PASSWORD_BY_ADMIN.
         */
        TO_USER_UPDATED_PASSWORD_BY_ADMIN("/email/toUserUpdatedPasswordByAdmin.xhtml"),

        /**
         * Field TO_USER_TO_CHNAGE_EMAIL.
         */
        TO_USER_TO_CHANGE_EMAIL("/email/toUserToChangeEmail.xhtml"),

        /**
         * Field TO_INSTITUTION_ADMIN_ON_PR_UNMATCHES.
         */
        TO_INSTITUTION_ADMIN_ON_PR_UNMATCHES("/email/prToInstitutionAdminOnUnmatches.xhtml"),

        /**
         * Field TO_INSTITUTION_ADMIN_ON_PR_UNREACHABLE.
         */
        TO_INSTITUTION_ADMIN_ON_PR_UNREACHABLE("/email/prToInstitutionAdminOnUnreachable.xhtml"),

        /**
         * Field TO_INSTITUTION_ADMIN_ON_PR_UNREFERENCE.
         */
        TO_INSTITUTION_ADMIN_ON_PR_UNREFERENCE("/email/prToInstitutionAdminOnUnreference.xhtml"),

        /**
         * Field TO_ADMINS_ON_CRAWLER_END.
         */
        TO_ADMINS_ON_CRAWLER_END("/email/prToAdminsOnCrawlerEnd.xhtml"),

        /**
         * Field TO_INSTITUTION_ADMIN_ON_PR_ADMIN_STATUS.
         */
        TO_INSTITUTION_ADMIN_ON_PR_ADMIN_STATUS("/email/prToInstitutionAdminOnAdminStatus.xhtml"),

        /**
         * Field TO_ADMIN_TO_TEST_EMAIL.
         */
        TO_ADMIN_TO_TEST_EMAIL("/email/toAdminToTestEmail.xhtml");

        /**
         * Field pageName.
         */
        private String pageName;

        /**
         * Constructor for EmailType.
         *
         * @param pageName String
         */
        EmailType(String pageName) {
            this.pageName = pageName;
        }

        /**
         * Method getPageName.
         *
         * @return String
         * @see net.ihe.gazelle.users.action.EmailTemplate#getPageName()
         */
        @Override
        public String getPageName() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getPageName");
            }
            return pageName;
        }

    }
}
