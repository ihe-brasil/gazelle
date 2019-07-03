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
package net.ihe.gazelle.tm.users.action;

import net.ihe.gazelle.common.action.CacheRequest;
import net.ihe.gazelle.common.action.CacheUpdater;
import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.filecache.FileCacheRenderer;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.menu.GazelleMenu;
import net.ihe.gazelle.preferences.PreferenceService;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemInSessionUser;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemInSessionUserQuery;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.users.model.UserPhoto;
import net.ihe.gazelle.tm.users.model.UserPreferences;
import net.ihe.gazelle.tm.users.model.UserPreferencesQuery;
import net.ihe.gazelle.users.action.Authenticator;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.util.ThumbnailCreator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * <b>Class Description : </b>UserManagerTM<br>
 * <br>
 * This class manage the User object only for TestManagement (ie. to reassign an owner(user) to a system). It corresponds to the Business Layer.
 * All operations to implement are done in this class :
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @class UserManagerTM.java
 * @package net.ihe.gazelle.tm.users.action
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */
@Name("userManagerExtra")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("UserManagerExtraLocal")
public class UserManagerExtra implements Serializable, UserManagerExtraLocal {

    public static final Integer DEFAULT_NUMBER_OF_RESULTS_PER_PAGE = 20;
    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = 1764723220971805773L;
    private static final Logger LOG = LoggerFactory.getLogger(UserManagerExtra.class);
    private static UserPreferences DEFAULT_USER_PREFERENCES = null;
    /**
     * Flag indicating if we belonging to a owner to delete, variable used between business and presentation layer
     */
    Boolean reassignPanelRendered;
    /**
     * System to reassign (owner) managed by this bean, variable used between business and presentation layer
     */
    System systemToReassign;
    /**
     * facesMessages is the interface used to display the information/error messages in the JSF.
     */
    private User selectedUser;
    private List<System> systemsWithOwnerToDelete;
    private boolean selectedUserForDeleteIsMonitor;
    private Boolean selectNewVendorAdmin;
    private Date lastUserAction;
    private TestingSession newTestingSessionSelected;
    private String fakedPasswordField;

    private static UserPreferences getDefaultUserPreferences() {
        if (DEFAULT_USER_PREFERENCES == null) {
            synchronized (UserManagerExtra.class) {
                if (DEFAULT_USER_PREFERENCES == null) {
                    DEFAULT_USER_PREFERENCES = new UserPreferences();
                    DEFAULT_USER_PREFERENCES.setNumberOfResultsPerPage(DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
                    DEFAULT_USER_PREFERENCES.setShowSequenceDiagram(true);
                }
            }
        }
        return DEFAULT_USER_PREFERENCES;
    }

    @Override
    public void userDoneAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("userDoneAction");
        }
        lastUserAction = new Date();
    }

    @Override
    public long getLogoutDateTime() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLogoutDateTime");
        }
        if (lastUserAction != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(lastUserAction);
            Integer sessionTimeout = PreferenceService.getInteger("use_messages_session_timeout");
            if (sessionTimeout == null) {
                sessionTimeout = 40;
            }
            c.add(Calendar.MINUTE, sessionTimeout);
            Date logoutDateTime = c.getTime();
            if (logoutDateTime.before(new Date())) {
                FacesContext context = FacesContext.getCurrentInstance();
                ExternalContext ec = context.getExternalContext();
                HttpServletRequest request = (HttpServletRequest) ec.getRequest();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    org.jboss.seam.web.Session.instance().invalidate();
                }
                return 0;
            }
            return logoutDateTime.getTime();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isSelectedUserForDeleteIsMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isSelectedUserForDeleteIsMonitor");
        }
        return selectedUserForDeleteIsMonitor;
    }

    @Override
    public void setSelectedUserForDeleteIsMonitor(boolean selectedUserForDeleteIsMonitor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedUserForDeleteIsMonitor");
        }
        this.selectedUserForDeleteIsMonitor = selectedUserForDeleteIsMonitor;
    }

    @Override
    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        UserPreferences selectedUserPreferences = getSelectedUserPreferences();
        if (selectedUserPreferences != null) {
            return selectedUserPreferences.getSelectedTestingSession();
        } else {
            return null;
        }
    }

    @Override
    public void setSelectedTestingSession(TestingSession selectedTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestingSession");
        }
        persistSelectedTestingSession(selectedTestingSession);
        //need to refresh the Menu once testing Session change is successful
        GazelleMenu.refreshMenu();

        // Invalidate bean to refresh the  aipo list when session is changed.
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userManagerExtra", null);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("aipoSelector", null);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("reportManagerBean", null);
    }

    @Override
    public void setSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestingSession");
        }
        persistSelectedTestingSession(getNewTestingSessionSelected());
        //need to refresh the Menu once testing Session change is successful
        GazelleMenu.refreshMenu();
    }

    @Override
    public boolean displayToolTips() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayToolTips");
        }
        if (!getSelectedUserPreferences().getDisplayTooltips()) {
            return false;
        }
        return true;
    }

    @Override
    public UserPreferences getSelectedUserPreferences() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedUserPreferences");
        }
        CacheRequest cacheRequest = (CacheRequest) Component.getInstance("cacheRequest");
        UserPreferences userPreferences = (UserPreferences) cacheRequest.getValueUpdater("getSelectedUserPreferences",
                new CacheUpdater() {
                    @Override
                    public Object getValue(String key, Object parameter) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("getValue");
                        }

                        User loggedInUser = User.loggedInUser();
                        if (loggedInUser == null) {
                            return getDefaultUserPreferences();
                        }
                        EntityManager em = EntityManagerService.provideEntityManager();

                        UserPreferencesQuery userPreferencesQuery = new UserPreferencesQuery();
                        userPreferencesQuery.user().id().eq(loggedInUser.getId());
                        UserPreferences userPreferences = userPreferencesQuery.getUniqueResult();

                        if (userPreferences == null) {

                            userPreferences = new UserPreferences();
                            userPreferences.setUser(loggedInUser);
                            userPreferences.setShowSequenceDiagram(true);
                            userPreferences
                                    .setNumberOfResultsPerPage(UserPreferences.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
                            em.persist(userPreferences);
                            em.flush();
                        }

                        if (userPreferences.getNumberOfResultsPerPage() == null) {
                            userPreferences
                                    .setNumberOfResultsPerPage(UserPreferences.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
                            em.merge(userPreferences);
                            em.flush();
                        }

                        if (userPreferences.getSelectedTestingSession() != null) {
                            if (!userPreferences.getSelectedTestingSession().getActiveSession()) {
                                userPreferences.setSelectedTestingSession(null);
                            }
                        }

                        if (userPreferences.getSelectedTestingSession() == null) {
                            TestingSession defaultTestingSession = TestingSession.GetDefaultTestingSession();
                            userPreferences.setSelectedTestingSession(defaultTestingSession);
                            em.merge(userPreferences);
                            em.flush();
                        }

                        return userPreferences;
                    }
                }, null);

        if (userPreferences.getSelectedTestingSession() != null) {
            Contexts.getSessionContext().set("selectedTestingSession", userPreferences.getSelectedTestingSession());
        }

        return userPreferences;
    }

    @Override
    public User getSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedUser");
        }
        if (selectedUser == null) {
            selectedUser = User.loggedInUser();
        }
        return selectedUser;
    }

    @Override
    public void setSelectedUser(User selectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedUser");
        }
        this.selectedUser = selectedUser;
    }

    @Override
    public List<System> getSystemsWithOwnerToDelete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsWithOwnerToDelete");
        }
        return systemsWithOwnerToDelete;
    }

    @Override
    public void setSystemsWithOwnerToDelete(List<System> systemsWithOwnerToDelete) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemsWithOwnerToDelete");
        }
        this.systemsWithOwnerToDelete = systemsWithOwnerToDelete;
    }

    @Override
    public System getSystemToReassign() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemToReassign");
        }
        return systemToReassign;
    }

    @Override
    public void setSystemToReassign(System systemToReassign) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemToReassign");
        }
        this.systemToReassign = systemToReassign;
    }

    @Override
    public Boolean getSelectNewVendorAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectNewVendorAdmin");
        }
        return selectNewVendorAdmin;
    }

    @Override
    public void setSelectNewVendorAdmin(Boolean selectNewVendorAdmin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectNewVendorAdmin");
        }
        this.selectNewVendorAdmin = selectNewVendorAdmin;
    }

    /**
     * Select and initialize variables before user deletion (eg. we check that this user is not owner of a system)
     *
     * @param selectedUser
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedUserForDelete(User selectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedUserForDelete");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        Session s = (Session) entityManager.getDelegate();
        systemsWithOwnerToDelete = null;
        selectNewVendorAdmin = false;

        selectedUserForDeleteIsMonitor = MonitorInSession.isUserMonitor(selectedUser);

        // if the selected user is vendor_admin, we need to check that another
        // user is vendor_admin
        // for this company. Otherwise, display a modal panel to designate
        // someone else
        if (selectedUser.getRoles().contains(Role.getVENDOR_ADMIN_ROLE())) {
            List<User> vendorAdminUsers = User.getUsersFiltered(selectedUser.getInstitution(),
                    Role.getVENDOR_ADMIN_ROLE());
            if ((vendorAdminUsers != null) && (vendorAdminUsers.size() == 1)) {
                selectNewVendorAdmin = true;
            } else {
                selectNewVendorAdmin = false;
            }
        }

        // We check if this user is owner of a system
        Criteria c = s.createCriteria(System.class);
        c.createAlias("ownerUser", "ownerUser");
        c.add(Restrictions.eq("ownerUser.id", selectedUser.getId()));

        systemsWithOwnerToDelete = c.list();

        reassignPanelRendered = false;
        this.selectedUser = selectedUser;
    }

    /**
     * @param inUser
     */
    @Override
    public void addRoleVendorAdminToUser(User inUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addRoleVendorAdminToUser");
        }
        if (inUser == null) {
            return;
        } else if (inUser.getRoles() != null) {
            inUser.getRoles().add(Role.getVENDOR_ADMIN_ROLE());
        } else {
            List<Role> roles = new ArrayList<Role>();
            roles.add(Role.getVENDOR_ADMIN_ROLE());
            inUser.setRoles(roles);
        }
        selectNewVendorAdmin = false;
    }

    /**
     * Get the UserPreferences object of the logged in User and set in in global variable
     */
    @Override
    public void saveShowSequenceDiagram() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveShowSequenceDiagram");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        em.merge(getSelectedUserPreferences());
        em.flush();
    }

    /**
     * Set the new owner of the current system to reassign
     */
    @Override
    public void assignOwner(User u) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("assignOwner");
        }

        if (u != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            systemToReassign.setOwnerUser(u);
            entityManager.merge(systemToReassign);
            entityManager.flush();

        }
        setSelectedUserForDelete(selectedUser);
    }

    /**
     * Set the boolean flag to TRUE indicating that we display the panel reassigning a system owner
     */
    @Override
    public void showReassignPanel(System sys) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showReassignPanel");
        }

        if (sys != null) {

            systemToReassign = sys;
        }
        reassignPanelRendered = true;
    }

    /**
     * Set the boolean flag to FALSE indicating that we hide the panel reassigning a system owner
     */
    @Override
    public void hideReassignPanel(System sys) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hideReassignPanel");
        }

        reassignPanelRendered = false;
    }

    /**
     * Get the boolean flag indicating if we display the panel reassigning a system owner
     *
     * @return Boolean : true if we render the panel
     */
    @Override
    public Boolean getReassignPanelRendered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReassignPanelRendered");
        }
        return reassignPanelRendered;
    }

    /**
     * Set the boolean flag indicating if we display the panel reassigning a system owner
     *
     * @param reassignPanelRendered - Boolean : true if we render the panel
     */
    @Override
    public void setReassignPanelRendered(Boolean reassignPanelRendered) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReassignPanelRendered");
        }
        this.reassignPanelRendered = reassignPanelRendered;
    }

    /**
     * We cannot delete a user which have some SystemInSessionUsers object. This method returns a boolean flag indicating if the user to delete
     * have some SystemInsESSIONuSER OBJECTS;
     *
     * @param user : user to be deleted
     * @return boolean - Boolean : true if we cannot delete the user
     */
    @Override
    @SuppressWarnings("unchecked")
    public Boolean hasSystemInSessionUserObject(User inUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasSystemInSessionUserObject");
        }
        Boolean iBoolean = false;

        if (inUser != null) {

            SystemInSessionUserQuery query = new SystemInSessionUserQuery();
            query.user().id().eq(inUser.getId());
            List<SystemInSessionUser> systemsInSessionUsers = query.getListDistinct();
            if (systemsInSessionUsers != null) {
                if (systemsInSessionUsers.size() > 0) {
                    iBoolean = true;
                }
            }

        } else {
            LOG.error("hasSystemInSessionUserObject - inUser is null");
            return false;
        }

        return iBoolean;
    }

    @Override
    public void outjectSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("outjectSelectedUser");
        }
        Contexts.getSessionContext().set("selectedUser", selectedUser);
    }

    /**
     * Set, outject and persist the information concerning the Testing Session selected by the user
     */
    @Override
    public void persistSelectedTestingSession(TestingSession sts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedTestingSession");
        }
        if (sts == null) {
            return;
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        TestingSession selectedTestingSession = entityManager.find(TestingSession.class, sts.getId());
        Contexts.getSessionContext().set("selectedTestingSession", selectedTestingSession);

        Query query = entityManager.createQuery("SELECT up FROM UserPreferences up where up.user = :inUser");
        query.setParameter("inUser", User.loggedInUser());

        UserPreferences userPreferencesFound = (UserPreferences) query.getSingleResult();
        userPreferencesFound.setSelectedTestingSession(selectedTestingSession);
        entityManager.merge(userPreferencesFound);
        entityManager.flush();

        Authenticator.resetSession(User.loggedInUser());
        CacheRequest cacheRequest = (CacheRequest) Component.getInstance("cacheRequest");
        cacheRequest.removeValue("user_testing_session");

    }

    /**
     * Delete the selected user (called from the confirmation modal panel) This operation is allowed for some granted users (check the security.drl)
     *
     * @return String : JSF page to render
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
        Contexts.getSessionContext().set("selectedUser", selectedUser);

        if (selectedUser == null) {
            LOG.error(" cannot delete user : user is null ");
            return;
        }

        try {
            User.deleteUser(selectedUser);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    public void getUserPhoto() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserPhoto");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        final int userId = Integer.parseInt(params.get("id"));

        String sizeString = StringUtils.trimToEmpty(params.get("size"));
        if (!sizeString.isEmpty()) {
            sizeString = "_XS";
        }

        FileCache.getFile("UserPhoto_" + userId + sizeString, sizeString, new FileCacheRenderer() {

            @Override
            public void render(OutputStream out, String value) throws Exception {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("render");
                }
                HQLQueryBuilder<UserPreferences> queryBuilder = new HQLQueryBuilder<UserPreferences>(
                        UserPreferences.class);
                queryBuilder.addEq("user.id", userId);
                List<UserPreferences> list = queryBuilder.getList();
                byte[] bytes = null;
                if (list.size() > 0) {
                    UserPreferences userPreferences = list.get(0);
                    UserPhoto userPhoto = userPreferences.getUserPhoto();
                    if (userPhoto != null) {
                        bytes = userPhoto.getPhotoBytes();
                    }
                }
                if (bytes == null) {
                    InputStream resourceAsStream = this.getClass().getResourceAsStream("/nophoto.png");
                    if (resourceAsStream != null) {
                        bytes = IOUtils.toByteArray(resourceAsStream);
                        resourceAsStream.close();
                    }
                }
                if (bytes != null) {
                    if ("_XS".equals(value)) {
                        bytes = ThumbnailCreator.createThumbnailPNG(bytes, 20);
                    }
                    out.write(bytes);
                }
            }

            @Override
            public String getContentType() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getContentType");
                }
                return "image/png";
            }
        });
    }

    /**
     * Destroy the Manager bean when the session is over.
     */
    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public String goHome() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("goHome");
        }
        return "/home.seam";
    }

    public TestingSession getNewTestingSessionSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNewTestingSessionSelected");
        }
        return newTestingSessionSelected;
    }

    public void setNewTestingSessionSelected(TestingSession newTestingSessionSelected) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNewTestingSessionSelected");
        }
        this.newTestingSessionSelected = newTestingSessionSelected;
    }

    public String getFakedPasswordField() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFakedPasswordField");
        }
        return fakedPasswordField;
    }

    public void setFakedPasswordField(String fakedPasswordField) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFakedPasswordField");
        }
        this.fakedPasswordField = fakedPasswordField;
    }
}
