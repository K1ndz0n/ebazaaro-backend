package com.example.ebazaarobackend.dto;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequest {
    @NotNull
    @Size(min = 1, max = 50, message = "Nazwa musi mieć od 1 do 50 znaków")
    private String name;

    @Nullable
    private String description;

    @Nullable
    private String phoneNumber;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Min(0)
    private float price;

    @NotNull
    @Pattern(regexp = "^(new|used)$", message = "Allowed values are new or used")
    private String condition;

    @NotNull
    private Long categoryId = 1L;

    @NotNull
    private Long cityId;
}
