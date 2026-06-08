package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.model.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    @Query(value = """
        SELECT * FROM cities
        WHERE name LIKE CONCAT('%', :name, '%')
        ORDER BY CASE WHEN name LIKE CONCAT(:name, '%') THEN 1 ELSE 2 END, name ASC
        """,
            countQuery = """
        SELECT COUNT(*) FROM cities
        WHERE name LIKE CONCAT('%', :name, '%')
        """,
            nativeQuery = true)
    Page<City> findByNameWithOrdering(@Param("name") String name, Pageable pageable);
}

