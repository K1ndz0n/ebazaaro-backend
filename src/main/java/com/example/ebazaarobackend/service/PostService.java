package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.*;
import com.example.ebazaarobackend.model.City;
import com.example.ebazaarobackend.model.Post;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.*;
import com.example.ebazaarobackend.spec.PostSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> getAll() {
        var posts = postRepository.findAll();
        return posts.stream().map(PostResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public PostResponse getById(Long id) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return new PostResponse(post);
    }

    public Post createPostObject(PostRequest request, User user) {
        Post post = new Post();
        post.setName(request.getName());
        post.setDescription(request.getDescription());
        post.setPhoneNumber(request.getPhoneNumber());
        post.setEmail(request.getEmail());
        post.setPrice(request.getPrice());
        post.setCondition(request.getCondition());

        post.setUser(user);
        post.setCity(cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono miasta o podanym ID!")));

        post.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono kategorii o podanym id!")));

        return post;
    }

    @Transactional
    public PostResponse createPost(PostRequest request, User user) {
        Post post = createPostObject(request, user);
        return new PostResponse(postRepository.save(post));
    }

    public void updatePostObjectWithRequest(Post post, PostRequest request, User user) {
        post.setName(request.getName());
        post.setDescription(request.getDescription());
        post.setPhoneNumber(request.getPhoneNumber());
        post.setEmail(request.getEmail());
        post.setPrice(request.getPrice());
        post.setCondition(request.getCondition());

        post.setUser(user);

        post.setCity(cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono miasta o podanym ID!")));

        post.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono kategorii o podanym id!")));
    }

    @Transactional
    public PostResponse updatePost(PostRequest request, Long id, User user) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND
                ));

        if (!post.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        updatePostObjectWithRequest(post, request, user);
        return new PostResponse(post);
    }

    @Transactional
    public void deletePost(Long id, User user) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!post.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        chatRepository.findAllByPost(post).forEach(chat -> {
            chat.setPost(null);
            chatRepository.save(chat);
        });

        post.getPhotos().forEach(p -> storageService.delete(p.getPath()));
        postRepository.delete(post);
    }

    public Pageable getSortedPageable(Pageable pageable, PostFilter filter) {
        Sort sort = switch (filter.getSort() != null ? filter.getSort() : "newest") {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "oldest" -> Sort.by("createdAt").ascending();
            default -> Sort.by("createdAt").descending();
        };

        return PageRequest.of(
                pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                sort
        );
    }

    public Specification<Post> getSpec(PostFilter filter) {
        Float lat = null, lng = null;

        if (filter.getUserLat() != null && filter.getUserLng() != null) {
            lat = filter.getUserLat();
            lng = filter.getUserLng();
        } else if (filter.getCityId() != null) {
            City city = cityRepository.findById(filter.getCityId()).orElse(null);
            if (city != null) {
                lat = city.getLatitude();
                lng = city.getLongitude();
            }
        }

        return PostSpec.withFilters(filter, lat, lng);
    }

    public Meta getMeta(Pageable sortedPageable, Page<Post> posts) {
        var meta = new Meta();
        meta.setCurrentPage(sortedPageable.getPageNumber());
        meta.setLastPage(posts.getTotalPages());
        meta.setTotal((int) posts.getTotalElements());

        return meta;
    }

    @Transactional(readOnly = true)
    public ThumbnailFilteredResponse getFilteredResponse(Pageable pageable, PostFilter filter) {
        var spec = getSpec(filter);
        var sortedPageable = getSortedPageable(pageable, filter);

        var posts = postRepository.findAll(spec, sortedPageable);

        var response = new ThumbnailFilteredResponse();
        response.setData(posts.stream().map(Thumbnail::new).toList());
        response.setMeta(getMeta(sortedPageable, posts));

        return response;
    }

    @Transactional(readOnly = true)
    public ThumbnailFilteredResponse getFilteredResponseByUsername(
            Pageable pageable, PostFilter filter, String username
    ) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var spec = getSpec(filter);
        var sortedPageable = getSortedPageable(pageable, filter);

        var posts = postRepository.findAllByUser(user, spec, sortedPageable);

        var response = new ThumbnailFilteredResponse();
        response.setData(posts.stream().map(Thumbnail::new).toList());
        response.setMeta(getMeta(sortedPageable, posts));

        return response;
    }

    @Transactional(readOnly = true)
    public ThumbnailFilteredResponse getFilteredResponseByLikes(
            Pageable pageable, PostFilter filter, User user
    ) {
        List<Long> likes = likeRepository.findAllByUser(user).stream()
                .map(l -> l.getPost().getId())
                .toList();

        var spec = getSpec(filter);
        var sortedPageable = getSortedPageable(pageable, filter);

        var posts = postRepository.findAllByIdIn(likes, spec, sortedPageable);

        var response = new ThumbnailFilteredResponse();
        response.setData(posts.stream().map(Thumbnail::new).toList());
        response.setMeta(getMeta(sortedPageable, posts));

        return response;
    }
}
