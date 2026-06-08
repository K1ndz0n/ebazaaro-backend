package com.example.ebazaarobackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ThumbnailFilteredResponse {
    private List<Thumbnail> data;
    private Meta meta;
}
