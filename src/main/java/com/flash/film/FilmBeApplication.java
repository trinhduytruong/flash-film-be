package com.flash.film;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class FilmBeApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilmBeApplication.class, args);
    }
}
