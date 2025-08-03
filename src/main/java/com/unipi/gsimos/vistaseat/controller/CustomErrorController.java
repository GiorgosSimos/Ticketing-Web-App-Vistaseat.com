package com.unipi.gsimos.vistaseat.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${server.error.path:/error}")
public class CustomErrorController implements ErrorController {

    @GetMapping
    public String handleError(HttpServletRequest request, ModelMap model) {

        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int status = statusCode != null ? Integer.parseInt(statusCode.toString()) : 500;
        model.addAttribute("statusCode", status);
        model.addAttribute("title", errorTitle(status));
        model.addAttribute("message", errorMessage(status, exception));

        return "customErrorPage";
    }

    private String errorTitle(int status) {
        return switch (status) {

            case 403 -> "Access Forbidden";
            case 404 -> "Page Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unexpected Error";
        };
    }

    private String errorMessage(int status, Object ex) {
        return switch (status) {
            case 404 -> "The page you’re looking for doesn’t exist.";
            case 403 -> "You don’t have permission to view this page.";
            case 500 -> "Internal Server Error";
            default  -> "Our team has been notified.";
        };
    }
}
