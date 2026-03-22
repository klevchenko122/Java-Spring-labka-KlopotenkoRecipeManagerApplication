package ua.klevchenko.api.klopotenkorecipemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.klevchenko.api.klopotenkorecipemanager.dto.IngredientDto;
import ua.klevchenko.api.klopotenkorecipemanager.dto.RecipeDto;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Category;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Ingredient;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Recipe;
import ua.klevchenko.api.klopotenkorecipemanager.exception.ResourceNotFoundException;
import ua.klevchenko.api.klopotenkorecipemanager.repository.CategoryRepository;
import ua.klevchenko.api.klopotenkorecipemanager.repository.RecipeRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public List<RecipeDto> getAllRecipes() {
        return recipeRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public RecipeDto getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
        return mapToDto(recipe);
    }

    public List<RecipeDto> searchRecipesByTitle(String title) {
        return recipeRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<RecipeDto> getRecipesByCategory(Long categoryId) {
        return recipeRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public RecipeDto createRecipe(RecipeDto recipeDto) {
        Category category = null;
        if (recipeDto.getCategoryId() != null) {
            category = categoryRepository.findById(recipeDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + recipeDto.getCategoryId()));
        }

        Recipe recipe = Recipe.builder()
                .title(recipeDto.getTitle())
                .description(recipeDto.getDescription())
                .instructions(recipeDto.getInstructions())
                .cookingTime(recipeDto.getCookingTime())
                .servings(recipeDto.getServings())
                .imageUrl(recipeDto.getImageUrl())
                .category(category)
                .ingredients(new ArrayList<>())
                .build();

        if (recipeDto.getIngredients() != null) {
            for (IngredientDto ingredientDto : recipeDto.getIngredients()) {
                Ingredient ingredient = Ingredient.builder()
                        .name(ingredientDto.getName())
                        .amount(ingredientDto.getAmount())
                        .recipe(recipe)
                        .build();
                recipe.getIngredients().add(ingredient);
            }
        }

        return mapToDto(recipeRepository.save(recipe));
    }

    public RecipeDto updateRecipe(Long id, RecipeDto recipeDto) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));

        Category category = null;
        if (recipeDto.getCategoryId() != null) {
            category = categoryRepository.findById(recipeDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + recipeDto.getCategoryId()));
        }

        recipe.setTitle(recipeDto.getTitle());
        recipe.setDescription(recipeDto.getDescription());
        recipe.setInstructions(recipeDto.getInstructions());
        recipe.setCookingTime(recipeDto.getCookingTime());
        recipe.setServings(recipeDto.getServings());
        recipe.setImageUrl(recipeDto.getImageUrl());
        recipe.setCategory(category);

        recipe.getIngredients().clear();

        if (recipeDto.getIngredients() != null) {
            for (IngredientDto ingredientDto : recipeDto.getIngredients()) {
                Ingredient ingredient = Ingredient.builder()
                        .name(ingredientDto.getName())
                        .amount(ingredientDto.getAmount())
                        .recipe(recipe)
                        .build();
                recipe.getIngredients().add(ingredient);
            }
        }

        return mapToDto(recipeRepository.save(recipe));
    }

    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));

        recipeRepository.delete(recipe);
    }

    private RecipeDto mapToDto(Recipe recipe) {
        List<IngredientDto> ingredientDtos = recipe.getIngredients()
                .stream()
                .map(ingredient -> IngredientDto.builder()
                        .id(ingredient.getId())
                        .name(ingredient.getName())
                        .amount(ingredient.getAmount())
                        .build())
                .toList();

        return RecipeDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .instructions(recipe.getInstructions())
                .cookingTime(recipe.getCookingTime())
                .servings(recipe.getServings())
                .imageUrl(recipe.getImageUrl())
                .categoryId(recipe.getCategory() != null ? recipe.getCategory().getId() : null)
                .categoryName(recipe.getCategory() != null ? recipe.getCategory().getName() : null)
                .ingredients(ingredientDtos)
                .build();
    }
}