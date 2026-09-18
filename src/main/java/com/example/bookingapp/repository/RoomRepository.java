package com.example.bookingapp.repository;

import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
           "(SELECT b.roomid FROM Booking b WHERE b.startdate < :endDate AND b.enddate > :startDate AND b.status != :cancelledStatus)")
    List<Room> findAvailableRooms(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("cancelledStatus") Booking.BookingStatus cancelledStatus);
}