package app.web.persistence.entities;


import app.web.listener.AuditCategoryListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una categoría, que puede ser utilizada para clasificar
 * ingresos o gastos.
 *
 * <p>Incluye el nombre de la categoría, si es un ingreso o gasto, y el usuario al que pertenece.</p>
 *
 * <p>Utiliza un `EntityListener` para registrar automáticamente las auditorías de las categorías.</p>
 *
 * <p>Se almacena en la tabla `tbl_category` en la base de datos.</p>
 *
 * @see AuditCategoryListener
 * @see UserEntity
 *
 * @author TuNombre
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditCategoryListener.class)
@Table(name = "tbl_category")
public class CategoryEntity {

    /**
     * Identificador único de la categoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la categoría.
     */
    @Column(name = "catName")
    private String name;

    /**
     * Indica si la categoría pertenece a ingresos (`true`) o gastos (`false`).
     */
    @Column(name = "is_income", nullable = false)
    private boolean isIncome;

    /**
     * Usuario al que pertenece la categoría.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
