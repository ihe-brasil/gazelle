package net.ihe.gazelle.tm.users.action;

import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.fineuploader.FineuploaderListener;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.menu.GazelleMenu;
import net.ihe.gazelle.tm.users.model.UserPhoto;
import net.ihe.gazelle.tm.users.model.UserPreferences;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.users.model.UserQuery;
import net.ihe.gazelle.util.ThumbnailCreator;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Name("userPreferencesManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("UserPreferencesManagerLocal")
public class UserPreferencesManager implements Serializable, FineuploaderListener, UserPreferencesManagerLocal {

    private static final long serialVersionUID = -7614265382910976890L;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UserPreferencesManager.class);

    private User selectedUser;
    private UserPreferences userPreferences;
    private boolean displayLanguageSelection;
    private boolean editUser;
    private String selectedLanguage;
    private List<SelectItem> languageList;
    private boolean displayLangMenu;
    private boolean displayFileUpload;

    // locale language codes
    private List<String> spokenLanguages;
    private boolean viewUser;

    // iso2 language codes - not used in JSF -
    private List<String> selectedLanguages;

    @Override
    public User getSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedUser");
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
    public UserPreferences getUserPreferences() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserPreferences");
        }
        if (userPreferences == null) {
            if (selectedUser == null || selectedUser.getId().equals(User.loggedInUser().getId())) {
                initialize();
            } else {
                initializeOther();
            }
        }
        return userPreferences;
    }

    @Override
    public void setUserPreferences(UserPreferences userPreferences) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setUserPreferences");
        }
        this.userPreferences = userPreferences;
    }

    @Override
    public boolean getDisplayLanguageSelection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayLanguageSelection");
        }
        return displayLanguageSelection;
    }

    @Override
    public void setDisplayLanguageSelection(boolean displayLanguageSelection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayLanguageSelection");
        }
        this.displayLanguageSelection = displayLanguageSelection;
    }

    @Override
    public boolean getEditUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditUser");
        }
        return editUser;
    }

    @Override
    public void setEditUser(boolean editUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditUser");
        }
        this.editUser = editUser;
    }

    @Override
    public boolean getViewUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getViewUser");
        }
        return viewUser;
    }

    @Override
    public void setViewUser(boolean viewUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setViewUser");
        }
        this.viewUser = viewUser;
    }

    @Override
    public String getSelectedLanguage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedLanguage");
        }
        return selectedLanguage;
    }

    @Override
    public void setSelectedLanguage(String selectedLanguage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedLanguage");
        }
        this.selectedLanguage = selectedLanguage;
    }

    @Override
    public List<String> getSpokenLanguages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpokenLanguages");
        }
        return spokenLanguages;
    }

    @Override
    public void setSpokenLanguages(List<String> spokenLanguages) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSpokenLanguages");
        }
        this.spokenLanguages = spokenLanguages;
    }

    @Override
    public List<SelectItem> getLanguageList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLanguageList");
        }
        return languageList;
    }

    @Override
    public void setLanguageList(List<SelectItem> languageList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLanguageList");
        }
        this.languageList = languageList;
    }

    @Override
    public boolean getDisplayLangMenu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayLangMenu");
        }
        return displayLangMenu;
    }

    @Override
    public void setDisplayLangMenu(boolean displayLangMenu) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayLangMenu");
        }
        this.displayLangMenu = displayLangMenu;
    }

    @Override
    public boolean getDisplayFileUpload() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayFileUpload");
        }
        return displayFileUpload;
    }

    @Override
    public void setDisplayFileUpload(boolean displayFileUpload) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayFileUpload");
        }
        this.displayFileUpload = displayFileUpload;
    }

    @Override
    public void initialize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialize");
        }

        selectedUser = User.loggedInUser();
        userPreferences = UserPreferences.getPreferencesForUser(selectedUser);
        displayFileUpload = false;
        displayLangMenu = false;
        viewPreferences();
    }

    @Override
    public void initializeOther() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeOther");
        }

        selectedUser = (User) Contexts.getSessionContext().get("userForPreferences");
        userPreferences = UserPreferences.getPreferencesForUser(selectedUser);
        if (userPreferences == null) {
            // Create user preferences
            userPreferences = new UserPreferences();
            userPreferences.setShowSequenceDiagram(true);
            userPreferences.setUser(selectedUser);
            userPreferences.setDisplayEmail(false);
            userPreferences.setNumberOfResultsPerPage(UserPreferences.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE);
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            userPreferences = entityManager.merge(userPreferences);
            entityManager.flush();

        }
        displayFileUpload = false;
        displayLangMenu = false;
        editPreferences();
    }

    private void listSpokenLanguages() {
        if ((userPreferences != null) && (userPreferences.getSpokenLanguages() != null)
                && !userPreferences.getSpokenLanguages().isEmpty()) {
            String[] languages = userPreferences.getSpokenLanguages().split(",");
            spokenLanguages = new ArrayList<String>();

            for (String lang : languages) {
                Locale locale = new Locale(lang);
                spokenLanguages.add(locale.getDisplayLanguage(LocaleSelector.instance().getLocale()));
            }
        } else {
            spokenLanguages = new ArrayList<String>();
        }
    }

    @Override
    public void editPreferences() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editPreferences");
        }
        editUser = true;
        viewUser = false;
        listSpokenLanguages();
        createLanguageList();
    }

    @Override
    public void viewPreferences() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewPreferences");
        }
        editUser = false;
        viewUser = true;
        selectedLanguage = null;
        listSpokenLanguages();
    }

    @Override
    public void savePreferences() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("savePreferences");
        }
        if (userPreferences != null) {
            if ((selectedLanguages != null) && !selectedLanguages.isEmpty()) {
                String languages = "";
                for (String lang : selectedLanguages) {
                    if (languages.length() == 0) {
                        languages = lang;
                    } else {
                        languages = languages.concat(",").concat(lang);
                    }
                }

                userPreferences.setSpokenLanguages(languages);
            }


            UserQuery query = new UserQuery();
            if (userPreferences.getUser().getId() != null) {
                query.id().neq(userPreferences.getUser().getId());
            }
            query.email().eq(userPreferences.getUser().getEmail());

            if (query.getListDistinct().size() != 0) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This email is already used !");
                return;
            }

            EntityManager em = EntityManagerService.provideEntityManager();
            userPreferences = em.merge(userPreferences);
            selectedUser = em.merge(selectedUser);
            em.flush();

            FacesMessages.instance().add(StatusMessage.Severity.INFO, selectedUser + " preferences are saved !");
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, selectedUser + " preferences are not saved !");
            LOG.error("The user preferences are null");
        }
        viewPreferences();
        GazelleMenu.refreshMenu();
    }

    @Transactional
    public void uploadedFile(File tmpFile, String filename, String id, String param) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("uploadedFile");
        }
        if ("userPhoto".equals(id)) {
            BufferedImage image = ImageIO.read(new FileInputStream(tmpFile));
            if (image == null) {
                if (tmpFile.delete()) {
                    LOG.info("tmpFile deleted");
                } else {
                    LOG.error("Failed to delete tmpFile");
                }
                throw new IOException("Not a valid image");
            }
            saveUserPhoto(tmpFile);
            if (tmpFile.delete()) {
                LOG.info("tmpFile deleted");
            } else {
                LOG.error("Failed to delete tmpFile");
            }
        }
    }

    @Transactional
    protected void saveUserPhoto(File tmpFile) throws IOException {
        byte[] bytes = ThumbnailCreator.createThumbnailPNG(new FileInputStream(tmpFile));

        EntityManager em = EntityManagerService.provideEntityManager();

        reloadUserPreferences();

        UserPhoto userPhoto = userPreferences.getUserPhoto();
        if (userPhoto == null) {
            userPhoto = new UserPhoto();
            em.persist(userPhoto);
            userPreferences.setUserPhoto(userPhoto);
        }

        userPhoto.setPhotoBytes(bytes);
        FileCache.resetCache("UserPhoto_" + userPreferences.getUser().getId());
        FileCache.resetCache("UserPhoto_" + userPreferences.getUser().getId() + "_XS");
        userPreferences = em.merge(userPreferences);
        em.flush();
    }

    private void reloadUserPreferences() {
        HQLQueryBuilder<UserPreferences> queryBuilder = new HQLQueryBuilder<UserPreferences>(UserPreferences.class);
        queryBuilder.addEq("id", userPreferences.getId());
        List<UserPreferences> list = queryBuilder.getList();
        if (list.size() > 0) {
            userPreferences = list.get(0);
        }
    }

    /**
     * @return
     */
    private void createLanguageList() {
        List<String> list = Arrays.asList(Locale.getISOLanguages());
        languageList = new ArrayList<SelectItem>();
        selectedLanguages = new ArrayList<String>();

        if (userPreferences != null) {
            if ((userPreferences.getSpokenLanguages() != null) && !userPreferences.getSpokenLanguages().isEmpty()) {
                for (String lang : userPreferences.getSpokenLanguages().split(",")) {
                    selectedLanguages.add(lang);
                }
            }
        }

        for (String language : list) {
            Locale locale = new Locale(language);
            languageList
                    .add(new SelectItem(language, locale.getDisplayLanguage(LocaleSelector.instance().getLocale())));
        }

        Collections.sort(languageList, new Comparator<SelectItem>() {
            @Override
            public int compare(SelectItem o1, SelectItem o2) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("compare");
                }
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

    }

    @Override
    public void addSpokenLanguage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSpokenLanguage");
        }
        if ((selectedLanguage != null) && !selectedLanguages.contains(selectedLanguage)) {
            selectedLanguages.add(selectedLanguage);
            Locale locale = new Locale(selectedLanguage);
            spokenLanguages.add(locale.getDisplayLanguage(LocaleSelector.instance().getLocale()));
        }
    }

    /**
     * @param selectedLanguage
     */
    @Override
    public void removeSpokenLanguage(String selectedLanguage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeSpokenLanguage");
        }
        spokenLanguages.remove(selectedLanguage);
        for (String lang : selectedLanguages) {
            Locale locale = new Locale(lang);
            if (!spokenLanguages.contains(locale.getDisplayLanguage(LocaleSelector.instance().getLocale()))) {
                selectedLanguages.remove(lang);
                break;
            } else {
                continue;
            }
        }
    }

    @Override
    public void saveNumberOfResultsPerPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveNumberOfResultsPerPage");
        }
        if (userPreferences != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            userPreferences = em.merge(userPreferences);
            em.flush();
        } else {
            return;
        }
    }

    @Override
    public void removePhoto() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removePhoto");
        }
        reloadUserPreferences();
        if ((userPreferences != null) && (userPreferences.getUserPhoto() != null)
                && (userPreferences.getUserPhoto().getPhotoBytes() != null)) {
            userPreferences.getUserPhoto().setPhotoBytes(null);
            FileCache.resetCache("UserPhoto_" + userPreferences.getUser().getId());
            FileCache.resetCache("UserPhoto_" + userPreferences.getUser().getId() + "_XS");
            EntityManager em = EntityManagerService.provideEntityManager();
            em.merge(userPreferences.getUserPhoto());
            userPreferences = em.merge(userPreferences);
            em.flush();
        } else {
            LOG.error("try to remove photo for user " + selectedUser.getUsername() + " but no path to image found");
        }
    }

    @Override
    public String changePasswordAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changePasswordAction");
        }
        return "/users/user/changePassword.seam";
    }

    @Override
    public String changePassword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changePassword");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        User userToModify = User.FindUserWithUsername(selectedUser.getUsername());
        if (userToModify == null) {
            return null;
        }
        if (userToModify.getPassword().equals(selectedUser.getOldPassword())) {
            if (selectedUser.getPassword().equals(selectedUser.getPasswordConfirmation())) {
                selectedUser = entityManager.merge(selectedUser);
                FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.users.authenticator.PasswordUpdated");
                return "/users/user/userPreferences.seam";
            } else {
                FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.authenticator.PasswordsDoNotMatch");
                return "/users/user/changePassword.seam";
            }
        } else {
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.users.authenticator.InvalidPassword");
            return "/users/user/changePassword.seam";
        }
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public UserPreferences getPreferencesForUser(User inUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPreferencesForUser");
        }
        return UserPreferences.getPreferencesForUser(inUser);
    }

    @Override
    public List<String> getSpokenLanguagesAsList(String languages) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpokenLanguagesAsList");
        }
        return UserPreferences.getSpokenLanguagesAsList(languages);
    }

    @Override
    public boolean displaySequenceDiagram() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displaySequenceDiagram");
        }
        return UserPreferences.displaySequenceDiagram();
    }

    public String displayPreviousPage() {
        String urlFrom = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if (urlFrom.equals("/users/user/userPreferences.xhtml")) {
            return "/users/user/userPreferences.seam";
        } else {
            return "/users/user/listUsersInstitution.seam";
        }
    }

}
