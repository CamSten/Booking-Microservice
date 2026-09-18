package com.example.bookingapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private Long roomid;
    private int cost;
    private LocalDate startdate;
    private LocalDate enddate;
    private int guestcount;
    private boolean extrabed;
}