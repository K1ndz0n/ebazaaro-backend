package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.model.Like;
import com.example.ebazaarobackend.model.Post;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.LikeRepository;
import com.example.ebazaarobackend.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTests {
    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private LikeService likeService;

    private User user1;
    private Post post;
    private Long postId;

    @BeforeEach
    void setUp() {
        postId = 100L;
        user1 = new User();
        user1.setId(1L);

        post = new Post();
        post.setId(postId);
    }

    @Test
    void isLikedShouldReturnTrueWhenLikeExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndPostId(user1.getId(), 1L)).thenReturn(true);

        boolean result = likeService.isLiked(user1, 1L);

        assertThat(result).isTrue();
    }

    @Test
    void isLikedShouldThrowNotFoundWhenPostDoesntExist() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.isLiked(user1, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addLikeShouldSaveLikeWhenSuccess() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndPostId(user1.getId(), 1L)).thenReturn(false);

        likeService.addLike(user1, 1L);

        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void addLikeShouldThrowNotFoundWhenPostDoesntExist() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.addLike(user1, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addLikeShouldThrowConflictWhenAlreadyLiked() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndPostId(user1.getId(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> likeService.addLike(user1, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteLikeShouldDeleteLikeWhenSuccess() {
        Like like = new Like();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.findByUserIdAndPostId(user1.getId(), 1L)).thenReturn(Optional.of(like));

        likeService.deleteLike(user1, 1L);

        verify(likeRepository).delete(like);
    }

    @Test
    void deleteLikeShouldThrowNotFoundWhenPostDoesntExist() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.deleteLike(user1, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteLikeShouldThrowNotFoundWhenLikeDoesntExist() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.findByUserIdAndPostId(user1.getId(), 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.deleteLike(user1, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
