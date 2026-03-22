package ua.klevchenko.api.klopotenkorecipemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
}