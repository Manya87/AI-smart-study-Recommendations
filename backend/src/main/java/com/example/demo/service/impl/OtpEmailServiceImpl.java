package com.example.demo.service.impl;

import com.example.demo.service.OtpEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpEmailServiceImpl implements OtpEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:no-reply@smartstudy.local}")
    private String from;

    @Override
    public void sendOtp(String email, String otp, String purpose) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. OTP fallback for {}: purpose={}, otp={}", email, purpose, otp);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("Smart Study OTP - " + purpose);
            message.setText("Your OTP is: " + otp + "\nThis code expires in 10 minutes.");
            mailSender.send(message);
            log.info("OTP email sent to {} for {}", email, purpose);
        } catch (Exception ex) {
            log.warn("Could not send OTP email to {}. Falling back to log output. OTP={}, purpose={}", email, otp, purpose);
        }
    }
}
