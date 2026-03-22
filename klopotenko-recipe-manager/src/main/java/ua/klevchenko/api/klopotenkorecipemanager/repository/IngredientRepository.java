package ua.klevchenko.api.klopotenkorecipemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}