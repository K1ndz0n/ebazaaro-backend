package com.example.ebazaarobackend.controller;

import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/likes")
@RestController
public class LikeController {
    @Autowired
    private LikeService likeService;

    @GetMapping("/exists/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> exists(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        return likeService.isLiked(user, postId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/add/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> add(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        likeService.addLike(user, postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        likeService.deleteLike(user, postId);
        return ResponseEntity.noContent().build();
    }
}
