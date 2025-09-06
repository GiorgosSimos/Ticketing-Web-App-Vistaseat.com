package com.unipi.gsimos.vistaseat.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

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
