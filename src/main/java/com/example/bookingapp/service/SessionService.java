package com.example.bookingapp.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    public Long getCustomerId(HttpSession session) {
        return (Long) session.getAttribute("loginCustomerId");
    }
}