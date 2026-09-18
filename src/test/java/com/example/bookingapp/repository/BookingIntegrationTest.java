package com.example.bookingapp.repository;

import com.example.bookingapp.model.Booking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class BookingIntegrationTest {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("bookify")
                    .withUsername("test")
                    .withPassword("test");
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldSaveBooking() {
        Booking booking = new Booking();
        booking.setRoomid(1L);
        booking.setCost(1000);
        booking.setStartdate(LocalDate.now().plusDays(1));
        booking.setEnddate(LocalDate.now().plusDays(2));
        booking.setGuestcount(2);
        booking.setExtrabed(false);
        booking.setCustomerid(1L);
        booking.setStatus(Booking.BookingStatus.ACTIVE);
        booking.setSubmitdate(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        assertNotNull(savedBooking.getId());
        Booking foundBooking = bookingRepository.findById(savedBooking.getId()).orElse(null);
        assertNotNull(foundBooking);
        assertEquals(1L, foundBooking.getRoomid());
        assertEquals(1000, foundBooking.getCost());
        assertEquals(1L, foundBooking.getCustomerid());
    }
}