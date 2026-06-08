package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.CityResponse;
import com.example.ebazaarobackend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CityService {
    @Autowired
    private CityRepository cityRepository;

    public List<CityResponse> getFiltered(String name) {
        Pageable pageable = PageRequest.of(0, 20);
        var cities = cityRepository.findByNameWithOrdering(name, pageable);

        return cities.stream().map(CityResponse::new).toList();
    }

    public CityResponse getById(Long id) {
        var city = cityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return  new CityResponse(city);
    }
}
