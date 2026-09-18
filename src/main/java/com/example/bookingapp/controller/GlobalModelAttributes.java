package com.example.bookingapp.controller;

import com.example.bookingapp.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {
    private final SessionService sessionService;

    public GlobalModelAttributes(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpSession session) {
        Long userId = sessionService.getCustomerId(session);
        model.addAttribute("loggedIn", userId != null);
        model.addAttribute("loginCustomerId", userId);
    }
}