package com.example.bookingapp.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerResponseDTO {

    private CustomerDTO customerDTO;
    private Feedback feedback;
    private String token;
    private String errorDetails;

    public CustomerResponseDTO() {
    }

    public CustomerResponseDTO(Feedback feedback) {
        this.feedback = feedback;
    }

    public CustomerResponseDTO(Feedback feedback, String errorDetails) {
        this.feedback = feedback;
        this.errorDetails = errorDetails;
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