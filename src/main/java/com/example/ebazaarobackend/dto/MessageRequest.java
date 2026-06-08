package com.example.ebazaarobackend.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class MessageRequest {
    @NotNull
    private Boolean isTextMessage;

    @Nullable
    private String message;

    @Nullable
    private MultipartFile file;
}
