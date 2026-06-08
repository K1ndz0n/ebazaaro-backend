package com.example.ebazaarobackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "photos")
@Getter
@Setter
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int displayOrder;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public Photo() {}
}
