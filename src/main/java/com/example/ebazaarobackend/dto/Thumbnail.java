package com.example.ebazaarobackend.dto;

import com.example.ebazaarobackend.model.Post;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Thumbnail {
    private Long id;
    private String name;
    private Float price;
    private String condition;

    private String thumbnailPhoto;
    private String category;

    private CityResponse city;
    private String author;

    public Thumbnail(Post post) {
        this.id = post.getId();
        this.name = post.getName();
        this.price = post.getPrice();
        this.condition = post.getCondition().toUpperCase();

        if (!post.getPhotos().isEmpty())
            this.thumbnailPhoto = post.getPhotos().getFirst().getPath();

        this.category = post.getCategory().getName();
        this.city = new CityResponse(post.getCity());
        this.author = post.getUser().getUsername();
    }
}
