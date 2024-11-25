package app.web.persistence.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * DTO que agrupa dos conjuntos de categorías: ingresos y gastos.
 * Utilizado para transferir datos relacionados con las categorías entre
 * las diferentes capas de la aplicación.
 *
 * @author TuNombre
 */
@Data
@AllArgsConstructor
public class CategoriesDTO {
    /**
     * Conjunto de categorías de tipo ingreso.
     */
    private Set<CategoryDTO> ingresos;

    /**
     * Conjunto de categorías de tipo gasto.
     */
    private Set<CategoryDTO> gastos;
}