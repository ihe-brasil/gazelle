package net.ihe.gazelle.tm.application.action;

import net.ihe.gazelle.tm.application.model.Home;
import net.ihe.gazelle.tm.application.model.HomeQuery;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Name("homeManager")
@Scope(ScopeType.PAGE)
public class HomeManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(HomeManager.class);

    private Home selectedHome = null;
    private boolean editMainPanelHeader;
    private boolean editMainContent;
    private boolean editSecondaryPanelHeader;
    private boolean editSecondaryContent;
    private List<SelectItem> positions;

    public Home getSelectedHome() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedHome");
        }
        selectedHome = getHomeContentForLocal();
        return selectedHome;
    }

    public void setSelectedHome(Home selectedHome) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedHome");
        }
        this.selectedHome = selectedHome;
    }

    public boolean isEditMainPanelHeader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEditMainPanelHeader");
        }
        return editMainPanelHeader;
    }

    public void setEditMainPanelHeader(boolean editMainPanelHeader) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditMainPanelHeader");
        }
        this.editMainPanelHeader = editMainPanelHeader;
    }

    public boolean isEditMainContent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEditMainContent");
        }
        return editMainContent;
    }

    public void setEditMainContent(boolean editMainContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditMainContent");
        }
        this.editMainContent = editMainContent;
    }

    public boolean isEditSecondaryPanelHeader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEditSecondaryPanelHeader");
        }
        return editSecondaryPanelHeader;
    }

    public void setEditSecondaryPanelHeader(boolean editSecondaryPanelHeader) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditSecondaryPanelHeader");
        }
        this.editSecondaryPanelHeader = editSecondaryPanelHeader;
    }

    public boolean isEditSecondaryContent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEditSecondaryContent");
        }
        return editSecondaryContent;
    }

    public void setEditSecondaryContent(boolean editSecondaryContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditSecondaryContent");
        }
        this.editSecondaryContent = editSecondaryContent;
    }

    public List<SelectItem> getPositions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPositions");
        }
        return positions;
    }

    public void setPositions(List<SelectItem> positions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPositions");
        }
        this.positions = positions;
    }

    /**
     * Save the home and cancel edit mode
     */
    public void saveHome(String part) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveHome");
        }
        Home savedHome = realSaveHome();
        if (savedHome != null) {
            selectedHome = savedHome;
        } else {
            FacesMessages.instance()
                    .add(StatusMessage.Severity.ERROR, "An error occurred when saving the home configuration for language : "
                            + selectedHome.getLanguage());
        }
        if (part.equals("mainHeader")) {
            editMainPanelHeader = false;
        }
        if (part.equals("mainContent")) {
            editMainContent = false;
        }
        if (part.equals("secondaryContent")) {
            editSecondaryContent = false;
        }
        if (part.equals("secondaryHeader")) {
            editSecondaryPanelHeader = false;
        }
    }

    protected Home realSaveHome() {
        if (selectedHome.getLanguage() == null) {
            selectedHome.setLanguage(org.jboss.seam.core.Locale.instance().getISO3Language());
        }
        Home savedHome = Home.saveHome(selectedHome);
        return savedHome;
    }

    /**
     * @return
     */
    public Home getHomeContentForLocal() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHomeContentForLocal");
        }
        String selectedLanguage = org.jboss.seam.core.Locale.instance().getISO3Language();
        if (StringUtils.trimToNull(selectedLanguage) == null) {
            selectedLanguage = "eng";
        }
        Home home = getHome(selectedLanguage);
        if (home == null) {
            home = getHome("eng");
        }
        if (home == null) {
            home = new Home(null);
        }
        return home;
    }

    protected Home getHome(String selectedLanguage) {
        HomeQuery homeQuery = new HomeQuery();
        homeQuery.language().eq(selectedLanguage);
        Home home = homeQuery.getUniqueResult();
        return home;
    }

    public void createListOfPositions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createListOfPositions");
        }
        positions = new ArrayList<SelectItem>();
        positions.add(new SelectItem("above", ResourceBundle.instance().getString("gazelle.tm.home.above")));
        positions.add(new SelectItem("below", ResourceBundle.instance().getString("gazelle.tm.home.below")));
    }

    public void moveSecondaryPanel(String newPosition) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("moveSecondaryPanel");
        }
        selectedHome.setSecondaryPanelPosition(newPosition);
        selectedHome = realSaveHome();
    }
}
