package ua.klevchenko.api.klopotenkorecipemanager.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDto {

    private Long id;
    private String title;
    private String description;
    private String instructions;
    private Integer cookingTime;
    private Integer servings;
    private String imageUrl;

    private Long categoryId;
    private String categoryName;

    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();
}
