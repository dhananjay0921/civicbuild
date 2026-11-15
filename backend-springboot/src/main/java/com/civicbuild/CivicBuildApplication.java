package com.civicbuild;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CivicBuildApplication {

    public static void main(String[] args) {
        SpringApplication.run(CivicBuildApplication.class, args);
    }
}
