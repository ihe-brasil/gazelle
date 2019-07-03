package net.ihe.gazelle.tm.filter.valueprovider;

import net.ihe.gazelle.hql.criterion.ValueProvider;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstitutionFixer implements ValueProvider {
    public static final InstitutionFixer INSTANCE = new InstitutionFixer();
    private static final Logger LOG = LoggerFactory.getLogger(InstitutionFixer.class);
    private static final long serialVersionUID = 4889171421799551325L;

    @Override
    public Object getValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValue");
        }
        if (Identity.instance().hasRole("admin_role") || Identity.instance().hasRole("monitor_role")
                || Identity.instance().hasRole(Role.ACCOUNTING_ROLE_STRING)
                || Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION.isGranted()) {
            return null;
        }
        User user = User.loggedInUser();
        if ((user != null) && (user.getInstitution() != null)) {
            return user.getInstitution();
        } else {
            return null;
        }
    }
}
