package com.drivesense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DriveSenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriveSenseApplication.class, args);
    }
}
