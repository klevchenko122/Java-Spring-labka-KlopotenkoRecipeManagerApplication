package ua.klevchenko.api.klopotenkorecipemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.klevchenko.api.klopotenkorecipemanager.dto.IngredientDto;
import ua.klevchenko.api.klopotenkorecipemanager.dto.RecipeDto;
import ua.klevchenko.api.klopotenkorecipemanager.service.CategoryService;
import ua.klevchenko.api.klopotenkorecipemanager.service.RecipeService;

import java.util.ArrayList;

@Controller
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final CategoryService categoryService;

    @GetMapping
    public String getAllRecipes(Model model) {
        model.addAttribute("recipes", recipeService.getAllRecipes());
        return "recipes";
    }

    @GetMapping("/{id}")
    public String getRecipeDetails(@PathVariable Long id, Model model) {
        model.addAttribute("recipe", recipeService.getRecipeById(id));
        return "recipe-details";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        RecipeDto recipeDto = new RecipeDto();
        recipeDto.setIngredients(new ArrayList<>());
        recipeDto.getIngredients().add(new IngredientDto());

        model.addAttribute("recipe", recipeDto);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "recipe-form";
    }

    @PostMapping
    public String createRecipe(@ModelAttribute("recipe") RecipeDto recipeDto) {
        recipeDto.setIngredients(
                recipeDto.getIngredients() == null ? new ArrayList<>() :
                        recipeDto.getIngredients().stream()
                                .filter(i -> i.getName() != null && !i.getName().isBlank())
                                .toList()
        );

        recipeService.createRecipe(recipeDto);
        return "redirect:/recipes";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        RecipeDto recipe = recipeService.getRecipeById(id);

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            recipe.setIngredients(new ArrayList<>());
            recipe.getIngredients().add(new IngredientDto());
        }

        model.addAttribute("recipe", recipe);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "recipe-form";
    }

    @PostMapping("/{id}")
    public String updateRecipe(@PathVariable Long id, @ModelAttribute("recipe") RecipeDto recipeDto) {
        recipeDto.setIngredients(
                recipeDto.getIngredients() == null ? new ArrayList<>() :
                        recipeDto.getIngredients().stream()
                                .filter(i -> i.getName() != null && !i.getName().isBlank())
                                .toList()
        );

        recipeService.updateRecipe(id, recipeDto);
        return "redirect:/recipes";
    }

    @PostMapping("/{id}/delete")
    public String deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return "redirect:/recipes";
    }
}