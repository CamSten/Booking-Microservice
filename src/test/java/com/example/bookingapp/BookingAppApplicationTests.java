package com.example.bookingapp;

import com.example.bookingapp.config.RestTemplateConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class BookingAppApplicationTests {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bookify")
            .withUsername("test")
            .withPassword("test");
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private RestTemplateConfig restTemplateConfig;
    @BeforeEach
    void mockCustomerService() {
        when(restTemplateConfig.restTemplate().getForEntity(
                "http://localhost:8081/api/customers/1",
                Object.class))
                .thenReturn(ResponseEntity.ok().build());

        when(restTemplateConfig.restTemplate().getForEntity(
                "http://localhost:8081/api/customers/-1",
                Object.class))
                .thenReturn(ResponseEntity.notFound().build());
    }
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