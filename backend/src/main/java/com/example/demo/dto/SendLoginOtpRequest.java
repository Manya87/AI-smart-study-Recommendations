package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendLoginOtpRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
