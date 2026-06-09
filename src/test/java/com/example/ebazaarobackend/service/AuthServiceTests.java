package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.RegisterRequest;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtEncoder jwtEncoder;
    @Mock private RestClient restClient;

    @InjectMocks
    private AuthService authService;

    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "recaptchaSecretKey", "secret_key");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@email.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        registerRequest.setRecaptchaToken("token123");

        user = new User("testuser", "test@email.com", "encoded_password");
        user.setId(1L);
    }

    private void mockRecaptchaResponse(String responseBody) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), anyString(), anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(responseBody);
    }

    @Test
    void validateRecaptchaShouldReturnTrueWhenGoogleReturnsSuccess() {
        mockRecaptchaResponse("{\"success\": true}");

        boolean result = authService.validateRecaptcha("token");

        assertThat(result).isTrue();
    }

    @Test
    void validateRecaptchaShouldReturnFalseWhenGoogleReturnsFailure() {
        mockRecaptchaResponse("{\"success\": false}");

        boolean result = authService.validateRecaptcha("token");

        assertThat(result).isFalse();
    }

    @Test
    void registerUserShouldSaveUserWhenRequestIsValid() {
        mockRecaptchaResponse("{\"success\": true}");
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");

        authService.registerUser(registerRequest);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUserShouldThrowIllegalArgumentExceptionWhenPasswordsDoNotMatch() {
        registerRequest.setConfirmPassword("different_password");

        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hasła nie są identyczne!");
    }

    @Test
    void registerUserShouldThrowIllegalStateExceptionWhenEmailIsTaken() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email jest zajęty!");
    }

    @Test
    void registerUserShouldThrowIllegalStateExceptionWhenUsernameIsTaken() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nazwa użytkownika jest zajęta!");
    }

    @Test
    void registerUserShouldThrowBadRequestWhenRecaptchaFails() {
        mockRecaptchaResponse("{\"success\": false}");
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void authenticateAndGenerateTokenShouldReturnTokenWhenCredentialsAreValid() {
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("mocked_jwt_token");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        String token = authService.authenticateAndGenerateToken("test@email.com", "password123");

        assertThat(token).isEqualTo("mocked_jwt_token");
    }

    @Test
    void authenticateAndGenerateTokenShouldThrowForbiddenWhenUserDoesNotExist() {
        when(userRepository.findByEmail("wrong@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticateAndGenerateToken("wrong@email.com", "password123"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void authenticateAndGenerateTokenShouldThrowForbiddenWhenPasswordIsIncorrect() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticateAndGenerateToken("test@email.com", "wrong_password"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}