package net.ihe.gazelle.tm.application.action;

import net.ihe.gazelle.tm.application.model.Intro;
import net.ihe.gazelle.tm.application.model.IntroQuery;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gthomazon on 03/05/16.
 */
@Name("introManager")
@Scope(ScopeType.PAGE)
public class IntroManager {

    private static final Logger LOG = LoggerFactory.getLogger(IntroManager.class);

    private boolean editMainContent;
    private Intro selectedIntro = null;

    public boolean isEditMainContent() {
        return editMainContent;
    }

    public void setEditMainContent(boolean editMainContent) {
        this.editMainContent = editMainContent;
    }

    public Intro getSelectedIntro() {
        this.selectedIntro = getIntroContentForLocal();
        return selectedIntro;
    }

    public void setSelectedIntro(Intro selectedIntro) {
        this.selectedIntro = selectedIntro;
    }

    /**
     * Save the intro and cancel edit mode
     */
    public void saveIntro(String part) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveIntro");
        }
        Intro savedIntro = realSaveIntro();
        if (savedIntro != null) {
            selectedIntro = savedIntro;
        } else {
            FacesMessages.instance()
                    .add(StatusMessage.Severity.ERROR, "An error occurred when saving the intro configuration for language : "
                            + selectedIntro.getLanguage());
        }
        if (part.equals("mainContent")) {
            editMainContent = false;
        }
    }

    protected Intro realSaveIntro() {
        if (selectedIntro.getLanguage() == null) {
            selectedIntro.setLanguage(org.jboss.seam.core.Locale.instance().getISO3Language());
        }
        Intro savedIntro = Intro.saveIntro(selectedIntro);
        return savedIntro;
    }

    public Intro getIntroContentForLocal() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntroContentForLocal");
        }
        String selectedLanguage = org.jboss.seam.core.Locale.instance().getISO3Language();
        if (StringUtils.trimToNull(selectedLanguage) == null) {
            selectedLanguage = "eng";
        }
        Intro intro = getIntro(selectedLanguage);
        if (intro == null) {
            intro = getIntro("eng");
        }
        if (intro == null) {
            intro = new Intro(null);
        }
        return intro;
    }

    protected Intro getIntro(String selectedLanguage) {
        IntroQuery introQuery = new IntroQuery();
        introQuery.language().eq(selectedLanguage);
        Intro intro = introQuery.getUniqueResult();
        return intro;
    }

}
