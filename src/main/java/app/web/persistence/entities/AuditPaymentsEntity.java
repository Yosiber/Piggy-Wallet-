package app.web.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de auditoría para los pagos.
 *
 * <p>Guarda detalles sobre las operaciones realizadas en los pagos,
 * incluyendo el nombre del pago, el monto, el usuario que realizó la operación
 * y la fecha en que ocurrió.</p>
 *
 * <p>Se almacena en una tabla persistente mediante JPA.</p>
 *
 * @author TuNombre
 */
@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AuditPaymentsEntity {

    /**
     * Identificador único de la auditoría de pagos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre del pago.
     */
    private String name;

    /**
     * Monto asociado al pago.
     */
    private Float amount;

    /**
     * Tipo de operación realizada (e.g., "INSERT", "UPDATE", "DELETE").
     */
    private String operation;

    /**
     * Nombre del usuario que realizó la operación.
     */
    private String username;

    /**
     * Fecha y hora en que se realizó la operación.
     */
    private LocalDateTime date;
}

