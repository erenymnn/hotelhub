package com.example.hotelhub.repository;

import com.example.hotelhub.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setEmail("test@hotelhub.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashedPass");
        entityManager.persistAndFlush(user);

        Optional<User> foundUser = userRepository.findByEmail("test@hotelhub.com");

        assertTrue(foundUser.isPresent());
        assertEquals("test@hotelhub.com", foundUser.get().getEmail());
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@hotelhub.com");

        assertTrue(foundUser.isEmpty());
    }
}
