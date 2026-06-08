package com.example.ebazaarobackend.dto;

import com.example.ebazaarobackend.model.Chat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatThumbnail {
    private Long id;
    private Long postId = null;
    private String postName = null;
    private String postPhotoPath = null;

    private String buyerUsername;
    private String sellerUsername;
    private LocalDateTime createdAt;

    public ChatThumbnail(Chat chat) {
        this.id = chat.getId();
        if (chat.getPost() != null) {
            this.postId = chat.getPost().getId();
            this.postName = chat.getPost().getName();

            if (!chat.getPost().getPhotos().isEmpty())
                this.postPhotoPath = chat.getPost().getPhotos().getFirst().getPath();
        }


        this.buyerUsername = chat.getBuyer().getUsername();
        this.sellerUsername = chat.getSeller().getUsername();
        this.createdAt = chat.getCreatedAt();
    }
}
