package com.igovern.data.config;

import com.igovern.data.entity.User;
import com.igovern.data.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    private static final Logger log = LogManager.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.findByUsername("admin").isEmpty()) {
                users.save(new User("admin", encoder.encode("admin123"), "ROLE_ADMIN"));
                log.info("Seeded default admin user (username=admin, password=admin123)");
            }
            if (users.findByUsername("user").isEmpty()) {
                users.save(new User("user", encoder.encode("user123"), "ROLE_USER"));
                log.info("Seeded default user (username=user, password=user123)");
            }
        };
    }
}
