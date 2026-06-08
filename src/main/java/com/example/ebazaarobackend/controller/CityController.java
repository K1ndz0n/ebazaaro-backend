package com.example.ebazaarobackend.controller;

import com.example.ebazaarobackend.dto.CityResponse;
import com.example.ebazaarobackend.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/cities")
@RestController
public class CityController {
    @Autowired
    private CityService cityService;

    @GetMapping("/find/{name}")
    public ResponseEntity<List<CityResponse>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(cityService.getFiltered(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getById(id));
    }
}
