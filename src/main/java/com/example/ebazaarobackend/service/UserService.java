package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.UserResponse;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserResponse getUserDetails(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return new UserResponse(user.getId(), user.getUsername());
    }
}
