package app.web.persistence.entities;

import app.web.listener.AuditUserListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad que representa a los usuarios del sistema.
 *
 * <p>Incluye información como el nombre, correo electrónico, nombre de usuario,
 * teléfono, contraseña, roles asignados y relaciones con otras entidades.</p>
 *
 * <p>Utiliza un `EntityListener` para registrar automáticamente las auditorías
 * relacionadas con las operaciones sobre los usuarios.</p>
 *
 * <p>Se almacena en la tabla `tbl_users` en la base de datos.</p>
 *
 * @see AuditUserListener
 * @see RoleEntity
 * @see CategoryEntity
 * @see UpcomingPaymentsEntity
 *
 * @author TuNombre
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditUserListener.class)
@Table(name = "tbl_users")
public class UserEntity {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario.
     */
    @Column(nullable = false, name = "nombre")
    private String name;

    /**
     * Correo electrónico del usuario.
     */
    @Column(unique = true, name = "correo")
    private String email;

    /**
     * Nombre de usuario único.
     */
    @Column(unique = true, name = "apodo")
    private String username;

    /**
     * Teléfono del usuario.
     */
    @Column(name = "telefono")
    private String phone;

    /**
     * Contraseña del usuario. Excluida de los métodos `toString` y `equals`.
     */
    @Column(nullable = false, name = "contraseña")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String password;

    /**
     * Fecha y hora de creación del usuario. Generada automáticamente.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion")
    private LocalDateTime createdAt;

    /**
     * Conjunto de roles asignados al usuario.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    /**
     * Categorías asociadas al usuario.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CategoryEntity> categories = new HashSet<>();

    /**
     * Pagos próximos asociados al usuario.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UpcomingPaymentsEntity> upcomingPayments;
}

