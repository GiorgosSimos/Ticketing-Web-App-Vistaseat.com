package com.unipi.gsimos.vistaseat.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import com.unipi.gsimos.vistaseat.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class ContentController {

    @GetMapping("/")
    public String home() {
        return "home"; // This should correspond to home.html in /templates/
    }

    @GetMapping("/adminLogin")
    public String adminLogin(HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            // Redirect to dashboard
            response.sendRedirect("/adminDashboard");
            return null;
        }

        // Otherwise, show the login page
        return "adminLogin";
    }

    @GetMapping("/userSignUp")
    public String userSignup(){
        return "userSignUp";
    }

    @GetMapping("/adminDashboard")
    public String adminDashboard(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        session.setAttribute("firstName", user.getFirstName());
        session.setAttribute("lastName", user.getLastName());

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        return "adminDashboard";
    }


}
