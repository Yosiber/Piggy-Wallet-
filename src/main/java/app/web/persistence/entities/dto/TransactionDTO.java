package app.web.persistence.entities.dto;

import app.web.persistence.entities.CategoryEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDTO {

    private String formattedValue;
    private double value;
    private String description;
    private CategoryEntity category;
    private LocalDateTime date;
}