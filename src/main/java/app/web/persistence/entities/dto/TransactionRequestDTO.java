package app.web.persistence.entities.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO utilizado para realizar solicitudes relacionadas con la creación o actualización
 * de transacciones financieras.
 *
 * <p>Incluye validaciones para asegurar que los datos ingresados sean válidos antes
 * de procesarlos.</p>
 *
 * <p>Utiliza {@link JsonFormat} para formatear la fecha en un patrón específico al
 * serializar o deserializar.</p>
 *
 * @author TuNombre
 */
@Data
public class TransactionRequestDTO {

    /**
     * Valor de la transacción. Es obligatorio.
     */
    @NotNull(message = "El valor es obligatorio")
    private Float value;

    /**
     * Descripción de la transacción. Es obligatoria.
     */
    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    /**
     * Fecha y hora de la transacción. Formato esperado: yyyy-MM-dd HH:mm:ss.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    /**
     * Identificador de la categoría asociada. Es obligatorio.
     */
    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoryId;
}
