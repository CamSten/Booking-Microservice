package com.example.bookingapp.controller;

import com.example.bookingapp.model.*;
import com.example.bookingapp.service.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final RoomService roomService;

    public BookingController (BookingService bookingService, RoomService roomService){
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @GetMapping("/get-all-bookings")
    public List<BookingDTO> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingById(id);
        return (booking == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(booking);
    }

    @GetMapping("/customer/{customerId}/has-active-booking")
    public boolean hasActiveBooking(@PathVariable Long customerId) {
        return bookingService.hasActiveBooking(customerId);
    }

    @GetMapping("/customer/{customerid}")
    public ResponseEntity<?> getBookingsByCustomerId(@PathVariable Long customerid, Authentication auth) {
        Long loggedInCustomerId = (Long) auth.getPrincipal();
        if (loggedInCustomerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!loggedInCustomerId.equals(customerid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(bookingService.getBookingsByCustomerId(customerid));
    }

    @GetMapping("/customer/review")
    public ResponseEntity<ReviewBookingDTO> getReviewBooking(@RequestParam Long customerId, @RequestParam Long roomId,
                                                             @RequestParam LocalDate startDate, @RequestParam LocalDate endDate){
        ReviewBookingDTO reviewBooking = bookingService.getReviewBookingByCustomerIdAndTimePeriod(customerId, roomId, startDate, endDate);
        return (reviewBooking != null) ? ResponseEntity.ok(reviewBooking) : ResponseEntity.notFound().build();
    }

    @GetMapping("/customer/active/{customerid}")
    public ResponseEntity<?> getUpcomingBookingsByCustomerId(@PathVariable Long customerid, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long loggedInCustomerId = (Long) auth.getPrincipal();
        if (loggedInCustomerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!loggedInCustomerId.equals(customerid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(bookingService.getActiveBookingsByCustomerId(customerid));
    }

    @GetMapping("/customer/completed/{customerid}")
    public ResponseEntity<?> getCompletedBookingsForCustomer(@PathVariable Long customerid, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long loggedInCustomerId = (Long) auth.getPrincipal();
        if (loggedInCustomerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!loggedInCustomerId.equals(customerid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(bookingService.getCompletedBookingsByCustomerId(customerid));
    }

    @GetMapping("/room/{roomid}")
    public List<BookingDTO> getBookingsByRoomId(@PathVariable Long roomid) {
        return bookingService.getBookingsByRoomId(roomid);
    }

    @GetMapping("/startdate/{date}")
    public List<BookingDTO> getBookingsByStartDate(@PathVariable LocalDate date) {
        return bookingService.getBookingsByStartDate(date);
    }

    @GetMapping("/availability/{roomid}/{startdate}/{enddate}")
    public boolean checkRoomAvailability(@PathVariable Long roomid, @PathVariable LocalDate startdate, @PathVariable LocalDate enddate) {
        return bookingService.checkRoomAvailability(roomid, startdate, enddate, null);
    }

    @GetMapping("/availability/{startdate}/{enddate}")
    public List<Room> getAvailableRoomsByTimeframe(@PathVariable LocalDate startdate, @PathVariable LocalDate enddate){
        List<Room> allRooms = roomService.getAllRooms();
        return bookingService.getAvailableRoomByTimeFrame(allRooms, startdate, enddate);
    }

    @GetMapping("/room/{roomid}/blocked-dates")
    public List<BookingDTO> getBlockedDates(@PathVariable Long roomid) {
        return bookingService.getActiveBookingsByRoomId(roomid);
    }

    @PostMapping("")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO booking, Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        ResponseEntity<Object> customer = bookingService.isAuthorizedCustomer(customerId);
        if (!customer.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        BookingResult response = bookingService.createBooking(booking, customerId);
        HttpStatus status = getStatus(response.status());
        if (response.dto() == null) {
            return ResponseEntity.status(status).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response.dto());
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> updateBooking(@PathVariable Long bookingId, @RequestBody BookingDTO booking,
                                                    Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        BookingResult response = bookingService.updateBooking(bookingId, booking, customerId);
        HttpStatus status = getStatus(response.status());
        if (status != HttpStatus.OK) {
            return ResponseEntity.status(status).build();
        }
        return ResponseEntity.ok(response.dto());
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long bookingId, Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        BookingResult response = bookingService.cancelBooking(bookingId, customerId);
        HttpStatus status = getStatus(response.status());

        if (status != HttpStatus.OK) {
            return ResponseEntity.status(status).build();
        }
        return ResponseEntity.ok(response.dto());
    }

    private HttpStatus getStatus(BookingResultStatus response){
        return switch (response) {
            case OK -> HttpStatus.OK;
            case INVALID_DATES -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ROOM_UNAVAILABLE -> HttpStatus.CONFLICT;
        };
    }
}