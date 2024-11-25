package app.web.persistence.entities.dto;

import app.web.persistence.entities.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO que representa una categoría con un identificador y un nombre.
 * Proporciona métodos para convertir una entidad {@code CategoryEntity}
 * en un DTO, facilitando el intercambio de datos entre capas.
 *
 * <p>La clase incluye un método estático para mapear entidades a objetos
 * de transferencia de datos (DTO).</p>
 *
 * @author TuNombre
 */
@Data
@AllArgsConstructor
public class CategoryDTO {

    /**
     * Identificador único de la categoría.
     */
    private Long id;

    /**
     * Nombre de la categoría.
     */
    private String name;

    /**
     * Convierte una entidad {@code CategoryEntity} en un objeto {@code CategoryDTO}.
     *
     * @param entity la entidad {@code CategoryEntity} que se desea convertir.
     * @return un nuevo objeto {@code CategoryDTO} con los datos de la entidad.
     */
    public static CategoryDTO fromEntity(CategoryEntity entity) {
        return new CategoryDTO(entity.getId(), entity.getName());
    }
}