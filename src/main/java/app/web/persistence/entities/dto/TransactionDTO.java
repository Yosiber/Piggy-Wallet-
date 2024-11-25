package app.web.persistence.entities.dto;

import app.web.persistence.entities.CategoryEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar una transacción financiera.
 *
 * <p>Contiene información detallada sobre una transacción, incluyendo su valor,
 * descripción, categoría asociada, y fecha.</p>
 *
 * <p>La anotación {@link @Builder} permite construir objetos de manera flexible.</p>
 *
 * @author TuNombre
 */
@Data
@Builder
public class TransactionDTO {

    /**
     * Valor formateado de la transacción (e.g., con moneda o separadores).
     */
    private String formattedValue;

    /**
     * Valor numérico de la transacción.
     */
    private double value;

    /**
     * Descripción de la transacción.
     */
    private String description;

    /**
     * Categoría asociada a la transacción.
     */
    private CategoryEntity category;

    /**
     * Fecha y hora en que ocurrió la transacción.
     */
    private LocalDateTime date;
}