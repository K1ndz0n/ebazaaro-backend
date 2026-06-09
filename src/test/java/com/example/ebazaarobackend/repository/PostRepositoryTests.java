package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.TestcontainersConfig;
import com.example.ebazaarobackend.dto.PostFilter;
import com.example.ebazaarobackend.model.*;
import com.example.ebazaarobackend.spec.PostSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, TestDataFactory.class})
public class PostRepositoryTests {
    @Autowired private PostRepository postRepository;
    @Autowired private TestDataFactory testDataFactory;

    private User user1, user2;
    private Category testCategory1, testCategory2;

    private Post post1, post2, post3;

    private City torun, warszawa, gdansk;

    @BeforeEach
    void setUp() {
        user1 = testDataFactory.createUser("user1", "user1@example.com");
        user2 = testDataFactory.createUser("user2", "user2@example.com");

        testCategory1 = testDataFactory.createCategory("Elektronika");
        testCategory2 = testDataFactory.createCategory("Kuchnia");

        torun = testDataFactory.createCity("Toruń", "Kujawsko-Pomorskie", 53.0138f,18.5984f);
        warszawa = testDataFactory.createCity("Warszawa", "Mazowieckie", 52.2297f, 21.0122f);
        gdansk = testDataFactory.createCity("Gdańsk", "Pomorskie", 54.3520f, 18.6466f);

        post1 = testDataFactory.createPost(user1, "telefon", testCategory1, gdansk);
        post2 = testDataFactory.createPost(user2, "laptop", testCategory1, torun);
        post3 = testDataFactory.createPost(user2, "patelnia", testCategory2,  warszawa);
    }

    @Test
    void shouldFindPostById() {
        Optional<Post> foundPost = postRepository.findById(post1.getId());

        assertThat(foundPost).isPresent();
    }

    @Test
    void shouldFindAllPostsWithEmptySpecificationAndPagination() {
        PostFilter testFilter = new PostFilter();
        Specification<Post> spec = PostSpec.withFilters(testFilter, null, null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Page<Post> result = postRepository.findAll(spec, pageable);

        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void shouldFindAllPostsByUserWithEmptySpecificationAndPagination() {
        PostFilter testFilter = new PostFilter();
        Specification<Post> spec = PostSpec.withFilters(testFilter, null, null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Page<Post> result = postRepository.findAllByUser(user2, spec, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(user2.getId());
        assertThat(result.getContent().get(1).getUser().getId()).isEqualTo(user2.getId());
    }

    @Test
    void shouldFindAllPostsWithSpecifiedCategory() {
        PostFilter testFilter = new PostFilter();
        testFilter.setCategory(testCategory1.getName());
        Specification<Post> spec = PostSpec.withFilters(testFilter, null, null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Page<Post> result = postRepository.findAll(spec, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCategory().getName()).isEqualTo(testCategory1.getName());
        assertThat(result.getContent().get(1).getCategory().getName()).isEqualTo(testCategory1.getName());
    }

    @Test
    void shouldFindAllPostsWithSpecifiedCity() {
        PostFilter testFilter = new PostFilter();
        testFilter.setCityId(gdansk.getId());
        Specification<Post> spec = PostSpec.withFilters(testFilter, gdansk.getLatitude(), gdansk.getLongitude());
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Page<Post> result = postRepository.findAll(spec, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getCity().getName()).isEqualTo(gdansk.getName());
    }

    @Test
    void shouldFindAllPostsWithSpecifiedCityAndRadius() {
        PostFilter testFilter = new PostFilter();
        testFilter.setCityId(gdansk.getId());
        testFilter.setRadius(200f);
        Specification<Post> spec = PostSpec.withFilters(testFilter, gdansk.getLatitude(), gdansk.getLongitude());
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Page<Post> result = postRepository.findAll(spec, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCity().getName()).isNotEqualTo(warszawa.getName());
        assertThat(result.getContent().get(1).getCity().getName()).isNotEqualTo(warszawa.getName());
    }

    @Test
    void shouldFindAllPostsWithUserLocalizationAndRadius() {
        PostFilter testFilter = new PostFilter();
        testFilter.setUserLat(gdansk.getLatitude());
        testFilter.setUserLng(gdansk.getLongitude());
        testFilter.setRadius(200f);
        Specification<Post> spec = PostSpec.withFilters(testFilter, gdansk.getLatitude(), gdansk.getLongitude());
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Page<Post> result = postRepository.findAll(spec, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCity().getName()).isNotEqualTo(warszawa.getName());
        assertThat(result.getContent().get(1).getCity().getName()).isNotEqualTo(warszawa.getName());
    }

    @Test
    void shouldFindAllPostsByIdWithEmptySpec() {
        PostFilter testFilter = new PostFilter();
        Specification<Post> spec = PostSpec.withFilters(testFilter, null, null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        List<Long> ids = new ArrayList<>();
        ids.add(post1.getId());
        ids.add(post2.getId());

        Page<Post> result = postRepository.findAllByIdIn(ids, spec, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0)).isNotEqualTo(post3.getId());
        assertThat(result.getContent().get(1)).isNotEqualTo(post3.getId());
    }
}
