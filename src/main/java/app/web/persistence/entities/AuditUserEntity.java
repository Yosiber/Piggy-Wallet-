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
 * Entidad que representa un registro de auditoría para operaciones relacionadas con usuarios.
 *
 * <p>Guarda detalles sobre las acciones realizadas en las entidades de usuario,
 * como la operación, el usuario que ejecutó la acción, y la fecha de la misma.</p>
 *
 * <p>Se utiliza para mantener un historial de cambios y facilitar la auditoría.</p>
 *
 * @author TuNombre
 */
@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AuditUserEntity {

    /**
     * Identificador único de la auditoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre del usuario afectado por la operación.
     */
    private String name;

    /**
     * Tipo de operación realizada (e.g., "INSERT", "UPDATE", "DELETE").
     */
    private String operation;

    /**
     * Nombre del usuario que realizó la operación.
     */
    private String username;

    /**
     * Fecha y hora de la operación.
     */
    private LocalDateTime date;
}

