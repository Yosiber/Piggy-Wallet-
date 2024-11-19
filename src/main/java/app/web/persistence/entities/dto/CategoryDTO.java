package app.web.persistence.entities.dto;

import app.web.persistence.entities.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;

    public static CategoryDTO fromEntity(CategoryEntity entity) {
        return new CategoryDTO(entity.getId(), entity.getName());
    }
}