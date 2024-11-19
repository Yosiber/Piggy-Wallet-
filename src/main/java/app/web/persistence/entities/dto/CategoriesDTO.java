package app.web.persistence.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class CategoriesDTO {
    private Set<CategoryDTO> ingresos;
    private Set<CategoryDTO> gastos;
}