package com.example.bookingapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Collections;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingAppApplicationTests {
    @Autowired MockMvc mvc;
    private final String bookingJson = """
                {
                "roomid": 1,
                "cost": 1000,
                "startdate": "%s",
                "enddate": "%s",
                "guestcount": 2,
                "extrabed": false
                }
                """.formatted(LocalDate.now().plusDays(10), LocalDate.now().plusDays(11));

    @Test
    void shouldCreateBookingForValidCustomer() throws Exception {
        mvc.perform(post("/bookings").with(authentication(customerAuthentication(1L)))
                .contentType(MediaType.APPLICATION_JSON).content(bookingJson)).andExpect(status().isCreated());
    }

    @Test
    void shouldNotAllowValidCustomerBookingUnavailableRoom() throws Exception {
        mvc.perform(post("/bookings").with(authentication(customerAuthentication(1L)))
                .contentType(MediaType.APPLICATION_JSON).content(bookingJson)).andExpect(status().isCreated());
        mvc.perform(post("/bookings").with(authentication(customerAuthentication(1L)))
                .contentType(MediaType.APPLICATION_JSON).content(bookingJson)).andExpect(status().isConflict());
    }

    @Test
    void shouldNotCreateBookingForUnknownCustomer() throws Exception {
        mvc.perform(post("/bookings").with(authentication(customerAuthentication(-1L)))
                .contentType(MediaType.APPLICATION_JSON).content(bookingJson)).andExpect(status().isForbidden());
    }

    private Authentication customerAuthentication(Long customerId) {
        return new UsernamePasswordAuthenticationToken(customerId, null, Collections.emptyList());
    }

    @Test
    void contextLoads() {
    }
}