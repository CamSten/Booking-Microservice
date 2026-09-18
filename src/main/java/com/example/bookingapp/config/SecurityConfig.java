package com.example.bookingapp.config;

import com.example.bookingapp.component.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    public SecurityConfig(JwtFilter f) {this.jwtFilter = f;}

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .requestMatchers(
                                "/",
                                "/home",
                                "/error",
                                "/room",
                                "/favicon.ico",
                                "/search",
                                "/book",
                                "/customer",
                                "/customer/review/**",
                                "/profile",
                                "/profile/**",
                                "/login",
                                "/signup",
                                "/logout",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/bookings/availability/**",
                                "/bookings/customer/**",
                                "/bookings/room/**")
                        .permitAll().anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(
                        SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }
}