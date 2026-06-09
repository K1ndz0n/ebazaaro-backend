package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.UserResponse;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserResponseFromUser() {
        User user = new User("test", "test@example.com", "test");
        user.setId(10L);

        UserResponse result = userService.getUserDetails(user);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("test");
    }

    @Test
    void shouldReturnUserResponseWhenUserExists() {
        User user = new User("test", "test@example.com", "test");
        user.setId(10L);

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserByUsername(user.getUsername());

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("test");
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        String fakeUsername = "fake";

        when(userRepository.findByUsername(fakeUsername)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername(fakeUsername))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
