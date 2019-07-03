package net.ihe.gazelle.menu;

import net.ihe.gazelle.common.pages.Authorization;
import net.ihe.gazelle.users.UserService;

public class AuthorizationRole implements Authorization {

    private String role;

    public AuthorizationRole(String role) {
        super();
        this.role = role;
    }

    @Override
    public boolean isGranted(Object... context) {
        return UserService.hasRole(role);
    }

}
