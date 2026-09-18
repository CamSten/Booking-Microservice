package com.example.bookingapp;

import com.example.bookingapp.model.*;
import com.example.bookingapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.*;

@Component
public class InitialDataLoader implements CommandLineRunner {
    private final RoomRepository roomRepo;
    private final BookingRepository bookingRepo;

    public InitialDataLoader(RoomRepository roomRepo, BookingRepository bookingRepo) {
        this.roomRepo = roomRepo;
        this.bookingRepo = bookingRepo;
    }

    @Override
    public void run(String... args) {
        if (roomRepo.count() == 0) {
            for (int i = 1; i <= 5; i++) {
                Room singleRoom = new Room();
                singleRoom.setRoomNumber("10" + i);
                singleRoom.setRoomType("Single");
                singleRoom.setExtraBedAvailable(false);
                singleRoom.setCostPerNight(1000);
                roomRepo.save(singleRoom);
            }
            for (int i = 6; i <= 10; i++) {
                Room doubleRoom = new Room();
                doubleRoom.setRoomNumber("10" + i);
                doubleRoom.setRoomType("Double");
                doubleRoom.setExtraBedAvailable(true);
                doubleRoom.setCostPerNight(1500);
                roomRepo.save(doubleRoom);
            }
        } else {
            java.util.List<Room> existingRooms = roomRepo.findAll();
            for (Room room : existingRooms) {
                if (room.getCostPerNight() == 0) {
                    if ("Single".equals(room.getRoomType())) {
                        room.setCostPerNight(1000);
                        roomRepo.save(room);
                    } else if ("Double".equals(room.getRoomType())) {
                        room.setCostPerNight(1500);
                        roomRepo.save(room);
                    }
                }
            }
        }
        if (!bookingRepo.existsByCustomeridAndStatus(1L, Booking.BookingStatus.COMPLETED)) {
            Booking b1 = new Booking();
            b1.setCustomerid(1L);
            b1.setRoomid(1L);
            b1.setGuestcount(1);
            b1.setExtrabed(false);
            b1.setStartdate(LocalDate.of(2026, 6, 1));
            b1.setEnddate(LocalDate.of(2026, 6, 4));
            b1.setCost(3 * 1000);
            b1.setStatus(Booking.BookingStatus.COMPLETED);
            b1.setSubmitdate(LocalDateTime.now());
            bookingRepo.save(b1);

            Booking b2 = new Booking();
            b2.setCustomerid(1L);
            b2.setRoomid(6L);
            b2.setGuestcount(3);
            b2.setExtrabed(true);
            b2.setStartdate(LocalDate.of(2026, 6, 10));
            b2.setEnddate(LocalDate.of(2026, 6, 13));
            b2.setCost(3 * (1500 + 250));
            b2.setStatus(Booking.BookingStatus.COMPLETED);
            b2.setSubmitdate(LocalDateTime.now());
            bookingRepo.save(b2);
        }
    }
}