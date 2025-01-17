package com.stathis.volunteering;

import com.stathis.volunteering.model.Role;
import com.stathis.volunteering.model.User;
import com.stathis.volunteering.model.UserStatus;
import com.stathis.volunteering.repository.RoleRepository;
import com.stathis.volunteering.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class VolunteeringApplication {

    private static final Logger log = LoggerFactory.getLogger(VolunteeringApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VolunteeringApplication.class, args);
    }

    @Bean
    public CommandLineRunner creation(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    )
    {
        return args -> {
            if (!roleRepository.existsByName("ADMIN")) {
                roleRepository.save(new Role("ADMIN"));
            }
            if (!roleRepository.existsByName("ORGANIZATION")) {
                roleRepository.save(new Role("ORGANIZATION"));
            }
            if (!roleRepository.existsByName("VOLUNTEER")) {
                roleRepository.save(new Role("VOLUNTEER"));
            }

            String adminUsername = "admin";
            if (!userRepository.existsByUsername(adminUsername)) {
                Role adminRole = roleRepository.findAllByName("ADMIN").stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("ADMIN role not found."));
                User adminUser = new User(
                        adminUsername,
                        passwordEncoder.encode("admin123"),
                        "Administrator",
                        "admin@example.com",
                        adminRole
                );

                adminUser.setStatus(UserStatus.APPROVED);
                userRepository.save(adminUser);
                log.info("Default admin user created with username '{}'", adminUsername);
            } else {
                log.info("Default admin user already exists");
            }
        };
    }
}