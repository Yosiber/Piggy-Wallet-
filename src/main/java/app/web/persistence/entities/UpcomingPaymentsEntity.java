package app.web.persistence.entities;

import app.web.listener.AuditCategoryListener;
import app.web.listener.AuditPaymentsListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Entidad que representa los pagos próximos de un usuario.
 *
 * <p>Incluye información sobre los pagos programados, como el nombre del pago,
 * su valor y el usuario al que pertenece.</p>
 *
 * <p>Utiliza un `EntityListener` para registrar automáticamente las auditorías relacionadas
 * con los pagos.</p>
 *
 * <p>Se almacena en la tabla `tbl_upp` en la base de datos.</p>
 *
 * @see AuditPaymentsListener
 * @see UserEntity
 *
 * @author TuNombre
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditPaymentsListener.class)
@Table(name = "tbl_upp")
public class UpcomingPaymentsEntity {

    /**
     * Identificador único del pago próximo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Valor del pago próximo.
     */
    @Column(name = "upp_value")
    private Float value;

    /**
     * Nombre o descripción del pago próximo.
     */
    @Column(name = "upp_name")
    private String name;

    /**
     * Usuario al que pertenece el pago próximo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;
}


