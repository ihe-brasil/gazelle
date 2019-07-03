package net.ihe.gazelle.users;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.IdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Startup;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Date;
import java.util.List;

import static org.jboss.seam.annotations.Install.APPLICATION;

@Name("org.jboss.seam.security.identity")
@Scope(ScopeType.SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class GazelleSSOIdentity extends Identity {

    public static final String AUTHENTICATED_USER = "org.jboss.seam.security.management.authenticatedUser";
    private static final long serialVersionUID = -631532323964539777L;
    private static final String SILENT_LOGIN = "org.jboss.seam.security.silentLogin";
    private static final String CAS_ASSERTION = "_const_cas_assertion_";

    private static final Logger LOG = LoggerFactory.getLogger(GazelleSSOIdentity.class);

    @Override
    public String login() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("login");
        }

        try {

            if (super.isLoggedIn()) {
                // If authentication has already occurred during this request
                // via a silent login, and login() is explicitly called then we
                // still want to raise the LOGIN_SUCCESSFUL event, and then
                // return.
                if (Contexts.isEventContextActive() && Contexts.getEventContext().isSet(SILENT_LOGIN)) {
                    if (Events.exists()) {
                        Events.instance().raiseEvent(EVENT_LOGIN_SUCCESSFUL);
                    }
                    return "loggedIn";
                }

                if (Events.exists()) {
                    Events.instance().raiseEvent(EVENT_ALREADY_LOGGED_IN);
                }
                return "loggedIn";
            }

            Principal casPrincipal = getCASPrincipal();

            if (casPrincipal == null) {
                super.login();
            } else if (casPrincipal.getName() != null) {
                return forceLogin(casPrincipal);
            }

        } catch (Throwable e) {
            unAuthenticate();
            LOG.error("Failed to login", e);
        }

        return null;
    }

    public String forceLogin(Principal casPrincipal) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("forceLogin");
        }
        String username = casPrincipal.getName();

        preAuthenticate();

        if (LOG.isInfoEnabled()) {
            LOG.info("Found CAS principal for " + username + ": authenticated");
        }
        acceptExternallyAuthenticatedPrincipal(casPrincipal);

        User userAccount = User.FindUserWithUsername(username);

        if (userAccount == null) {
            LOG.warn("userAccount for " + username + " not found");
            userAccount = null;
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("userAccount for " + username + " loaded : " + userAccount);
            }
        }

        if (userAccount != null) {
            loginGazelle(userAccount);
            postAuthenticate();
            return "loggedIn";
        } else {
            LOG.warn("userAccount for " + username + " not found");
            unAuthenticate();
            Object sessionObj = FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            if ((sessionObj != null) && (sessionObj instanceof HttpSession)) {
                HttpSession session = (HttpSession) sessionObj;
                session.setAttribute(CAS_ASSERTION, null);
            }
        }
        return null;
    }

    private void loginGazelle(User userAccount) {

        String username = userAccount.getUsername();

        if (IdentityManager.instance().isEnabled()) {
            // Ok, we're authenticated from CAS, let's load up the roles
            List<String> impliedRoles = IdentityManager.instance().getImpliedRoles(username);
            if (impliedRoles != null) {
                for (String role : impliedRoles) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Adding role \"" + role + "\" to " + username);
                    }
                    addRole(role);
                }
            }
        }

        List<Role> foundRoles = userAccount.getRoles();

        if (foundRoles != null) {
            for (int i = 0; i < foundRoles.size(); i++) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Setting roles = " + foundRoles.get(i).getName());
                }
                addRole(foundRoles.get(i).getName());
            }
        }

        // Fill credentials
        getCredentials().setUsername(username);

        LOG.warn("Login : " + userAccount.getFirstname() + " " + userAccount.getLastname() + " from "
                + userAccount.getInstitution().getName() + " is logged in !    (" + userAccount.getUsername() + ", "
                + userAccount.getEmail() + ", logins = " + userAccount.getLoginsCounter() + ")");
        userAccount.setLastLogin(new Date());
        userAccount.setLoginsCounter(userAccount.getLoginsCounter() + 1);
        userAccount.setFailedLoginAttemptsCounter(0);
        if (userAccount.getActivationCode() != null) {
            userAccount.setActivationCode(null);
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        if (em != null) {
            userAccount = em.merge(userAccount);
        }
        Contexts.getSessionContext().set("selectedUser", userAccount);

        StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.users.user.YouAreNowLoggedInAs",
                userAccount.getUsername());

        if (Events.exists()) {
            if (Contexts.isEventContextActive()) {
                Contexts.getEventContext().set(AUTHENTICATED_USER, userAccount);
            }

            Events.instance().raiseEvent(EVENT_LOGIN_SUCCESSFUL, userAccount);
        }
        if (em != null) {
            em.flush();
        }
    }

    private Principal getCASPrincipal() {

        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return null;
        }
        final HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();
        final HttpSession session = request.getSession(false);
        final Assertion assertion = (Assertion) (session == null ? request
                .getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : session
                .getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION));
        return assertion == null ? null : assertion.getPrincipal();
    }

    @Override
    public boolean isLoggedIn() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isLoggedIn");
        }
        if (!super.isLoggedIn()) {
            if (getCASPrincipal() != null) {
                login();
            }
        }
        return super.isLoggedIn();
    }

    @Override
    public void logout() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("logout");
        }
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (request instanceof ServletRequest) {
            ServletRequest servletRequest = (ServletRequest) request;
            servletRequest.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
        }
        Object session = FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (session instanceof HttpSession) {
            HttpSession httpSession = (HttpSession) session;
            httpSession.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
        }
        super.logout();
    }

}
