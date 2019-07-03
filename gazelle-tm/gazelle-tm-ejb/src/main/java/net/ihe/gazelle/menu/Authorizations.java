package net.ihe.gazelle.menu;

import net.ihe.gazelle.common.pages.Authorization;
import net.ihe.gazelle.common.pages.AuthorizationAnd;
import net.ihe.gazelle.common.pages.AuthorizationOr;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.security.Identity;

import java.util.List;

public enum Authorizations implements Authorization {

    ALL,

    NONE,

    // application mode
    // see also ONLY_GMM
    GMM,
    // see also NOT_ONLY_GMM
    NOT_GMM,

    TM,

    NOT_TM,

    PR,

    NOT_PR,

    // login status

    LOGGED,

    NOT_LOGGED,

    // TM specific

    SESSION_WITH_CONTRACT,

    SESSION_WITH_PRECAT,

    SESSION_WITH_PARTICIPANTS,

    SESSION_WITH_CERTIFICATE_URL,

    DISPLAY_TESTS_AS_NOT_LOGGED_IN,

    // roles

    ADMIN,

    PROJECT_MANAGER,

    MONITOR,

    VENDOR,

    VENDOR_ADMIN,

    ACCOUNTING,

    EDITOR,

    TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION,

    TESTING_SESSION_ADMIN,
    // Drools

    REGISTRATION_EDITOR,

    OBJECT_VIEWER,

    REGISTRATION_ADMIN,

    SESSION_WITH_INTEROPERABILITY,

    SESSION_WITH_PATIENTS;

    public static final Authorization ONLY_GMM = new AuthorizationAnd(GMM, NOT_TM, NOT_PR);

    public static final Authorization NOT_ONLY_GMM = new AuthorizationOr(TM, PR);

    public static final Authorization DISPLAY_TESTS = new AuthorizationOr(LOGGED, DISPLAY_TESTS_AS_NOT_LOGGED_IN);

    public static final Authorization ADMIN_OR_MONITOR = new AuthorizationOr(ADMIN, MONITOR);

    public static final Authorization ADMIN_OR_VENDOR_ADMIN = new AuthorizationOr(ADMIN, VENDOR_ADMIN);

    public static final Authorization PROJECT_MANAGER_OR_VENDOR_ADMIN = new AuthorizationOr(PROJECT_MANAGER,
            VENDOR_ADMIN);

    public static final Authorization ADMIN_OR_VENDOR = new AuthorizationOr(ADMIN, VENDOR, VENDOR_ADMIN);

    public static final Authorization ADMIN_OR_VENDOR_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION = new AuthorizationOr(
            ADMIN_OR_VENDOR, TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION);

    @Deprecated
    public static final Authorization ADMIN_OR_TESTING_SESSION_ADMIN = new AuthorizationOr(ADMIN_OR_VENDOR,
            TESTING_SESSION_ADMIN);

    @Deprecated
    public static final Authorization MONITOR_OR_VENDOR = new AuthorizationOr(MONITOR, VENDOR);

    public static final Authorization ADMIN_OR_ACCOUNTING = new AuthorizationOr(ADMIN, ACCOUNTING);

    public static final Authorization ACCOUNTING_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION = new AuthorizationOr(ACCOUNTING,
            TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION);

    @Deprecated
    public static final Authorization ACCOUNTING_OR_TESTING_SESSION_ADMIN = new AuthorizationOr(ACCOUNTING,
            TESTING_SESSION_ADMIN);

    @Deprecated
    public static final Authorization VENDOR_ADMIN_OR_TESTING_SESSION_ADMIN = new AuthorizationOr(VENDOR_ADMIN,
            TESTING_SESSION_ADMIN);

    public static final Authorization TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION_OR_ACCOUNTING = new AuthorizationOr(
            TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, ACCOUNTING);

    public static final Authorization ADMIN_OR_MONITOR_OR_VENDOR = new AuthorizationOr(ADMIN_OR_MONITOR, VENDOR);

    public static final Authorization ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION = new AuthorizationOr(
            ADMIN, TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION);
    public static final Authorization VENDOR_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION = new AuthorizationOr(
            VENDOR, TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION);
    public static final Authorization ADMIN_OR_VENDOR_ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION = new AuthorizationOr(
            ADMIN_OR_VENDOR_ADMIN, TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION);

    @Override
    public boolean isGranted(Object... context) {
        TestingSession selectedTestingSession;
        boolean result = false;
        switch (this) {
            case ALL:
                result = true;
                break;
            case NONE:
                result = false;
                break;
            case GMM:
                result = ApplicationManager.instance().isMasterModel();
                break;
            case NOT_GMM:
                result = !ApplicationManager.instance().isMasterModel();
                break;
            case TM:
                result = ApplicationManager.instance().isTestManagement();
                break;
            case DISPLAY_TESTS_AS_NOT_LOGGED_IN:
                result = ApplicationManager.instance().isTestsDisplayAsNotLoggedIn();
                break;
            case SESSION_WITH_CONTRACT:
                selectedTestingSession = TestingSession.getSelectedTestingSession();
                if (selectedTestingSession != null) {
                    result = selectedTestingSession.isContractRequired();
                }
                break;
            case SESSION_WITH_PRECAT:
                selectedTestingSession = TestingSession.getSelectedTestingSession();
                if (selectedTestingSession != null) {
                    result = selectedTestingSession.isEnablePrecatTests();
                }
                break;
            case SESSION_WITH_INTEROPERABILITY:
                selectedTestingSession = TestingSession.getSelectedTestingSession();
                if (selectedTestingSession != null) {
                    result = selectedTestingSession.isTestingInteroperability();
                }
                break;
            case SESSION_WITH_PARTICIPANTS:
                selectedTestingSession = TestingSession.getSelectedTestingSession();
                if (selectedTestingSession != null) {
                    result = selectedTestingSession.getAllowParticipantRegistration();
                }
                if (Role.isLoggedUserAdmin()) {
                    result = true;
                }
                break;
            case SESSION_WITH_CERTIFICATE_URL:
                selectedTestingSession = TestingSession.getSelectedTestingSession();
                if (selectedTestingSession != null) {
                    result = selectedTestingSession.isDisplayCertificatesMenu();
                }
                break;
            case SESSION_WITH_PATIENTS:
                selectedTestingSession = TestingSession.getSelectedTestingSession();
                if (selectedTestingSession != null) {
                    result = !selectedTestingSession.getDisablePatientGenerationAndSharing();
                }
                break;
            case NOT_TM:
                result = !ApplicationManager.instance().isTestManagement();
                break;
            case PR:
                result = ApplicationManager.instance().isProductRegistry();
                break;
            case NOT_PR:
                result = !ApplicationManager.instance().isProductRegistry();
                break;
            case LOGGED:
                result = Identity.instance().isLoggedIn();
                break;
            case NOT_LOGGED:
                result = !Identity.instance().isLoggedIn();
                break;
            case ADMIN:
                result = Role.isLoggedUserAdmin();
                break;
            case PROJECT_MANAGER:
                // FIXME check testing session?
                result = Role.isLoggedUserProjectManager() || Role.isLoggedUserAdmin();
                break;
            case MONITOR:
                // FIXME check testing session?
                result = Role.isLoggedUserMonitor();
                break;
            case VENDOR:
                result = has_vendor_role();
                break;
            case VENDOR_ADMIN:
                result = Role.isLoggedUserVendorAdmin();
                break;
            case ACCOUNTING:
                result = has_accounting_role();
                break;
            case EDITOR:
                if (ApplicationManager.instance().isMasterModel() && has_editor_role()) {
                    result = true;
                }
                break;
            case REGISTRATION_ADMIN:
                result = has_accounting_role() || Role.isLoggedUserProjectManager() || Role.isLoggedUserVendorAdmin();
                break;
            case REGISTRATION_EDITOR:
                result = has_accounting_role() || Role.isLoggedUserMonitor() || Role.isLoggedUserProjectManager()
                        || has_vendor_role();
                break;
            case OBJECT_VIEWER:
                result = has_editor_role() || Role.isLoggedUserMonitor();
                break;

            case TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION:
                result = Role.isLoggedUserTestingSessionAdmin() && isUserAdminOfCurrentTestingSession();
                break;
            case TESTING_SESSION_ADMIN:
                result = Role.isLoggedUserTestingSessionAdmin();
                break;

            default:
                break;
        }
        return result;
    }

    private boolean isUserAdminOfCurrentTestingSession() {
        boolean isCurrentTestingSessionAdmin = false;
        User currentUser = User.loggedInUser();
        if (currentUser != null) {
            TestingSession selectedTestingSession = TestingSession.getSelectedTestingSession();
            if (selectedTestingSession != null) {
                List<User> testingSessionAdmin = selectedTestingSession.getTestingSessionAdmin();

                isCurrentTestingSessionAdmin = testingSessionAdmin.contains(currentUser);
            }
        }

        return isCurrentTestingSessionAdmin;
    }

    private boolean has_vendor_role() {
        return Role.isLoggedUserVendorUser() || Role.isLoggedUserVendorAdmin();
    }

    private boolean has_accounting_role() {
        return Role.isLoggedUserAccounting() || Role.isLoggedUserAdmin();
    }

    private boolean has_editor_role() {
        return Role.isLoggedUserAdmin() || Role.isLoggedUserTestEditor();
    }
}
