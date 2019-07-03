package net.ihe.gazelle.users.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.pages.GazelleSecurityCheck;
import net.ihe.gazelle.common.session.SessionTimeoutFilter;
import net.ihe.gazelle.common.util.GazelleCookie;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.exception.ExceptionHandler;
import org.jboss.seam.exception.Exceptions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.ViewExpiredException;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Name("sessionTimeoutManager")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@GenerateInterface("SessionTimeoutManagerLocal")
public class SessionTimeoutManager extends ExceptionHandler implements SessionTimeoutManagerLocal {

    private static final Logger LOG = LoggerFactory.getLogger(SessionTimeoutManager.class);
    private static final String LOGGED_ONCE = "LOGGED_ONCE";
    private static Map<String, String> storedLocations = Collections.synchronizedMap(new HashMap<String, String>());

    public SessionTimeoutManager() {
        super();
    }

    @Override
    @Observer("org.jboss.seam.postInitialization")
    public void initializeExceptionHandler() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeExceptionHandler");
        }

        // this handler will delegate some exceptions to us
        Exceptions.instance().getHandlers().add(0, new GazelleExceptionHandler());
    }

    @Override
    public boolean isHandler(Exception e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isHandler");
        }
        if ((e instanceof AuthorizationException) || (e instanceof NotLoggedInException)
                || (e instanceof ViewExpiredException)) {
            return true;
        }
        return false;
    }

    @Override
    public void handle(Exception e) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("handle");
        }

        if ((User.loggedInUser() != null) && (e instanceof AuthorizationException)) {
            restoreHome(StatusMessage
                    .getBundleMessage("net.ihe.gazelle.rights.needed", "net.ihe.gazelle.rights.needed"));
            return;
        }

        boolean forceLogin = false;
        if (GazelleCookie.getCookie(LOGGED_ONCE) != null) {
            forceLogin = true;
        } else if (e instanceof NotLoggedInException) {
            forceLogin = true;
        }

        String lastServletPath;
        if (e instanceof ViewExpiredException) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            lastServletPath = GazelleSecurityCheck.getServletURL(request);
        } else {
            lastServletPath = GazelleSecurityCheck.LAST_SERVLET_PATH.get();
        }
        if (forceLogin && (User.loggedInUser() == null)) {
            String browserId = GazelleCookie.getCookie(SessionTimeoutFilter.COOKIE_BROWSER_ID);
            if (lastServletPath != null) {
                storedLocations.put(browserId, lastServletPath);
            } else {
                storedLocations.put(browserId, null);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Unable to restore page viewed...", e);
            }

            Redirect redirectLogin = Redirect.instance();
            if (ApplicationPreferenceManager.instance().getCASEnabled()) {
                redirectLogin.setViewId("/users/loginCAS/login.xhtml");
            } else {
                redirectLogin.setViewId("/users/login/login.xhtml");
            }
            redirectLogin.execute();
        } else {
            restoreView(lastServletPath);
        }

    }

    @Override
    @Observer(Identity.EVENT_LOGIN_SUCCESSFUL)
    public void restoreViewIfNeeded() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("restoreViewIfNeeded");
        }
        GazelleCookie.setCookie(LOGGED_ONCE, "true");
        String browserId = GazelleCookie.getCookie(SessionTimeoutFilter.COOKIE_BROWSER_ID);
        String servletPath = storedLocations.get(browserId);
        storedLocations.put(browserId, null);
        restoreView(servletPath);
    }

    private void restoreView(String path) {
        if (path != null) {
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
                    .getResponse();
            try {
                response.sendRedirect(path);
                FacesContext.getCurrentInstance().responseComplete();
            } catch (IOException e) {
                LOG.error("Failed to redirect to " + path);
            }
        }
    }

    private void restoreHome(String message) {
        if (message != null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, message);
        }
        Redirect redirectHome = Redirect.instance();
        redirectHome.setViewId("/home.xhtml");
        redirectHome.execute();
    }

}
