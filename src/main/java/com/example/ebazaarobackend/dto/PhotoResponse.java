package com.example.ebazaarobackend.dto;

import com.example.ebazaarobackend.model.Photo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhotoResponse {
    private Long id;
    private String path;
    private int order;

    public PhotoResponse(Photo photo) {
        this.id = photo.getId();
        this.path = photo.getPath();
        this.order = photo.getDisplayOrder();
    }
}
