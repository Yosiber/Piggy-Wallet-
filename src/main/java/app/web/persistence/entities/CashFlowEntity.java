package app.web.persistence.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_caf")
public class CashFlowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "caf_transaction")
    private Boolean Transaction;

    @Column(name = "caf_value")
    private Float value;

    @Column(name = "caf_date")
    private Timestamp date;

    @Column(name = "caf_detalles")
    private String description;


    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private CategoryEntity category;


}
