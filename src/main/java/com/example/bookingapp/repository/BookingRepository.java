package com.example.bookingapp.repository;

import com.example.bookingapp.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerid(Long customerid);
    List<Booking> findByStartdate(LocalDate startdate);
    List<Booking> findByRoomid(Long roomid);
    List<Booking> findByRoomidAndStatus(Long roomid, Booking.BookingStatus status);
    List<Booking> findByCustomeridAndStatus(Long customerid, Booking.BookingStatus status);
    Booking findByCustomeridAndRoomidAndStartdateAndEnddateAndStatus(Long customerId, Long roomid, LocalDate startdate, LocalDate enddate, Booking.BookingStatus status);
    boolean existsByCustomeridAndStatus(Long customerid, Booking.BookingStatus status);
}