package com.example.bookingapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;

@Setter
@Getter
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime submitdate;
    private Long customerid;
    private Long roomid;
    private int guestcount;
    private boolean extrabed;
    private LocalDate startdate;
    private LocalDate enddate;
    private int cost;
    public enum BookingStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED
    }
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    public Booking() {

    }
}