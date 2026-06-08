package com.example.ebazaarobackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostFilter {
    private String search;
    private String condition;
    private Float priceFrom;
    private Float priceTo;
    private String category;
    private Long cityId;
    private Float userLat;
    private Float userLng;
    private Float radius;
    private String sort;
}
