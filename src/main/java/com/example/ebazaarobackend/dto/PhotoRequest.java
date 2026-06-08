package com.example.ebazaarobackend.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PhotoRequest {
    private Long id;
    private int order;
    private MultipartFile file;

    public PhotoRequest() {}
}
