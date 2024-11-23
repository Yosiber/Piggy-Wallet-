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

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditTransactionListener.class)
@Table(name = "tbl_caf")
public class CashFlowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caf_value")
    private Float value;

    @Column(name = "caf_date")
    private Timestamp date;

    @Column(name = "caf_detalles")
    private String description;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY) // o FetchType.EAGER
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private CategoryEntity category;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;
}
