package com.example.bookingapp.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String phone;
    private String password;

    public CustomerDTO() {
    }
}