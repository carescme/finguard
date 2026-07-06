package com.finguard.core.repositories;

import com.finguard.core.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        String testEmail = "test_" + System.currentTimeMillis() + "@finguard.com";
        User user = User.builder()
                .email(testEmail)
                .password("secure_password")
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        
        Optional<User> foundUser = userRepository.findByEmail(testEmail);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(testEmail);
    }
}