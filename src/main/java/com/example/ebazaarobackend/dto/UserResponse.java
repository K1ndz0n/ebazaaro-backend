package com.example.ebazaarobackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;

    public UserResponse(Long id, String username){
        this.username = username;
        this.id = id;
    }
}
