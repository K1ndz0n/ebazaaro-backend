package com.example.ebazaarobackend.controller;

import com.example.ebazaarobackend.dto.LoginRequest;
import com.example.ebazaarobackend.dto.RegisterRequest;
import com.example.ebazaarobackend.dto.UserResponse;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.UserRepository;
import com.example.ebazaarobackend.service.AuthService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/auth")
@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest request) {
        authService.registerUser(request);
        String token = authService.authenticateAndGenerateToken(
                request.getEmail(),
                request.getPassword()
        );

        return Map.of("token", token);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        String token = authService.authenticateAndGenerateToken(
                request.getEmail(),
                request.getPassword()
        );

        return Map.of("token", token);
    }
}
