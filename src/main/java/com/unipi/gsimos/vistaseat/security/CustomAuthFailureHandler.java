package com.unipi.gsimos.vistaseat.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    private static final String ADMIN_LOGIN_PAGE = "/adminLogin";
    private static final String USER_LOGIN_PAGE = "/userLogin";


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String baseLoginPage = determineLoginPage(request);

        String errorType;

        if (exception instanceof BadCredentialsException) {
            errorType = "bad_credentials";
        } else if (exception instanceof DisabledException) {
            errorType = "disabled";
        } else {
            errorType = "unknown";
        }

        response.sendRedirect(baseLoginPage + "?error=" + errorType);

    }

    /**
     * Resolves which login page to redirect the user to after an authentication failure.
     * <p>
     * The decision is based on request attributes such as the servlet path or, optionally,
     * custom parameters provided in the login form. By default, if the request corresponds
     * to an admin login attempt, the admin login page path is returned; otherwise, the
     * standard user login page path is used.
     * <p>
     * This method can be extended to handle multiple conditions, for example:
     * <ul>
     *   <li>Requests whose servlet path starts with {@code /admin} → admin login page</li>
     *   <li>Requests with a hidden form parameter (e.g., {@code area=admin}) → admin login page</li>
     *   <li>All other requests → user login page</li>
     * </ul>
     *
     * @param request the current HTTP request
     * @return the resolved login page path (admin or user)
     */
    private String determineLoginPage(HttpServletRequest request) {
        String servletPath = request.getServletPath();            // path without context

        if (servletPath != null && (servletPath.equals("/adminLogin"))) {
            return ADMIN_LOGIN_PAGE;
        }

        return USER_LOGIN_PAGE;
    }
}
