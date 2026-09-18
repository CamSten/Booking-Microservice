package com.example.bookingapp.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerResponseDTO {

    private CustomerDTO customerDTO;
    private Feedback feedback;
    private String token;

    public CustomerResponseDTO() {
    }

    public CustomerResponseDTO(Feedback feedback) {
        this.feedback = feedback;
    }

    public CustomerResponseDTO(CustomerDTO customer, Feedback feedback) {
        this.customerDTO = customer;
        this.feedback = feedback;
    }

    public CustomerResponseDTO(CustomerDTO customer, Feedback feedback, String token) {
        this.customerDTO = customer;
        this.feedback = feedback;
        this.token = token;
    }
}