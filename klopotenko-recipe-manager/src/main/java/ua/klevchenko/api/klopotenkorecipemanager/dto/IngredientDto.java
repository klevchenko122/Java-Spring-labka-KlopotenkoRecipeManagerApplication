package ua.klevchenko.api.klopotenkorecipemanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDto {

    private Long id;
    private String name;
    private String amount;
}