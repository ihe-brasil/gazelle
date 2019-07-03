package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLReloader;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.users.model.Role;
import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;

import static org.jboss.seam.ScopeType.PAGE;


@Name("noteManager")
@Scope(PAGE)
@Synchronized(timeout = 10000)
@GenerateInterface("NoteManagerLocal")
public class NoteManager implements Serializable, NoteManagerLocal {

    private static final Logger LOG = LoggerFactory.getLogger(NoteManager.class);
    protected SystemInSession selectedSystemInSession;
    @In
    private EntityManager entityManager;

    private String noteTobeAdded;

    public SystemInSession getSelectedSystemInSession() {
        return selectedSystemInSession;
    }

    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        this.selectedSystemInSession = selectedSystemInSession;
    }

    public String getNoteTobeAdded() {
        return noteTobeAdded;
    }

    public void setNoteTobeAdded(String noteTobeAdded) {
        this.noteTobeAdded = noteTobeAdded;
    }

    @Override
    @Create
    public void init() {
        String systemInSessionId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (systemInSessionId == null) {
            systemInSessionId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("system");
        }
        if (systemInSessionId != null) {
            try {
                Integer id = Integer.parseInt(systemInSessionId);
                selectedSystemInSession = entityManager.find(SystemInSession.class, id);
            } catch (NumberFormatException e) {
                LOG.error("failed to find system in session with id = " + systemInSessionId);
            }
        }
    }

    @Override
    public boolean userCanAddMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("userCanAddMessage");
        }
        return (net.ihe.gazelle.users.model.Role.isLoggedUserAdmin() || net.ihe.gazelle.users.model.Role.isLoggedUserMonitor() || Role
                .isLoggedUserProjectManager());
    }

    @Override
    public void addNotesOnSystem(SystemInSession ssis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNotesOnSystem");
        }
        if (ssis != null) {
            if (selectedSystemInSession == null || (!selectedSystemInSession.getId().equals(ssis.getId()))) {
                setSelectedSystemInSession(ssis);
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No system in session selected");
        }
        addNotesOnSystem();
    }

    @Override
    public void addNotesOnSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNotesOnSystem");
        }

        if (noteTobeAdded.trim().length() > 0) {
            String note = StringEscapeUtils.escapeHtml(noteTobeAdded);
            note = note.replace("\r\n", "<br/>");
            note = note.replace("\n", "<br/>");
            note = note.replace("\r", "<br/>");

            getSelectedSystemInSession().addNote(note);

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedSystemInSession = entityManager.merge(selectedSystemInSession);
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Your comment is empty !");
        }
        noteTobeAdded = null;
    }

    //    @Override
//    public void addNotesOnSystemAndInTestInstance(TestInstance currentTi, SystemInSession systemInSession,
//                                                  String commentTobeAdded) {
//        if (commentTobeAdded.trim().length() > 0) {
//            setSelectedSystemInSession(systemInSession);
//            setNoteTobeAdded("(from " + currentTi.getId().toString() + ")" + commentTobeAdded);
//            addNotesOnSystem();
//            TestInstanceManager tim = new TestInstanceManager();
//            tim.setSelectedTestInstance(currentTi);
//            tim.addComment();
//        }
//    }
    public void reloadSystemInSession() {
        selectedSystemInSession = HQLReloader.reloadDetached(selectedSystemInSession);
    }
}
