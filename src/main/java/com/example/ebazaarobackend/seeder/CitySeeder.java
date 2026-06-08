package com.example.ebazaarobackend.seeder;

import com.example.ebazaarobackend.model.City;
import com.example.ebazaarobackend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Component
public class CitySeeder implements CommandLineRunner {
    @Autowired
    private CityRepository cityRepository;

    private static final Map<String, String> TRANSLATE_VOIVODESHIPS = Map.ofEntries(
            Map.entry("Łódź Voivodeship", "Łódzkie"),
            Map.entry("Mazovia", "Mazowieckie"),
            Map.entry("Greater Poland", "Wielkopolskie"),
            Map.entry("Lesser Poland", "Małopolskie"),
            Map.entry("Silesia", "Śląskie"),
            Map.entry("Lower Silesia", "Dolnośląskie"),
            Map.entry("Pomerania", "Pomorskie"),
            Map.entry("West Pomerania", "Zachodniopomorskie"),
            Map.entry("Warmia-Masuria", "Warmińsko-Mazurskie"),
            Map.entry("Podlaskie Voivodeship", "Podlaskie"),
            Map.entry("Subcarpathia", "Podkarpackie"),
            Map.entry("Kuyavian-Pomeranian Voivodeship", "Kujawsko-Pomorskie"),
            Map.entry("Lubusz", "Lubuskie"),
            Map.entry("Lublin", "Lubelskie"),
            Map.entry("Świętokrzyskie Voivodeship", "Świętokrzyskie"),
            Map.entry("Opole Voivodeship", "Opolskie")
    );

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (cityRepository.count() == 0) {
            seedDatabase();
        }
    }

    public void seedDatabase() throws Exception {
        System.out.println("Rozpoczynam import miast z pliku PL.txt...");

        ClassPathResource resource = new ClassPathResource("PL.txt");
        if (!resource.exists()) {
            System.err.println("Plik PL.txt nie istnieje w src/main/resources!");
            return;
        }

        List<City> citiesToInsert = new ArrayList<>();
        Set<String> uniqueCities = new HashSet<>();
        int batchSize = 1000;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\t");

                if (data.length > 10) {
                    String cityName = data[2].trim();
                    String englishRegion = data[3].trim();

                    String voivodeship = TRANSLATE_VOIVODESHIPS.getOrDefault(englishRegion, englishRegion);

                    String lat = data[9].trim();
                    String lng = data[10].trim();

                    String uniqueKey = cityName + "_" + voivodeship;

                    if (!uniqueCities.contains(uniqueKey)) {
                        City city = new City();
                        city.setName(cityName);
                        city.setVoivodeship(voivodeship);
                        city.setLatitude(Float.parseFloat(lat));
                        city.setLongitude(Float.parseFloat(lng));

                        citiesToInsert.add(city);
                        uniqueCities.add(uniqueKey);
                    }
                }

                if (citiesToInsert.size() >= batchSize) {
                    cityRepository.saveAll(citiesToInsert);
                    citiesToInsert.clear();
                }
            }

            if (!citiesToInsert.isEmpty()) {
                cityRepository.saveAll(citiesToInsert);
            }
        }

        System.out.println("Pomyślnie zaimportowano miasta!");
    }
}
