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
        String originalPath = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString();

        int status = statusCode != null ? Integer.parseInt(statusCode.toString()) : 500;

        model.addAttribute("statusCode", status);
        model.addAttribute("title", errorTitle(status));
        model.addAttribute("message", errorMessage(status, exception));

        if (originalPath != null && originalPath.startsWith("/admin")) {
            return "customAdminErrorPage";
        }

        return "customUserErrorPage";
    }

    private String errorTitle(int status) {
        return switch (status) {

            // 4xx - Client Errors
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Access Forbidden";
            case 404 -> "Page Not Found";

            // 5xx - Server Errors
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "Unexpected Error";
        };
    }

    private String errorMessage(int status, Object ex) {
        return switch (status) {

            case 400 -> "Malformed request";
            case 401 -> "Not authenticated (missing or invalid token)";
            case 403 -> "You don’t have permission to view this page.";
            case 404 -> "The page you’re looking for doesn’t exist.";
            case 500 -> "Internal Server Error";
            case 502 -> "Upstream (dependency/proxy) returned invalid response";
            case 503 -> "Service overloaded or down for maintenance";
            default  -> "Our team has been notified.";
        };
    }
}
