package com.example.bookingapp.service;

import com.example.bookingapp.config.RestTemplateConfig;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RestTemplateConfig restTemplateConfig;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private BookingService bookingService;
    private Booking b1;

    @BeforeEach
    public void setup() {
        b1 = new Booking();
        b1.setId(40L);
        b1.setRoomid(10L);
        b1.setStartdate(LocalDate.now().plusDays(2));
        b1.setEnddate(LocalDate.now().plusDays(5));
        b1.setStatus(Booking.BookingStatus.ACTIVE);
    }

    @Test
    public void checkDateValidityReturnsFalseForWrongDateOrder() {
        LocalDate startdate = LocalDate.now().minusDays(1);
        LocalDate enddate = LocalDate.now().minusDays(3);
        boolean result = bookingService.checkDateValidity(startdate, enddate);
        assertFalse(result);
    }
    @Test
    public void checkDateValidityReturnsFalseForPastDates() {
        LocalDate startdate = LocalDate.now().minusDays(3);
        LocalDate enddate = LocalDate.now().minusDays(1);
        boolean result = bookingService.checkDateValidity(startdate, enddate);
        assertFalse(result);
    }
    @Test
    public void checkDateValidityReturnsFalseForSameDate() {
        LocalDate startdate = LocalDate.now();
        LocalDate enddate = LocalDate.now();
        boolean result = bookingService.checkDateValidity(startdate, enddate);
        assertFalse(result);
    }

    @Test
    public void checkAvailabilityReturnsFalseForCompleteDateOverlap() {
        Long roomId = b1.getRoomid();
        Booking b2 = new Booking();
        b2.setId(41L);
        b2.setRoomid(roomId);
        b2.setStartdate(LocalDate.now().plusDays(2));
        b2.setEnddate(LocalDate.now().plusDays(5));
        b2.setStatus(Booking.BookingStatus.ACTIVE);
        when(bookingRepository.findByRoomidAndStatus(
                roomId,
                Booking.BookingStatus.ACTIVE))
                .thenReturn(List.of(b1));
        boolean result = bookingService.checkRoomAvailability(roomId, b2.getStartdate(), b2.getEnddate(), b2.getId());
        assertFalse(result);
    }

    @Test
    public void checkAvailabilityReturnsFalseForPartialDateOverlap() {
        Long roomId = b1.getRoomid();
        Booking b3 = new Booking();
        b3.setId(42L);
        b3.setRoomid(roomId);
        b3.setStartdate(LocalDate.now().plusDays(3));
        b3.setEnddate(LocalDate.now().plusDays(6));
        b3.setStatus(Booking.BookingStatus.ACTIVE);
        when(bookingRepository.findByRoomidAndStatus(
                roomId,
                Booking.BookingStatus.ACTIVE))
                .thenReturn(List.of(b1));
        boolean result = bookingService.checkRoomAvailability(roomId, b3.getStartdate(), b3.getEnddate(), b3.getId());
        assertFalse(result);
    }

    @Test
    public void checkAvailabilityIgnoresOverlapForSameBookingId (){
        Long roomId = b1.getRoomid();
        LocalDate newStartDate = b1.getStartdate().plusDays(1);
        LocalDate newEndDate = b1.getEnddate().plusDays(1);
        when(bookingRepository.findByRoomidAndStatus(
                roomId,
                Booking.BookingStatus.ACTIVE))
                .thenReturn(List.of(b1));
        boolean result = bookingService.checkRoomAvailability(roomId, newStartDate, newEndDate, b1.getId());
        assertTrue(result);
    }

    @Test
    public void updateBookingFailsWhenNewDatesOverlapOtherActiveBooking() {
        Long roomId = b1.getRoomid();
        LocalDate newStartDate = b1.getStartdate().plusDays(2);
        LocalDate newEndDate = b1.getEnddate().plusDays(2);
        Booking b4 = new Booking();
        b4.setId(42L);
        b4.setRoomid(roomId);
        b4.setStartdate(LocalDate.now().plusDays(3));
        b4.setEnddate(LocalDate.now().plusDays(6));
        b4.setStatus(Booking.BookingStatus.ACTIVE);
        when(bookingRepository.findByRoomidAndStatus(
                roomId,
                Booking.BookingStatus.ACTIVE))
                .thenReturn(List.of(b1, b4));
        boolean result = bookingService.checkRoomAvailability(roomId, newStartDate, newEndDate, b1.getId());
        assertFalse(result);
    }

    //TODO
    /*
    @Test
    public void cancelBookingSetsStatusToCancelled(){
        Long id = 40L;
        when(bookingRepository.findById(id)).thenReturn(Optional.of(b1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(b1);
        BookingResult result = bookingService.cancelBooking(id);
        Booking cancelled = bookingRepository.findById(id).orElse(null);
        verify(bookingRepository).save(any(Booking.class));
        assertNotNull(result);
        assertEquals(Booking.BookingStatus.CANCELLED, cancelled.getStatus());
    }
     */
}