package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.TestcontainersConfig;
import com.example.ebazaarobackend.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, TestDataFactory.class})
class MessageRepositoryTests {

    @Autowired private MessageRepository messageRepository;
    @Autowired private ChatRepository chatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CityRepository cityRepository;

    private Chat testChat;
    private User buyer;
    private User seller;
    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        buyer = testDataFactory.createUser("buyer", "buyer@example.com");
        seller = testDataFactory.createUser("seller", "seller@example.com");

        var cat = testDataFactory.createCategory("test");
        var city = testDataFactory.createCity("city", "test", 0f, 0f);
        Post post = testDataFactory.createPost(seller, "name", cat, city);

        testChat = testDataFactory.createChat(post, buyer);
    }

    @Test
    void shouldReturnMessagesInChronologicalOrder() {
        Message msg1 = new Message();
        msg1.setChat(testChat);
        msg1.setUser(buyer);
        msg1.setMessage("1");
        msg1.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        messageRepository.save(msg1);

        Message msg2 = new Message();
        msg2.setChat(testChat);
        msg2.setUser(seller);
        msg2.setMessage("2");
        msg2.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        messageRepository.save(msg2);

        Message msg3 = new Message();
        msg3.setChat(testChat);
        msg3.setUser(buyer);
        msg3.setMessage("3");
        msg3.setCreatedAt(LocalDateTime.now());
        messageRepository.save(msg3);

        messageRepository.flush();

        List<Message> result = messageRepository.getAllByChatOrderByCreatedAtAsc(testChat);

        assertThat(result).hasSize(3);

        assertThat(result.get(0).getMessage()).isEqualTo("1");
        assertThat(result.get(1).getMessage()).isEqualTo("2");
        assertThat(result.get(2).getMessage()).isEqualTo("3");
    }
}
