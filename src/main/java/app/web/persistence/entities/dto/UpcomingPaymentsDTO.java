package app.web.persistence.entities.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar pagos futuros.
 *
 * <p>Se utiliza para transferir datos relacionados con los pagos que están pendientes
 * o programados.</p>
 *
 * <p>Proporciona información básica como el identificador, el nombre del pago y su valor.</p>
 *
 * @author TuNombre
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingPaymentsDTO {

    /**
     * Identificador único del pago futuro.
     */
    private Long id;

    /**
     * Nombre o descripción del pago.
     */
    private String name;

    /**
     * Valor del pago.
     */
    private Float value;
}
