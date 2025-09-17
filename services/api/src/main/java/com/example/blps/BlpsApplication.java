package com.example.blps;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableProcessApplication
public class BlpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlpsApplication.class, args);
    }

}
