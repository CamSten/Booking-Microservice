package com.example.bookingapp.component;

import com.example.bookingapp.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    JwtFilter(JwtService j){
        this.jwt = j;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String token = null;
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            token = h.substring(7);
        }
        else {
            HttpSession session = req.getSession(false);
            if (session != null) {
                token = (String) session.getAttribute("jwtToken");
            }
        }
        if (token != null && jwt.isTokenValid(token)) {
            Long customerId = jwt.extractCustomerId(token);
            var auth = new UsernamePasswordAuthenticationToken(customerId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }
}