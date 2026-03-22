package com.example.demo.service;

public interface OtpEmailService {
    void sendOtp(String email, String otp, String purpose);
}
