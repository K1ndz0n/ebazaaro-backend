package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.dto.CityResponse;
import com.example.ebazaarobackend.model.City;
import com.example.ebazaarobackend.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Import(TestDataFactory.class)
@ExtendWith(MockitoExtension.class)
public class CityServiceTests {
    @Mock private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    @Test
    void shouldReturnCityResponseWhenCityExists() {
        City city = new City();
        city.setId(1L);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        CityResponse response = cityService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowNotFoundWhenCityDoesntExist() {
        Long fakeId = 10L;

        when(cityRepository.findById(fakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cityService.getById(fakeId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
