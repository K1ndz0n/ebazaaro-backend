package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.TestDataFactory;
import com.example.ebazaarobackend.TestcontainersConfig;
import com.example.ebazaarobackend.model.City;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class, TestDataFactory.class})
class CityRepositoryTests {

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void shouldReturnProperlyOrderedPage() {
        testDataFactory.createCity("Wrocław", "Dolnośląskie", 0f,0f);
        testDataFactory.createCity("Inowrocław", "Kujawsko-Pomorskie", 0f, 0f);
        testDataFactory.createCity("Warszawa", "Mazowieckie", 0f, 0f);

        Pageable pageable = PageRequest.of(0, 10);

        Page<City> result = cityRepository.findByNameWithOrdering("Wroc", pageable);

        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent().get(0).getName()).isEqualTo("Wrocław");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Inowrocław");
    }

    @Test
    void shouldReturnEmptyPage() {
        City city = testDataFactory.createCity("Poznań", "Wielkopolskie", 0f, 0f);
        cityRepository.save(city);

        Page<City> result = cityRepository.findByNameWithOrdering("Gdańsk", PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}
