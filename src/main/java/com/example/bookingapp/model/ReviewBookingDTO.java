package com.example.bookingapp.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Setter
@Getter
public class ReviewBookingDTO {
    private LocalDate startdate;
    private LocalDate enddate;

    public ReviewBookingDTO(LocalDate startdate, LocalDate enddate){
        this.startdate = startdate;
        this.enddate = enddate;
    }
}