package com.example.ebazaarobackend.controller;

import com.example.ebazaarobackend.dto.SetPhotosRequest;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/photos")
@RestController
public class PhotoController {
    @Autowired
    private PhotoService photoService;

    @PostMapping("/set/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> set(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @ModelAttribute SetPhotosRequest request
    ) {
        photoService.setPhotos(user, postId, request);
        return ResponseEntity.ok().build();
    }
}
