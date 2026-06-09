package com.example.ebazaarobackend.repository;


import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.TestcontainersConfig;
import com.example.ebazaarobackend.model.Chat;
import com.example.ebazaarobackend.model.Post;
import com.example.ebazaarobackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, TestDataFactory.class})
public class ChatRepositoryTests {
    @Autowired private ChatRepository chatRepository;
    @Autowired private TestDataFactory testDataFactory;

    private User user1, user2, user3;
    private Post post1, post2, post3;
    private Chat chat1, chat2, chat3;

    @BeforeEach
    void setUp() {
        user1 = testDataFactory.createUser("test1", "test1@example.com");
        user2 = testDataFactory.createUser("test2", "test2@example.com");
        user3 = testDataFactory.createUser("test3", "test3@example.com");

        var cat = testDataFactory.createCategory("test");
        var city = testDataFactory.createCity("city", "test", 0f, 0f);
        post1 = testDataFactory.createPost(user1, "name", cat, city);
        post2 = testDataFactory.createPost(user2, "name2", cat, city);
        post3 = testDataFactory.createPost(user3, "name3", cat, city);

        chat1 = testDataFactory.createChat(post1, user2);
        chat2 = testDataFactory.createChat(post2, user1);
        chat3 = testDataFactory.createChat(post3, user1);
    }

    @Test
    void shouldReturnTrueIfExistsByPostAndBuyer() {
        boolean exists = chatRepository.existsByPostAndBuyer(post1, user2);

        assertThat(exists).isEqualTo(true);
    }

    @Test
    void shouldReturnFalseIfDoesntExistByPostAndBuyer() {
        boolean exists = chatRepository.existsByPostAndBuyer(post1, user3);

        assertThat(exists).isEqualTo(false);
    }

    @Test
    void shouldFindAllBySellerOrBuyer() {
        List<Chat> chats = chatRepository.findAllBySellerOrBuyer(user2, user2);

        assertThat(chats).hasSize(2);
        assertThat(chats.get(0).getBuyer().getId()).isEqualTo(user2.getId());
        assertThat(chats.get(1).getSeller().getId()).isEqualTo(user2.getId());
    }

    @Test
    void shouldFindAllByPost() {
        List<Chat> chats = chatRepository.findAllByPost(post1);

        assertThat(chats).hasSize(1);
        assertThat(chats.getFirst().getId()).isEqualTo(chat1.getId());
    }
}
