package com.unipi.gsimos.vistaseat.security.authenticationProvider;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

/**
 * Custom {@link DaoAuthenticationProvider} that restricts an admin
 * from authenticating through the user login flow.
 * <p>
 * Specifically, if a user possesses any of the roles in {@code blockedRoles}
 * (e.g. {@code ROLE_REGISTERED}), the authentication attempt will fail
 * with a {@link BadCredentialsException}, even if the username and password
 * are valid.
 * <p>
 * This ensures that regular user accounts cannot log in via the admin
 * authentication endpoint, and must instead use the main site header.
 */
public final class AdminOnlyAuthenticationProvider extends DaoAuthenticationProvider {
    private final Set<String> blockedRoles = Set.of("ROLE_REGISTERED");

    @Override
    protected Authentication createSuccessAuthentication(Object principal,
                                                         Authentication authentication,
                                                         UserDetails user) {

        boolean isBlocked = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(blockedRoles::contains);

        if (isBlocked) {
            throw new BadCredentialsException("Users must sign in via the the main site header.");
        }
        return super.createSuccessAuthentication(principal, authentication, user);
    }

}
