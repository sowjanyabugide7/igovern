package com.igovern.amq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class AmqServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AmqServiceApplication.class, args);
    }
}
