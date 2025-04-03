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


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        if (exception instanceof BadCredentialsException) {
            response.sendRedirect("/adminLogin?error=bad_credentials");
        } else if (exception instanceof DisabledException) {
            response.sendRedirect("/adminLogin?error=disabled");
        } else {
            response.sendRedirect("/adminLogin?error=unknown");
        }

    }
}
