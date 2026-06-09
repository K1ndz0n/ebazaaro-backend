package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.TestcontainersConfig;
import com.example.ebazaarobackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, TestDataFactory.class})
public class UserRepositoryTests {
    @Autowired private UserRepository userRepository;
    @Autowired private TestDataFactory testDataFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser("test", "test@example.com");
    }

    @Test
    void shouldFindUserByUsername() {
        Optional<User> foundUser = userRepository.findByUsername("test");
        assertThat(foundUser).isPresent();
    }

    @Test
    void shouldFindUserById() {
        Optional<User> foundUser = userRepository.findById(testUser.getId());
        assertThat(foundUser).isPresent();
    }

    @Test
    void shouldFindByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        assertThat(foundUser).isPresent();
    }

    @Test
    void shouldThrowExceptionWhenSavingDuplicateEmail() {
        User duplicateUser = new User("test1", "test@example.com", "test");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldThrowExceptionWhenSavingDuplicateUsername() {
        User duplicateUser = new User("test", "test1@example.com", "test");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
