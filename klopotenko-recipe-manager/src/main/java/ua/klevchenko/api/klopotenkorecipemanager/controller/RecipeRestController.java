package ua.klevchenko.api.klopotenkorecipemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.klevchenko.api.klopotenkorecipemanager.dto.RecipeDto;
import ua.klevchenko.api.klopotenkorecipemanager.service.RecipeService;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeRestController {

    private final RecipeService recipeService;

    @GetMapping
    public List<RecipeDto> getAllRecipes(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId
    ) {
        if (title != null && !title.isBlank()) {
            return recipeService.searchRecipesByTitle(title);
        }

        if (categoryId != null) {
            return recipeService.getRecipesByCategory(categoryId);
        }

        return recipeService.getAllRecipes();
    }

    @GetMapping("/{id}")
    public RecipeDto getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeDto createRecipe(@RequestBody RecipeDto recipeDto) {
        return recipeService.createRecipe(recipeDto);
    }

    @PutMapping("/{id}")
    public RecipeDto updateRecipe(@PathVariable Long id, @RequestBody RecipeDto recipeDto) {
        return recipeService.updateRecipe(id, recipeDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
    }
}