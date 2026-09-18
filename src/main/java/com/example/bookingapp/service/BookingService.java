
package com.example.bookingapp.service;

import com.example.bookingapp.config.RestTemplateConfig;
import com.example.bookingapp.model.*;
import com.example.bookingapp.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.time.*;
import java.util.*;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final RestTemplate restTemplate;
    @Value("${customer.service.url}")
    private String customerServiceUrl;

    public BookingService(BookingRepository bookingRepo, RestTemplateConfig restTemplateConfig) {
        this.bookingRepo = bookingRepo;
        this.restTemplate = restTemplateConfig.restTemplate();
    }

    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepo.findById(id).orElse(null);
        return booking != null ? toDTO(booking) : null;
    }

    public List<BookingDTO> getBookingsByCustomerId(Long customerId) {
        List<Booking> bookingList = bookingRepo.findByCustomerid(customerId);
        return bookingList != null ? toDTOList(bookingList) : Collections.emptyList();
    }

    public ReviewBookingDTO getReviewBookingByCustomerIdAndTimePeriod(Long customerId, Long roomId, LocalDate startdate, LocalDate enddate) {
        Booking booking = bookingRepo.findByCustomeridAndRoomidAndStartdateAndEnddateAndStatus(customerId, roomId, startdate, enddate, Booking.BookingStatus.COMPLETED);
        return booking != null ? new ReviewBookingDTO(startdate, enddate) : null;
    }

    public boolean hasActiveBooking(Long customerId) {
        return bookingRepo.existsByCustomeridAndStatus(customerId, Booking.BookingStatus.ACTIVE);
    }

    public List<BookingDTO> getBookingsByStartDate(LocalDate startDate) {
        return toDTOList(bookingRepo.findByStartdate(startDate));
    }

    public List<BookingDTO> getBookingsByRoomId(Long roomId) {
        return toDTOList(bookingRepo.findByRoomid(roomId));
    }

    public List<BookingDTO> getAllBookings() {
        return toDTOList(bookingRepo.findAll());
    }

    public BookingDTO toDTO(Booking b) {
        return new BookingDTO(b.getId(), b.getRoomid(), b.getCost(), b.getStartdate(), b.getEnddate(), b.getGuestcount(), b.isExtrabed());
    }

    public List<BookingDTO> toDTOList(List<Booking> bookingList) {
        List<BookingDTO> toReturn = new ArrayList<>();
        for (Booking b : bookingList) {
            toReturn.add(toDTO(b));
        }
        return toReturn;
    }

    public BookingResult createBooking(BookingDTO req, Long customerId) {
        if (!checkDateValidity(req.getStartdate(), req.getEnddate())) {
            return toResult(null, BookingResultStatus.INVALID_DATES);
        }
        if (!checkRoomAvailability(req.getRoomid(), req.getStartdate(), req.getEnddate(), null)) {
            return toResult(null, BookingResultStatus.ROOM_UNAVAILABLE);
        }
        Booking booking = new Booking();
        booking.setRoomid(req.getRoomid());
        booking.setStartdate(req.getStartdate());
        booking.setEnddate(req.getEnddate());
        booking.setGuestcount(req.getGuestcount());
        booking.setExtrabed(req.isExtrabed());
        booking.setCost(req.getCost());
        booking.setCustomerid(customerId);
        booking.setStatus(Booking.BookingStatus.ACTIVE);
        booking.setSubmitdate(LocalDateTime.now());
        return toResult(bookingRepo.save(booking), BookingResultStatus.OK);
    }

    public BookingResult toResult(Booking b, BookingResultStatus status) {
        BookingDTO dto = b != null ? toDTO(b) : null;
        return new BookingResult(dto, status);
    }

    private List<Booking> retrieveActiveBookingsByRoomId(Long roomId) {
        return bookingRepo.findByRoomidAndStatus(roomId, Booking.BookingStatus.ACTIVE);
    }

    public List<BookingDTO> getActiveBookingsByRoomId(Long roomId) {
        return toDTOList(retrieveActiveBookingsByRoomId(roomId));
    }

    public List<BookingDTO> getCompletedBookingsByCustomerId(Long customerId) {
        return toDTOList(bookingRepo.findByCustomeridAndStatus(customerId, Booking.BookingStatus.COMPLETED));
    }

    public List<BookingDTO> getActiveBookingsByCustomerId(long customerId) {
        return toDTOList(bookingRepo.findByCustomeridAndStatus(customerId, Booking.BookingStatus.ACTIVE)
                .stream().filter(b -> !b.getEnddate().isBefore(LocalDate.now())).toList());
    }

    public boolean checkRoomAvailability(Long roomId, LocalDate startDate, LocalDate endDate, Long bookingId) {
        List<Booking> activeBookings = retrieveActiveBookingsByRoomId(roomId);
        for (Booking b : activeBookings) {
            boolean dateTaken = startDate.isBefore(b.getEnddate()) && endDate.isAfter(b.getStartdate());
            if (dateTaken && (bookingId == null || !b.getId().equals(bookingId))) {
                return false;
            }
        }
        return true;
    }

    public BookingResult updateBooking(Long bookingId, BookingDTO booking, Long customerId) {
        Booking existing = bookingRepo.findById(bookingId).orElse(null);
        if (existing == null) {
            return toResult(null, BookingResultStatus.NOT_FOUND);
        }
        if (!existing.getCustomerid().equals(customerId)) {
            return toResult(null, BookingResultStatus.NOT_FOUND);
        }
        if (!checkDateValidity(booking.getStartdate(), booking.getEnddate())) {
            return toResult(null, BookingResultStatus.INVALID_DATES);
        }
        if (checkRoomAvailability(booking.getRoomid(), booking.getStartdate(), booking.getEnddate(), existing.getId())) {
            existing.setRoomid(booking.getRoomid());
            existing.setGuestcount(booking.getGuestcount());
            existing.setStartdate(booking.getStartdate());
            existing.setEnddate(booking.getEnddate());
            existing.setExtrabed(booking.isExtrabed());
            existing.setCost(booking.getCost());
            return toResult(bookingRepo.save(existing), BookingResultStatus.OK);
        }
        return toResult(null, BookingResultStatus.ROOM_UNAVAILABLE);
    }

    public BookingResult cancelBooking(Long id, Long customerId) {
        Booking existingBooking = bookingRepo.findById(id).orElse(null);
        if (existingBooking == null) {
            return toResult(null, BookingResultStatus.NOT_FOUND);
        }
        if (!existingBooking.getCustomerid().equals(customerId)) {
            return toResult(null, BookingResultStatus.NOT_FOUND);
        }
        existingBooking.setStatus(Booking.BookingStatus.CANCELLED);
        return toResult(bookingRepo.save(existingBooking), BookingResultStatus.OK);
    }

    public boolean checkDateValidity(LocalDate startDate, LocalDate endDate) {
        boolean correctOrder = (!startDate.isAfter(endDate) && !startDate.isEqual(endDate));
        boolean notHistoric = (!startDate.isBefore(LocalDate.now()));
        return (correctOrder && notHistoric);
    }

    public List<Room> getAvailableRoomByTimeFrame(List<Room> allRooms, LocalDate startDate, LocalDate endDate) {
        List<Room> availableRooms = new ArrayList<>();
        for (Room r : allRooms) {
            if (checkRoomAvailability(r.getId(), startDate, endDate, null)) {
                availableRooms.add(r);
            }
        }
        return availableRooms;
    }

    public ResponseEntity<Object> isAuthorizedCustomer(Long customerId) {
        try {
            return restTemplate.getForEntity(customerServiceUrl + "/" + customerId, Object.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}