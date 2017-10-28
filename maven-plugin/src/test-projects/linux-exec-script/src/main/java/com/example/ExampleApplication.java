package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        if(Boolean.TRUE.toString().equals(System.getProperty("thinjar.launcher.active"))){
            System.setProperty("spring.devtools.restart.enabled", "false");
        }
        SpringApplication.run(ExampleApplication.class, args);
    }
}
