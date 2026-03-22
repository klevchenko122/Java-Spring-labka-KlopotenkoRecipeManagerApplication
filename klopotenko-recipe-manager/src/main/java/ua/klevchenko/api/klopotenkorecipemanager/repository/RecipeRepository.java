package ua.klevchenko.api.klopotenkorecipemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Recipe;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByTitleContainingIgnoreCase(String title);

    List<Recipe> findByCategoryId(Long categoryId);
}