package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.*;
import com.example.ebazaarobackend.model.*;
import com.example.ebazaarobackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTests {

    @Mock private ChatRepository chatRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private PostRepository postRepository;
    @Mock private StorageService storageService;

    @InjectMocks
    private ChatService chatService;

    private User buyer;
    private User seller;
    private Post post;
    private Chat chat;

    @BeforeEach
    void setUp() {
        buyer = new User(); buyer.setId(1L);
        seller = new User(); seller.setId(2L);

        post = new Post();
        post.setId(10L);
        post.setUser(seller);

        chat = new Chat();
        chat.setId(100L);
        chat.setBuyer(buyer);
        chat.setSeller(seller);
        chat.setPost(post);
    }

    @Test
    void createChatShouldSaveChatWhenValid() {
        when(chatRepository.existsByPostAndBuyer(post, buyer)).thenReturn(false);
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));

        Chat result = chatService.createChat(buyer, post);

        assertThat(result).isNotNull();
        assertThat(result.getBuyer()).isEqualTo(buyer);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void createChatShouldThrowBadRequestWhenUserTriesToChatWithHimself() {
        post.setUser(buyer);

        assertThatThrownBy(() -> chatService.createChat(buyer, post))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createChatShouldThrowConflictWhenChatAlreadyExists() {
        when(chatRepository.existsByPostAndBuyer(post, buyer)).thenReturn(true);

        assertThatThrownBy(() -> chatService.createChat(buyer, post))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void sendMessageShouldSaveTextMessageWhenValid() {
        MessageRequest request = new MessageRequest();
        request.setIsTextMessage(true);
        request.setMessage("test");

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        chatService.sendMessage(buyer, request, 100L);

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessageShouldSaveFileMessageWhenValid() {
        MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", "data".getBytes());
        MessageRequest request = new MessageRequest();
        request.setIsTextMessage(false);
        request.setFile(file);

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));
        when(storageService.store(file, "messages")).thenReturn("path/to/img.jpg");

        chatService.sendMessage(seller, request, 100L);

        verify(storageService).store(file, "messages");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessageShouldThrowForbiddenWhenUserIsNotInChat() {
        User hacker = new User(); hacker.setId(99L);
        MessageRequest request = new MessageRequest();

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.sendMessage(hacker, request, 100L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sendMessageShouldThrowBadRequestWhenFileIsMissing() {
        MessageRequest request = new MessageRequest();
        request.setIsTextMessage(false);

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.sendMessage(buyer, request, 100L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteMessageShouldSoftDeleteAndRemoveFileWhenItIsFileMessage() {
        Message msg = new Message();
        msg.setUser(buyer);
        msg.setIsTextMessage(false);
        msg.setFilePath("path/to/delete.jpg");

        when(messageRepository.findById(50L)).thenReturn(Optional.of(msg));

        chatService.deleteMessage(buyer, 50L);

        assertThat(msg.getIsDeleted()).isTrue();
        verify(storageService).delete("path/to/delete.jpg");
    }

    @Test
    void deleteMessageShouldThrowForbiddenWhenUserIsNotAuthor() {
        Message msg = new Message();
        msg.setUser(seller);

        when(messageRepository.findById(50L)).thenReturn(Optional.of(msg));

        assertThatThrownBy(() -> chatService.deleteMessage(buyer, 50L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sendMessageWithCreateChatShouldCreateChatAndSendMessage() {
        MessageRequest request = new MessageRequest();
        request.setIsTextMessage(true);
        request.setMessage("test");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(chatRepository.existsByPostAndBuyer(post, buyer)).thenReturn(false);

        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> {
            Chat createdChat = inv.getArgument(0);
            createdChat.setId(777L);
            return createdChat;
        });

        when(chatRepository.findById(777L)).thenReturn(Optional.of(chat));

        chatService.sendMessageWithCreateChat(buyer, request, 10L);

        verify(chatRepository).save(any(Chat.class));
        verify(messageRepository).save(any(Message.class));
    }


    @Test
    void getChatShouldReturnChatResponseAndMarkMessagesAsRead() {
        Message msg = new Message();
        msg.setUser(seller);
        msg.setIsRead(false);
        msg.setIsTextMessage(true);
        msg.setMessage("test");

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));
        when(messageRepository.getAllByChatOrderByCreatedAtAsc(chat)).thenReturn(new ArrayList<>(List.of(msg)));

        ChatResponse response = chatService.getChat(buyer, 100L);

        assertThat(response).isNotNull();
        assertThat(msg.getIsRead()).isTrue();
    }

    @Test
    void getChatThumbnailsByUserShouldReturnList() {
        when(chatRepository.findAllBySellerOrBuyer(buyer, buyer)).thenReturn(List.of(chat));

        List<ChatThumbnail> result = chatService.getChatThumbnailsByUser(buyer);

        assertThat(result).hasSize(1);
    }
}