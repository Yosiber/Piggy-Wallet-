package app.web.persistence.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FinancialAnalysisDTO {
    private List<TransactionDTO> ingresos;
    private List<TransactionDTO> gastos;
}
