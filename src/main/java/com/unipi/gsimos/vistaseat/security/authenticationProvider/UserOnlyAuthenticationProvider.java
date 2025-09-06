package com.unipi.gsimos.vistaseat.security.authenticationProvider;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

/**
 * Custom {@link DaoAuthenticationProvider} that restricts certain roles
 * from authenticating through the user login flow.
 * <p>
 * Specifically, if a user possesses any of the roles in {@code blockedRoles}
 * (e.g. {@code ROLE_DOMAIN_ADMIN}), the authentication attempt will fail
 * with a {@link BadCredentialsException}, even if the username and password
 * are valid.
 * <p>
 * This ensures that administrative accounts cannot log in via the regular
 * user authentication endpoint, and must instead use the dedicated admin
 * login flow.
 */
public final class UserOnlyAuthenticationProvider extends DaoAuthenticationProvider {
    private final Set<String> blockedRoles = Set.of("ROLE_DOMAIN_ADMIN");

    @Override
    protected Authentication createSuccessAuthentication(Object principal,
                                                         Authentication authentication,
                                                         UserDetails user) {

        boolean isBlocked = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(blockedRoles::contains);

        if (isBlocked) {
            throw new BadCredentialsException("Admins must sign in via the admin portal.");
        }
        return super.createSuccessAuthentication(principal, authentication, user);
    }

}
