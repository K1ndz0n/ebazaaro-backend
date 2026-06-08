package com.example.ebazaarobackend.controller;


import com.example.ebazaarobackend.dto.ChatResponse;
import com.example.ebazaarobackend.dto.ChatThumbnail;
import com.example.ebazaarobackend.dto.MessageRequest;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/chats")
@RestController
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> getChat(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(chatService.getChat(user, id));
    }

    @PostMapping("/start/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> startChat(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @Valid @ModelAttribute MessageRequest request
    ) {
        chatService.sendMessageWithCreateChat(user, request, postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/send/{chatId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> sendMessage(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute MessageRequest request,
            @PathVariable Long chatId
    ) {
        chatService.sendMessage(user, request, chatId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        chatService.deleteMessage(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/thumbnails")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatThumbnail>> getThumbnailsByUser(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(chatService.getChatThumbnailsByUser(user));
    }
}