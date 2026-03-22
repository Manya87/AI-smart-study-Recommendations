package com.example.demo.config;

import com.example.demo.entity.AppUser;
import com.example.demo.entity.Role;
import com.example.demo.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapConfig {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.phone-number:9999999999}")
    private String adminPhoneNumber;

    @Bean
    public CommandLineRunner bootstrapAdminUser() {
        return args -> {
            String email = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
            String password = adminPassword == null ? "" : adminPassword.trim();

            if (email.isBlank() || password.isBlank()) {
                return;
            }

            if (appUserRepository.findByEmail(email).isPresent()) {
                return;
            }

            AppUser admin = new AppUser();
            admin.setName("System Admin");
            admin.setEmail(email);
            admin.setPhoneNumber(adminPhoneNumber);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(Role.ROLE_ADMIN);
            appUserRepository.save(admin);
            log.info("Bootstrap admin user created for email={}", email);
        };
    }
}
