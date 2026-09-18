package com.example.bookingapp.service;

import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.exception.RoomNotFoundException;
import com.example.bookingapp.repository.RoomRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElseThrow(() ->
                new RoomNotFoundException("Room with id " + id + " was not found."));
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> findAvailableRooms(LocalDate startDate, LocalDate endDate) {
        return roomRepository.findAvailableRooms(startDate, endDate, Booking.BookingStatus.CANCELLED);
    }
}