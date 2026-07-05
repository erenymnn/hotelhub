package com.example.hotelhub.config;

import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.Role;
import com.example.hotelhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@hotelhub.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@hotelhub.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRoles(Set.of(Role.ADMIN));
            userRepository.save(admin);
        }
    }
}
