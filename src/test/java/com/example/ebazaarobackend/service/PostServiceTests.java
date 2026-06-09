package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.dto.PostFilter;
import com.example.ebazaarobackend.dto.PostRequest;
import com.example.ebazaarobackend.dto.PostResponse;
import com.example.ebazaarobackend.dto.ThumbnailFilteredResponse;
import com.example.ebazaarobackend.model.*;
import com.example.ebazaarobackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestDataFactory.class)
public class PostServiceTests {
    @Mock private PostRepository postRepository;
    @Mock private CityRepository cityRepository;
    @Mock private CategoryRepository categoryRepository;

    @Mock private UserRepository userRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private StorageService storageService;
    @Mock private ChatRepository chatRepository;

    @InjectMocks
    private PostService postService;

    private TestDataFactory testDataFactory;

    private User user1, user2;
    private Category category;
    private City city;

    @BeforeEach
    void setUp() {
        testDataFactory = new TestDataFactory();

        user1 = testDataFactory.createUserObject("test1", "test1@example.com");
        user1.setId(1L);

        user2 = testDataFactory.createUserObject("test2", "test2@example.com");
        user2.setId(2L);

        city = testDataFactory.createCityObject("test", "test", 0f, 0f);
        city.setId(1L);

        category = testDataFactory.createCategoryObject("test");
        category.setId(1L);
    }

    @Test
    void shouldReturnPostResponseWhenPostExists() {
        Post post = testDataFactory.createPostObject(user1, "test", category, city);
        post.setId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostResponse response = postService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowNotFoundWhenPostDoesntExist() {
        Long fakeId = 30L;

        when(postRepository.findById(fakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getById(fakeId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnPostResponseOnCreatePost() {
        PostRequest request = new PostRequest();
        request.setName("test");
        request.setEmail("test@example.com");
        request.setCondition("new");
        request.setPrice(0f);
        request.setCategoryId(1L);
        request.setCityId(1L);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Post post = testDataFactory.createPostObject(user1, "test", category, city);
        post.setId(1L);

        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = postService.createPost(request, user1);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowBadRequestOnCreatePostWhenCityNotFound() {
        Long fakeCityId = 10L;

        PostRequest request = new PostRequest();
        request.setName("test");
        request.setEmail("test@example.com");
        request.setCondition("new");
        request.setPrice(0f);
        request.setCategoryId(1L);
        request.setCityId(fakeCityId);

        when(cityRepository.findById(fakeCityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(request, user1))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldThrowBadRequestOnCreatePostWhenCategoryNotFound() {
        Long fakeCategoryId = 10L;

        PostRequest request = new PostRequest();
        request.setName("test");
        request.setEmail("test@example.com");
        request.setCondition("new");
        request.setPrice(0f);
        request.setCategoryId(fakeCategoryId);
        request.setCityId(1L);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(categoryRepository.findById(fakeCategoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(request, user1))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldUpdatePost() {
        Post post = testDataFactory.createPostObject(user1, "name", category, city);
        post.setId(1L);

        PostRequest updateRequest = new PostRequest();
        updateRequest.setName("new name");
        updateRequest.setCategoryId(1L);
        updateRequest.setCityId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        PostResponse result = postService.updatePost(updateRequest, 1L, user1);

        assertThat(result.getName()).isEqualTo("new name");
        assertThat(post.getName()).isEqualTo("new name");
    }

    @Test
    void shouldThrowNotFoundOnUpdatePostWhenPostNotFound() {
        Long fakeId = 10L;

        PostRequest updateRequest = new PostRequest();

        when(postRepository.findById(fakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(updateRequest, fakeId, user1))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldThrowForbiddenOnUpdateWhenUserIsNotOwner() {
        Post post = testDataFactory.createPostObject(user1, "test", category, city);
        post.setId(1L);

        PostRequest updateRequest = new PostRequest();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(updateRequest, 1L, user2))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldDeletePost() {
        Post post = testDataFactory.createPostObject(user1, "test", category, city);
        post.setId(1L);

        Chat chat = testDataFactory.createChatObject(post, user2);
        chat.setId(1L);

        Photo photo1 = testDataFactory.createPhotoObject(post);
        Photo photo2 = testDataFactory.createPhotoObject(post);
        photo1.setPath("test1");
        photo2.setPath("test2");
        photo1.setId(1L);
        photo2.setId(2L);

        post.setPhotos(List.of(photo1, photo2));

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(chatRepository.findAllByPost(post)).thenReturn(List.of(chat));

        postService.deletePost(1L, user1);

        assertThat(chat.getPost()).isNull();
        verify(chatRepository).save(chat);

        verify(storageService).delete("test1");
        verify(storageService).delete("test2");

        verify(postRepository).delete(post);
    }

    @Test
    void shouldReturnDefaultSortWhenFilerIsEmpty() {
        Pageable inputPageable = PageRequest.of(2, 20);
        PostFilter filter = new PostFilter();

        Pageable result = postService.getSortedPageable(inputPageable, filter);

        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(20);

        Sort expectedSort = Sort.by("createdAt").descending();
        assertThat(result.getSort()).isEqualTo(expectedSort);
    }

    @Test
    void shouldReturnSortByPriceAsc() {
        Pageable inputPageable = PageRequest.of(2, 20);
        PostFilter filter = new PostFilter();
        filter.setSort("price_asc");

        Pageable result = postService.getSortedPageable(inputPageable, filter);

        Sort expectedSort = Sort.by("price").ascending();
        assertThat(result.getSort()).isEqualTo(expectedSort);
    }

    @Test
    void shouldReturnDefaultSortWhenFilerIsUnknown() {
        Pageable inputPageable = PageRequest.of(2, 20);
        PostFilter filter = new PostFilter();
        filter.setSort("bad_filter");

        Pageable result = postService.getSortedPageable(inputPageable, filter);

        Sort expectedSort = Sort.by("createdAt").descending();
        assertThat(result.getSort()).isEqualTo(expectedSort);
    }

    @Test
    void shouldReturnThumbnailResponse() {
        Pageable inputPageable = PageRequest.of(1, 10);
        PostFilter filter = new PostFilter();

        Post post = testDataFactory.createPostObject(user1, "Laptop", category, city);
        Page<Post> mockPage = new PageImpl<>(List.of(post));

        when(postRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        ThumbnailFilteredResponse response = postService.getFilteredResponse(inputPageable, filter);

        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void shouldReturnThumbnailResponseByUsername() {
        Pageable inputPageable = PageRequest.of(1, 10);
        PostFilter filter = new PostFilter();

        Post post = testDataFactory.createPostObject(user1, "Laptop", category, city);
        Page<Post> mockPage = new PageImpl<>(List.of(post));

        when(userRepository.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(postRepository.findAllByUser(eq(user1), any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        ThumbnailFilteredResponse response = postService.getFilteredResponseByUsername(
                inputPageable, filter, user1.getUsername());

        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getAuthor()).isEqualTo(user1.getUsername());
    }

    @Test
    void shouldThrowExceptionOnThumbnailResponseByUsernameWhenUserDoesntExist() {
        Pageable inputPageable = PageRequest.of(1, 10);
        PostFilter filter = new PostFilter();

        String fakeUsername = "fake";

        when(userRepository.findByUsername(fakeUsername)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getFilteredResponseByUsername(inputPageable, filter, fakeUsername))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnThumbnailResponseOfLikedPostsByUser() {
        Pageable inputPageable = PageRequest.of(1, 10);
        PostFilter filter = new PostFilter();

        Post post = testDataFactory.createPostObject(user1, "Laptop", category, city);
        post.setId(1L);
        Like like = testDataFactory.createLikeObject(user1, post);
        like.setId(1L);

        Page<Post> mockPage = new PageImpl<>(List.of(post));

        when(likeRepository.findAllByUser(user1)).thenReturn(List.of(like));
        when(postRepository.findAllByIdIn(eq(List.of(1L)), any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        ThumbnailFilteredResponse response = postService.getFilteredResponseByLikes(
                inputPageable, filter, user1);

        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getAuthor()).isEqualTo(user1.getUsername());
    }
}
