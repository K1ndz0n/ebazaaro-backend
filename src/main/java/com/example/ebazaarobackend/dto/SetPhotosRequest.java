package com.example.ebazaarobackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SetPhotosRequest {
    private List<PhotoRequest> photos;

    public SetPhotosRequest() {}
}
