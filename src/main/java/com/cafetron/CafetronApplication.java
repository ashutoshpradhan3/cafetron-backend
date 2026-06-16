package com.cafetron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CafetronApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafetronApplication.class, args);
    }
}
