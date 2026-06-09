package com.example.ebazaarobackend.model;

import com.example.ebazaarobackend.dto.PostRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    private String email;

    @Column(nullable = false)
    private Float price;

    @Column(name = "item_condition", nullable = false)
    private String condition;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<Photo> photos = new ArrayList<>();

    public Post() {}

    public Post(PostRequest request, User user, City city, Category category) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.phoneNumber = request.getPhoneNumber();
        this.email = request.getEmail();
        this.price = request.getPrice();
        this.condition = request.getCondition();

        this.user = user;
        this.city = city;
        this.category = category;
    }

    public void update(PostRequest request, City city, Category category) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.phoneNumber = request.getPhoneNumber();
        this.email = request.getEmail();
        this.price = request.getPrice();
        this.condition = request.getCondition();

        this.city = city;
        this.category = category;
    }
}
