package com.rusty.mailingbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MailingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailingBackendApplication.class, args);
    }

}
