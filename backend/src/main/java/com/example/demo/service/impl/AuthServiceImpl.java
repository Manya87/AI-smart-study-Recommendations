package com.example.demo.service.impl;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.OtpResponse;
import com.example.demo.dto.SendLoginOtpRequest;
import com.example.demo.dto.SendSignupOtpRequest;
import com.example.demo.dto.VerifyLoginOtpRequest;
import com.example.demo.dto.VerifySignupOtpRequest;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.Role;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.AuthService;
import com.example.demo.service.OtpEmailService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpEmailService otpEmailService;

    @Value("${app.otp.expiry-minutes:10}")
    private long otpExpiryMinutes;

    private final Map<String, PendingSignup> signupOtpStore = new ConcurrentHashMap<>();
    private final Map<String, PendingSignin> signinOtpStore = new ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public OtpResponse sendSignupOtp(SendSignupOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhoneNumber().trim();

        appUserRepository.findByEmail(email).ifPresent(existing -> {
            throw new DuplicateResourceException("User already exists with email: " + email);
        });
        appUserRepository.findByPhoneNumber(phone).ifPresent(existing -> {
            throw new DuplicateResourceException("User already exists with phone number: " + phone);
        });

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        signupOtpStore.put(email, new PendingSignup(request.getName().trim(), email, phone, request.getPassword(), otp, expiresAt));

        otpEmailService.sendOtp(email, otp, "Signup Verification");
        log.info("Signup OTP generated for {}", email);

        return OtpResponse.builder().message("OTP sent to your email for signup verification").build();
    }

    @Override
    @Transactional
    public AuthResponse verifySignupOtp(VerifySignupOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        PendingSignup pending = signupOtpStore.get(email);
        validateOtpRequest(pending != null, "No signup OTP request found for this email");
        validateOtpRequest(!pending.expiresAt().isBefore(LocalDateTime.now()), "OTP expired. Request a new OTP");
        validateOtpRequest(pending.otp().equals(request.getOtp()), "Invalid OTP");

        appUserRepository.findByEmail(email).ifPresent(existing -> {
            throw new DuplicateResourceException("User already exists with email: " + email);
        });
        appUserRepository.findByPhoneNumber(pending.phoneNumber()).ifPresent(existing -> {
            throw new DuplicateResourceException("User already exists with phone number: " + pending.phoneNumber());
        });

        AppUser user = new AppUser();
        user.setName(pending.name());
        user.setEmail(email);
        user.setPhoneNumber(pending.phoneNumber());
        user.setPassword(passwordEncoder.encode(pending.password()));
        user.setRole(Role.ROLE_USER);

        AppUser saved = appUserRepository.save(user);
        signupOtpStore.remove(email);

        String token = jwtService.generateToken(saved.getEmail());
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .email(saved.getEmail())
            .role(saved.getRole().name())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OtpResponse sendLoginOtp(SendLoginOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        AppUser user = appUserRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        signinOtpStore.put(email, new PendingSignin(email, user.getRole(), otp, expiresAt));

        otpEmailService.sendOtp(email, otp, "Signin Verification");
        log.info("Signin OTP generated for {}", email);

        return OtpResponse.builder().message("OTP sent to your email for signin verification").build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse verifyLoginOtp(VerifyLoginOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        PendingSignin pending = signinOtpStore.get(email);

        validateOtpRequest(pending != null, "No signin OTP request found for this email");
        validateOtpRequest(!pending.expiresAt().isBefore(LocalDateTime.now()), "OTP expired. Request a new OTP");
        validateOtpRequest(pending.otp().equals(request.getOtp()), "Invalid OTP");

        signinOtpStore.remove(email);
        String token = jwtService.generateToken(email);

        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .email(email)
            .role(pending.role().name())
            .build();
    }

    private String generateOtp() {
        int value = 100000 + new Random().nextInt(900000);
        return String.valueOf(value);
    }

    private void validateOtpRequest(boolean condition, String message) {
        if (!condition) {
            throw new UnauthorizedException(message);
        }
    }

    private record PendingSignup(
        String name,
        String email,
        String phoneNumber,
        String password,
        String otp,
        LocalDateTime expiresAt
    ) { }

    private record PendingSignin(
        String email,
        Role role,
        String otp,
        LocalDateTime expiresAt
    ) { }
}
