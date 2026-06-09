package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.RegisterRequest;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class AuthService {
    @Value("${recaptcha.secret.key}")
    private String recaptchaSecretKey;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private RestClient restClient;

    public boolean validateRecaptcha(String token) {
        String response = restClient.post()
                .uri("https://www.google.com/recaptcha/api/siteverify?secret={secret}&response={token}",
                        recaptchaSecretKey, token)
                .retrieve()
                .body(String.class);

        return response != null && response.contains("\"success\": true");
    }

    public void registerUser(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new IllegalArgumentException("Hasła nie są identyczne!");

        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new IllegalStateException("Email jest zajęty!");

        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new IllegalStateException("Nazwa użytkownika jest zajęta!");

        if (!validateRecaptcha(request.getRecaptchaToken()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Błąd weryfikacji recaptcha!");

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
    }

    public String authenticateAndGenerateToken(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Zły login lub hasło"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Zły login lub hasło");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
