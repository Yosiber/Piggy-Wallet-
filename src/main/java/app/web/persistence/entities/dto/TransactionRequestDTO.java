package app.web.persistence.entities.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionRequestDTO {
    @NotNull(message = "El valor es obligatorio")
    private Float value;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoryId;
}