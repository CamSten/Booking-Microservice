package com.example.bookingapp.model;

import java.time.LocalDate;

public record RoomRequest(Long roomId, LocalDate startDate, LocalDate endDate){ }