package com.unipi.gsimos.vistaseat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home"; // This should correspond to home.html in /templates/
    }

    @GetMapping("/adminDashboard")
    public String adminDashboard() {
        return "adminDashboard";
    }

    @GetMapping("/adminLogin")
    public String adminLogin() {
        return "adminLogin";
    }
}
