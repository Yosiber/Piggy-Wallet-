package app.web.persistence.entities;


import app.web.listener.AuditTransactionListener;
import app.web.listener.AuditUserListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa el flujo de caja, es decir, las transacciones financieras de un usuario.
 *
 * <p>Esta clase incluye detalles como el valor de la transacción, la fecha,
 * la descripción, la categoría asociada y el usuario que la realizó.</p>
 *
 * <p>Utiliza un `EntityListener` para registrar automáticamente las auditorías de las transacciones.</p>
 *
 * <p>Se almacena en la tabla `tbl_caf` en la base de datos.</p>
 *
 * @see AuditTransactionListener
 * @see CategoryEntity
 * @see UserEntity
 *
 * @author TuNombre
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditTransactionListener.class)
@Table(name = "tbl_caf")
public class CashFlowEntity {

    /**
     * Identificador único de la transacción.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Valor de la transacción.
     */
    @Column(name = "caf_value")
    private Float value;

    /**
     * Fecha de la transacción.
     */
    @Column(name = "caf_date")
    private Timestamp date;

    /**
     * Descripción detallada de la transacción.
     */
    @Column(name = "caf_detalles")
    private String description;

    /**
     * Categoría asociada a la transacción.
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private CategoryEntity category;

    /**
     * Usuario que realizó la transacción.
     */
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;
}
