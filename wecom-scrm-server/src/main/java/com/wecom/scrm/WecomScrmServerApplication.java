package com.wecom.scrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WecomScrmServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WecomScrmServerApplication.class, args);
    }

}
