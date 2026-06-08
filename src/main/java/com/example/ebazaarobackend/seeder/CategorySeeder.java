package com.example.ebazaarobackend.seeder;

import com.example.ebazaarobackend.model.Category;
import com.example.ebazaarobackend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CategorySeeder implements CommandLineRunner {
    @Autowired
    private CategoryRepository categoryRepository;

    private final String[] categories = new String[] {
            "Inne",
            "Elektronika",
            "Motoryzacja",
            "RTV",
            "AGD",
            "Kuchnia",
            "Materiały budowlane",
            "Moda i Ubrania",
            "Dom i Ogród",
            "Sport i Hobby",
            "Rolnictwo",
            "Antyki i Kolekcje",
            "Narzędzia",
    };

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            seedDatabase();
        }
    }

    private void seedDatabase() {
        System.out.println("Rozpoczynam seedowanie kategorii...");

        for (String c : categories) {
            Category cat = new Category();
            cat.setName(c);
            categoryRepository.save(cat);
        }

        System.out.println("Dane zostały pomyślnie załadowane!");
    }
}
