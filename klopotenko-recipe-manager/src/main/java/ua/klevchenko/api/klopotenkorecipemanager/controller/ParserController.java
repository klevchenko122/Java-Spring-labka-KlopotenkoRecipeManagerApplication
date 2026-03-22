package ua.klevchenko.api.klopotenkorecipemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.klevchenko.api.klopotenkorecipemanager.dto.RecipeDto;
import ua.klevchenko.api.klopotenkorecipemanager.service.ParserService;

@Controller
@RequiredArgsConstructor
public class ParserController {

    private final ParserService parserService;

    @GetMapping("/import")
    public String showImportPage() {
        return "import-form";
    }

    @PostMapping("/import")
    public String importRecipeFromForm(@RequestParam String url, Model model) {
        RecipeDto importedRecipe = parserService.importRecipeFromUrl(url);
        return "redirect:/recipes/" + importedRecipe.getId();
    }

    @PostMapping("/api/parser/import")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeDto importRecipeFromApi(@RequestParam String url) {
        return parserService.importRecipeFromUrl(url);
    }
}