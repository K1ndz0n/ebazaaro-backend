package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.dto.PhotoRequest;
import com.example.ebazaarobackend.dto.SetPhotosRequest;
import com.example.ebazaarobackend.model.Photo;
import com.example.ebazaarobackend.model.Post;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.PhotoRepository;
import com.example.ebazaarobackend.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(TestDataFactory.class)
public class PhotoServiceTests {
    @Mock private PhotoRepository photoRepository;
    @Mock private PostRepository postRepository;
    @Mock private StorageService storageService;

    @InjectMocks
    private PhotoService photoService;

    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory = new TestDataFactory();
    }

    @Test
    void shouldSetPhotosWhenListIsEmpty() {
        User user = testDataFactory.createUserObject("test", "test@example.com");
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);
        post.setUser(user);

        MultipartFile expectedFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes()
        );

        PhotoRequest p1 = new PhotoRequest();
        p1.setId(-1L);
        p1.setFile(expectedFile);
        p1.setOrder(1);

        PhotoRequest p2 = new PhotoRequest();
        p2.setId(-1L);
        p2.setFile(expectedFile);
        p2.setOrder(2);

        SetPhotosRequest request = new SetPhotosRequest();
        request.setPhotos(List.of(p1, p2));

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        photoService.setPhotos(user, 1L, request);

        verify(storageService, times(2)).store(expectedFile, "posts");
        verify(photoRepository, times(2)).save(any(Photo.class));
    }

    @Test
    void shouldSetPhotosAndChangeOrderWhenListIsNotEmpty() {
        User user = testDataFactory.createUserObject("test", "test@example.com");
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);
        post.setUser(user);

        Photo photo1 = testDataFactory.createPhotoObject(post);
        photo1.setId(1L);
        photo1.setDisplayOrder(1);

        Photo photo2 = testDataFactory.createPhotoObject(post);
        photo2.setId(2L);
        photo2.setDisplayOrder(2);

        post.setPhotos(new ArrayList<>(List.of(photo1, photo2)));

        MultipartFile expectedFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes()
        );

        PhotoRequest p1 = new PhotoRequest();
        p1.setId(1L);
        p1.setFile(expectedFile);
        p1.setOrder(2);

        PhotoRequest p2 = new PhotoRequest();
        p2.setId(-1L);
        p2.setFile(expectedFile);
        p2.setOrder(1);

        SetPhotosRequest request = new SetPhotosRequest();
        request.setPhotos(List.of(p1, p2));

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo1));

        photoService.setPhotos(user, 1L, request);

        verify(storageService).store(expectedFile, "posts");
        verify(photoRepository).save(any(Photo.class));

        assertThat(p1.getOrder()).isEqualTo(2);

        verify(photoRepository).delete(any(Photo.class));
        verify(storageService).delete(any(String.class));
    }

    @Test
    void shouldDeleteAllPhotosWhenRequestIsEmpty() {
        User user = testDataFactory.createUserObject("test", "test@example.com");
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);
        post.setUser(user);

        Photo photo1 = testDataFactory.createPhotoObject(post);
        photo1.setId(1L);
        photo1.setDisplayOrder(1);

        Photo photo2 = testDataFactory.createPhotoObject(post);
        photo2.setId(2L);
        photo2.setDisplayOrder(2);

        post.setPhotos(new ArrayList<>(List.of(photo1, photo2)));

        SetPhotosRequest request = new SetPhotosRequest();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        photoService.setPhotos(user, 1L, request);

        verify(storageService, times(2)).delete(any(String.class));
        verify(photoRepository, times(2)).delete(any(Photo.class));

        assertThat(post.getPhotos().isEmpty()).isTrue();
    }

    @Test
    void shouldThrowNotFoundIfPostDoesntExist() {
        User user = testDataFactory.createUserObject("test", "test@example.com");
        Long fakeId = 10L;
        SetPhotosRequest request = new SetPhotosRequest();

        when(postRepository.findById(fakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> photoService.setPhotos(user, fakeId, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldThrowForbiddenWhenUserIsNotOwner() {
        User user = testDataFactory.createUserObject("test", "test@example.com");
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);
        post.setUser(user);

        User badUser = testDataFactory.createUserObject("bad", "bad@example.com");
        badUser.setId(2L);

        SetPhotosRequest request = new SetPhotosRequest();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> photoService.setPhotos(badUser, 1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}