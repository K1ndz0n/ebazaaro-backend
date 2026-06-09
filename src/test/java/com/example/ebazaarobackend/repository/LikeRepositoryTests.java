package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.TestcontainersConfig;
import com.example.ebazaarobackend.model.Like;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, TestDataFactory.class})
public class LikeRepositoryTests {
    @Autowired private LikeRepository likeRepository;
    @Autowired private TestDataFactory testDataFactory;

    private Post post1, post2, post3;
    private User user1, user2;
    private Like like1, like2, like3;

    @BeforeEach
    void setUp() {
        user1 = testDataFactory.createUser("test1", "test1@example.com");
        user2 = testDataFactory.createUser("test2", "test2@example.com");

        var cat = testDataFactory.createCategory("test");
        var city = testDataFactory.createCity("city", "test", 0f, 0f);
        post1 = testDataFactory.createPost(user1, "name", cat, city);
        post2 = testDataFactory.createPost(user1, "name2", cat, city);
        post3 = testDataFactory.createPost(user1, "name3", cat, city);

        like1 = testDataFactory.createLike(user1, post1);
        like2 = testDataFactory.createLike(user1, post2);
        like3 = testDataFactory.createLike(user2, post1);
    }

    @Test
    void shouldReturnTrueIfLikeExistsByUserIdAndPostId() {
        boolean exists = likeRepository.existsByUserIdAndPostId(user1.getId(), post1.getId());

        assertThat(exists).isEqualTo(true);
    }

    @Test
    void shouldReturnFalseIfLikeDoesntExistByUserIdAndPostId() {
        boolean exists = likeRepository.existsByUserIdAndPostId(user1.getId(), post3.getId());

        assertThat(exists).isEqualTo(false);
    }

    @Test
    void shouldFindByUserIdAndPostId() {
        Optional<Like> foundLike = likeRepository.findByUserIdAndPostId(user1.getId(), post1.getId());

        assertThat(foundLike).isPresent();
    }

    @Test
    void shouldFindAllByUser() {
        List<Like> likes = likeRepository.findAllByUser(user1);

        assertThat(likes).hasSize(2);
        assertThat(likes.get(0).getUser().getId()).isEqualTo(user1.getId());
        assertThat(likes.get(1).getUser().getId()).isEqualTo(user1.getId());
    }
}
