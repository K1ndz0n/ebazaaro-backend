package com.example.ebazaarobackend.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    private int currentPage;
    private int lastPage;
    private int total;
}
