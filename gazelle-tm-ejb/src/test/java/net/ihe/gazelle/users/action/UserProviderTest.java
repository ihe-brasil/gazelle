package net.ihe.gazelle.users.action;

import net.ihe.gazelle.users.UserProvider;
import org.kohsuke.MetaInfServices;

import java.util.Locale;

@MetaInfServices(UserProvider.class)
public class UserProviderTest implements UserProvider {

    @Override
    public String getUsername() {
        return "junit";
    }

    @Override
    public boolean hasRole(String roleName) {
        return false;
    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
