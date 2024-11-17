package app.web.persistence.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_upp")
public class UpcomingPaymentsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upp_value")
    private Float value;

    @Column(name = "upp_name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)  // Cambiado a LAZY
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore  // Añade esta anotación
    private UserEntity user;
}

