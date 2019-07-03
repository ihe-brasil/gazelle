package net.ihe.gazelle.users.action;

import net.ihe.gazelle.common.action.CacheRequest;
import net.ihe.gazelle.common.action.CacheUpdater;
import net.ihe.gazelle.users.UserService;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.users.service.GazelleUserProvider;
import org.jboss.seam.Component;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(GazelleUserProvider.class)
public class SeamGazelleUserProvider implements GazelleUserProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SeamGazelleUserProvider.class);

    @Override
    public User getUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUser");
        }
        CacheRequest cacheRequest = (CacheRequest) Component.getInstance("cacheRequest");
        Object result = cacheRequest.getValueUpdater("loggedInUser", new CacheUpdater() {
            @Override
            public Object getValue(String key, Object parameter) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getValue");
                }
                String credentialsUsername = UserService.getUsername();
                if (credentialsUsername == null) {
                    return null;
                } else {
                    return User.FindUserWithUsername(credentialsUsername);
                }
            }
        }, null);

        return (User) result;
    }

}
