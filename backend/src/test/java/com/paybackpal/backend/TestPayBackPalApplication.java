package com.paybackpal.backend;

import org.springframework.boot.SpringApplication;

public class TestPayBackPalApplication {

    public static void main(String[] args) {
        SpringApplication.from(PayBackPalApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
