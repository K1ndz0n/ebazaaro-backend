package com.example.ebazaarobackend.dto;

import com.example.ebazaarobackend.model.Message;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageResponse {
    private Long id;
    private Boolean isTextMessage;
    private String message = null;
    private String filePath = null;

    private String username;

    private Boolean isDeleted;
    private Boolean isRead;

    private LocalDateTime createdAt;

    public MessageResponse(Message message) {
        this.id = message.getId();
        this.isTextMessage = message.getIsTextMessage();
        this.username = message.getUser().getUsername();

        this.isDeleted = message.getIsDeleted();

        if (!this.isDeleted) {
            if (this.isTextMessage) {
                this.message = message.getMessage();
            } else {
                this.filePath = message.getFilePath();
            }
        }

        this.isRead = message.getIsRead();
        this.createdAt = message.getCreatedAt();
    }
}
