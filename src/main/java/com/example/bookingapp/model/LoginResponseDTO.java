package com.example.bookingapp.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponseDTO {

    private CustomerDTO customer;
    private String token;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(CustomerDTO customer, String token) {
        this.customer = customer;
        this.token = token;
    }
}