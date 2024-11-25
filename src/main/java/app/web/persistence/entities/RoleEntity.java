package app.web.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un rol en el sistema, como "ADMIN" o "USER".
 *
 * <p>Se utiliza para definir y gestionar los roles asignados a los usuarios.</p>
 *
 * <p>Se almacena en la tabla `roles` en la base de datos.</p>
 *
 * @see UserEntity
 *
 * @author TuNombre
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "roles")
public class RoleEntity {

    /**
     * Identificador único del rol.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del rol (e.g., "ADMIN", "USER").
     */
    private String rolName;
}

