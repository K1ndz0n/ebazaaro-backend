package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.ChatResponse;
import com.example.ebazaarobackend.dto.ChatThumbnail;
import com.example.ebazaarobackend.dto.MessageRequest;
import com.example.ebazaarobackend.dto.MessageResponse;
import com.example.ebazaarobackend.model.Chat;
import com.example.ebazaarobackend.model.Message;
import com.example.ebazaarobackend.model.Post;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.ChatRepository;
import com.example.ebazaarobackend.repository.MessageRepository;
import com.example.ebazaarobackend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private StorageService storageService;

    public Chat createChat(User buyer, Post post) {
        if (post.getUser().getId().equals(buyer.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie możesz stworzyć czatu sam ze sobą!");

        if (chatRepository.existsByPostAndBuyer(post, buyer))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Taki czat już istnieje");

        var chat = new Chat();
        chat.setBuyer(buyer);
        chat.setSeller(post.getUser());
        chat.setPost(post);

        return chatRepository.save(chat);
    }

    @Transactional
    public void sendMessage(User user, MessageRequest request, Long chatId) {
        var chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!user.getId().equals(chat.getBuyer().getId()) && !user.getId().equals(chat.getSeller().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        var message = new Message();
        message.setIsTextMessage(request.getIsTextMessage());

        if (request.getIsTextMessage()) {
            message.setMessage(request.getMessage());
        } else {
            if (request.getFile() == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Błąd przesyłania pliku!");

            String path = storageService.store(request.getFile(), "messages");
            message.setFilePath(path);
        }

        message.setUser(user);
        message.setChat(chat);
        messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(User user, Long messageId) {
        var message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!message.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        if (!message.getIsTextMessage() && message.getFilePath() != null) {
            storageService.delete(message.getFilePath());
        }

        message.setIsDeleted(true);
    }

    @Transactional
    public void sendMessageWithCreateChat(User user, MessageRequest request, Long postId) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var chat = createChat(user, post);
        sendMessage(user, request, chat.getId());
    }

    @Transactional(readOnly = true)
    public ChatResponse getChat(User user, Long chatId) {
        var chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!user.getId().equals(chat.getBuyer().getId()) && !user.getId().equals(chat.getSeller().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        var messages =  messageRepository.getAllByChatOrderByCreatedAtAsc(chat);
        messages.forEach(m -> m.setIsRead(!m.getUser().getId().equals(user.getId())));

        var messageResponse = messages.stream().map(MessageResponse::new).toList();

        return new ChatResponse(chat, messageResponse);
    }

    public List<ChatThumbnail> getChatThumbnailsByUser(User user) {
        return chatRepository.findAllBySellerOrBuyer(user, user)
                .stream().map(ChatThumbnail::new).toList();
    }
}
