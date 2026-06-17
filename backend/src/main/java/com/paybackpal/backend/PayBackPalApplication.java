package com.paybackpal.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PayBackPalApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayBackPalApplication.class, args);
    }
}
