package com.example.ebazaarobackend.dto;

import com.example.ebazaarobackend.model.Post;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PostResponse {
    private Long id;
    private String name;
    private String description;
    private CityResponse city;
    private Float price;
    private String email;
    private String phoneNumber;
    private String condition;
    private LocalDateTime createdAt;
    private List<PhotoResponse> photos = new ArrayList<>();
    private CategoryResponse category;
    private String author;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.name = post.getName();
        this.description = post.getDescription();
        this.price = post.getPrice();
        this.email = post.getEmail();
        this.phoneNumber = post.getPhoneNumber();
        this.condition = post.getCondition();
        this.createdAt = post.getCreatedAt();

        if (post.getUser() != null)
            this.author = post.getUser().getUsername();

        if (post.getCity() != null)
            this.city = new CityResponse(post.getCity());

        if (post.getCategory() != null)
            this.category = new CategoryResponse(post.getCategory());

        if (!post.getPhotos().isEmpty()) {
            this.photos = post.getPhotos()
                    .stream()
                    .map(PhotoResponse::new)
                    .toList();
        }
    }
}