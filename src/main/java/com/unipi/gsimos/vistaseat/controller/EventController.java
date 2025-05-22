package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.model.EventType;
import org.springframework.ui.Model;
import com.unipi.gsimos.vistaseat.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EventController {

    @GetMapping("/adminDashboard/manageEvents")
    public String manageEvents(@RequestParam(required = false) EventType eventType, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        //This keeps track of the current filter so the right button can be highlighted or pre-selected in the UI.
        model.addAttribute("selectEventType", eventType);

        return "manageEvents";

    }
}
