package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartStudyOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Smart Study Recommendation API")
            .description("Backend APIs for student profile, performance analysis, recommendations, and timetable generation")
            .version("v1")
            .contact(new Contact().name("Smart Study Backend Team")));
    }
}
