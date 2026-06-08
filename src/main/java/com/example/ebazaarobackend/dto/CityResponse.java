package com.example.ebazaarobackend.dto;

import com.example.ebazaarobackend.model.City;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityResponse {
    private Long id;
    private String name;
    private String voivodeship;
    private Float latitude;
    private Float longitude;

    public CityResponse(City city) {
        this.id = city.getId();
        this.name = city.getName();
        this.voivodeship = city.getVoivodeship();
        this.latitude = city.getLatitude();
        this.longitude = city.getLongitude();
    }
}