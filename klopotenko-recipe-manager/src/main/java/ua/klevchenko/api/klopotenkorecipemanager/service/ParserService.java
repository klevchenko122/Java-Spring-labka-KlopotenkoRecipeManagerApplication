package ua.klevchenko.api.klopotenkorecipemanager.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import ua.klevchenko.api.klopotenkorecipemanager.dto.IngredientDto;
import ua.klevchenko.api.klopotenkorecipemanager.dto.RecipeDto;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Category;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Ingredient;
import ua.klevchenko.api.klopotenkorecipemanager.entity.Recipe;
import ua.klevchenko.api.klopotenkorecipemanager.repository.CategoryRepository;
import ua.klevchenko.api.klopotenkorecipemanager.repository.RecipeRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ParserService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public RecipeDto importRecipeFromUrl(String url) {
        validateUrl(url);

        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(15000)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException("Не вдалося завантажити сторінку рецепта: " + e.getMessage());
        }

        String wholeText = doc.wholeText();

        String title = firstNonBlank(
                doc.select("meta[property=og:title]").attr("content"),
                doc.select("h1").text(),
                doc.title()
        );

        String description = firstNonBlank(
                doc.select("meta[property=og:description]").attr("content"),
                doc.select("meta[name=description]").attr("content")
        );

        String imageUrl = firstNonBlank(
                doc.select("meta[property=og:image]").attr("content")
        );

        Integer cookingTime = extractCookingTime(wholeText);
        Integer servings = extractServings(wholeText);
        String instructions = extractInstructions(wholeText);
        List<String> ingredientLines = extractIngredients(wholeText);

        Category importedCategory = categoryRepository.findByName("Імпортовані рецепти")
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name("Імпортовані рецепти")
                                .build()
                ));

        Recipe recipe = Recipe.builder()
                .title(title != null && !title.isBlank() ? title : "Імпортований рецепт")
                .description(description)
                .instructions(instructions)
                .cookingTime(cookingTime)
                .servings(servings)
                .imageUrl(imageUrl)
                .category(importedCategory)
                .ingredients(new ArrayList<>())
                .build();

        for (String line : ingredientLines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            Ingredient ingredient = Ingredient.builder()
                    .name(line.trim())
                    .amount("")
                    .recipe(recipe)
                    .build();

            recipe.getIngredients().add(ingredient);
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return mapToDto(savedRecipe);
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL не може бути порожнім");
        }

        String lower = url.toLowerCase();
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) {
            throw new IllegalArgumentException("URL має починатися з http:// або https://");
        }

        if (!lower.contains("klopotenko.com")) {
            throw new IllegalArgumentException("Дозволено імпорт тільки з сайту klopotenko.com");
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private Integer extractCookingTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String fragment = extractFragment(text,
                "(Приготування|Cooking time)",
                "(Порції|Servings|Складність|Difficulty)");

        if (fragment == null) {
            return null;
        }

        return parseTimeToMinutes(fragment);
    }

    private Integer extractServings(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(Порції|Servings)\\s*(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(2));
        }

        return null;
    }

    private String extractInstructions(String text) {
        if (text == null || text.isBlank()) {
            return "Інструкцію не вдалося автоматично витягнути.";
        }

        String fragment = extractFragment(text,
                "(Приготування|How\\s*to\\s*prepare.*?|Preparation)",
                "(?=Схожі рецепти|More recipes|Читайте також|$)");

        if (fragment == null || fragment.isBlank()) {
            return "Інструкцію не вдалося автоматично витягнути.";
        }

        fragment = fragment.replaceAll("(?i)(Приготування|How\\s*to\\s*prepare.*?|Preparation)", "");
        fragment = fragment.replaceAll("\\s*Крок\\s*(\\d+)\\s*", "\nКрок $1: ");
        fragment = fragment.replaceAll("\\s*Step\\s*(\\d+)\\s*", "\nКрок $1: ");
        fragment = fragment.replaceAll("[ \\t]+", " ");
        fragment = fragment.replaceAll("\\n{2,}", "\n");
        fragment = fragment.trim();

        if (fragment.isBlank()) {
            return "Інструкцію не вдалося автоматично витягнути.";
        }

        return fragment;
    }

    private List<String> extractIngredients(String text) {
        List<String> result = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return result;
        }

        String fragment = extractFragment(text,
                "(Ingredients|Інгредієнти)",
                "(?=Приготування|How\\s*to\\s*prepare|Preparation|Крок\\s*1|Step\\s*1|$)");

        if (fragment == null || fragment.isBlank()) {
            return result;
        }

        fragment = fragment.replaceAll("(?i)(Ingredients|Інгредієнти)", "").trim();

        String[] lines = fragment.split("\\r?\\n");
        for (String line : lines) {
            String cleaned = line.trim();
            if (cleaned.isBlank()) {
                continue;
            }
            if (cleaned.length() > 200) {
                continue;
            }
            result.add(cleaned);
        }

        return result;
    }

    private String extractFragment(String text, String startRegex, String endRegex) {
        Pattern pattern = Pattern.compile(startRegex + "(.*?)" + endRegex,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private Integer parseTimeToMinutes(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        int total = 0;

        Matcher hoursUa = Pattern.compile("(\\d+)\\s*год", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(raw);
        if (hoursUa.find()) {
            total += Integer.parseInt(hoursUa.group(1)) * 60;
        }

        Matcher minsUa = Pattern.compile("(\\d+)\\s*хв", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(raw);
        if (minsUa.find()) {
            total += Integer.parseInt(minsUa.group(1));
        }

        Matcher hoursEn = Pattern.compile("(\\d+)\\s*hour", Pattern.CASE_INSENSITIVE).matcher(raw);
        if (hoursEn.find()) {
            total += Integer.parseInt(hoursEn.group(1)) * 60;
        }

        Matcher minsEn = Pattern.compile("(\\d+)\\s*min", Pattern.CASE_INSENSITIVE).matcher(raw);
        if (minsEn.find()) {
            total += Integer.parseInt(minsEn.group(1));
        }

        if (total == 0) {
            Matcher justNumber = Pattern.compile("(\\d+)").matcher(raw);
            if (justNumber.find()) {
                return Integer.parseInt(justNumber.group(1));
            }
        }

        return total == 0 ? null : total;
    }

    private RecipeDto mapToDto(Recipe recipe) {
        List<IngredientDto> ingredients = Optional.ofNullable(recipe.getIngredients())
                .orElse(new ArrayList<>())
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
                .ingredients(ingredients)
                .build();
    }
}