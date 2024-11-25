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
 * Entidad que representa un registro de auditoría para las categorías.
 *
 * <p>Guarda detalles sobre las operaciones realizadas en las categorías,
 * incluyendo si la categoría pertenece a ingresos o gastos, el usuario que realizó
 * la operación y la fecha en que ocurrió.</p>
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
public class AuditCategoryEntity {

    /**
     * Identificador único de la auditoría de categoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre de la categoría.
     */
    private String name;

    /**
     * Tipo de operación realizada (e.g., "INSERT", "UPDATE", "DELETE").
     */
    private String operation;

    /**
     * Indica si la categoría pertenece a ingresos (true) o gastos (false).
     */
    private boolean isIncome;

    /**
     * Nombre del usuario que realizó la operación.
     */
    private String username;

    /**
     * Fecha y hora en que se realizó la operación.
     */
    private LocalDateTime date;
}
