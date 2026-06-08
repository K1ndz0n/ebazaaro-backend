package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.model.Chat;
import com.example.ebazaarobackend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> getAllByChatOrderByCreatedAtAsc(Chat chat);
}
