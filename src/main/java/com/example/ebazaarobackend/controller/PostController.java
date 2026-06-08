package com.example.ebazaarobackend.controller;

import com.example.ebazaarobackend.dto.PostRequest;
import com.example.ebazaarobackend.dto.PostFilter;
import com.example.ebazaarobackend.dto.PostResponse;
import com.example.ebazaarobackend.dto.ThumbnailFilteredResponse;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/api/posts")
@RestController
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/all")
    public ResponseEntity<List<PostResponse>> getAll() {
        return ResponseEntity.ok(postService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> addPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PostRequest request
    ) {
        var response = postService.createPost(request, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request
    ) {
        var response = postService.updatePost(request, id, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        postService.deletePost(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/thumbnails")
    public ResponseEntity<ThumbnailFilteredResponse> getFilteredThumbnails(
            @ModelAttribute PostFilter filter,
            @PageableDefault(page = 1, size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                postService.getFilteredResponse(pageable, filter)
        );
    }

    @GetMapping("/thumbnails/{username}")
    public ResponseEntity<ThumbnailFilteredResponse> getThubmbnailsByUsername(
            @ModelAttribute PostFilter filter,
            @PageableDefault(page = 1, size = 20) Pageable pageable,
            @PathVariable String username
    ) {
        return ResponseEntity.ok(
                postService.getFilteredResponseByUsername(pageable, filter, username)
        );
    }

    @GetMapping("/thumbnails/liked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThumbnailFilteredResponse> getThumbnailsByLikes(
            @AuthenticationPrincipal User user,
            @ModelAttribute PostFilter filter,
            @PageableDefault(page = 1, size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                postService.getFilteredResponseByLikes(pageable, filter, user)
        );
    }
}
